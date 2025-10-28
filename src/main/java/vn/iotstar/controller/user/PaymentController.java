package vn.iotstar.controller.user;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Payment;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.PaymentRepository;
import vn.iotstar.service.VNPayService;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final VNPayService vnPayService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    
    @GetMapping("/vnpay-return")
    @Transactional
    public String vnpayReturn(HttpServletRequest request, Model model) {
        try {
            // Get all params from VNPay
            Map<String, String> params = new HashMap<>();
            request.getParameterMap().forEach((key, value) -> {
                if (value != null && value.length > 0) {
                    params.put(key, value[0]);
                }
            });
            
            log.info("VNPay return params: {}", params);
            
            // Validate signature
            boolean isValid = vnPayService.validateReturnData(params);
            
            if (!isValid) {
                log.error("Invalid VNPay signature");
                model.addAttribute("success", false);
                model.addAttribute("message", "Chữ ký không hợp lệ");
                return "user/payment-result";
            }
            
            // Get payment status
            String paymentStatus = vnPayService.getPaymentStatus(params);
            String orderId = params.get("vnp_TxnRef");
            String transactionId = params.get("vnp_TransactionNo");
            String responseCode = params.get("vnp_ResponseCode");
            
            log.info("Payment status for order {}: {}", orderId, paymentStatus);
            
            // Update order and payment
            Order order = orderRepository.findById(orderId).orElse(null);
            
            if (order == null) {
                log.error("Order not found: {}", orderId);
                model.addAttribute("success", false);
                model.addAttribute("message", "Không tìm thấy đơn hàng");
                return "user/payment-result";
            }
            
            Payment payment = order.getPayment();
            log.info("Order payment: {}", payment);
            
            if (payment != null) {
                payment.setTransactionId(transactionId);
                
                if ("SUCCESS".equals(paymentStatus)) {
                    log.info("Updating payment and order for successful payment");
                    payment.setStatus(Payment.PaymentStatus.PAID);
                    payment.setPaymentDate(LocalDateTime.now());
                    order.setIsPaidBefore(true);
                    
                    log.info("Before save - Order isPaidBefore: {}", order.getIsPaidBefore());
                    log.info("Before save - Payment status: {}", payment.getStatus());
                    
                    paymentRepository.save(payment);
                    orderRepository.save(order);
                    
                    log.info("After save - Order saved successfully");
                    
                    model.addAttribute("success", true);
                    model.addAttribute("message", "Thanh toán thành công");
                } else {
                    payment.setStatus(Payment.PaymentStatus.FAILED);
                    
                    model.addAttribute("success", false);
                    model.addAttribute("message", "Thanh toán thất bại - Mã lỗi: " + responseCode);
                }
                
                paymentRepository.save(payment);
                orderRepository.save(order);
            }
            
            model.addAttribute("orderId", orderId);
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("amount", params.get("vnp_Amount"));
            
            return "user/payment-result";
            
        } catch (Exception e) {
            log.error("Error processing VNPay return", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "Có lỗi xảy ra: " + e.getMessage());
            return "user/payment-result";
        }
    }
}
