package vn.iotstar.exception;

/**
 * Exception thrown when user tries to access store they don't own
 * Can be used by Vendor, Admin modules
 * @author BadmintonMarketplace
 * @since 2025-10-24
 */
public class UnauthorizedStoreAccessException extends RuntimeException {
    public UnauthorizedStoreAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedStoreAccessException() {
        super("Bạn không có quyền truy cập cửa hàng này");
    }
}
