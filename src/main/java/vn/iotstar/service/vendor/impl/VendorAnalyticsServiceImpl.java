package vn.iotstar.service.vendor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.vendor.VendorDashboardStatsDTO;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.service.vendor.VendorAnalyticsService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of VendorAnalyticsService
 * @author Vendor Module
 * @since 2025-10-24
 */
@Service
public class VendorAnalyticsServiceImpl implements VendorAnalyticsService {
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Override
    @Transactional(readOnly = true)
    public VendorDashboardStatsDTO getDashboardStats(String storeId) {
        VendorDashboardStatsDTO stats = new VendorDashboardStatsDTO();
        
        // Product stats
        stats.setTotalProducts(productRepository.countByStoreId(storeId));
        stats.setActiveProducts(productRepository.countByStoreIdAndIsSelling(storeId, true));
        stats.setSellingProducts(productRepository.countByStoreIdAndIsSelling(storeId, true));
        
        // Out of stock products
        Long outOfStock = productRepository.findByStoreIdOrderByCreatedAtDesc(storeId)
            .stream()
            .filter(p -> p.getQuantity() == 0)
            .count();
        stats.setOutOfStockProducts(outOfStock);
        
        // Order stats by status
        stats.setNewOrders(orderRepository.countByStoreIdAndStatus(storeId, Order.OrderStatus.NOT_PROCESSED));
        stats.setPendingOrders(stats.getNewOrders()); // Alias for newOrders
        stats.setProcessingOrders(orderRepository.countByStoreIdAndStatus(storeId, Order.OrderStatus.PROCESSING));
        stats.setShippingOrders(orderRepository.countByStoreIdAndStatus(storeId, Order.OrderStatus.SHIPPED));
        stats.setDeliveredOrders(orderRepository.countByStoreIdAndStatus(storeId, Order.OrderStatus.DELIVERED));
        stats.setCancelledOrders(orderRepository.countByStoreIdAndStatus(storeId, Order.OrderStatus.CANCELLED));
        stats.setReturnedOrders(orderRepository.countByStoreIdAndStatus(storeId, Order.OrderStatus.RETURNED));
        
        Long totalOrders = stats.getNewOrders() + stats.getProcessingOrders() + 
                          stats.getShippingOrders() + stats.getDeliveredOrders() + 
                          stats.getCancelledOrders() + stats.getReturnedOrders();
        stats.setTotalOrders(totalOrders);
        
        // Revenue stats
        LocalDateTime now = LocalDateTime.now();
        
        // Today
        LocalDateTime startOfDay = now.truncatedTo(ChronoUnit.DAYS);
        stats.setTodayRevenue(orderRepository.calculateRevenueByDateRange(storeId, startOfDay, now));
        
        // This week
        LocalDateTime startOfWeek = now.minusDays(7);
        stats.setWeekRevenue(orderRepository.calculateRevenueByDateRange(storeId, startOfWeek, now));
        
        // This month
        LocalDateTime startOfMonth = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        stats.setMonthRevenue(orderRepository.calculateRevenueByDateRange(storeId, startOfMonth, now));
        
        // Total revenue
        stats.setTotalRevenue(orderRepository.calculateTotalRevenue(storeId));
        
        // Store info
        Store store = storeRepository.findById(storeId).orElse(null);
        if (store != null) {
            stats.setStoreRating(store.getRating());
            stats.setStorePoint(store.getPoint());
            stats.setEWallet(store.getEWallet());
        }
        
        // Ensure no null values
        if (stats.getTodayRevenue() == null) stats.setTodayRevenue(BigDecimal.ZERO);
        if (stats.getWeekRevenue() == null) stats.setWeekRevenue(BigDecimal.ZERO);
        if (stats.getMonthRevenue() == null) stats.setMonthRevenue(BigDecimal.ZERO);
        if (stats.getTotalRevenue() == null) stats.setTotalRevenue(BigDecimal.ZERO);
        if (stats.getEWallet() == null) stats.setEWallet(BigDecimal.ZERO);
        if (stats.getStoreRating() == null) stats.setStoreRating(BigDecimal.ZERO);
        if (stats.getStorePoint() == null) stats.setStorePoint(0);
        
        return stats;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<Integer, BigDecimal> getMonthlyRevenue(String storeId, int year) {
        List<Object[]> results = orderRepository.getMonthlyRevenue(storeId, year);
        
        Map<Integer, BigDecimal> monthlyRevenue = new HashMap<>();
        
        // Initialize all months with 0
        for (int i = 1; i <= 12; i++) {
            monthlyRevenue.put(i, BigDecimal.ZERO);
        }
        
        // Fill in actual revenue
        for (Object[] result : results) {
            Integer month = (Integer) result[0];
            BigDecimal revenue = (BigDecimal) result[1];
            monthlyRevenue.put(month, revenue != null ? revenue : BigDecimal.ZERO);
        }
        
        return monthlyRevenue;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getDailyRevenue(String storeId, int days) {
        Map<String, BigDecimal> dailyRevenue = new LinkedHashMap<>();
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        List<Order> orders = orderRepository.findByStoreIdAndDateRange(storeId, startDate, endDate)
            .stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .collect(Collectors.toList());
        
        // Initialize all days with 0
        for (int i = 0; i < days; i++) {
            LocalDateTime date = endDate.minusDays(days - i - 1);
            String dateKey = date.toLocalDate().toString();
            dailyRevenue.put(dateKey, BigDecimal.ZERO);
        }
        
        // Fill in actual revenue
        for (Order order : orders) {
            String dateKey = order.getCreatedAt().toLocalDate().toString();
            BigDecimal currentRevenue = dailyRevenue.getOrDefault(dateKey, BigDecimal.ZERO);
            dailyRevenue.put(dateKey, currentRevenue.add(order.getAmountToStore()));
        }
        
        return dailyRevenue;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopSellingProducts(String storeId, int limit) {
        List<Object[]> results = orderRepository.findTopSellingByStore(storeId, PageRequest.of(0, limit));
        
        return results.stream()
            .map(result -> {
                Map<String, Object> product = new HashMap<>();
                product.put("id", result[0]);
                product.put("name", result[1]);
                product.put("soldCount", result[2]);
                product.put("price", result[3]);
                product.put("categoryName", result[4]);
                return product;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getRevenueSummary(String storeId) {
        Map<String, BigDecimal> summary = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        
        // Today
        LocalDateTime startOfDay = now.truncatedTo(ChronoUnit.DAYS);
        BigDecimal todayRevenue = orderRepository.calculateRevenueByDateRange(storeId, startOfDay, now);
        
        // This week
        LocalDateTime startOfWeek = now.minusDays(7);
        BigDecimal weekRevenue = orderRepository.calculateRevenueByDateRange(storeId, startOfWeek, now);
        
        // This month
        LocalDateTime startOfMonth = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        BigDecimal monthRevenue = orderRepository.calculateRevenueByDateRange(storeId, startOfMonth, now);
        
        // Total
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue(storeId);
        
        // Calculate total commission paid
        List<Order> allOrders = orderRepository.findByStoreIdOrderByCreatedAtDesc(storeId)
            .stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .collect(Collectors.toList());
        
        BigDecimal totalCommission = allOrders.stream()
            .map(Order::getAmountToGd)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal profit = (totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
            .subtract(totalCommission != null ? totalCommission : BigDecimal.ZERO);
        
        // Put values with keys that match template expectations
        summary.put("today", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
        summary.put("thisWeek", weekRevenue != null ? weekRevenue : BigDecimal.ZERO);
        summary.put("thisMonth", monthRevenue != null ? monthRevenue : BigDecimal.ZERO);
        summary.put("total", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        summary.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        summary.put("monthRevenue", monthRevenue != null ? monthRevenue : BigDecimal.ZERO);
        summary.put("totalCommission", totalCommission);
        summary.put("profit", profit);
        
        return summary;
    }
}