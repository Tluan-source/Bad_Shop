package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_addresses")
@Data
public class UserAddress {
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "full_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String fullName;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(20)")
    private String phone;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(500)")
    private String address;
    
    @Column(columnDefinition = "NVARCHAR(100)")
    private String province;
    
    @Column(columnDefinition = "NVARCHAR(100)")
    private String district;
    
    @Column(columnDefinition = "NVARCHAR(100)")
    private String ward;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
