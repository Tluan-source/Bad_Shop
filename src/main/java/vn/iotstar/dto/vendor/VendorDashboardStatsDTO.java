package vn.iotstar.dto.vendor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Vendor Dashboard Statistics
 * @author Vendor Module
 * @since 2025-10-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorDashboardStatsDTO {
    // Product stats
    private Long totalProducts;
    private Long activeProducts;
    private Long sellingProducts;
    private Long outOfStockProducts;
    
    // Order stats
    private Long totalOrders;
    private Long pendingOrders;       // NOT_PROCESSED (alias for newOrders)
    private Long newOrders;           // NOT_PROCESSED
    private Long processingOrders;    // PROCESSING
    private Long shippingOrders;      // SHIPPED
    private Long deliveredOrders;     // DELIVERED
    private Long cancelledOrders;     // CANCELLED
    private Long returnedOrders;      // RETURNED
    
    // Revenue stats
    private BigDecimal todayRevenue;
    private BigDecimal weekRevenue;
    private BigDecimal monthRevenue;
    private BigDecimal totalRevenue;
    private BigDecimal eWallet;
    
    // Store stats
    private BigDecimal storeRating;
    private Integer storePoint;
    
    // Top products (can be list of simple objects)
    private String topProductsJson; // JSON string
}