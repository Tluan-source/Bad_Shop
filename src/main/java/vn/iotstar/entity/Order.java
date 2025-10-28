package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
    
    @ManyToOne
    @JoinColumn(name = "commission_id")
    private Commission commission;
    
    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;
    
    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;
    
    @Column(columnDefinition = "NVARCHAR(500)")
    private String address;
    
    @Column(columnDefinition = "NVARCHAR(20)")
    private String phone;
    
    @Column(name = "shipping_fee", precision = 15, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.NOT_PROCESSED;
    
    @Column(name = "is_paid_before")
    private Boolean isPaidBefore = false;
    
    @Column(name = "amount_from_user", precision = 15, scale = 2)
    private BigDecimal amountFromUser = BigDecimal.ZERO;
    
    @Column(name = "amount_from_store", precision = 15, scale = 2)
    private BigDecimal amountFromStore = BigDecimal.ZERO;
    
    @Column(name = "amount_to_store", precision = 15, scale = 2)
    private BigDecimal amountToStore = BigDecimal.ZERO;
    
    @Column(name = "amount_to_gd", precision = 15, scale = 2)
    private BigDecimal amountToGd = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "promotion_discount", precision = 15, scale = 2)
    private BigDecimal promotionDiscount = BigDecimal.ZERO;
    
    @Column(name = "voucher_discount", precision = 15, scale = 2)
    private BigDecimal voucherDiscount = BigDecimal.ZERO;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
    
    @OneToOne(mappedBy = "order")
    private Shipment shipment;
    
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;
    
    public enum OrderStatus {
        NOT_PROCESSED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, RETURNED
    }
}