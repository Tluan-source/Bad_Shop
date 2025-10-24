package vn.iotstar.dto.vendor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for displaying Product in Vendor panel (includes all management fields)
 * @author Vendor Module
 * @since 2025-10-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorProductDTO {
    private String id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal promotionalPrice;
    private Integer quantity;
    private Integer sold;
    private Boolean isActive;      // Admin approved
    private Boolean isSelling;     // Vendor control
    private String listImages;     // JSON array
    private BigDecimal rating;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Category info
    private String categoryId;
    private String categoryName;
    
    // Store info
    private String storeId;
    private String storeName;
    
    // Calculated fields
    private BigDecimal totalRevenue; // price * sold
    private String stockStatus;      // "Còn hàng", "Sắp hết", "Hết hàng"
}
