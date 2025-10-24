package vn.iotstar.service.vendor;

import vn.iotstar.dto.vendor.VendorDashboardStatsDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Vendor Analytics & Dashboard
 * 
 * @author Vendor Module
 * @since 2025-10-24
 */
public interface VendorAnalyticsService {
    
    /**
     * Get dashboard statistics
     */
    VendorDashboardStatsDTO getDashboardStats(String storeId);
    
    /**
     * Get revenue by month (for current year)
     */
    Map<Integer, BigDecimal> getMonthlyRevenue(String storeId, int year);
    
    /**
     * Get daily revenue (last 30 days)
     */
    Map<String, BigDecimal> getDailyRevenue(String storeId, int days);
    
    /**
     * Get top selling products
     */
    List<Map<String, Object>> getTopSellingProducts(String storeId, int limit);
    
    /**
     * Get revenue summary
     */
    Map<String, BigDecimal> getRevenueSummary(String storeId);
}
