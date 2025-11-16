package vn.iotstar.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Payment;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.PaymentRepository;
import vn.iotstar.service.OrderService;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.UserService;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    
    /**
     * Xem danh sách đơn hàng của user
     */
    @GetMapping
    @Transactional(readOnly = true)
    public String viewOrders(Model model, Authentication auth, @org.springframework.web.bind.annotation.RequestParam(value = "date", required = false) String date) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Order> orders = orderService.getUserOrdersWithStyleValuesFiltered(user.getId(), date);
        
        // Debug: Log số lượng đơn hàng
        System.out.println("=== DEBUG: User " + user.getEmail() + " có " + orders.size() + " đơn hàng ===");
        for (Order order : orders) {
            System.out.println("- Order ID: " + order.getId() + ", Status: " + order.getStatus() + 
                             ", Items: " + order.getOrderItems().size());
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("date", date);
        
        return "user/orders";
    }
    
    /**
     * Xem chi tiết đơn hàng
     */
    @GetMapping("/{orderId}")
    @Transactional(readOnly = true)
    public String viewOrderDetail(@PathVariable String orderId, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Kiểm tra order có thuộc về user không
        if (!order.getUser().getId().equals(user.getId())) {
            return "redirect:/orders";
        }
        
        // Lấy thông tin Payment để hiển thị phương thức thanh toán
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        
        // Tính tổng tiền sản phẩm (subtotal)
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(item -> item.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Lấy phí vận chuyển thực tế (ưu tiên từ ShippingProvider)
        BigDecimal actualShippingFee = BigDecimal.ZERO;
        if (order.getShippingProvider() != null) {
            actualShippingFee = order.getShippingProvider().getShippingFee();
        } else if (order.getShippingFee() != null) {
            actualShippingFee = order.getShippingFee();
        }
        
        // Lấy sản phẩm liên quan dựa trên các sản phẩm trong đơn hàng
        // Lấy category từ sản phẩm đầu tiên trong đơn hàng
        if (!order.getOrderItems().isEmpty()) {
            Product firstProduct = order.getOrderItems().get(0).getProduct();
            String categoryId = firstProduct.getCategory().getId();
            
            // Lấy các sản phẩm liên quan cùng category (tối đa 8 sản phẩm)
            List<Product> relatedProducts = productService.findByCategoryId(categoryId, PageRequest.of(0, 8))
                    .stream()
                    // Loại bỏ các sản phẩm đã có trong đơn hàng
                    .filter(p -> order.getOrderItems().stream()
                            .noneMatch(item -> item.getProduct().getId().equals(p.getId())))
                    .collect(Collectors.toList());
            
            model.addAttribute("relatedProducts", relatedProducts);
        }
        
        model.addAttribute("order", order);
        model.addAttribute("payment", payment);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("actualShippingFee", actualShippingFee);
        
        return "user/order-detail";
    }
    
    /**
     * Xác nhận đã nhận hàng (cho cả COD và đơn đã thanh toán trước)
     */
    @PostMapping("/{orderId}/confirm-receipt")
    @Transactional
    public String confirmReceipt(@PathVariable String orderId, 
                                 Authentication auth, 
                                 RedirectAttributes redirectAttributes) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Kiểm tra order có thuộc về user không
        if (!order.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xác nhận đơn hàng này");
            return "redirect:/orders";
        }
        
        // Kiểm tra trạng thái đơn hàng
        if (order.getStatus() != Order.OrderStatus.AWAITING_CONFIRMATION) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng không ở trạng thái chờ xác nhận");
            return "redirect:/orders/" + orderId;
        }
        
        // Cập nhật trạng thái đơn hàng
        order.setStatus(Order.OrderStatus.DELIVERED);
        order.setConfirmedByUserAt(LocalDateTime.now());
        orderRepository.save(order);
        
        redirectAttributes.addFlashAttribute("success", "Đã xác nhận nhận hàng thành công! Bạn có thể đánh giá sản phẩm ngay bây giờ.");
        
        return "redirect:/orders/" + orderId;
    }
}