package vn.iotstar.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import vn.iotstar.dto.vendor.ProductCreateDTO;
import vn.iotstar.dto.vendor.ProductUpdateDTO;
import vn.iotstar.dto.vendor.StoreUpdateDTO;
import vn.iotstar.dto.vendor.VendorDashboardStatsDTO;
import vn.iotstar.dto.vendor.VendorOrderDTO;
import vn.iotstar.dto.vendor.VendorProductDTO;
import vn.iotstar.dto.vendor.VendorStoreDTO;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.User;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.service.UserService;
import vn.iotstar.service.vendor.VendorAnalyticsService;
import vn.iotstar.service.vendor.VendorOrderService;
import vn.iotstar.service.vendor.VendorProductService;
import vn.iotstar.service.vendor.VendorSecurityService;
import vn.iotstar.service.vendor.VendorStoreService;

/**
 * Main Vendor Controller - handles all vendor operations
 * Routes: /vendor/**
 * @author Vendor Module
 * @since 2025-10-24
 */
@Controller
@RequestMapping("/vendor")
public class VendorController {
    
    @Autowired
    private VendorStoreService storeService;
    
    @Autowired
    private VendorProductService productService;
    
    @Autowired
    private VendorOrderService orderService;
    
    @Autowired
    private VendorAnalyticsService analyticsService;
    
