package vn.iotstar.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import vn.iotstar.entity.CartItem;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.repository.CartItemRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    /**
     * Get current logged in user
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("User not authenticated");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * Add product to cart
     */
    @Transactional
    public CartItem addToCart(String productId, Integer quantity, List<String> styleValueIds) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 🔹 Chuyển styleValueIds thành chuỗi JSON để lưu
        String styleValueIdsJson = null;
        try {
            if (styleValueIds != null && !styleValueIds.isEmpty()) {
                styleValueIdsJson = new ObjectMapper().writeValueAsString(styleValueIds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 🔹 Kiểm tra sản phẩm đã có trong giỏ chưa (cùng product + style)
        CartItem cartItem = cartItemRepository
                .findByUserAndProductAndStyleValueIds(user, product, styleValueIdsJson)
                .orElse(null);

        if (cartItem != null) {
            // Nếu đã có → cộng thêm số lượng
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setPrice(getProductPrice(product).multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        } else {
            // Nếu chưa có → tạo mới
            cartItem = new CartItem();
            cartItem.setId(UUID.randomUUID().toString());
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPrice(getProductPrice(product).multiply(BigDecimal.valueOf(quantity)));
            cartItem.setStyleValueIds(styleValueIdsJson);
        }

        return cartItemRepository.save(cartItem);
    }
        
    /**
     * Update cart item quantity
     */
    @Transactional
    public CartItem updateQuantity(String cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }
        
        cartItem.setQuantity(quantity);
        cartItem.setPrice(getProductPrice(cartItem.getProduct()).multiply(BigDecimal.valueOf(quantity)));
        return cartItemRepository.save(cartItem);
    }
    
    /**
     * Remove item from cart
     */
    @Transactional
    public void removeFromCart(String cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }
    
    /**
     * Get all cart items for current user
     */
    public List<CartItem> getCartItems() {
        User user = getCurrentUser();
        return cartItemRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Get cart items count
     */
    public int getCartCount() {
        try {
            User user = getCurrentUser();
            return cartItemRepository.countByUser(user);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Calculate total cart value
     */
    public BigDecimal getCartTotal() {
        List<CartItem> items = getCartItems();
        return items.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Clear entire cart
     */
    @Transactional
    public void clearCart() {
        User user = getCurrentUser();
        cartItemRepository.deleteByUser(user);
    }
    
    /**
     * Get product effective price (promotional or regular)
     */
    private BigDecimal getProductPrice(Product product) {
        return product.getPromotionalPrice() != null ? 
                product.getPromotionalPrice() : product.getPrice();
    }
}
