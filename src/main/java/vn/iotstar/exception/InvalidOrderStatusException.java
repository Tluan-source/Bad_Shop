package vn.iotstar.exception;

import vn.iotstar.entity.Order;

/**
 * Exception thrown when order status transition is invalid
 * Can be used by Vendor, Shipper, Admin modules
 * @author BadmintonMarketplace
 * @since 2025-10-24
 */
public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(String message) {
        super(message);
    }
    
    public InvalidOrderStatusException(Order.OrderStatus currentStatus, Order.OrderStatus targetStatus) {
        super(String.format("Không thể chuyển đơn hàng từ trạng thái %s sang %s", 
            currentStatus, targetStatus));
    }
}
