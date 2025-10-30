package vn.iotstar.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ValidateDiscountRequest {
    private String voucherCode;      // Mã voucher
    private String promotionId;      // ID promotion
    private String storeId;          // ID shop (bắt buộc cho promotion)
    private BigDecimal orderAmount;  // Tổng tiền đơn hàng
}
