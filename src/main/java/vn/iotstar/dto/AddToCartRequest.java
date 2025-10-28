package vn.iotstar.dto;

import java.util.List;

import lombok.Data;

@Data
public class AddToCartRequest {
    private String productId;
    private int quantity = 1;
    private List<String> styleValueIds;
}
