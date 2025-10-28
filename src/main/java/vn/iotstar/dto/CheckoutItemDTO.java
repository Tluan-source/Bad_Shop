package vn.iotstar.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutItemDTO {
    private String productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private int quantity;
    private List<String> styleValueIds;
    private String styleValues; // Display string for styles
    private BigDecimal total;
    private String storeId;
    private String storeName;
}
