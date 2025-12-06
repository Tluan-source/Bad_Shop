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
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    private String id;
    
    @Column(name = "full_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String fullName;
    
    @Column(unique = true, nullable = false, columnDefinition = "NVARCHAR(255)")
    private String email;
    
    @Column(unique = true, length = 20, columnDefinition = "NVARCHAR(20)")
    private String phone;
    
    private String salt;
    
    @Column(name = "hashed_password")
    @JsonIgnore
    private String hashedPassword;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.INACTIVE;
    
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String addresses; // JSON array
    
    private Integer point = 0;
    
    @Column(name = "e_wallet", precision = 15, scale = 2)
    private BigDecimal eWallet = BigDecimal.ZERO;
    
    @Column(name = "avatar")
    private String avatar;

    // New: birthday and bio for profile
    private java.time.LocalDate birthday;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String bio;
    
    @ManyToOne
    @JoinColumn(name = "shipping_provider_id")
    private ShippingProvider shippingProvider;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "owner")
    @JsonIgnore
    private List<Store> stores;
    
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Order> orders;
    
    @OneToMany(mappedBy = "shipper")
    @JsonIgnore
    private List<Shipment> shipments;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<UserAddress> userAddresses;
    
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Favorite> favorites;
    
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<ProductView> productViews;
    
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<CartItem> cartItems;
    
    public enum UserStatus {
        ACTIVE, INACTIVE, BANNED
    }
    
    public enum UserRole {
        USER, ADMIN, SHIPPER, VENDOR
    }
}