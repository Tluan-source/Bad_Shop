package vn.iotstar.dto.vendor;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.iotstar.entity.Promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for creating Promotion
 * @author Vendor Module
 * @since 2025-10-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionCreateDTO {
    
    @NotBlank(message = "Tên chương trình khuyến mãi không được trống")
    @Size(max = 200, message = "Tên không được vượt quá 200 ký tự")
    private String name;
    
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;
    
    @NotNull(message = "Loại giảm giá không được trống")
    private Promotion.DiscountType discountType;
    
    @NotNull(message = "Giá trị giảm không được trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;
    
    @DecimalMin(value = "0.0", message = "Giảm tối đa không được âm")
    private BigDecimal maxDiscount;
    
    @DecimalMin(value = "0.0", message = "Đơn tối thiểu không được âm")
    private BigDecimal minOrderAmount;
    
    @NotNull(message = "Ngày bắt đầu không được trống")
    @Future(message = "Ngày bắt đầu phải là thời điểm trong tương lai")
    private LocalDateTime startDate;
    
    @NotNull(message = "Ngày kết thúc không được trống")
    private LocalDateTime endDate;
    
    @NotNull(message = "Phạm vi áp dụng không được trống")
    private Promotion.AppliesTo appliesTo;
    
    // List of product IDs (if appliesTo = SPECIFIC_PRODUCTS)
    private List<String> productIds;
    
    private Boolean isActive = true;
}
