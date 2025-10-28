package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Commission;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Commission entity
 * Admin uses this to manage app discount/commission for stores
 */
@Repository
public interface CommissionRepository extends JpaRepository<Commission, String> {
    
    /**
     * Find commission by name
     */
    Optional<Commission> findByName(String name);
    
    /**
     * Search commissions by name or description
     */
    @Query("SELECT c FROM Commission c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Commission> searchCommissions(@Param("keyword") String keyword);
    
    /**
     * Find all commissions ordered by name
     */
    List<Commission> findAllByOrderByNameAsc();
    
    /**
     * Check if commission name exists (excluding specific id)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Commission c " +
           "WHERE LOWER(c.name) = LOWER(:name) AND c.id != :excludeId")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("excludeId") String excludeId);
    
    /**
     * Check if commission name exists
     */
    boolean existsByNameIgnoreCase(String name);
}
