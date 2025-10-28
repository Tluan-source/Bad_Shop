package vn.iotstar.dto.vendor;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating Store information
 * @author Vendor Module
 * @since 2025-10-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreUpdateDTO {
    
    @NotBlank(message = "Tên cửa hàng không được trống")
    @Size(min = 3, max = 200, message = "Tên cửa hàng phải từ 3 đến 200 ký tự")
    private String name;
    
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phone;
    
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String bio;
    
    private String featuredImages; // JSON array of image URLs
}