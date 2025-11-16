package vn.iotstar.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "shipments")
@Data
public class Shipment {
    @Id
    private String id;
    
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "shipper_id")
    private User shipper;
    
    @ManyToOne
    @JoinColumn(name = "shipping_provider_id")
    private ShippingProvider shippingProvider;
    
    @Enumerated(EnumType.STRING)
    private ShipmentStatus status = ShipmentStatus.DELIVERING;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "shipping_fee", precision = 15, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;
    
    @Column(columnDefinition = "NVARCHAR(500)")
    private String note;

    @Column(name = "delivery_image_url", columnDefinition = "NVARCHAR(500)")
    private String deliveryImageUrl;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum ShipmentStatus {
        ASSIGNED,        // shipper nhận giao
    DELIVERING,      // đang giao
    DELIVERED,       // giao thành công (kèm ảnh)
    FAILED 
    }
}