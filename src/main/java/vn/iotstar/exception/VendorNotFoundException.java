package vn.iotstar.exception;

/**
 * Exception thrown when store/vendor not found
 * @author BadmintonMarketplace
 * @since 2025-10-24
 */
public class VendorNotFoundException extends RuntimeException {
    public VendorNotFoundException(String message) {
        super(message);
    }
    
    public VendorNotFoundException() {
        super("Không tìm thấy cửa hàng của bạn");
    }
}
