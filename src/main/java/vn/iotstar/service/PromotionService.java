package vn.iotstar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Promotion;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.PromotionRepository;
import vn.iotstar.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing Promotions (Vendor-managed, store-specific promotions)
 */
@Service
@RequiredArgsConstructor
public class PromotionService {
    
    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    
    /**
     * Create a new promotion for a store
     */
    @Transactional
    public Promotion createPromotion(Promotion promotion, Store store) {
        promotion.setId(UUID.randomUUID().toString());
        promotion.setStore(store);
        promotion.setCreatedAt(LocalDateTime.now());
        
        // If specific products, validate they belong to the store
        if (promotion.getAppliesTo() == Promotion.AppliesTo.SPECIFIC_PRODUCTS && 
            promotion.getProducts() != null) {
            for (Product product : promotion.getProducts()) {
                if (!product.getStore().getId().equals(store.getId())) {
                    throw new RuntimeException("Product does not belong to this store");
                }
            }
        }
        
        return promotionRepository.save(promotion);
    }
    
    /**
     * Update existing promotion
     */
    @Transactional
    public Promotion updatePromotion(String id, Promotion promotionDetails) {
        Promotion promotion = promotionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Promotion not found"));
        
        // Verify ownership
        if (!promotion.getStore().getId().equals(promotionDetails.getStore().getId())) {
            throw new RuntimeException("Cannot update promotion of another store");
        }
        
        promotion.setName(promotionDetails.getName());
        promotion.setDescription(promotionDetails.getDescription());
        promotion.setDiscountType(promotionDetails.getDiscountType());
        promotion.setDiscountValue(promotionDetails.getDiscountValue());
        promotion.setMaxDiscount(promotionDetails.getMaxDiscount());
        promotion.setMinOrderAmount(promotionDetails.getMinOrderAmount());
        promotion.setStartDate(promotionDetails.getStartDate());
        promotion.setEndDate(promotionDetails.getEndDate());
        promotion.setIsActive(promotionDetails.getIsActive());
        promotion.setAppliesTo(promotionDetails.getAppliesTo());
        
        // Update products if specific products
        if (promotionDetails.getAppliesTo() == Promotion.AppliesTo.SPECIFIC_PRODUCTS) {
            promotion.setProducts(promotionDetails.getProducts());
        } else {
            promotion.setProducts(null);
        }
        
        return promotionRepository.save(promotion);
    }
    
    /**
     * Delete promotion
     */
    @Transactional
    public void deletePromotion(String id) {
        Promotion promotion = promotionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Promotion not found"));
        
        // Delete many-to-many relationships first
        promotionRepository.deletePromotionProducts(id);
        promotionRepository.delete(promotion);
    }
    
    /**
     * Get all promotions for a store
     */
    public List<Promotion> getPromotionsByStore(String storeId) {
        return promotionRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
    }
    
    /**
     * Get active promotions for a store
     */
    public List<Promotion> getActivePromotionsByStore(String storeId) {
        return promotionRepository.findByStoreIdAndIsActiveTrueOrderByCreatedAtDesc(storeId);
    }
    
    /**
     * Get currently running promotions for a store
     */
    public List<Promotion> getCurrentPromotions(String storeId) {
        return promotionRepository.findActivePromotions(storeId, LocalDateTime.now());
    }
    
    /**
     * Find best promotion for a product
     */
    public Promotion findBestPromotionForProduct(Product product) {
        List<Promotion> promotions = promotionRepository.findActivePromotions(
            product.getStore().getId(), 
            LocalDateTime.now()
        );
        
        Promotion bestPromotion = null;
        BigDecimal maxDiscount = BigDecimal.ZERO;
        
        for (Promotion promotion : promotions) {
            if (!promotion.isApplicableToProduct(product)) {
                continue;
            }
            
            BigDecimal discount = promotion.calculateDiscount(product.getPrice());
            if (discount.compareTo(maxDiscount) > 0) {
                maxDiscount = discount;
                bestPromotion = promotion;
            }
        }
        
        return bestPromotion;
    }
    
