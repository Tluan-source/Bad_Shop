package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vouchers")
@Data
public class Voucher {
    
    public enum DiscountType {
        PERCENTAGE,  // Giảm theo phần trăm
        FIXED        // Giảm theo số tiền cố định
    }
    
    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String code;
    
    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType;

    @Column(name = "discount_value", precision = 15, scale = 2)
    private BigDecimal discountValue = BigDecimal.ZERO;

    @Column(name = "max_discount", precision = 15, scale = 2)
    private BigDecimal maxDiscount = BigDecimal.ZERO;

    @Column(name = "min_order_value", precision = 15, scale = 2)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "quantity")
    private Integer quantity = 0;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "voucher")
    private List<Order> orders;
    
    // Helper methods
    public boolean isExpired() {
        if (endDate == null) return false;
        return LocalDateTime.now().isAfter(endDate);
    }
    
    public boolean isNotStarted() {
        if (startDate == null) return false;
        return LocalDateTime.now().isBefore(startDate);
    }
    
    public boolean isAvailable() {
        if (isActive == null) return false;
        if (quantity == null || usageCount == null) return false;
        return isActive && !isExpired() && !isNotStarted() && usageCount < quantity;
    }
    
    public int getRemainingQuantity() {
        if (quantity == null || usageCount == null) return 0;
        return quantity - usageCount;
    }
    
    public double getUsagePercentage() {
        if (quantity == null || quantity == 0 || usageCount == null) return 0;
        return (usageCount * 100.0) / quantity;
    }
}