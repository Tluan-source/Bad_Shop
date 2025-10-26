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
@Table(name = "commissions")
@Data
public class Commission {
    @Id
    private String id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;

    @Column(name = "fee_percent", precision = 5, scale = 2)
    private BigDecimal feePercent = BigDecimal.ZERO; // percentage, e.g. 5.00 -> 5%

    @Column(columnDefinition = "NVARCHAR(500)")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "commission")
    @JsonIgnore
    private List<Store> stores;

    @OneToMany(mappedBy = "commission")
    @JsonIgnore
    private List<Order> orders;
}