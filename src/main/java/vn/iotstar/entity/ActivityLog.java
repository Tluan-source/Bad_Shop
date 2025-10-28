package vn.iotstar.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {
    
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;
    
    @Column(nullable = false, length = 500)
    private String description;
    
    @Column(length = 50)
    private String ipAddress;
    
    @Column(length = 500)
    private String userAgent;
    
    @Column(length = 100)
    private String targetEntity; // Ví dụ: "User", "Store", "Order"
    
    @Column(length = 50)
    private String targetId; // ID của entity bị tác động
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (id == null) {
            id = "AL_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        }
    }
    
    public enum ActivityType {
        LOGIN("Đăng nhập", "success"),
        LOGOUT("Đăng xuất", "secondary"),
        CREATE_USER("Tạo người dùng", "primary"),
        UPDATE_USER("Cập nhật người dùng", "primary"),
        DELETE_USER("Xóa người dùng", "danger"),
        CHANGE_USER_ROLE("Thay đổi vai trò", "warning"),
        TOGGLE_USER_STATUS("Thay đổi trạng thái người dùng", "warning"),
        CREATE_STORE("Tạo cửa hàng", "primary"),
        UPDATE_STORE("Cập nhật cửa hàng", "primary"),
        DELETE_STORE("Xóa cửa hàng", "danger"),
        TOGGLE_STORE_STATUS("Thay đổi trạng thái cửa hàng", "warning"),
        CREATE_CATEGORY("Tạo danh mục", "primary"),
        UPDATE_CATEGORY("Cập nhật danh mục", "primary"),
        DELETE_CATEGORY("Xóa danh mục", "danger"),
        UPDATE_ORDER_STATUS("Cập nhật trạng thái đơn hàng", "info"),
        CREATE_VOUCHER("Tạo voucher", "primary"),
        DELETE_VOUCHER("Xóa voucher", "danger"),
        TOGGLE_VOUCHER_STATUS("Thay đổi trạng thái voucher", "warning"),
        EXPORT_REPORT("Xuất báo cáo", "info"),
        UPDATE_PROFILE("Cập nhật thông tin cá nhân", "primary"),
        CHANGE_PASSWORD("Đổi mật khẩu", "warning"),
        UPLOAD_AVATAR("Tải lên ảnh đại diện", "primary"),
        VIEW_DASHBOARD("Xem dashboard", "info"),
        VIEW_REPORT("Xem báo cáo", "info"),
        ADMIN_ACTION("Hành động quản trị", "primary");
        
        private final String displayName;
        private final String badgeColor;
        
        ActivityType(String displayName, String badgeColor) {
            this.displayName = displayName;
            this.badgeColor = badgeColor;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getBadgeColor() {
            return badgeColor;
        }
    }
}