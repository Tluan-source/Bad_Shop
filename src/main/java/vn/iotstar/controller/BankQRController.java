package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import vn.iotstar.entity.Order;
import vn.iotstar.service.OrderService;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class BankQRController {

    private final OrderService orderService;

    @GetMapping("/bank-qr")
    public String bankQR(@RequestParam String orderId, Model model) {

        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        model.addAttribute("orderId", order.getId());
        model.addAttribute("amount", order.getAmountFromUser());
        model.addAttribute("bankAccount", "1031421223"); // NGÂN HÀNG NHÀ BẠN
        model.addAttribute("accountName", "NGUYEN THANH LUAN");     // TÊN TÀI KHOẢN
        model.addAttribute("bankCode", "VCB"); // MBBANK (ví dụ)
        model.addAttribute("description", "PAY-" + order.getId());

        return "user/bank-qr";
    }
}
