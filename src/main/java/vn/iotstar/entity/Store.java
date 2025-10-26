package vn.iotstar.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "stores")
@Data
public class Store {
    @Id
    private String id;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;
    
    @Column(columnDefinition = "NVARCHAR(1000)")
    private String bio;
    
    @Column(columnDefinition = "NVARCHAR(255)")
    private String slug;
    
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    @ManyToOne
    @JoinColumn(name = "commission_id")
    private Commission commission;
    
    @Column(name = "is_active")
    private Boolean isActive = false;
    
    @Column(name = "featured_images", columnDefinition = "NVARCHAR(MAX)")
    private String featuredImages; // JSON array
    
    private Integer point = 0;
    
    @Column(precision = 2, scale = 1)
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Column(name = "e_wallet", precision = 15, scale = 2)
    private BigDecimal eWallet = BigDecimal.ZERO;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "store")
    @JsonIgnore
    private List<Product> products;
    
    @OneToMany(mappedBy = "store")
    @JsonIgnore
    private List<Order> orders;
    
    @OneToMany(mappedBy = "store")
    @JsonIgnore
    private List<Promotion> promotions;
}