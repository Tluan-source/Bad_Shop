package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.StyleValue;

import java.util.List;

/**
 * Repository for StyleValue entity
 * @author BadmintonMarketplace
 * @since 2025-10-25
 */
@Repository
public interface StyleValueRepository extends JpaRepository<StyleValue, String> {
    
    /**
     * Tìm tất cả StyleValue đang hoạt động (chưa xóa)
     */
    List<StyleValue> findByIsDeletedFalse();
    
    /**
     * Tìm StyleValue theo Style ID
     */
    List<StyleValue> findByStyleIdAndIsDeletedFalse(String styleId);

    /**
     * Tìm tất cả StyleValue theo Style ID (bao gồm các giá trị đã vô hiệu)
     */
    List<StyleValue> findByStyleId(String styleId);
    
    /**
     * Tìm StyleValue theo tên
     */
    StyleValue findByNameAndStyleId(String name, String styleId);
}
