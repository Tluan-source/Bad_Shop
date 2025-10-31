package vn.iotstar.controller.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            String orderIdsParam = params.get("vnp_TxnRef");
            String transactionId = params.get("vnp_TransactionNo");
            String responseCode = params.get("vnp_ResponseCode");
            
            log.info("Payment status for orderIds {}: {}", orderIdsParam, paymentStatus);
            
            // Split orderIds if multiple orders (comma-separated)
            String[] orderIds = orderIdsParam.split(",");
            List<Order> orders = new ArrayList<>();
            List<String> notFoundOrders = new ArrayList<>();
            
            // Find all orders
            for (String rawOrderId : orderIds) {
                if (rawOrderId == null || rawOrderId.trim().isEmpty()) {
                    continue;
                }
                String orderId = rawOrderId.trim();
                Order order = orderRepository.findById(orderId).orElse(null);
                if (order != null) {
                    orders.add(order);
                } else {
                    notFoundOrders.add(orderId);
                    log.warn("Order not found: {}", orderId);
                }
            }
            
            // Check if any order not found
            if (orders.isEmpty()) {
                log.error("No orders found for orderIds: {}", orderIdsParam);
                model.addAttribute("success", false);
                model.addAttribute("message", "Không tìm thấy đơn hàng");
                return "user/payment-result";
            }
            
            // Update all orders and payments
            boolean allSuccess = true;
            for (Order order : orders) {
                Payment payment = order.getPayment();
                log.info("Order {} payment: {}", order.getId(), payment);
                
                if (payment != null) {
                    payment.setTransactionId(transactionId);
                    
                    if ("SUCCESS".equals(paymentStatus)) {
                        log.info("Updating payment and order {} for successful payment", order.getId());
                        payment.setStatus(Payment.PaymentStatus.PAID);
                        payment.setPaymentDate(LocalDateTime.now());
                        order.setIsPaidBefore(true);
                        
                        paymentRepository.save(payment);
                        orderRepository.save(order);
                        
                        log.info("Order {} saved successfully", order.getId());
                    } else {
                        payment.setStatus(Payment.PaymentStatus.FAILED);
                        paymentRepository.save(payment);
                        orderRepository.save(order);
                        allSuccess = false;
                    }
                } else {
                    log.warn("Payment not found for order: {}", order.getId());
                }
            }
            
            // Set result message
            if ("SUCCESS".equals(paymentStatus) && allSuccess) {
                if (orders.size() > 1) {
                    model.addAttribute("success", true);
                    model.addAttribute("message", "Thanh toán thành công cho " + orders.size() + " đơn hàng");
                } else {
                    model.addAttribute("success", true);
                    model.addAttribute("message", "Thanh toán thành công");
                }
            } else {
                model.addAttribute("success", false);
                model.addAttribute("message", "Thanh toán thất bại - Mã lỗi: " + responseCode);
            }
            
            // Warning if some orders not found
            if (!notFoundOrders.isEmpty()) {
                log.warn("Some orders not found: {}", notFoundOrders);
                String existingMessage = (String) model.getAttribute("message");
                model.addAttribute("message", existingMessage + " (Một số đơn hàng không tìm thấy: " + String.join(", ", notFoundOrders) + ")");
            }
            
            // Set attributes for display
            model.addAttribute("orderId", orderIds.length == 1 ? orderIds[0] : orderIdsParam);
            model.addAttribute("orderIds", orderIdsParam);
            model.addAttribute("ordersCount", orders.size());
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
