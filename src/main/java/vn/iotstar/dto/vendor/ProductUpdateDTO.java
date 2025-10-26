package vn.iotstar.dto.vendor;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for updating existing Product
 * @author Vendor Module
 * @since 2025-10-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateDTO {
    
    @NotBlank(message = "Tên sản phẩm không được trống")
    @Size(min = 10, max = 200, message = "Tên sản phẩm phải từ 10 đến 200 ký tự")
    private String name;
    
    @NotBlank(message = "Mô tả sản phẩm không được trống")
    @Size(min = 20, max = 1000, message = "Mô tả phải từ 20 đến 1000 ký tự")
    private String description;
    
    @NotNull(message = "Giá sản phẩm không được trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;
    
    @DecimalMin(value = "0.0", message = "Giá khuyến mãi không được âm")
    private BigDecimal promotionalPrice;
    
    @NotNull(message = "Số lượng không được trống")
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer quantity;
    
    @NotBlank(message = "Danh mục không được trống")
    private String categoryId;
    
    private Boolean isSelling;
    
    // Image URLs (JSON array string) - existing images
    private String listImages;
    
    // New images to upload to Cloudinary
    private List<MultipartFile> newImages;
    
    // Or list of URLs to add/replace
    private List<String> imageUrls;
}