    @Autowired
    private VendorSecurityService securityService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    // ========================================
    // DASHBOARD
    // ========================================
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());
        
        // Get dashboard statistics
        VendorDashboardStatsDTO stats = analyticsService.getDashboardStats(storeId);
        model.addAttribute("stats", stats);
        
        // Get store info
        VendorStoreDTO store = storeService.getMyStore(storeId, user.getId());
        model.addAttribute("store", store);
        
        // Get recent orders (top 5)
        List<VendorOrderDTO> recentOrders = orderService.getMyOrders(storeId, PageRequest.of(0, 5)).getContent();
        model.addAttribute("recentOrders", recentOrders);
        
        model.addAttribute("username", user.getFullName());
        return "vendor/dashboard";
    }
    
    // ========================================
    // STORE MANAGEMENT
    // ========================================
    
    @GetMapping("/store")
    public String storeInfo(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());
        
        VendorStoreDTO store = storeService.getMyStore(storeId, user.getId());
        model.addAttribute("store", store);
        
        return "vendor/store-info";
    }
    
    @GetMapping("/store/edit")
    public String editStore(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());
        
        VendorStoreDTO store = storeService.getMyStore(storeId, user.getId());
        model.addAttribute("store", store);
        
        // Create DTO for form
        StoreUpdateDTO updateDTO = new StoreUpdateDTO();
        updateDTO.setName(store.getName());
        updateDTO.setBio(store.getBio());
        updateDTO.setFeaturedImages(store.getFeaturedImages());
        
        model.addAttribute("storeDTO", updateDTO);
        
        return "vendor/store-edit";
    }
    
    @PostMapping("/store/update")
    public String updateStore(@Valid @ModelAttribute("storeDTO") StoreUpdateDTO updateDTO,
                             BindingResult result,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "vendor/store-edit";
        }
        
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());
        
        storeService.updateStore(storeId, updateDTO, user.getId());
        
        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin cửa hàng thành công!");
        return "redirect:/vendor/store";
    }
    
    // ========================================
    // PRODUCT MANAGEMENT
    // ========================================
    
    @GetMapping("/products")
    public String productList(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Boolean isSelling,
                             Model model,
                             Authentication auth) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VendorProductDTO> products;
        
        if (keyword != null && !keyword.isEmpty()) {
            products = productService.searchMyProducts(storeId, keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else if (isSelling != null) {
            List<VendorProductDTO> filtered = productService.getMyProductsByStatus(storeId, isSelling, null);
            products = new org.springframework.data.domain.PageImpl<>(
                filtered.subList(0, Math.min(size, filtered.size())),
                pageable,
                filtered.size()
            );
        } else {
            products = productService.getMyProducts(storeId, pageable);
        }
        
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        
        return "vendor/products/list";
    }
    
    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("productDTO", new ProductCreateDTO());
        model.addAttribute("isEdit", false);
        
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByNameAsc();
        model.addAttribute("categories", categories);
        
        return "vendor/products/form";
    }
    
    @PostMapping("/products/create")
    public String createProduct(@Valid @ModelAttribute("productDTO") ProductCreateDTO createDTO,
                               @RequestParam(value = "imageUrlsText", required = false) String imageUrlsText,
                               BindingResult result,
                               Authentication auth,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        // Chuyển đổi textarea imageUrlsText thành List<String> imageUrls
        if (imageUrlsText != null && !imageUrlsText.trim().isEmpty()) {
            List<String> urls = Arrays.stream(imageUrlsText.split("\\r?\\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            createDTO.setImageUrls(urls);
        }
        
        if (result.hasErrors()) {
            List<Category> categories = categoryRepository.findByIsActiveTrueOrderByNameAsc();
            model.addAttribute("categories", categories);
            return "vendor/products/form";
        }
        
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());
        
        productService.createProduct(createDTO, storeId);
        
        redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công! Chờ admin duyệt.");
        return "redirect:/vendor/products";
    }
    
    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable String id,
                                 Model model,
                                 Authentication auth) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());
        
        VendorProductDTO product = productService.getMyProduct(id, storeId);
        
        // Convert to update DTO
        ProductUpdateDTO updateDTO = new ProductUpdateDTO();
        updateDTO.setName(product.getName());
        updateDTO.setDescription(product.getDescription());
        updateDTO.setPrice(product.getPrice());
        updateDTO.setPromotionalPrice(product.getPromotionalPrice());
        updateDTO.setQuantity(product.getQuantity());
        updateDTO.setCategoryId(product.getCategoryId());
        updateDTO.setIsSelling(product.getIsSelling());
        updateDTO.setListImages(product.getListImages());
        
        model.addAttribute("productDTO", updateDTO);
        model.addAttribute("product", product);
        model.addAttribute("isEdit", true);
        
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByNameAsc();
        model.addAttribute("categories", categories);
        
        return "vendor/products/form";
    }
    
    @PostMapping("/products/{id}/update")
    public String updateProduct(@PathVariable String id,
                               @Valid @ModelAttribute("productDTO") ProductUpdateDTO updateDTO,
                               BindingResult result,
                               Authentication auth,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (result.hasErrors()) {
            List<Category> categories = categoryRepository.findByIsActiveTrueOrderByNameAsc();
            model.addAttribute("categories", categories);
            return "vendor/products/form";
        }
        
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());
        
        productService.updateProduct(id, updateDTO, storeId);
        
        redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
        return "redirect:/vendor/products";
    }
    
    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable String id, 
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());        productService.deleteProduct(id, storeId);
        
        redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        return "redirect:/vendor/products";
    }
    
    @PostMapping("/products/{id}/toggle-selling")
    public String toggleSelling(@PathVariable String id, 
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());        productService.toggleSelling(id, storeId);
        
        redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái bán hàng thành công!");
        return "redirect:/vendor/products";
    }
    
    // ========================================
    // ORDER MANAGEMENT
    // ========================================
    
    @GetMapping("/orders")
    public String orderList(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           @RequestParam(required = false) String status,
                           Model model,
                           Authentication auth) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VendorOrderDTO> orders;
        
        if (status != null && !status.isEmpty()) {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status);
            orders = orderService.getMyOrdersByStatus(storeId, orderStatus, pageable);
            model.addAttribute("currentStatus", status);
        } else {
            orders = orderService.getMyOrders(storeId, pageable);
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        
        // Order counts by status
        model.addAttribute("newCount", orderService.countOrdersByStatus(storeId, Order.OrderStatus.NOT_PROCESSED));
        model.addAttribute("processingCount", orderService.countOrdersByStatus(storeId, Order.OrderStatus.PROCESSING));
        model.addAttribute("shippingCount", orderService.countOrdersByStatus(storeId, Order.OrderStatus.SHIPPED));
        model.addAttribute("deliveredCount", orderService.countOrdersByStatus(storeId, Order.OrderStatus.DELIVERED));
        model.addAttribute("cancelledCount", orderService.countOrdersByStatus(storeId, Order.OrderStatus.CANCELLED));
        
        return "vendor/orders/list";
    }
    
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable String id,
                             Model model,
                             Authentication auth) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());
        
        VendorOrderDTO order = orderService.getOrderDetail(id, storeId);
        model.addAttribute("order", order);
        
        return "vendor/orders/detail";
    }
    
    @PostMapping("/orders/{id}/confirm")
    public String confirmOrder(@PathVariable String id,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String storeId = securityService.getCurrentVendorStoreId(user.getId());
            
            orderService.confirmOrder(id, storeId);
            
            redirectAttributes.addFlashAttribute("success", "Xác nhận đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/vendor/orders/" + id;
    }
    
    @PostMapping("/orders/{id}/ship")
    public String prepareShipment(@PathVariable String id,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String storeId = securityService.getCurrentVendorStoreId(user.getId());
            
            orderService.prepareShipment(id, storeId, null);
            
            redirectAttributes.addFlashAttribute("success", "Đơn hàng đã sẵn sàng giao!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/vendor/orders/" + id;
    }
    
    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable String id,
                             @RequestParam(required = false) String reason,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String storeId = securityService.getCurrentVendorStoreId(user.getId());
            
            orderService.cancelOrder(id, storeId, reason);
            
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/vendor/orders/" + id;
    }
    
    // ========================================
    // ANALYTICS & REPORTS
    // ========================================
    
    @GetMapping("/analytics")
    public String analytics(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());
        
        // Revenue summary
        var revenueSummary = analyticsService.getRevenueSummary(storeId);
        model.addAttribute("revenueSummary", revenueSummary);
        
        // Monthly revenue (current year)
        int currentYear = java.time.LocalDate.now().getYear();
        var monthlyRevenue = analyticsService.getMonthlyRevenue(storeId, currentYear);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("currentYear", currentYear);
        
        // Daily revenue (last 30 days)
        var dailyRevenue = analyticsService.getDailyRevenue(storeId, 30);
        model.addAttribute("dailyRevenue", dailyRevenue);
        
        // Top selling products
        var topProducts = analyticsService.getTopSellingProducts(storeId, 10);
        model.addAttribute("topProducts", topProducts);
        
        return "vendor/analytics";
    }
    
    // ========================================
    // WALLET
    // ========================================
    
    @GetMapping("/wallet")
    public String wallet(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());
        
        VendorStoreDTO store = storeService.getMyStore(storeId, user.getId());
        model.addAttribute("store", store);
        
        // TODO: Add transaction history
        
        return "vendor/wallet";
    }
}
