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
@Table(name = "users")
@Data
public class User {
    @Id
    private String id;
    
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(unique = true)
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