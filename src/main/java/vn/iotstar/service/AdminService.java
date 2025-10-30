package vn.iotstar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.OrderItem;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.Category;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.OrderItemRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.repository.CategoryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
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
    
    /**
     * Get pending vendor registrations (stores with isActive = false)
     */
    public List<Store> getPendingVendorRegistrations() {
        return storeRepository.findAll().stream()
            .filter(store -> !store.getIsActive())
            .sorted((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()))
            .collect(Collectors.toList());
    }
    
    /**
     * Count pending vendor registrations
     */
    public long countPendingVendorRegistrations() {
        return storeRepository.findAll().stream()
            .filter(store -> !store.getIsActive())
            .count();
    }
    
    /**
     * Get report statistics
     */
    public Map<String, Object> getReportStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get top selling products
        List<Map<String, Object>> topProducts = getTopSellingProducts(5);
        stats.put("topProducts", topProducts);
        
        // Get sales by category
        List<Map<String, Object>> categorySales = getSalesByCategory();
        stats.put("categorySales", categorySales);
        
        // Get top stores by revenue
        List<Map<String, Object>> topStores = getTopStoresByRevenue(5);
        stats.put("topStores", topStores);
        
        // Get all stores (for admin to see their managed stores)
        List<Store> allStores = storeRepository.findAll();
        stats.put("allStores", allStores);
        
        // Get order statistics
        List<Order> allOrders = orderRepository.findAll();
        long totalOrders = allOrders.size();
        long completedOrders = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .count();
        long cancelledOrders = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED)
            .count();
        
        stats.put("totalOrders", totalOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("cancelledOrders", cancelledOrders);
        
        // Calculate total revenue (only from DELIVERED orders)
        BigDecimal totalRevenue = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .map(Order::getAmountFromUser)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);
        
        return stats;
    }
    
    /**
     * Get report statistics by date range
     */
    public Map<String, Object> getReportStatsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        // Filter orders by date range
        List<Order> filteredOrders = orderRepository.findAll().stream()
            .filter(order -> order.getCreatedAt() != null &&
                           order.getCreatedAt().isAfter(startDate) && 
                           order.getCreatedAt().isBefore(endDate))
            .collect(Collectors.toList());
        
        // Get order statistics
        long totalOrders = filteredOrders.size();
        long completedOrders = filteredOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .count();
        long cancelledOrders = filteredOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED)
            .count();
        
        stats.put("totalOrders", totalOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("cancelledOrders", cancelledOrders);
        
        // Calculate total revenue
        BigDecimal totalRevenue = filteredOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .map(Order::getAmountFromUser)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);
        
        // Get filtered order items
        List<OrderItem> filteredOrderItems = orderItemRepository.findAll().stream()
            .filter(item -> item.getOrder().getCreatedAt() != null &&
                          item.getOrder().getCreatedAt().isAfter(startDate) && 
                          item.getOrder().getCreatedAt().isBefore(endDate))
            .collect(Collectors.toList());
        
        // Get top selling products in date range
        List<Map<String, Object>> topProducts = getTopSellingProductsByDateRange(filteredOrderItems, 5);
        stats.put("topProducts", topProducts);
        
        // Get sales by category in date range
        List<Map<String, Object>> categorySales = getSalesByCategoryByDateRange(filteredOrderItems);
        stats.put("categorySales", categorySales);
        
        // Get top stores by revenue in date range
        List<Map<String, Object>> topStores = getTopStoresByDateRange(filteredOrderItems, 5);
        stats.put("topStores", topStores);
        
        return stats;
    }
    
    /**
     * Get top selling products (from all orders, not just DELIVERED)
     */
    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        List<OrderItem> allOrderItems = orderItemRepository.findAll();
        
        if (allOrderItems.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Only count items from DELIVERED orders for revenue calculation
        List<OrderItem> deliveredOrderItems = allOrderItems.stream()
            .filter(item -> item.getOrder().getStatus() == Order.OrderStatus.DELIVERED)
            .collect(Collectors.toList());
        
        // Group by product and sum quantities (all orders except CANCELLED)
        Map<Product, Integer> productSales = allOrderItems.stream()
            .filter(item -> item.getOrder().getStatus() != Order.OrderStatus.CANCELLED)
            .collect(Collectors.groupingBy(
                OrderItem::getProduct,
                Collectors.summingInt(OrderItem::getQuantity)
            ));
        
        // Calculate revenue for each product (ONLY from DELIVERED orders)
        Map<Product, BigDecimal> productRevenue = deliveredOrderItems.stream()
            .collect(Collectors.groupingBy(
                OrderItem::getProduct,
                Collectors.reducing(
                    BigDecimal.ZERO,
                    OrderItem::getTotal,
                    BigDecimal::add
                )
            ));
        
        // Sort by quantity and create result list
        return productSales.entrySet().stream()
            .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                Product product = entry.getKey();
                item.put("product", product);
                item.put("quantity", entry.getValue());
                item.put("revenue", productRevenue.getOrDefault(product, BigDecimal.ZERO));
                return item;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get sales statistics by category (from all orders except CANCELLED)
     */
    public List<Map<String, Object>> getSalesByCategory() {
        List<OrderItem> allOrderItems = orderItemRepository.findAll();
        
        if (allOrderItems.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Only count items from DELIVERED orders for revenue calculation
        List<OrderItem> deliveredOrderItems = allOrderItems.stream()
            .filter(item -> item.getOrder().getStatus() == Order.OrderStatus.DELIVERED)
            .collect(Collectors.toList());
        
        // Group by category
        Map<Category, Integer> categorySales = allOrderItems.stream()
            .filter(item -> item.getOrder().getStatus() != Order.OrderStatus.CANCELLED)
            .filter(item -> item.getProduct().getCategory() != null)
            .collect(Collectors.groupingBy(
                item -> item.getProduct().getCategory(),
                Collectors.summingInt(OrderItem::getQuantity)
            ));
        
        // Calculate revenue by category (ONLY from DELIVERED orders)
        Map<Category, BigDecimal> categoryRevenue = deliveredOrderItems.stream()
            .filter(item -> item.getProduct().getCategory() != null)
            .collect(Collectors.groupingBy(
                item -> item.getProduct().getCategory(),
                Collectors.reducing(
                    BigDecimal.ZERO,
                    OrderItem::getTotal,
                    BigDecimal::add
                )
            ));
        
        return categorySales.entrySet().stream()
            .sorted(Map.Entry.<Category, Integer>comparingByValue().reversed())
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                Category category = entry.getKey();
                item.put("category", category);
                item.put("quantity", entry.getValue());
                item.put("revenue", categoryRevenue.getOrDefault(category, BigDecimal.ZERO));
                return item;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get top stores by revenue (from all orders except CANCELLED)
     */
    public List<Map<String, Object>> getTopStoresByRevenue(int limit) {
        List<OrderItem> allOrderItems = orderItemRepository.findAll();
        
        if (allOrderItems.isEmpty()) {
            // If no orders, return all stores with zero stats
            return storeRepository.findAll().stream()
                .limit(limit)
                .map(store -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("store", store);
                    item.put("quantity", 0);
                    item.put("revenue", BigDecimal.ZERO);
                    return item;
                })
                .collect(Collectors.toList());
        }
        
        // Only count items from DELIVERED orders for revenue calculation
        List<OrderItem> deliveredOrderItems = allOrderItems.stream()
            .filter(item -> item.getOrder().getStatus() == Order.OrderStatus.DELIVERED)
            .collect(Collectors.toList());
        
        // Group by store
        Map<Store, Integer> storeSales = allOrderItems.stream()
            .filter(item -> item.getOrder().getStatus() != Order.OrderStatus.CANCELLED)
            .collect(Collectors.groupingBy(
                item -> item.getProduct().getStore(),
                Collectors.summingInt(OrderItem::getQuantity)
            ));
        
        // Calculate revenue by store (ONLY from DELIVERED orders)
        Map<Store, BigDecimal> storeRevenue = deliveredOrderItems.stream()
            .collect(Collectors.groupingBy(
                item -> item.getProduct().getStore(),
                Collectors.reducing(
                    BigDecimal.ZERO,
                    OrderItem::getTotal,
                    BigDecimal::add
                )
            ));
        
        // Get all stores and include those without sales
        List<Store> allStores = storeRepository.findAll();
        
        return allStores.stream()
            .map(store -> {
                Map<String, Object> item = new HashMap<>();
                item.put("store", store);
                item.put("quantity", storeSales.getOrDefault(store, 0));
                item.put("revenue", storeRevenue.getOrDefault(store, BigDecimal.ZERO));
                return item;
            })
            .sorted((a, b) -> ((BigDecimal)b.get("revenue")).compareTo((BigDecimal)a.get("revenue")))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Get top selling products by date range
     */
    private List<Map<String, Object>> getTopSellingProductsByDateRange(List<OrderItem> orderItems, int limit) {
        if (orderItems.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Only count items from DELIVERED orders for revenue calculation
        List<OrderItem> deliveredOrderItems = orderItems.stream()
            .filter(item -> item.getOrder().getStatus() == Order.OrderStatus.DELIVERED)
            .collect(Collectors.toList());
        
        // Group by product and sum quantities
        Map<Product, Integer> productSales = orderItems.stream()
            .filter(item -> item.getOrder().getStatus() != Order.OrderStatus.CANCELLED)
            .collect(Collectors.groupingBy(
                OrderItem::getProduct,
                Collectors.summingInt(OrderItem::getQuantity)
            ));
        
        // Calculate revenue for each product (ONLY from DELIVERED orders)
        Map<Product, BigDecimal> productRevenue = deliveredOrderItems.stream()
            .collect(Collectors.groupingBy(
                OrderItem::getProduct,
                Collectors.reducing(
                    BigDecimal.ZERO,
                    OrderItem::getTotal,
                    BigDecimal::add
                )
            ));
        
        // Sort by quantity and create result list
        return productSales.entrySet().stream()
            .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                Product product = entry.getKey();
                item.put("product", product);
                item.put("quantity", entry.getValue());
                item.put("revenue", productRevenue.getOrDefault(product, BigDecimal.ZERO));
                return item;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get sales by category by date range
     */
    private List<Map<String, Object>> getSalesByCategoryByDateRange(List<OrderItem> orderItems) {
        if (orderItems.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Only count items from DELIVERED orders for revenue calculation
        List<OrderItem> deliveredOrderItems = orderItems.stream()
            .filter(item -> item.getOrder().getStatus() == Order.OrderStatus.DELIVERED)
            .collect(Collectors.toList());
        
        // Group by category
        Map<Category, Integer> categorySales = orderItems.stream()
            .filter(item -> item.getOrder().getStatus() != Order.OrderStatus.CANCELLED)
            .filter(item -> item.getProduct().getCategory() != null)
            .collect(Collectors.groupingBy(
                item -> item.getProduct().getCategory(),
                Collectors.summingInt(OrderItem::getQuantity)
            ));
        
        // Calculate revenue by category (ONLY from DELIVERED orders)
        Map<Category, BigDecimal> categoryRevenue = deliveredOrderItems.stream()
            .filter(item -> item.getProduct().getCategory() != null)
            .collect(Collectors.groupingBy(
                item -> item.getProduct().getCategory(),
                Collectors.reducing(
                    BigDecimal.ZERO,
                    OrderItem::getTotal,
                    BigDecimal::add
                )
            ));
        
        // Create result list
        return categorySales.entrySet().stream()
            .sorted(Map.Entry.<Category, Integer>comparingByValue().reversed())
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                Category category = entry.getKey();
                item.put("category", category);
                item.put("quantity", entry.getValue());
                item.put("revenue", categoryRevenue.getOrDefault(category, BigDecimal.ZERO));
                return item;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get top stores by date range
     */
    private List<Map<String, Object>> getTopStoresByDateRange(List<OrderItem> orderItems, int limit) {
        if (orderItems.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Only count items from DELIVERED orders for revenue calculation
        List<OrderItem> deliveredOrderItems = orderItems.stream()
            .filter(item -> item.getOrder().getStatus() == Order.OrderStatus.DELIVERED)
            .collect(Collectors.toList());
        
        // Group by store
        Map<Store, Integer> storeSales = orderItems.stream()
            .filter(item -> item.getOrder().getStatus() != Order.OrderStatus.CANCELLED)
            .filter(item -> item.getProduct().getStore() != null)
            .collect(Collectors.groupingBy(
                item -> item.getProduct().getStore(),
                Collectors.summingInt(OrderItem::getQuantity)
            ));
        
        // Calculate revenue by store (ONLY from DELIVERED orders)
        Map<Store, BigDecimal> storeRevenue = deliveredOrderItems.stream()
            .filter(item -> item.getProduct().getStore() != null)
            .collect(Collectors.groupingBy(
                item -> item.getProduct().getStore(),
                Collectors.reducing(
                    BigDecimal.ZERO,
                    OrderItem::getTotal,
                    BigDecimal::add
                )
            ));
        
        // Sort by revenue and create result list
        return storeRevenue.entrySet().stream()
            .sorted(Map.Entry.<Store, BigDecimal>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                Store store = entry.getKey();
                item.put("store", store);
                item.put("quantity", storeSales.getOrDefault(store, 0));
                item.put("revenue", entry.getValue());
                return item;
            })
            .collect(Collectors.toList());
    }
}
