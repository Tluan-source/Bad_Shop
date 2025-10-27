package vn.iotstar.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.iotstar.entity.ActivityLog;
import vn.iotstar.entity.User;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {
    
    // Lấy các hoạt động gần đây của một user
    List<ActivityLog> findByUserOrderByCreatedAtDesc(User user);
    
    // Lấy N hoạt động gần đây nhất của user
    @Query("SELECT al FROM ActivityLog al WHERE al.user = :user ORDER BY al.createdAt DESC")
    List<ActivityLog> findTopByUser(@Param("user") User user);
    
    // Lấy hoạt động trong khoảng thời gian
    @Query("SELECT al FROM ActivityLog al WHERE al.user = :user AND al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    List<ActivityLog> findByUserAndDateRange(@Param("user") User user, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    // Lấy hoạt động theo loại
    List<ActivityLog> findByUserAndActivityTypeOrderByCreatedAtDesc(User user, ActivityLog.ActivityType activityType);
    
    // Đếm số hoạt động của user
    long countByUser(User user);
}
