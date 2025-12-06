package vn.iotstar.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.iotstar.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, String> {
   
    List<Review> findByProduct_Id(String productId);
    
    @Query("SELECT DISTINCT r FROM Review r " +
           "LEFT JOIN FETCH r.reviewImages " +
           "LEFT JOIN FETCH r.user " +
           "LEFT JOIN FETCH r.orderItem " +
           "WHERE r.product.id = :productId " +
           "ORDER BY r.createdAt DESC")
    List<Review> findByProductIdWithImages(@Param("productId") String productId);

    List<Review> findByOrderItem_Id(String orderItemId);

    boolean existsByOrderItem_IdAndUser_Id(String orderItemId, String userId);

    Optional<Review> findByOrderItem_IdAndUser_Id(String orderItemId, String userId);

    boolean existsByOrderItemIdAndUserId(String orderItemId, String userId);


    // Admin management queries
    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.user " +
           "LEFT JOIN FETCH r.product " +
           "ORDER BY r.createdAt DESC")
    List<Review> findAllWithDetails();
    
    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.user " +
           "LEFT JOIN FETCH r.product " +
           "WHERE r.isRemoved = true " +
           "ORDER BY r.removedAt DESC")
    List<Review> findAllRemoved();
    
    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.user " +
           "LEFT JOIN FETCH r.product " +
           "WHERE r.isRemoved = false OR r.isRemoved IS NULL " +
           "ORDER BY r.createdAt DESC")
    List<Review> findAllActive();
    
    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.user u " +
           "LEFT JOIN FETCH r.product p " +
           "WHERE (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(r.comment) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY r.createdAt DESC")
    List<Review> searchReviews(@Param("search") String search);
}