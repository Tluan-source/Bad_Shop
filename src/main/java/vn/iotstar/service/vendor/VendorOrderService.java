package vn.iotstar.service.vendor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.iotstar.dto.vendor.VendorOrderDTO;
import vn.iotstar.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for Vendor Order Management
 * Use this for VENDOR to process orders
 * 
 * @author Vendor Module
 * @since 2025-10-24
 */
public interface VendorOrderService {
    
    /**
     * Get all orders of store
     */
    List<VendorOrderDTO> getMyOrders(String storeId);
    
    Page<VendorOrderDTO> getMyOrders(String storeId, Pageable pageable);
    
    /**
     * Get orders by status
     */
    List<VendorOrderDTO> getMyOrdersByStatus(String storeId, Order.OrderStatus status);
    
    Page<VendorOrderDTO> getMyOrdersByStatus(String storeId, Order.OrderStatus status, Pageable pageable);

    /**
     * Get orders that already have an assigned shipment (i.e., a shipper has been allocated)
     */
    List<VendorOrderDTO> getMyAssignedShipments(String storeId);
    
    /**
     * Get order detail
     */
    VendorOrderDTO getOrderDetail(String orderId, String storeId);
    
    /**
     * Confirm order (NOT_PROCESSED -> PROCESSING)
     * - Check stock
     * - Deduct quantity
     * - Increase sold
     */
    VendorOrderDTO confirmOrder(String orderId, String storeId);
    
    /**
     * Prepare for shipping (PROCESSING -> SHIPPED)
     * - Create shipment
     */
    VendorOrderDTO prepareShipment(String orderId, String storeId, String shipperId);
    
    /**
     * Cancel order
     * - Rollback stock if confirmed
     * - Refund if paid
     */
    VendorOrderDTO cancelOrder(String orderId, String storeId, String reason);
    
    /**
     * Handle return/refund (DELIVERED -> RETURNED)
     */
    VendorOrderDTO handleReturn(String orderId, String storeId, String reason);
    
    /**
     * Count orders by status
     */
    Long countOrdersByStatus(String storeId, Order.OrderStatus status);
    
    /**
     * Calculate revenue
     */
    BigDecimal calculateTotalRevenue(String storeId);
    
    BigDecimal calculateRevenueByDateRange(String storeId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get orders by date range
     */
    List<VendorOrderDTO> getOrdersByDateRange(String storeId, LocalDateTime startDate, LocalDateTime endDate);
}
