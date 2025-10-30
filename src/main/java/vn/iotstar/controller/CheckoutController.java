package vn.iotstar.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.BuyNowRequest;
import vn.iotstar.dto.CheckoutFromCartRequest;
import vn.iotstar.dto.CheckoutItemDTO;
import vn.iotstar.dto.CheckoutRequest;
import vn.iotstar.entity.*;
import vn.iotstar.repository.CartItemRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.StyleValueRepository;
import vn.iotstar.service.CartService;
import vn.iotstar.service.CheckoutService;
import vn.iotstar.service.OrderService;
import vn.iotstar.service.UserService;
import vn.iotstar.service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    
    private final CheckoutService checkoutService;
    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final StyleValueRepository styleValueRepository;
    private final ObjectMapper objectMapper;
    private final VNPayService vnPayService;
    
    @PostMapping("/buy-now")
    public String buyNow(@RequestBody BuyNowRequest request, 
                        Authentication auth,
                        RedirectAttributes redirectAttributes) {
        
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            
            if (!product.getIsSelling()) {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm hiện không bán");
                return "redirect:/products/" + request.getProductId();
            }
            
            if (product.getQuantity() < request.getQuantity()) {
                redirectAttributes.addFlashAttribute("error", "Số lượng sản phẩm không đủ");
                return "redirect:/products/" + request.getProductId();
            }
            
            CheckoutItemDTO item = new CheckoutItemDTO();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(getFirstImage(product.getListImages()));
            item.setPrice(product.getPromotionalPrice() != null && product.getPromotionalPrice().compareTo(BigDecimal.ZERO) > 0 
                ? product.getPromotionalPrice() 
                : product.getPrice());
            item.setQuantity(request.getQuantity());
            item.setStyleValueIds(request.getStyleValueIds());
            item.setStyleValues(getStyleValuesDisplay(request.getStyleValueIds()));
            item.setTotal(item.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
            item.setStoreId(product.getStore().getId());
            item.setStoreName(product.getStore().getName());
            
            List<CheckoutItemDTO> items = new ArrayList<>();
            items.add(item);
            checkoutService.setCheckoutItems(items);
            
            return "redirect:/checkout";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/products/" + request.getProductId();
        }
    }
    
    @PostMapping("/from-cart")
    public String checkoutFromCart(@RequestBody CheckoutFromCartRequest request,
                                   Authentication auth,
                                   RedirectAttributes redirectAttributes) {
        
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            List<String> cartItemIds = request.getCartItemIds();
            System.out.println("=== CHECKOUT FROM CART DEBUG ===");
            System.out.println("Cart item IDs: " + cartItemIds);
            
            if (cartItemIds == null || cartItemIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một sản phẩm");
                return "redirect:/cart";
            }
            
            // Get cart items
            List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);
            System.out.println("Found cart items: " + cartItems.size());
            
            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm trong giỏ hàng");
                return "redirect:/cart";
            }
            
            // Convert cart items to checkout items
            List<CheckoutItemDTO> checkoutItems = new ArrayList<>();
            for (CartItem cartItem : cartItems) {
                Product product = cartItem.getProduct();
                
                CheckoutItemDTO item = new CheckoutItemDTO();
                item.setProductId(product.getId());
                item.setProductName(product.getName());
                item.setProductImage(getFirstImage(product.getListImages()));
                item.setPrice(product.getPromotionalPrice() != null && product.getPromotionalPrice().compareTo(BigDecimal.ZERO) > 0 
                    ? product.getPromotionalPrice() 
                    : product.getPrice());
                item.setQuantity(cartItem.getQuantity());
                // 🔹 Parse styleValueIds từ JSON trong CartItem (nếu có)
                item.setStyleValueIds(parseStyleIds(cartItem.getStyleValueIds()));

                // 🔹 Hiển thị dạng "Màu: Xanh, Size: 39"
                item.setStyleValues(getStyleValuesDisplay(item.getStyleValueIds()));
                item.setTotal(cartItem.getPrice());
                item.setStoreId(product.getStore().getId());
                item.setStoreName(product.getStore().getName());
                
                checkoutItems.add(item);
            }
            
            // Save to checkout session
            checkoutService.setCheckoutItems(checkoutItems);
            
            return "redirect:/checkout";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/cart";
        }
    }
    
    @GetMapping
    public String checkoutPage(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        List<CheckoutItemDTO> items = checkoutService.getCheckoutItems();
        if (items == null || items.isEmpty()) {
            return "redirect:/cart";
        }
        
        String email = auth.getName();
        User user = userService.findByEmail(email).orElseThrow();
        
        BigDecimal total = items.stream()
            .map(CheckoutItemDTO::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<UserAddress> addresses = checkoutService.getUserAddresses();
        UserAddress defaultAddress = checkoutService.getDefaultAddress();
        
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        model.addAttribute("defaultAddress", defaultAddress);
        
        return "user/checkout";
    }
    
    @PostMapping("/place-order")
    @ResponseBody
    public Map<String, Object> placeOrder(@RequestBody CheckoutRequest request, 
                                          Authentication auth,
                                          HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== PLACE ORDER DEBUG ===");
            System.out.println("Auth: " + (auth != null ? auth.getName() : "null"));
            
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return response;
            }
            
            List<CheckoutItemDTO> items = checkoutService.getCheckoutItems();
            System.out.println("Checkout items: " + (items != null ? items.size() : "null"));
            
            if (items == null || items.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không có sản phẩm để đặt hàng");
                return response;
            }
            
            String email = auth.getName();
            User user = userService.findByEmail(email).orElseThrow();
            
            System.out.println("Payment method: " + request.getPaymentMethod());
            
            // Create orders for multiple stores
            List<Order> orders = orderService.createOrders(items, request, user.getId());
            System.out.println("Orders created: " + orders.size());

            // Clear checkout items
            checkoutService.clearCheckoutItems();

            // Handle payment method for the first order (example)
            Order firstOrder = orders.get(0);
            String paymentMethod = request.getPaymentMethod();

            if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                System.out.println("=== VNPAY PAYMENT PROCESSING ===");
                System.out.println("Order ID: " + firstOrder.getId());
                System.out.println("Amount: " + firstOrder.getAmountFromUser());

                // Generate VNPay payment URL
                String orderInfo = "Thanh toan don hang " + firstOrder.getId();
                System.out.println("Order Info: " + orderInfo);

                String paymentUrl = vnPayService.createPaymentUrl(
                    firstOrder.getAmountFromUser(),
                    orderInfo,
                    firstOrder.getId(),
                    httpRequest
                );

                System.out.println("Generated VNPay URL: " + paymentUrl);

                if (paymentUrl != null && !paymentUrl.isEmpty()) {
                    response.put("success", true);
                    response.put("paymentMethod", "VNPAY");
                    response.put("paymentUrl", paymentUrl);
                    response.put("orderId", firstOrder.getId());
                    System.out.println("VNPay payment URL created successfully");
                } else {
                    System.out.println("ERROR: VNPay URL is null or empty");
                    response.put("success", false);
                    response.put("message", "Không thể tạo liên kết thanh toán VNPay");
                }
            } else {
                // COD or other payment methods
                response.put("success", true);
                response.put("message", "Đặt hàng thành công");
                response.put("orderId", firstOrder.getId());
                response.put("paymentMethod", "COD");
            }
            
            return response;
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return response;
        }
    }
    
    @GetMapping("/success")
    public String orderSuccess(@RequestParam String orderId, Model model) {
        Order order = orderService.getOrderById(orderId).orElse(null);
        model.addAttribute("order", order);
        return "user/order-success";
    }
    
    private String getFirstImage(String listImages) {
        try {
            if (listImages == null || listImages.isEmpty()) {
                return "/images/no-image.jpg";
            }
            List<String> images = objectMapper.readValue(listImages, new TypeReference<List<String>>() {});
            return images.isEmpty() ? "/images/no-image.jpg" : images.get(0);
        } catch (Exception e) {
            return "/images/no-image.jpg";
        }
    }
    
    private String getStyleValuesDisplay(List<String> styleValueIds) {
        if (styleValueIds == null || styleValueIds.isEmpty()) {
            return "";
        }
        
        List<String> styleValues = new ArrayList<>();
        for (String id : styleValueIds) {
            styleValueRepository.findById(id).ifPresent(sv -> 
                styleValues.add(sv.getStyle().getName() + ": " + sv.getName())
            );
        }
        
        return String.join(", ", styleValues);
    }
    private List<String> parseStyleIds(String json) {
        try {
            if (json == null || json.isBlank() || json.equals("[]")) return new ArrayList<>();
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
