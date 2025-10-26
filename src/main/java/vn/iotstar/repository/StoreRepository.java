package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Store;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Store entity
 * @author Vendor Module
 * @since 2025-10-24
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, String> {
    
    // Find store by owner ID
    List<Store> findByOwnerId(String ownerId);
    
    Optional<Store> findByOwnerIdAndId(String ownerId, String storeId);
    
    // Find active stores
    List<Store> findByIsActiveTrue();
    
    // Find stores by owner and active status
    List<Store> findByOwnerIdAndIsActiveTrue(String ownerId);
    
    // Check if user owns a store
    boolean existsByOwnerIdAndId(String ownerId, String storeId);
    
    // Get store with products count
    @Query("SELECT s FROM Store s LEFT JOIN FETCH s.products WHERE s.id = :storeId")
    Optional<Store> findByIdWithProducts(@Param("storeId") String storeId);
    
    // Search stores by name
    List<Store> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    
    // Search stores by name for admin (all statuses)
    List<Store> findByNameContainingIgnoreCase(String name);
    
    // Search stores by name or owner name
    @Query("SELECT s FROM Store s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.owner.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Store> searchStoresByNameOrOwner(@Param("keyword") String keyword);
}