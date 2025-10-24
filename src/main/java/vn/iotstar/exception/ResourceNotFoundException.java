package vn.iotstar.exception;

/**
 * Generic Resource Not Found Exception
 * Can be used for Product, Order, Store, User, etc.
 * @author BadmintonMarketplace
 * @since 2025-10-24
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Không tìm thấy %s với %s: '%s'", resourceName, fieldName, fieldValue));
    }
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    // Convenience methods
    public static ResourceNotFoundException product(String productId) {
        return new ResourceNotFoundException("sản phẩm", "ID", productId);
    }
    
    public static ResourceNotFoundException order(String orderId) {
        return new ResourceNotFoundException("đơn hàng", "ID", orderId);
    }
    
    public static ResourceNotFoundException store(String storeId) {
        return new ResourceNotFoundException("cửa hàng", "ID", storeId);
    }
    
    public static ResourceNotFoundException category(String categoryId) {
        return new ResourceNotFoundException("danh mục", "ID", categoryId);
    }
}
