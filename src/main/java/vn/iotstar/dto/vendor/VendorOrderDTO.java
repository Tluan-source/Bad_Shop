package vn.iotstar.dto.vendor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.iotstar.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for displaying Order in Vendor panel
 * @author Vendor Module
 * @since 2025-10-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorOrderDTO {
    private String id;
    private Order.OrderStatus status;
    private Boolean isPaidBefore;
    private String address;
    private String phone;
    private BigDecimal shippingFee;
    private BigDecimal amountFromUser;
    private BigDecimal amountToStore;
    private BigDecimal amountToGd;      // Commission
    private BigDecimal discountAmount;
    
    // Discount details
    private BigDecimal promotionDiscount;
    private BigDecimal voucherDiscount;
    private String promotionId;
    private String promotionName;
    private String voucherId;
    private String voucherCode;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Customer info
    private String userId;
    private String userFullName;
    private String userEmail;
    private String userPhone;
    private String userAvatar;
    
    // Store info
    private String storeId;
    private String storeName;
    
    // Payment info
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;
    
    // Shipment info
    private String shipmentStatus;
    private String shipperName;
    private String shipperPhone;
    private String shipperAvatar;
    private String shippingProviderName;
    private String shipmentId;
    private String deliveryImageUrl;
    private LocalDateTime assignedAt;
    private LocalDateTime deliveredAt;
    
    // Confirmation info
    private LocalDateTime confirmedByUserAt;

    // Convenience fields for templates
    private String orderId;
    private String customerName;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    
    // Order items
    private List<VendorOrderItemDTO> orderItems;
    
    // Calculated
    private Integer totalItems;
    private BigDecimal totalProductAmount; // Sum of items
}
