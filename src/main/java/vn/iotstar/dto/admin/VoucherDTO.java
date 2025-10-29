package vn.iotstar.dto.admin;

import lombok.Data;
import vn.iotstar.entity.Voucher;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for creating/updating Voucher (Admin only)
 */
@Data
public class VoucherDTO {
    private String id;
    private String code;
    private String description;
    private Voucher.DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderValue;
    private Integer quantity;
    private Integer usageCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    
    // Helper fields for display
    private Integer remainingQuantity;
    private Double usagePercentage;
    private Boolean isExpired;
    private Boolean isAvailable;
    
    public static VoucherDTO fromEntity(Voucher voucher) {
        VoucherDTO dto = new VoucherDTO();
        dto.setId(voucher.getId());
        dto.setCode(voucher.getCode());
        dto.setDescription(voucher.getDescription());
        dto.setDiscountType(voucher.getDiscountType());
        dto.setDiscountValue(voucher.getDiscountValue());
        dto.setMaxDiscount(voucher.getMaxDiscount());
        dto.setMinOrderValue(voucher.getMinOrderValue());
        dto.setQuantity(voucher.getQuantity());
        dto.setUsageCount(voucher.getUsageCount());
        dto.setStartDate(voucher.getStartDate());
        dto.setEndDate(voucher.getEndDate());
        dto.setIsActive(voucher.getIsActive());
        
        // Calculate helper fields
        dto.setRemainingQuantity(voucher.getRemainingQuantity());
        dto.setUsagePercentage(voucher.getUsagePercentage());
        dto.setIsExpired(voucher.isExpired());
        dto.setIsAvailable(voucher.isAvailable());
        
        return dto;
    }
    
    public Voucher toEntity() {
        Voucher voucher = new Voucher();
        voucher.setId(this.id);
        voucher.setCode(this.code);
        voucher.setDescription(this.description);
        voucher.setDiscountType(this.discountType);
        voucher.setDiscountValue(this.discountValue);
        voucher.setMaxDiscount(this.maxDiscount);
        voucher.setMinOrderValue(this.minOrderValue);
        voucher.setQuantity(this.quantity);
        voucher.setUsageCount(this.usageCount != null ? this.usageCount : 0);
        voucher.setStartDate(this.startDate);
        voucher.setEndDate(this.endDate);
        voucher.setIsActive(this.isActive != null ? this.isActive : true);
        return voucher;
    }
    
    // Custom getter methods for template compatibility
    public boolean isExpired() {
        return this.isExpired != null && this.isExpired;
    }
    
    public boolean isAvailable() {
        return this.isAvailable != null && this.isAvailable;
    }
}