package vn.iotstar.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String title;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(1000)")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;
    
    @Column(name = "related_id", columnDefinition = "NVARCHAR(255)")
    private String relatedId;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public enum NotificationType {
        ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED,
        STORE_LOCKED,
        STORE_UNLOCKED,
        PRODUCT_REJECTED,
        PRODUCT_APPROVED,
        COMMENT_REMOVED,
        ORDER_STATUS,
        SYSTEM
    }
}