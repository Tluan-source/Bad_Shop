package vn.iotstar.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import vn.iotstar.entity.Notification;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.NotificationService;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Display notifications page
     */
    @GetMapping
    public String notificationsPage(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        List<Notification> notifications = notificationService.getUserNotifications(user.getId());
        long unreadCount = notificationService.countUnreadNotifications(user.getId());
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        return "user/notifications";
    }
    
    /**
     * Get notifications list (API endpoint)
     */
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<List<Notification>> getNotifications(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        List<Notification> notifications = notificationService.getUserNotifications(user.getId());
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Get unread notifications count
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(@AuthenticationPrincipal User user) {
        long unreadCount = notificationService.countUnreadNotifications(user.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("count", unreadCount);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get recent unread notifications (for dropdown)
     */
    @GetMapping("/recent")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRecentNotifications(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        System.out.println("DEBUG: User ID = " + user.getId());
        System.out.println("DEBUG: User Email = " + user.getEmail());
        
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(user.getId());
        System.out.println("DEBUG: Unread notifications count = " + unreadNotifications.size());
        
        long totalUnread = notificationService.countUnreadNotifications(user.getId());
        
        // Limit to 5 most recent for dropdown
        List<Notification> recentNotifications = unreadNotifications.size() > 5 
            ? unreadNotifications.subList(0, 5) 
            : unreadNotifications;
        
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", recentNotifications);
        response.put("totalUnread", totalUnread);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Mark a notification as read
     */
    @PostMapping("/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable String id,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        notificationService.markAsRead(id);
        long unreadCount = notificationService.countUnreadNotifications(user.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("unreadCount", unreadCount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Mark all notifications as read
     */
    @PostMapping("/read-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        notificationService.markAllAsRead(user.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("unreadCount", 0);
        
        return ResponseEntity.ok(response);
    }
}