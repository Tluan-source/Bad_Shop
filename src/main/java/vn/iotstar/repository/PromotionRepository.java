package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Promotion;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Promotion entity
 * @author Vendor Module
 * @since 2025-10-24
 */
@Repository
public interface PromotionRepository extends JpaRepository<Promotion, String> {
    
    // Find promotions by store
    List<Promotion> findByStoreIdOrderByCreatedAtDesc(String storeId);
    
    // Find active promotions by store
    List<Promotion> findByStoreIdAndIsActiveTrueOrderByCreatedAtDesc(String storeId);
    
    // Find active promotions within date range
    @Query("SELECT p FROM Promotion p WHERE p.store.id = :storeId " +
           "AND p.isActive = true " +
           "AND p.startDate <= :now AND p.endDate >= :now")
    List<Promotion> findActivePromotions(
        @Param("storeId") String storeId,
        @Param("now") LocalDateTime now
    );
    
    // Find promotions by apply type
    List<Promotion> findByStoreIdAndAppliesTo(String storeId, Promotion.AppliesTo appliesTo);
    
    // Delete promotion products relationship
    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM promotion_products WHERE promotion_id = :promotionId", nativeQuery = true)
    void deletePromotionProducts(@Param("promotionId") String promotionId);
}