package vn.iotstar.service.vendor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.iotstar.dto.vendor.PromotionCreateDTO;
import vn.iotstar.dto.vendor.VendorPromotionDTO;

import java.util.List;

/**
 * Service interface for Vendor Promotion Management
 * Vendors can create and manage their own promotions
 * 
 * @author Vendor Module
 * @since 2025-10-27
 */
public interface VendorPromotionService {
    
    /**
     * Get all promotions of a store
     */
    List<VendorPromotionDTO> getMyPromotions(String storeId);
    
    Page<VendorPromotionDTO> getMyPromotions(String storeId, Pageable pageable);
    
    /**
     * Get active promotions only
     */
    List<VendorPromotionDTO> getActivePromotions(String storeId);
    
    /**
     * Get promotion detail
     */
    VendorPromotionDTO getPromotionDetail(String promotionId, String storeId);
    
    /**
     * Create new promotion
     */
    VendorPromotionDTO createPromotion(PromotionCreateDTO dto, String storeId);
    
    /**
     * Update promotion
     */
    VendorPromotionDTO updatePromotion(String promotionId, PromotionCreateDTO dto, String storeId);
    
    /**
     * Activate/Deactivate promotion
     */
    void togglePromotionStatus(String promotionId, String storeId, boolean isActive);
    
    /**
     * Delete promotion
     */
    void deletePromotion(String promotionId, String storeId);
    
    /**
     * Check if promotion name exists in store
     */
    boolean isPromotionNameExists(String storeId, String name, String excludeId);
}
