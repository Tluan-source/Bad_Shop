package vn.iotstar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.iotstar.entity.Style;

/**
 * Repository for Style entity
 * @author BadmintonMarketplace
 * @since 2025-10-25
 */
@Repository
public interface StyleRepository extends JpaRepository<Style, String> {
    
    /**
     * Tìm tất cả Style đang hoạt động (chưa xóa)
     */
    List<Style> findByIsDeletedFalse();
    
    /**
     * Tìm Style theo tên
     */
    Style findByName(String name);
    
    /**
     * Kiểm tra Style có áp dụng cho category này không
     * (Cần implement ở service layer vì categoryIds là JSON)
     */
    // List<Style> findByCategoryIdsContaining(String categoryId);
    
    @Query(
    value = "SELECT * FROM styles s WHERE JSON_VALUE(s.category_ids, '$') LIKE %:categoryId% AND s.is_deleted = 0",
    nativeQuery = true
    )
    List<Style> findByCategoryId(String categoryId);
}
