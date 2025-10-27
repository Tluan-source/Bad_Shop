package vn.iotstar.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import vn.iotstar.entity.ActivityLog;
import vn.iotstar.entity.User;
import vn.iotstar.repository.ActivityLogRepository;

@Service
public class ActivityLogService {
    
    @Autowired
    private ActivityLogRepository activityLogRepository;
    
    /**
     * Ghi log hoạt động
     */
    @Transactional
    public void logActivity(User user, ActivityLog.ActivityType activityType, String description, 
                           String targetEntity, String targetId, HttpServletRequest request) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setActivityType(activityType);
        log.setDescription(description);
        log.setTargetEntity(targetEntity);
        log.setTargetId(targetId);
        
        // Lấy thông tin IP và User Agent từ request
        if (request != null) {
            log.setIpAddress(getClientIp(request));
            log.setUserAgent(request.getHeader("User-Agent"));
        }
        
        activityLogRepository.save(log);
    }
    
    /**
     * Ghi log hoạt động đơn giản (không cần target)
     */
    @Transactional
    public void logActivity(User user, ActivityLog.ActivityType activityType, String description, HttpServletRequest request) {
        logActivity(user, activityType, description, null, null, request);
    }
    
    /**
     * Lấy N hoạt động gần đây nhất của user
     */
    public List<ActivityLog> getRecentActivities(User user, int limit) {
        List<ActivityLog> allActivities = activityLogRepository.findByUserOrderByCreatedAtDesc(user);
        if (allActivities.size() <= limit) {
            return allActivities;
        }
        return allActivities.subList(0, limit);
    }
    
    /**
     * Lấy tất cả hoạt động của user
     */
    public List<ActivityLog> getUserActivities(User user) {
        return activityLogRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Đếm số hoạt động của user
     */
    public long countUserActivities(User user) {
        return activityLogRepository.countByUser(user);
    }
    
    /**
     * Lấy IP thực của client (xử lý proxy)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Nếu có nhiều IP (qua nhiều proxy), lấy IP đầu tiên
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
