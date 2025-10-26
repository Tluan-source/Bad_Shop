package vn.iotstar.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity Style - Loại thuộc tính sản phẩm
 * VD: Màu sắc, Kích thước, Trọng lượng vợt, Độ căng dây...
 * @author BadmintonMarketplace
 * @since 2025-10-25
 */
@Entity
@Table(name = "styles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Style {
    
    @Id
    @Column(length = 50)
    private String id; // VD: "ST001", "ST002"
    
    @Column(nullable = false, unique = true, length = 32, columnDefinition = "NVARCHAR(32)")
    private String name; // VD: "Màu sắc", "Kích thước", "Trọng lượng"
    
    @Column(name = "category_ids", columnDefinition = "NVARCHAR(MAX)")
    private String categoryIds; // JSON array các category_id áp dụng style này
    // VD: ["CAT001","CAT002"] - Màu sắc áp dụng cho Vợt và Giày
    
    @Column(name = "is_deleted")
    private Boolean isDeleted = false; // 0: Đang dùng, 1: Đã xóa
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "style", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<StyleValue> styleValues; // Các giá trị của style này
}
