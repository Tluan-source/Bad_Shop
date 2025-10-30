package vn.iotstar.controller.vendor;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for Vendor Analytics Dashboard
 */
@Controller
@RequestMapping("/vendor/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDOR')")
public class VendorAnalyticsController {
    
    private final StoreRepository storeRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    
    /**
     * Get current vendor's store
     */
    private Store getCurrentStore(Authentication auth) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Store> stores = storeRepository.findByOwnerId(user.getId());
        if (stores.isEmpty()) {
            throw new RuntimeException("Store not found for this vendor");
        }
        return stores.get(0);
    }
    
    /**
     * Display analytics dashboard
     */
    @GetMapping
    public String analyticsPage(Authentication auth, Model model) {
        Store store = getCurrentStore(auth);
        
        // Get all completed orders for this store
        List<Order> completedOrders = orderRepository.findByStoreIdAndStatus(
                store.getId(), Order.OrderStatus.DELIVERED);
        
        // Calculate revenue summary
        Map<String, BigDecimal> revenueSummary = calculateRevenueSummary(completedOrders);
        
        // Calculate monthly revenue (last 12 months)
        Map<String, BigDecimal> monthlyRevenue = calculateMonthlyRevenue(completedOrders);
        
        // Calculate daily revenue (last 30 days)
        Map<String, BigDecimal> dailyRevenue = calculateDailyRevenue(completedOrders);
        
        // Get top selling products
        List<Map<String, Object>> topProducts = getTopProducts(completedOrders);
        
        model.addAttribute("store", store);
        model.addAttribute("revenueSummary", revenueSummary);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("dailyRevenue", dailyRevenue);
        model.addAttribute("topProducts", topProducts);
        
        return "vendor/analytics";
    }
    
    /**
     * Calculate revenue summary (today, this week, this month, total)
     */
    private Map<String, BigDecimal> calculateRevenueSummary(List<Order> orders) {
        Map<String, BigDecimal> summary = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        
    BigDecimal today = orders.stream()
        .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfDay))
        .map(Order::getAmountToStore)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal thisWeek = orders.stream()
        .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfWeek))
        .map(Order::getAmountToStore)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal thisMonth = orders.stream()
        .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfMonth))
        .map(Order::getAmountToStore)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal total = orders.stream()
                .map(Order::getAmountToStore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        summary.put("today", today);
        summary.put("thisWeek", thisWeek);
        summary.put("thisMonth", thisMonth);
        summary.put("total", total);
        
        return summary;
    }
    
    /**
     * Calculate monthly revenue for last 12 months
     */
    private Map<String, BigDecimal> calculateMonthlyRevenue(List<Order> orders) {
        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        
        // Initialize last 12 months with 0
        for (int i = 11; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            monthlyRevenue.put(month.format(formatter), BigDecimal.ZERO);
        }
        
        // Calculate revenue for each month
        orders.forEach(order -> {
            if (order.getCreatedAt() != null && order.getAmountToStore() != null) {
                String monthKey = order.getCreatedAt().format(formatter);
                if (monthlyRevenue.containsKey(monthKey)) {
                    monthlyRevenue.put(monthKey, 
                        monthlyRevenue.get(monthKey).add(order.getAmountToStore()));
                }
            }
        });
        
        return monthlyRevenue;
    }
    
    /**
     * Calculate daily revenue for last 30 days
     */
    private Map<String, BigDecimal> calculateDailyRevenue(List<Order> orders) {
        Map<String, BigDecimal> dailyRevenue = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        // Initialize last 30 days with 0
        for (int i = 29; i >= 0; i--) {
            LocalDate day = now.minusDays(i);
            dailyRevenue.put(day.format(formatter), BigDecimal.ZERO);
        }
        
        // Calculate revenue for each day
        orders.forEach(order -> {
            if (order.getCreatedAt() != null && order.getAmountToStore() != null) {
                String dayKey = order.getCreatedAt().toLocalDate().format(formatter);
                if (dailyRevenue.containsKey(dayKey)) {
                    dailyRevenue.put(dayKey, 
                        dailyRevenue.get(dayKey).add(order.getAmountToStore()));
                }
            }
        });
        
        return dailyRevenue;
    }
    
    /**
     * Get top 10 selling products
     */
    private List<Map<String, Object>> getTopProducts(List<Order> orders) {
        // Group by product and count quantities
        Map<String, Integer> productSales = new HashMap<>();
        Map<String, String> productNames = new HashMap<>();
        Map<String, BigDecimal> productPrices = new HashMap<>();
        
        orders.forEach(order -> {
            if (order.getOrderItems() != null) {
                order.getOrderItems().forEach(item -> {
                    String productId = item.getProduct().getId();
                    String productName = item.getProduct().getName();
                    BigDecimal price = item.getPrice();
                    
                    productSales.merge(productId, item.getQuantity(), Integer::sum);
                    productNames.put(productId, productName);
                    productPrices.put(productId, price);
                });
            }
        });
        
        // Sort by sales quantity and take top 10
        return productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> product = new HashMap<>();
                    product.put("id", entry.getKey());
                    product.put("name", productNames.get(entry.getKey()));
                    product.put("sold", entry.getValue());
                    product.put("price", productPrices.get(entry.getKey()));
                    product.put("revenue", productPrices.get(entry.getKey())
                            .multiply(BigDecimal.valueOf(entry.getValue())));
                    return product;
                })
                .collect(Collectors.toList());
    }
}
