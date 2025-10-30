package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "promotions")
@Data
public class Promotion {
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(200)")
    private String name;
    
    @Column(columnDefinition = "NVARCHAR(1000)")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType = DiscountType.PERCENTAGE;
    
    @Column(name = "discount_value", precision = 15, scale = 2)
    private BigDecimal discountValue = BigDecimal.ZERO;
    
    @Column(name = "max_discount", precision = 15, scale = 2)
    private BigDecimal maxDiscount = BigDecimal.ZERO;
    
    @Column(name = "min_order_amount", precision = 15, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;
    
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
    
    @Column(name = "applies_to")
    @Enumerated(EnumType.STRING)
    private AppliesTo appliesTo = AppliesTo.ALL_PRODUCTS;
    
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "promotion_products",
        joinColumns = @JoinColumn(name = "promotion_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;
    
    @OneToMany(mappedBy = "promotion")
    private List<Order> orders;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
    }
    
    public enum AppliesTo {
        ALL_PRODUCTS, SPECIFIC_PRODUCTS, CATEGORY
    }
    
    // Helper methods
    public boolean isExpired() {
        if (endDate == null) return false;
        return LocalDateTime.now().isAfter(endDate);
    }
    
    public boolean isNotStarted() {
        if (startDate == null) return false;
        return LocalDateTime.now().isBefore(startDate);
    }
    
    public boolean isUpcoming() {
        return isNotStarted();
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
    
    public Integer getProductCount() {
        return products != null ? products.size() : 0;
    }
    
    public boolean isApplicableToProduct(Product product) {
        if (!isAvailable()) return false;
        
        switch (appliesTo) {
            case ALL_PRODUCTS:
                return true;
            case SPECIFIC_PRODUCTS:
                return products != null && products.contains(product);
            case CATEGORY:
                // Có thể mở rộng thêm logic kiểm tra category
                return true;
            default:
                return false;
        }
    }
    
    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (!isAvailable()) return BigDecimal.ZERO;
        
        BigDecimal discount = BigDecimal.ZERO;
        
        switch (discountType) {
            case PERCENTAGE:
                discount = amount.multiply(discountValue).divide(BigDecimal.valueOf(100));
                if (maxDiscount != null && maxDiscount.compareTo(BigDecimal.ZERO) > 0) {
                    discount = discount.min(maxDiscount);
                }
                break;
            case FIXED_AMOUNT:
                discount = discountValue;
                break;
            case FREE_SHIPPING:
                // Xử lý miễn phí ship ở logic Order
                discount = BigDecimal.ZERO;
                break;
        }
        
        return discount;
    }
    
}