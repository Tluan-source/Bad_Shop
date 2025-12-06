package vn.iotstar.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Table(name = "reviews")
@Data
public class Review {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Column(precision = 2, scale = 1)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(columnDefinition = "NVARCHAR(1000)")
    private String comment;
    
    @Column(columnDefinition = "NVARCHAR(1000)")
    private String vendorReply;
    
    @Column(name = "vendor_reply_at")
    private LocalDateTime vendorReplyAt;
    
    @Column(name = "is_removed")
    private Boolean isRemoved = false;
    
    @Column(name = "removed_reason", columnDefinition = "NVARCHAR(500)")
    private String removedReason;
    
    @Column(name = "removed_by")
    private String removedBy;
    
    @Column(name = "removed_at")
    private LocalDateTime removedAt;
    
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ReviewImage> reviewImages;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private boolean reviewed;
}