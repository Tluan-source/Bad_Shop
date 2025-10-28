package vn.iotstar.dto.vendor;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for creating new Product
 * @author Vendor Module
 * @since 2025-10-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDTO {
    
    @NotBlank(message = "Tên sản phẩm không được trống")
    @Size(min = 10, max = 200, message = "Tên sản phẩm phải từ 10 đến 200 ký tự")
    private String name;
    
    @NotBlank(message = "Mô tả sản phẩm không được trống")
    private String description;
    
    @NotNull(message = "Giá sản phẩm không được trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    @Digits(integer = 15, fraction = 2, message = "Giá không hợp lệ")
    private BigDecimal price;
    
    @DecimalMin(value = "0.0", message = "Giá khuyến mãi không được âm")
    @Digits(integer = 15, fraction = 2, message = "Giá khuyến mãi không hợp lệ")
    private BigDecimal promotionalPrice;
    
    @NotNull(message = "Số lượng không được trống")
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer quantity;
    
    @NotBlank(message = "Danh mục không được trống")
    private String categoryId;
    
    private Boolean isSelling = true;
    
    // Style values (màu sắc, kích thước...)
    private List<String> styleValueIds;
    
    // Images uploaded
    private List<MultipartFile> images;
    
    // Or image URLs (if already uploaded to Cloudinary)
    private List<String> imageUrls;
}