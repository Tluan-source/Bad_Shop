package vn.iotstar.service.vendor;

import vn.iotstar.dto.vendor.StoreUpdateDTO;
import vn.iotstar.dto.vendor.VendorStoreDTO;
import vn.iotstar.entity.Store;

import java.util.List;

/**
 * Service interface for Vendor Store Management
 * @author Vendor Module
 * @since 2025-10-24
 */
public interface VendorStoreService {
    
    /**
     * Get store by ID with security check (owner must match current user)
     */
    VendorStoreDTO getMyStore(String storeId, String currentUserId);
    
    /**
     * Get all stores owned by user
     */
    List<VendorStoreDTO> getMyStores(String currentUserId);
    
    /**
     * Update store information
     */
    VendorStoreDTO updateStore(String storeId, StoreUpdateDTO updateDTO, String currentUserId);
    
    /**
     * Get store entity (for internal use)
     */
    Store getStoreEntity(String storeId);
    
    /**
     * Check if user owns the store
     */
    boolean isOwner(String storeId, String userId);
}
