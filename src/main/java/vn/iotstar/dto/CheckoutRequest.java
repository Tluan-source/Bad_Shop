package vn.iotstar.dto;

import lombok.Data;

@Data
public class CheckoutRequest {
    private String fullName;
    private String phone;
    private String address;
    private String province;
    private String district;
    private String ward;
    private String note;
    private String paymentMethod; // COD, BANK_TRANSFER, etc.
    private String shippingProviderId; // ID của nhà vận chuyển được chọn

    
    // Discount fields
    private String voucherCode;      // Mã voucher (toàn sàn)
    private String promotionId;      // ID promotion của shop
}
