package vn.iotstar.repository;

import vn.iotstar.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    // ===== PUBLIC USER METHODS (EXISTING) =====
    List<Product> findByIsActiveTrueAndIsSellingTrue();
    List<Product> findByStoreIdAndIsActiveTrue(String storeId);
    List<Product> findByCategoryIdAndIsActiveTrueAndIsSellingTrue(String categoryId);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isSelling = true ORDER BY p.sold DESC")
    List<Product> findTop10ByOrderBySoldDesc();
    
    List<Product> findByNameContainingIgnoreCaseAndIsActiveTrueAndIsSellingTrue(String name);
    
    // ===== VENDOR METHODS - Added 2025-10-24 =====
    
    // Find all products by store (for vendor management)
    List<Product> findByStoreIdOrderByCreatedAtDesc(String storeId);
    
    Page<Product> findByStoreId(String storeId, Pageable pageable);
    
    // Find products by store and status
    List<Product> findByStoreIdAndIsSelling(String storeId, Boolean isSelling);
    
    List<Product> findByStoreIdAndIsActive(String storeId, Boolean isActive);
    
    // Count products by store
    Long countByStoreId(String storeId);
    
    Long countByStoreIdAndIsSelling(String storeId, Boolean isSelling);
    
    // Search products in store
    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId " +
           "AND p.name LIKE %:keyword% ORDER BY p.createdAt DESC")
    List<Product> searchByStoreAndName(@Param("storeId") String storeId, 
                                        @Param("keyword") String keyword);
    
    Page<Product> searchByStoreAndName(@Param("storeId") String storeId, 
                                        @Param("keyword") String keyword, 
                                        Pageable pageable);
    
    // Find product by store and id (security check)
    Optional<Product> findByIdAndStoreId(String id, String storeId);
    
    // Get top selling products by store
    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId " +
           "ORDER BY p.sold DESC")
    List<Product> findTopSellingByStore(@Param("storeId") String storeId, Pageable pageable);
    
    // Get products with low stock
    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId " +
           "AND p.quantity < :threshold AND p.isSelling = true")
    List<Product> findLowStockProducts(@Param("storeId") String storeId, 
                                        @Param("threshold") Integer threshold);
}