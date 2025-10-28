package vn.iotstar.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
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
    
    /**
     * Xem danh sách đơn hàng của user
     */
    @GetMapping
    @Transactional(readOnly = true)
    public String viewOrders(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Order> orders = orderService.getUserOrdersWithStyleValues(user.getId());
        
        // Debug: Log số lượng đơn hàng
        System.out.println("=== DEBUG: User " + user.getEmail() + " có " + orders.size() + " đơn hàng ===");
        for (Order order : orders) {
            System.out.println("- Order ID: " + order.getId() + ", Status: " + order.getStatus() + 
                             ", Items: " + order.getOrderItems().size());
        }
        
        model.addAttribute("orders", orders);
        
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
        
        return "user/order-detail";
    }
}
