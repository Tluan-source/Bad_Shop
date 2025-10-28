package vn.iotstar.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.entity.Favorite;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.repository.FavoriteRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    
    private final FavoriteRepository favoriteRepository;
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
     * Toggle favorite (add if not exists, remove if exists)
     */
    @Transactional
    public boolean toggleFavorite(String productId) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (favoriteRepository.existsByUserAndProduct(user, product)) {
            favoriteRepository.deleteByUserAndProduct(user, product);
            return false; // Removed
        } else {
            Favorite favorite = new Favorite();
            favorite.setId(UUID.randomUUID().toString());
            favorite.setUser(user);
            favorite.setProduct(product);
            favoriteRepository.save(favorite);
            return true; // Added
        }
    }
    
    /**
     * Check if product is in favorites
     */
    public boolean isFavorite(String productId) {
        try {
            User user = getCurrentUser();
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) return false;
            return favoriteRepository.existsByUserAndProduct(user, product);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get all favorites for current user
     */
    public List<Favorite> getFavorites() {
        User user = getCurrentUser();
        return favoriteRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Get favorites count
     */
    public int getFavoriteCount() {
        try {
            User user = getCurrentUser();
            return favoriteRepository.countByUser(user);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Remove from favorites
     */
    @Transactional
    public void removeFavorite(String productId) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        favoriteRepository.deleteByUserAndProduct(user, product);
    }
}
