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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Controller for Vendor Wallet Management
 */
@Controller
@RequestMapping("/vendor/wallet")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDOR')")
public class VendorWalletController {
    
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
     * Display wallet management page
     */
    @GetMapping
    public String walletPage(Authentication auth, Model model) {
        Store store = getCurrentStore(auth);
        
        // Get wallet information
        Map<String, Object> wallet = new HashMap<>();
        wallet.put("balance", store.getEWallet() != null ? store.getEWallet() : BigDecimal.ZERO);
        
        // Calculate pending balance (from orders being processed)
        BigDecimal pendingBalance = calculatePendingBalance(store.getId());
        
        // Calculate total income (all completed orders)
        BigDecimal totalIncome = calculateTotalIncome(store.getId());
        
        model.addAttribute("store", store);
        model.addAttribute("wallet", wallet);
        model.addAttribute("pendingBalance", pendingBalance);
        model.addAttribute("totalIncome", totalIncome);
        
        return "vendor/wallet";
    }
    
    /**
     * Calculate pending balance from processing orders
     */
    private BigDecimal calculatePendingBalance(String storeId) {
        List<Order> processingOrders = orderRepository.findByStoreIdAndStatusIn(
                storeId, 
                List.of(Order.OrderStatus.PROCESSING, Order.OrderStatus.SHIPPED));
        
        return processingOrders.stream()
                .map(Order::getAmountToStore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calculate total income from all delivered orders
     */
    private BigDecimal calculateTotalIncome(String storeId) {
        List<Order> deliveredOrders = orderRepository.findByStoreIdAndStatus(
                storeId, Order.OrderStatus.DELIVERED);
        
        return deliveredOrders.stream()
                .map(Order::getAmountToStore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
