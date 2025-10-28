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
}
