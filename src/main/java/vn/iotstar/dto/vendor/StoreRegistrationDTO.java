package vn.iotstar.dto.vendor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

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

    /** 
     * Địa chỉ đầy đủ được chọn từ bản đồ hoặc autocomplete 
     */
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    /** Phường/Xã */
    private String ward;

    /** Quận/Huyện */
    private String district;

    /** Tỉnh/Thành phố */
    private String city;

    /** Tọa độ lấy từ bản đồ */
    private Double latitude;
    private Double longitude;

    /** Optional */
    private String logoUrl;
    private String businessLicenseUrl;
}
