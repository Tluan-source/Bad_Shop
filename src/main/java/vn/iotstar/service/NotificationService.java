package vn.iotstar.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.iotstar.entity.Notification;
import vn.iotstar.entity.User;
import vn.iotstar.repository.NotificationRepository;

@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public Notification createNotification(User user, String title, String content, 
                                          Notification.NotificationType type, String relatedId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }
    
    public void notifyAccountLocked(User user, String reason, String lockedBy) {
        String title = "Warning: Your account has been locked";
        String content = String.format(
            "Your account has been locked by admin %s.\n\nReason: %s\n\n" +
            "Please contact support for more information.",
            lockedBy, reason
        );
        
        createNotification(user, title, content, Notification.NotificationType.ACCOUNT_LOCKED, user.getId());
    }
    
    public void notifyStoreLocked(User vendor, String storeName, String storeId, 
                                 String reason, String lockedBy) {
        String title = "Warning: Store '" + storeName + "' has been locked";
        String content = String.format(
            "Your store '%s' has been locked by admin %s.\n\nReason: %s\n\n" +
            "Please contact support for more information.",
            storeName, lockedBy, reason
        );
        
        createNotification(vendor, title, content, Notification.NotificationType.STORE_LOCKED, storeId);
    }
    
    public void notifyProductRejected(User vendor, String productName, String productId,
                                     String reason, String rejectedBy) {
        String title = "Product '" + productName + "' has been rejected";
        String content = String.format(
            "Your product '%s' has been rejected by admin.\n\nReason: %s\n\n" +
            "Please edit the product according to requirements and resubmit.",
            productName, reason
        );
        
        createNotification(vendor, title, content, Notification.NotificationType.PRODUCT_REJECTED, productId);
    }
    
    public void notifyCommentRemoved(User user, String productName, String reason, String removedBy) {
        String title = "Cảnh báo: Bình luận của bạn đã bị xóa";
        String content = String.format(
            "Bình luận của bạn về sản phẩm '%s' đã bị xóa bởi Admin.\n\nLý do: %s\n\n" +
            "Vui lòng tuân thủ các quy tắc cộng đồng khi đăng bình luận.",
            productName, reason
        );
        
        createNotification(user, title, content, Notification.NotificationType.COMMENT_REMOVED, null);
    }
    
    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findUnreadByUserId(userId);
    }
    
    public long countUnreadNotifications(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }
    
    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }
}