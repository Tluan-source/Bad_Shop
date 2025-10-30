package vn.iotstar.controller.vendor;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Controller for Vendor Revenue Management
 */
@Controller
@RequestMapping("/vendor/revenue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDOR')")
public class VendorRevenueController {
    
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
     * Display revenue management page
     */
    @GetMapping
    public String revenuePage(
        Authentication auth,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model) {
        
        Store store = getCurrentStore(auth);
        
        // Get all orders for this store
        List<Order> allOrders = orderRepository.findByStoreIdOrderByCreatedAtDesc(store.getId());
        
        // Filter orders based on criteria
        List<Order> filteredOrders = filterOrders(allOrders, startDate, endDate, status);
        
        // Calculate revenue statistics
        BigDecimal totalRevenue = calculateTotalRevenue(allOrders);
        BigDecimal pendingRevenue = calculatePendingRevenue(allOrders);
        BigDecimal paidRevenue = calculatePaidRevenue(allOrders);
    // Calculate revenue for the filtered range/status as requested by the user
    BigDecimal filteredTotalRevenue = calculateTotalRevenue(filteredOrders);
    BigDecimal filteredPendingRevenue = calculatePendingRevenue(filteredOrders);
    BigDecimal filteredPaidRevenue = calculatePaidRevenue(filteredOrders);
        BigDecimal walletBalance = store.getEWallet() != null ? store.getEWallet() : BigDecimal.ZERO;
        
        // Pagination: slice filteredOrders into pages (simple server-side pagination)
        if (size <= 0) size = 10;
        if (page < 0) page = 0;
        int totalItems = filteredOrders.size();
        int fromIndex = page * size;
        if (fromIndex > totalItems) {
            page = 0;
            fromIndex = 0;
        }
        int toIndex = Math.min(fromIndex + size, totalItems);
        List<Order> pageOrders = filteredOrders.subList(fromIndex, toIndex);

        // Create transaction list from the current page orders
        List<Map<String, Object>> transactions = createTransactionsList(pageOrders);
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        // Calculate totals for the table footer (from filtered transactions)
        // Recalculate total amount correctly: amountToStore * 100 / 90
        BigDecimal totalAmount = filteredOrders.stream()
                .map(o -> {
                    BigDecimal toStore = o.getAmountToStore() != null ? o.getAmountToStore() : BigDecimal.ZERO;
                    // Recalculate commission: toStore * 10 / 90
                    BigDecimal commission = toStore
                            .multiply(BigDecimal.valueOf(10))
                            .divide(BigDecimal.valueOf(90), 0, java.math.RoundingMode.HALF_UP);
                    return toStore.add(commission);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Recalculate total fee correctly
        BigDecimal totalFee = filteredOrders.stream()
                .map(o -> {
                    BigDecimal toStore = o.getAmountToStore() != null ? o.getAmountToStore() : BigDecimal.ZERO;
                    return toStore.multiply(BigDecimal.valueOf(10))
                            .divide(BigDecimal.valueOf(90), 0, java.math.RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalNet = filteredOrders.stream()
                .map(Order::getAmountToStore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("store", store);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("pendingRevenue", pendingRevenue);
        model.addAttribute("paidRevenue", paidRevenue);
    model.addAttribute("filteredTotalRevenue", filteredTotalRevenue);
    model.addAttribute("filteredPendingRevenue", filteredPendingRevenue);
    model.addAttribute("filteredPaidRevenue", filteredPaidRevenue);
        model.addAttribute("walletBalance", walletBalance);
        model.addAttribute("transactions", transactions);
    model.addAttribute("currentPage", page);
    model.addAttribute("pageSize", size);
    model.addAttribute("totalPages", totalPages);
    model.addAttribute("totalItems", totalItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("totalFee", totalFee);
        model.addAttribute("totalNet", totalNet);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);
        
        return "vendor/revenue";
    }
    
    /**
     * Filter orders based on criteria
     */
    private List<Order> filterOrders(List<Order> orders, LocalDate startDate, LocalDate endDate, String status) {
        return orders.stream()
                .filter(order -> {
                    // Filter by date range
                    if (startDate != null && order.getCreatedAt() != null) {
                        if (order.getCreatedAt().toLocalDate().isBefore(startDate)) {
                            return false;
                        }
                    }
                    if (endDate != null && order.getCreatedAt() != null) {
                        if (order.getCreatedAt().toLocalDate().isAfter(endDate)) {
                            return false;
                        }
                    }
                    
                    // Filter by status
                    if (status != null && !status.isEmpty()) {
                        if ("PENDING".equals(status)) {
                            // Pending = orders being processed (not yet delivered)
                            return order.getStatus() == Order.OrderStatus.PROCESSING ||
                                   order.getStatus() == Order.OrderStatus.SHIPPED;
                        } else if ("PAID".equals(status)) {
                            // Paid = all delivered orders (auto-paid)
                            return order.getStatus() == Order.OrderStatus.DELIVERED;
                        } else if ("PROCESSING".equals(status)) {
                            return order.getStatus() == Order.OrderStatus.PROCESSING ||
                                   order.getStatus() == Order.OrderStatus.SHIPPED;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate total revenue from all completed orders
     */
    private BigDecimal calculateTotalRevenue(List<Order> orders) {
        return orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getAmountToStore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calculate pending revenue (delivered but not yet paid)
     * Since we auto-transfer money when order is DELIVERED, pending = orders being processed
     */
    private BigDecimal calculatePendingRevenue(List<Order> orders) {
        return orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PROCESSING || 
                            o.getStatus() == Order.OrderStatus.SHIPPED)
                .map(Order::getAmountToStore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calculate paid revenue (already transferred to wallet)
     * All DELIVERED orders are automatically paid
     */
    private BigDecimal calculatePaidRevenue(List<Order> orders) {
        return orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getAmountToStore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Create transactions list from orders
     */
    private List<Map<String, Object>> createTransactionsList(List<Order> orders) {
        return orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED || 
                            o.getStatus() == Order.OrderStatus.PROCESSING ||
                            o.getStatus() == Order.OrderStatus.SHIPPED)
                .map(order -> {
                    Map<String, Object> transaction = new HashMap<>();
                    transaction.put("id", order.getId());
                    transaction.put("orderId", order.getId());
                    transaction.put("date", order.getCreatedAt());
                    
                    // FIX: Recalculate commission correctly
                    // If amountToStore is 90%, then commission should be 10%
                    // Formula: commission = amountToStore * 10 / 90
                    // Total = amountToStore + commission = amountToStore * 100 / 90
                    BigDecimal amountToStore = order.getAmountToStore() != null ? order.getAmountToStore() : BigDecimal.ZERO;
                    
                    // Calculate correct 10% commission
                    BigDecimal commission = amountToStore
                            .multiply(BigDecimal.valueOf(10))
                            .divide(BigDecimal.valueOf(90), 0, java.math.RoundingMode.HALF_UP);
                    
                    // Calculate total amount (100%)
                    BigDecimal totalAmount = amountToStore.add(commission);
                    
                    transaction.put("amount", totalAmount);        // Total price (100%)
                    transaction.put("commission", commission);      // 10% fee (recalculated)
                    transaction.put("net", amountToStore);         // Net amount (90%)
                    transaction.put("status", determineTransactionStatus(order));
                    // Fix: Check length before substring to avoid IndexOutOfBoundsException
                    String orderId = order.getId();
                    String shortId = orderId.length() > 8 ? orderId.substring(0, 8) : orderId;
                    transaction.put("description", "Đơn hàng #" + shortId);
                    return transaction;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Determine transaction status based on order
     */
    private String determineTransactionStatus(Order order) {
        // If order is delivered, automatically mark as PAID (money transferred to wallet)
        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            return "PAID";
        } else if (order.getStatus() == Order.OrderStatus.PROCESSING || 
                   order.getStatus() == Order.OrderStatus.SHIPPED) {
            return "PROCESSING";
        } else {
            return "PENDING";
        }
    }


}
