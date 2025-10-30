package vn.iotstar.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, String> {
   
    List<Review> findByProduct_Id(String productId);

    List<Review> findByOrderItem_Id(String orderItemId);

    boolean existsByOrderItem_IdAndUser_Id(String orderItemId, String userId);

    Optional<Review> findByOrderItem_IdAndUser_Id(String orderItemId, String userId);

    boolean existsByOrderItemIdAndUserId(String orderItemId, String userId);

}