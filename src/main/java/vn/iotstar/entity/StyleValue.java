package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity StyleValue - Giá trị cụ thể của thuộc tính
 * VD: Đỏ, Xanh, Vàng (của Style "Màu sắc")
 *     S, M, L, XL (của Style "Kích thước")
 *     5U (75-79g), 4U (80-84g) (của Style "Trọng lượng vợt")
 * @author BadmintonMarketplace
 * @since 2025-10-25
 */
@Entity
@Table(name = "style_values")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StyleValue {
    
    @Id
    @Column(length = 50)
    private String id; // VD: "SV001", "SV007"
    
    @Column(nullable = false, length = 32, columnDefinition = "NVARCHAR(32)")
    private String name; // VD: "Đỏ", "M", "5U (75-79g)"
    
    @ManyToOne
    @JoinColumn(name = "style_id", nullable = false)
    private Style style; // Thuộc style nào
    
    @Column(name = "is_deleted")
    private Boolean isDeleted = false; // 0: Đang dùng, 1: Đã xóa
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
