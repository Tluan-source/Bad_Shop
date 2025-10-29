package vn.iotstar.dto;

import lombok.Data;
import java.util.Map;

/**
 * Request DTO cho checkout nhiều shop cùng lúc
 * Mỗi shop có thể áp dụng promotion riêng
 */
@Data
public class MultiStoreCheckoutRequest {
    private String fullName;
    private String phone;
    private String address;
    private String province;
    private String district;
    private String ward;
    private String note;
    private String paymentMethod; // COD, VNPAY
    
    // Voucher toàn sàn (chỉ 1 voucher cho toàn bộ đơn hàng)
    private String voucherCode;
    
    // Map<storeId, promotionId> - Mỗi shop có thể chọn 1 promotion
    private Map<String, String> promotionIds;
}
