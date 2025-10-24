package vn.iotstar.service.vendor;

/**
 * Service interface for Vendor Security checks
 * Ensures vendor can only access their own data
 * 
 * @author Vendor Module
 * @since 2025-10-24
 */
public interface VendorSecurityService {
    
    /**
     * Check if user owns the store
     * @throws vn.iotstar.exception.UnauthorizedStoreAccessException if not owner
     */
    void checkStoreOwnership(String storeId, String userId);
    
    /**
     * Check if product belongs to vendor's store
     * @throws vn.iotstar.exception.UnauthorizedAccessException if not
     */
    void checkProductOwnership(String productId, String storeId);
    
    /**
     * Check if order belongs to vendor's store
     * @throws vn.iotstar.exception.UnauthorizedAccessException if not
     */
    void checkOrderOwnership(String orderId, String storeId);
    
    /**
     * Get store ID of current logged-in vendor
     */
    String getCurrentVendorStoreId(String userId);
    
    /**
     * Verify user has VENDOR role
     */
    boolean isVendor(String userId);
}
