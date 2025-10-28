package vn.iotstar.dto;

import java.util.List;

import lombok.Data;

@Data
public class CheckoutFromCartRequest {
    private List<String> cartItemIds;
}
