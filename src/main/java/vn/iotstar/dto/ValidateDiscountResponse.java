package vn.iotstar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateDiscountResponse {
    private boolean success;
    private String message;
    
    // Voucher info
    private boolean voucherValid;
    private String voucherCode;
    private BigDecimal voucherDiscount;
    private String voucherError;
    
    // Promotion info
    private boolean promotionValid;
    private String promotionId;
    private String promotionName;
    private BigDecimal promotionDiscount;
    private String promotionError;
    
    // Summary
    private BigDecimal originalAmount;
    private BigDecimal totalDiscount;
    private BigDecimal finalAmount;
    
    public static ValidateDiscountResponse error(String message) {
        ValidateDiscountResponse response = new ValidateDiscountResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
    
    public static ValidateDiscountResponse success(String message) {
        ValidateDiscountResponse response = new ValidateDiscountResponse();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }
}
