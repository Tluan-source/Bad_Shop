package vn.iotstar.dto.vendor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for displaying Store information in Vendor panel
 * @author Vendor Module
 * @since 2025-10-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorStoreDTO {
    private String id;
    private String name;
    private String email;  // Store's own email
    private String phone;  // Store's own phone
    private String bio;
    private String slug;
    private String featuredImages; // JSON array
    private Boolean isActive;
    private Integer point;
    private BigDecimal rating;
    private BigDecimal eWallet;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Owner info
    private String ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
    
    // Commission info
    private String commissionId;
    private String commissionName;
    private BigDecimal commissionFeePercent;
    
    // Statistics
    private Long totalProducts;
    private Long activeProducts;
    private Long totalOrders;
    private Long pendingOrders;
    private BigDecimal monthlyRevenue;
}