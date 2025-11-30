package vn.iotstar.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {
    
    // Approval status enum for admin moderation
    public enum ApprovalStatus {
        PENDING,    // Chờ duyệt
        APPROVED,   // Đã duyệt
        REJECTED    // Bị từ chối
    }
    
    @Id
    private String id;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(500)")
    private String name;
    
    @Column(columnDefinition = "NVARCHAR(500)")
    private String slug;
    
    @Column(columnDefinition = "NVARCHAR(1000)")
    private String description;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal price;
    
    @Column(name = "promotional_price", precision = 15, scale = 2)
    private BigDecimal promotionalPrice;
    
    private Integer quantity = 0;
    
    private Integer sold = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = false;
    
    @Column(name = "is_selling")
    private Boolean isSelling = true;
    
    // Admin moderation fields
    @Column(name = "approval_status")
    @Enumerated(EnumType.ORDINAL)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    
    @Column(name = "rejection_reason", columnDefinition = "NVARCHAR(1000)")
    private String rejectionReason;
    
    @Column(name = "moderated_by")
    private String moderatedBy; // Admin user ID who approved/rejected
    
    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;
    
    @Column(name = "list_images", columnDefinition = "NVARCHAR(MAX)")
    private String listImages; // JSON array
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
    
    @Column(name = "style_value_ids", columnDefinition = "NVARCHAR(MAX)")
    private String styleValueIds; // JSON array
    
    @Column(precision = 2, scale = 1)
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<CartItem> cartItems;
    
    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<OrderItem> orderItems;
    
    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<Review> reviews;
    
    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<Favorite> favorites;
    
    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<ProductView> productViews;
    
    @ManyToMany(mappedBy = "products")
    @JsonIgnore
    private List<Promotion> promotions;

    
    // Helper method to get first image from JSON array
    public String getFirstImage() {
        if (listImages == null || listImages.isEmpty() || listImages.equals("[]")) {
            return "https://via.placeholder.com/500x500/CCCCCC/666666?text=No+Image";
        }
        try {
            // Parse JSON array and get first element
            String trimmed = listImages.trim();
            if (trimmed.startsWith("[") && trimmed.contains("\"")) {
                int start = trimmed.indexOf("\"") + 1;
                int end = trimmed.indexOf("\"", start);
                if (end > start) {
                    return trimmed.substring(start, end);
                }
            }
        } catch (Exception e) {
            // Return default image if parsing fails
        }
        return "https://via.placeholder.com/500x500/CCCCCC/666666?text=No+Image";
    }
    
    // Helper method to parse all images from JSON array
    public List<String> getParsedImages() {
        List<String> images = new ArrayList<>();
        if (listImages == null || listImages.isEmpty() || listImages.equals("[]")) {
            return images;
        }
        try {
            // Parse JSON array: ["url1", "url2", "url3"]
            String trimmed = listImages.trim();
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                String content = trimmed.substring(1, trimmed.length() - 1);
                String[] parts = content.split("\",\"");
                for (String part : parts) {
                    String url = part.replace("\"", "").trim();
                    if (!url.isEmpty()) {
                        images.add(url);
                    }
                }
            }
        } catch (Exception e) {
            // Return empty list if parsing fails
        }
        return images;
    }
}
