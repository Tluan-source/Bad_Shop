package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.OrderItem;

import java.util.List;

/**
 * Repository for OrderItem entity
 * @author Vendor Module
 * @since 2025-10-24
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    
    @Query("SELECT i FROM OrderItem i WHERE i.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") String orderId);
    
    List<OrderItem> findByProductId(String productId);
}
