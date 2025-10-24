package vn.iotstar.exception;

/**
 * Exception thrown when product stock is insufficient
 * Can be used by Vendor (order processing) and User (checkout)
 * @author BadmintonMarketplace
 * @since 2025-10-24
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName) {
        super(String.format("Sản phẩm '%s' không đủ số lượng trong kho", productName));
    }
    
    public InsufficientStockException() {
        super("Không đủ số lượng trong kho");
    }
}
