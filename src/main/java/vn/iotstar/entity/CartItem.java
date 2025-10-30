package vn.iotstar.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cart_items")
@Data
public class CartItem {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    private Integer quantity = 1;

    @Column(precision = 15, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 🟢 Lưu danh sách ID của các style đã chọn (VD: ["SV01","SV02"])
    @Column(name = "style_value_ids", columnDefinition = "NVARCHAR(MAX)")
    private String styleValueIds;

    // 🟢 Chỉ dùng để hiển thị trên view (không lưu DB)
    @Transient
    private List<String> styleValueNames;
}
