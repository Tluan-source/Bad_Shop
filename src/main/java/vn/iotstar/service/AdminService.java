package vn.iotstar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.Order;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private StoreRepository storeRepository;
    
    /**
     * Get dashboard statistics
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Count total users
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);
        
        // Count total products
        long totalProducts = productRepository.count();
        stats.put("totalProducts", totalProducts);
        
        // Count total orders
        long totalOrders = orderRepository.count();
        stats.put("totalOrders", totalOrders);
        
        // Count total stores
        long totalStores = storeRepository.count();
        stats.put("totalStores", totalStores);
        
        // Calculate total revenue (only DELIVERED orders)
        BigDecimal totalRevenue = calculateTotalRevenue();
        stats.put("totalRevenue", totalRevenue);
        
        // Count orders by status
        List<Order> allOrders = orderRepository.findAll();
        long shippedToday = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.SHIPPED)
            .count();
        long deliveredToday = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED && 
                        o.getCreatedAt().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
            .count();
        
        stats.put("shippedCount", shippedToday);
        stats.put("deliveredToday", deliveredToday);
        
        return stats;
    }
    
    /**
     * Calculate total revenue from all delivered orders
     */
    private BigDecimal calculateTotalRevenue() {
        List<Order> deliveredOrders = orderRepository.findAll().stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
            .toList();
        
        return deliveredOrders.stream()
            .map(Order::getAmountFromUser)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get recent orders (last 10)
     */
    public List<Order> getRecentOrders() {
        List<Order> allOrders = orderRepository.findAll();
        allOrders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
        return allOrders.stream().limit(10).toList();
    }
    
    /**
     * Get statistics by date range
     */
    public Map<String, Object> getStatsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Order> orders = orderRepository.findAll().stream()
            .filter(order -> order.getCreatedAt().isAfter(startDate) && 
                           order.getCreatedAt().isBefore(endDate))
            .toList();
        
        stats.put("ordersCount", orders.size());
        
        BigDecimal revenue = orders.stream()
            .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
            .map(Order::getAmountFromUser)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.put("revenue", revenue);
        
        return stats;
    }
}