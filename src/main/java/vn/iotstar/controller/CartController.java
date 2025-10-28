package vn.iotstar.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import vn.iotstar.dto.AddToCartRequest;
import vn.iotstar.entity.CartItem;
import vn.iotstar.service.CartService;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping
    public String viewCart(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        List<CartItem> cartItems = cartService.getCartItems();
        BigDecimal cartTotal = cartService.getCartTotal();
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", cartTotal);
        
        return "user/cart";
    }
    
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestBody AddToCartRequest request,
            Authentication auth) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return ResponseEntity.ok(response);
            }
            
            int qty = request.getQuantity() > 0 ? request.getQuantity() : 1;
            CartItem cartItem = cartService.addToCart(request.getProductId(), qty);
            
            response.put("success", true);
            response.put("message", "Đã thêm sản phẩm vào giỏ hàng");
            response.put("cartCount", cartService.getCartCount());
            response.put("cartItem", cartItem);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    @PostMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @PathVariable String id,
            @RequestParam int quantity) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            CartItem cartItem = cartService.updateQuantity(id, quantity);
            
            response.put("success", true);
            response.put("message", "Đã cập nhật số lượng");
            response.put("cartItem", cartItem);
            response.put("cartTotal", cartService.getCartTotal());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    @DeleteMapping("/remove/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            cartService.removeFromCart(id);
            
            response.put("success", true);
            response.put("message", "Đã xóa sản phẩm khỏi giỏ hàng");
            response.put("cartCount", cartService.getCartCount());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearCart() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            cartService.clearCart();
            
            response.put("success", true);
            response.put("message", "Đã xóa toàn bộ giỏ hàng");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartCount() {
        Map<String, Object> response = new HashMap<>();
        response.put("count", cartService.getCartCount());
        return ResponseEntity.ok(response);
    }
}