    /**
     * Calculate promotional price for a product
     */
    public BigDecimal calculatePromotionalPrice(Product product) {
        Promotion promotion = findBestPromotionForProduct(product);
        
        if (promotion == null) {
            return product.getPrice();
        }
        
        BigDecimal discount = promotion.calculateDiscount(product.getPrice());
        return product.getPrice().subtract(discount);
    }
    
    /**
     * Apply promotion to order
     */
    public PromotionResult applyPromotionToOrder(String storeId, BigDecimal orderAmount, 
                                                  BigDecimal shippingFee) {
        List<Promotion> promotions = promotionRepository.findActivePromotions(
            storeId, 
            LocalDateTime.now()
        );
        
        Promotion bestPromotion = null;
        BigDecimal maxDiscount = BigDecimal.ZERO;
        boolean freeShipping = false;
        
        // Check minimum order amount and find best promotion
        for (Promotion promotion : promotions) {
            if (promotion.getMinOrderAmount() != null && 
                orderAmount.compareTo(promotion.getMinOrderAmount()) < 0) {
                continue;
            }
            
            if (promotion.getDiscountType() == Promotion.DiscountType.FREE_SHIPPING) {
                freeShipping = true;
                bestPromotion = promotion;
                continue;
            }
            
            BigDecimal discount = promotion.calculateDiscount(orderAmount);
            if (discount.compareTo(maxDiscount) > 0) {
                maxDiscount = discount;
                bestPromotion = promotion;
            }
        }
        
        return new PromotionResult(
            bestPromotion, 
            maxDiscount, 
            freeShipping ? BigDecimal.ZERO : shippingFee
        );
    }
    
    /**
     * Toggle promotion active status
     */
    @Transactional
    public Promotion toggleActive(String id, String storeId) {
        Promotion promotion = promotionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Promotion not found"));
        
        // Verify ownership
        if (!promotion.getStore().getId().equals(storeId)) {
            throw new RuntimeException("Cannot modify promotion of another store");
        }
        
        promotion.setIsActive(!promotion.getIsActive());
        return promotionRepository.save(promotion);
    }
    
    /**
     * Add products to promotion
     */
    @Transactional
    public Promotion addProductsToPromotion(String promotionId, List<String> productIds) {
        Promotion promotion = promotionRepository.findById(promotionId)
            .orElseThrow(() -> new RuntimeException("Promotion not found"));
        
        if (promotion.getAppliesTo() != Promotion.AppliesTo.SPECIFIC_PRODUCTS) {
            throw new RuntimeException("Can only add products to SPECIFIC_PRODUCTS promotions");
        }
        
        List<Product> products = productRepository.findAllById(productIds);
        
        // Verify all products belong to the same store
        for (Product product : products) {
            if (!product.getStore().getId().equals(promotion.getStore().getId())) {
                throw new RuntimeException("Product does not belong to this store");
            }
        }
        
        promotion.setProducts(products);
        return promotionRepository.save(promotion);
    }
    
    /**
     * Get promotion by ID (with ownership check)
     */
    public Promotion getPromotionById(String id, String storeId) {
        Promotion promotion = promotionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Promotion not found"));
        
        if (!promotion.getStore().getId().equals(storeId)) {
            throw new RuntimeException("Access denied");
        }
        
        return promotion;
    }
    
    // Result class for applying promotion to order
    public static class PromotionResult {
        public final Promotion promotion;
        public final BigDecimal discount;
        public final BigDecimal finalShippingFee;
        
        public PromotionResult(Promotion promotion, BigDecimal discount, BigDecimal finalShippingFee) {
            this.promotion = promotion;
            this.discount = discount;
            this.finalShippingFee = finalShippingFee;
        }
    }
}
