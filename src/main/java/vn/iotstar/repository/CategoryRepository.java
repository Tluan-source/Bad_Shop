package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Category;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity
 * @author Vendor Module
 * @since 2025-10-24
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    
    List<Category> findByIsActiveTrueOrderByNameAsc();
    
    Optional<Category> findBySlug(String slug);
    
    List<Category> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    
}
