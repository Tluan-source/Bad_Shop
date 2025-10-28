package vn.iotstar.service.vendor;

import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.vendor.StoreRegistrationDTO;
import vn.iotstar.entity.User;

/**
 * Service for vendor registration
 */
public interface VendorRegistrationService {
    
    /**
     * Register a new vendor
     * Creates store, uploads files, but keeps user as USER role (waiting for admin approval)
     * 
     * @param user Current user
     * @param registrationDTO Registration data
     * @param logoFile Logo file (optional)
     * @param licenseFile Business license file (optional)
     * @return Store ID
     */
    String registerVendor(User user, StoreRegistrationDTO registrationDTO, 
                         MultipartFile logoFile, MultipartFile licenseFile);
    
    /**
     * Check if user has pending vendor registration
     */
    boolean hasPendingRegistration(String userId);
    
    /**
     * Get pending registration store ID
     */
    String getPendingStoreId(String userId);
}
