package vn.iotstar.dto.vendor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO for vendor registration
 */
@Data
public class StoreRegistrationDTO {
    
    @NotBlank(message = "Tên cửa hàng không được để trống")
    private String storeName;
    
    @NotBlank(message = "Mô tả không được để trống")
    private String description;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phone;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;
    
    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String city;
    
    @NotBlank(message = "Quận/Huyện không được để trống")
    private String district;
    
    @NotBlank(message = "Phường/Xã không được để trống")
    private String ward;
    
    // Optional fields - removed validation
    private String businessType; // INDIVIDUAL, COMPANY, HOUSEHOLD (optional)
    private String taxCode; // optional
    private String bankName; // optional
    private String bankAccountNumber; // optional
    private String bankAccountName; // optional
    private String bankBranch; // optional
    
    // File paths will be set after upload
    private String logoUrl;
    private String businessLicenseUrl;
}