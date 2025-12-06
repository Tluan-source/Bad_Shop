package vn.iotstar.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import vn.iotstar.dto.vendor.VendorStoreDTO;
import vn.iotstar.entity.User;
import vn.iotstar.service.UserService;
import vn.iotstar.service.vendor.VendorSecurityService;
import vn.iotstar.service.vendor.VendorStoreService;

@ControllerAdvice
public class GlobalControllerAdvice {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private VendorStoreService vendorStoreService;
    
    @Autowired
    private VendorSecurityService vendorSecurityService;
    
    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            Optional<User> userOpt = userService.findByEmail(authentication.getName());
            return userOpt.orElse(null);
        }
        return null;
    }
    
    @ModelAttribute("isStoreActive")
    public Boolean isStoreActive() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                // Kiểm tra xem user có role VENDOR không
                boolean isVendor = authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .anyMatch(role -> role.equals("ROLE_VENDOR") || role.equals("ROLE_ADMIN"));
                
                System.out.println("=== isStoreActive check ===");
                System.out.println("User: " + authentication.getName());
                System.out.println("Is Vendor: " + isVendor);
                
                if (!isVendor) {
                    return false;
                }
                
                // Lấy userId
                Optional<User> userOpt = userService.findByEmail(authentication.getName());
                if (!userOpt.isPresent()) {
                    System.out.println("User not found in DB");
                    return false;
                }
                String userId = userOpt.get().getId();
                System.out.println("UserId: " + userId);
                
                // Lấy store của vendor
                String storeId = vendorSecurityService.getCurrentVendorStoreId(userId);
                System.out.println("StoreId: " + storeId);
                if (storeId == null) {
                    return false;
                }
                
                VendorStoreDTO store = vendorStoreService.getMyStore(storeId, userId);
                System.out.println("Store: " + store);
                if (store != null) {
                    System.out.println("Store isActive: " + store.getIsActive());
                    return store.getIsActive();
                }
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error in isStoreActive: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}


