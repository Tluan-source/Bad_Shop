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
import vn.iotstar.repository.VoucherRepository;
import vn.iotstar.repository.PromotionRepository;
import vn.iotstar.service.CartService;
import vn.iotstar.service.CheckoutService;
import vn.iotstar.service.OrderService;
import vn.iotstar.service.UserService;
import vn.iotstar.service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final VoucherRepository voucherRepository;
    private final PromotionRepository promotionRepository;
    private final ObjectMapper objectMapper;
    private final VNPayService vnPayService;
    private final vn.iotstar.repository.ShippingProviderRepository shippingProviderRepository;
    
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
        
        // Group items by store
        Map<String, List<CheckoutItemDTO>> itemsByStore = items.stream()
            .collect(Collectors.groupingBy(CheckoutItemDTO::getStoreId));
        
        // Calculate total for each store
        Map<String, BigDecimal> totalsByStore = new HashMap<>();
        for (Map.Entry<String, List<CheckoutItemDTO>> entry : itemsByStore.entrySet()) {
            BigDecimal storeTotal = entry.getValue().stream()
                .map(CheckoutItemDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalsByStore.put(entry.getKey(), storeTotal);
        }
        
        BigDecimal grandTotal = items.stream()
            .map(CheckoutItemDTO::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Load available vouchers (toàn sàn)
        List<Voucher> availableVouchers = voucherRepository.findAvailableVouchers(LocalDateTime.now());
        
        // Load promotions for each store
        Map<String, List<Promotion>> promotionsByStore = new HashMap<>();
        for (String storeId : itemsByStore.keySet()) {
            List<Promotion> storePromotions = promotionRepository.findActivePromotions(storeId, LocalDateTime.now());
            promotionsByStore.put(storeId, storePromotions);
        }
        
        List<UserAddress> addresses = checkoutService.getUserAddresses();
        UserAddress defaultAddress = checkoutService.getDefaultAddress();
        
        // Get all active shipping providers
        List<vn.iotstar.entity.ShippingProvider> shippingProviders = shippingProviderRepository.findAll()
            .stream()
            .filter(vn.iotstar.entity.ShippingProvider::getIsActive)
            .toList();
        
        model.addAttribute("items", items);
        model.addAttribute("itemsByStore", itemsByStore);
        model.addAttribute("totalsByStore", totalsByStore);
        model.addAttribute("total", grandTotal);
        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        model.addAttribute("defaultAddress", defaultAddress);
        model.addAttribute("shippingProviders", shippingProviders);
        model.addAttribute("availableVouchers", availableVouchers);
        model.addAttribute("promotionsByStore", promotionsByStore);
        
        return "user/checkout";
    }
    
    @PostMapping("/place-order")
    @ResponseBody
    public Map<String, Object> placeOrder(@RequestBody Map<String, Object> requestData, 
                                          Authentication auth,
                                          HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== PLACE ORDER DEBUG ===");
            
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return response;
            }
            
            List<CheckoutItemDTO> items = checkoutService.getCheckoutItems();
            
            if (items == null || items.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không có sản phẩm để đặt hàng");
                return response;
            }
            
            String email = auth.getName();
            User user = userService.findByEmail(email).orElseThrow();
            
            // Parse request data
            CheckoutRequest request = new CheckoutRequest();
            request.setFullName((String) requestData.get("fullName"));
            request.setPhone((String) requestData.get("phone"));
            request.setAddress((String) requestData.get("address"));
            request.setProvince((String) requestData.get("province"));
            request.setDistrict((String) requestData.get("district"));
            request.setWard((String) requestData.get("ward"));
            request.setNote((String) requestData.get("note"));
            request.setPaymentMethod((String) requestData.get("paymentMethod"));
            request.setVoucherCode((String) requestData.get("voucherCode"));
            
            // Parse promotions by store
            @SuppressWarnings("unchecked")
            Map<String, String> promotionsByStore = (Map<String, String>) requestData.get("promotionsByStore");
            
            System.out.println("Payment method: " + request.getPaymentMethod());
            System.out.println("Voucher: " + request.getVoucherCode());
            System.out.println("Promotions: " + promotionsByStore);
            
            // Create orders (one per store)
            List<Order> orders = orderService.createMultiStoreOrders(items, request, promotionsByStore, user.getId());
            System.out.println("Created " + orders.size() + " orders");
            
            // Create orders for multiple stores
            // List<Order> orders = orderService.createOrders(items, request, user.getId());
            // System.out.println("Orders created: " + orders.size());

            // Clear checkout items
            checkoutService.clearCheckoutItems();

           // Handle payment method for the first order (example)
            Order firstOrder = orders.get(0);
            String paymentMethod = request.getPaymentMethod();

            if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                // Calculate total amount from all orders
                BigDecimal totalAmount = orders.stream()
                    .map(Order::getAmountFromUser)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                String orderIds = orders.stream()
                    .map(Order::getId)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
                
                String orderInfo = "Thanh toan " + orders.size() + " don hang";
                
                String paymentUrl = vnPayService.createPaymentUrl(
                    totalAmount,
                    orderInfo,
                    orderIds,
                    httpRequest
                );
                
                System.out.println("=== VNPAY PAYMENT PROCESSING ===");

                // String orderInfo = "Thanh toan don hang " + firstOrder.getId();
                // String paymentUrl = vnPayService.createPaymentUrl(
                //     firstOrder.getAmountFromUser(),
                //     orderInfo,
                //     firstOrder.getId(),
                //     httpRequest
                // );

                if (paymentUrl != null && !paymentUrl.isEmpty()) {
                    response.put("success", true);
                    response.put("paymentMethod", "VNPAY");
                    response.put("paymentUrl", paymentUrl);
                    response.put("orderIds", orderIds);
                    response.put("orderId", orders.get(0).getId());
                } else {
                    response.put("success", false);
                    response.put("message", "Không thể tạo link thanh toán VNPay");
                }
            // } else {
            //     // COD payment
            //     response.put("success", true);

            } else if ("BANK_QR".equalsIgnoreCase(paymentMethod)) {
                System.out.println("=== BANK QR PAYMENT ===");

                String bankCode = "VCB"; // Ví dụ
                String bankAccount = "1031421223";
                String accountName = "NGUYEN THANH LUAN";

                String description = "PAY-" + firstOrder.getId();
                String qrUrl = "https://img.vietqr.io/image/" + bankCode + "-" + bankAccount + "-compact.png"
                        + "?amount=" + firstOrder.getAmountFromUser()
                        + "&addInfo=" + description
                        + "&accountName=" + accountName;

                response.put("success", true);
                response.put("paymentMethod", "BANK_QR");
                response.put("orderId", firstOrder.getId());
                response.put("amount", firstOrder.getAmountFromUser());
                response.put("qrImage", qrUrl);
                response.put("description", description);
            } else { // COD
                response.put("success", true);
                response.put("message", "Đặt hàng thành công");
                response.put("orderId", firstOrder.getId());
                response.put("paymentMethod", "COD");
                response.put("orderId", orders.get(0).getId());
                response.put("message", "Đặt hàng thành công! Đã tạo " + orders.size() + " đơn hàng.");
            }
            
        } catch (Exception e) {
            System.err.println("Place order error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return response;
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
