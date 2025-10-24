package vn.iotstar.exception;

/**
 * Exception thrown when user tries to access resources they don't own
 * Can be used across all modules (Vendor, Admin, User)
 * @author BadmintonMarketplace
 * @since 2025-10-24
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedAccessException() {
        super("Bạn không có quyền thực hiện thao tác này");
    }
}
