package vn.iotstar.dto.vendor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.iotstar.entity.Promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for displaying Promotion in Vendor panel
 * @author Vendor Module
 * @since 2025-10-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorPromotionDTO {
    private String id;
    private String name;
    private String description;
    private Promotion.DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Promotion.AppliesTo appliesTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Store info
    private String storeId;
    private String storeName;
    
    // Calculated fields
    private Boolean isExpired;
    private Boolean isUpcoming;
    private Integer appliedProductCount;
    private Integer productCount; // Number of products in this promotion
}