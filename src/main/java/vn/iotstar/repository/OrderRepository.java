package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Order entity
 * @author Vendor Module
 * @since 2025-10-24
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    // Find orders by store ID
    List<Order> findByStoreIdOrderByCreatedAtDesc(String storeId);
    
    Page<Order> findByStoreId(String storeId, Pageable pageable);
    
    // Find orders by store and status
    List<Order> findByStoreIdAndStatus(String storeId, Order.OrderStatus status);
    
    Page<Order> findByStoreIdAndStatus(String storeId, Order.OrderStatus status, Pageable pageable);
    
    // Count orders by store and status
    Long countByStoreIdAndStatus(String storeId, Order.OrderStatus status);
    
    // Find orders by user
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    
    // Find orders by date range
    @Query("SELECT o FROM Order o WHERE o.store.id = :storeId " +
           "AND o.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByStoreIdAndDateRange(
        @Param("storeId") String storeId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Calculate revenue by store
    @Query("SELECT SUM(o.amountToStore) FROM Order o " +
           "WHERE o.store.id = :storeId AND o.status = 'DELIVERED'")
    BigDecimal calculateTotalRevenue(@Param("storeId") String storeId);
    
    // Calculate revenue by store and date range
    @Query("SELECT SUM(o.amountToStore) FROM Order o " +
           "WHERE o.store.id = :storeId AND o.status = 'DELIVERED' " +
           "AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueByDateRange(
        @Param("storeId") String storeId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Get monthly revenue
    @Query("SELECT MONTH(o.createdAt) as month, SUM(o.amountToStore) as revenue " +
           "FROM Order o WHERE o.store.id = :storeId " +
           "AND YEAR(o.createdAt) = :year AND o.status = 'DELIVERED' " +
           "GROUP BY MONTH(o.createdAt) ORDER BY MONTH(o.createdAt)")
    List<Object[]> getMonthlyRevenue(@Param("storeId") String storeId, @Param("year") int year);
    
    // Find order with items
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Order findByIdWithItems(@Param("orderId") String orderId);

       // Find orders that have an associated shipment (assigned to shipper)
       List<Order> findByStoreIdAndShipmentIsNotNullOrderByCreatedAtDesc(String storeId);
    
    // Get top selling products by store
    @Query("SELECT p.id, p.name, p.sold, SUM(oi.total) as revenue, c.name " +
           "FROM Product p JOIN p.orderItems oi JOIN oi.order o JOIN p.category c " +
           "WHERE p.store.id = :storeId AND o.status = 'DELIVERED' " +
           "GROUP BY p.id, p.name, p.sold, c.name ORDER BY p.sold DESC")
    List<Object[]> findTopSellingByStore(@Param("storeId") String storeId, Pageable pageable);
}