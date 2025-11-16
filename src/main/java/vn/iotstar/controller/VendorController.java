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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import vn.iotstar.dto.vendor.ProductCreateDTO;
import vn.iotstar.dto.vendor.ProductUpdateDTO;
import vn.iotstar.dto.vendor.StoreRegistrationDTO;
import vn.iotstar.dto.vendor.StoreUpdateDTO;
import vn.iotstar.dto.vendor.VendorDashboardStatsDTO;
import vn.iotstar.dto.vendor.VendorOrderDTO;
import vn.iotstar.dto.vendor.VendorProductDTO;
import vn.iotstar.dto.vendor.VendorStoreDTO;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Shipment;
import vn.iotstar.entity.Style;
import vn.iotstar.entity.StyleValue;
import vn.iotstar.entity.User;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.PromotionRepository;
import vn.iotstar.repository.ShipmentRepository;
import vn.iotstar.repository.StyleRepository;
import vn.iotstar.repository.StyleValueRepository;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.UserService;
import vn.iotstar.service.vendor.VendorAnalyticsService;
import vn.iotstar.service.vendor.VendorOrderService;
import vn.iotstar.service.vendor.VendorProductService;
import vn.iotstar.service.vendor.VendorPromotionService;
import vn.iotstar.service.vendor.VendorRegistrationService;
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
    
    @Autowired
    private StyleRepository styleRepository;
    
    @Autowired
    private StyleValueRepository styleValueRepository;
    
    @Autowired
    private VendorRegistrationService vendorRegistrationService;
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private VendorPromotionService promotionService;
    
    @Autowired
    private PromotionRepository promotionRepository;
    
    @Autowired
    private vn.iotstar.service.ChatService chatService;
    
    @Autowired
    private ShipmentRepository shipmentRepository;
    // ========================================
    // VENDOR REGISTRATION (for USER role)
    // ========================================
    
    @GetMapping("/register")
    public String showRegistrationForm(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user already has a store
        if (vendorRegistrationService.hasPendingRegistration(user.getId())) {
            model.addAttribute("hasPending", true);
            model.addAttribute("message", "Bạn đã có yêu cầu đăng ký vendor đang chờ admin phê duyệt.");
            return "vendor/register";
        }
        
        model.addAttribute("storeRegistration", new StoreRegistrationDTO());
        model.addAttribute("hasPending", false);
        return "vendor/register";
    }
    
    @PostMapping("/register")
    public String registerVendor(@Valid @ModelAttribute("storeRegistration") StoreRegistrationDTO registrationDTO,
                                 BindingResult result,
                                 @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                                 @RequestParam(value = "licenseFile", required = false) MultipartFile licenseFile,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("hasPending", false);
            return "vendor/register";
        }
        
        try {
            User user = userService.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Register vendor - creates store but keeps user as USER role (waiting for admin approval)
            String storeId = vendorRegistrationService.registerVendor(user, registrationDTO, logoFile, licenseFile);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đăng ký vendor thành công! Yêu cầu của bạn đang chờ admin phê duyệt. " +
                "Bạn sẽ nhận được thông báo qua email khi được duyệt.");
            
            return "redirect:/user/profile";
            
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            model.addAttribute("hasPending", false);
            return "vendor/register";
        }
    }
    
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
        
        // Get recent conversations (top 5)
        List<vn.iotstar.dto.ConversationDTO> recentConversations = chatService.getVendorConversations(user.getId())
                .stream()
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentConversations", recentConversations);
        
        // Count unread messages
        long unreadCount = recentConversations.stream()
                .mapToInt(vn.iotstar.dto.ConversationDTO::getUnreadCount)
                .sum();
        model.addAttribute("unreadMessagesCount", unreadCount);
        
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
    // Populate email so vendor can update shop email from this form
    updateDTO.setEmail(store.getEmail());
    updateDTO.setPhone(store.getPhone());
        
        model.addAttribute("storeDTO", updateDTO);
        
        return "vendor/store-edit";
    }
    
    @PostMapping("/store/update")
    public String updateStore(@Valid @ModelAttribute("storeDTO") StoreUpdateDTO updateDTO,
                             BindingResult result,
                             Authentication auth,
                             RedirectAttributes redirectAttributes,
                             @RequestParam(value = "featuredImagesFiles", required = false) List<MultipartFile> featuredImagesFiles,
                             @RequestParam(value = "existingFeaturedImages", required = false) String existingFeaturedImagesJson) {
        if (result.hasErrors()) {
            return "vendor/store-edit";
        }
        
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());
        
        // Build final featured images list: start with kept existing images (if any), then append newly uploaded images
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        java.util.List<String> finalList = new java.util.ArrayList<>();
        if (existingFeaturedImagesJson != null && !existingFeaturedImagesJson.trim().isEmpty()) {
            try {
                java.util.List<String> existing = mapper.readValue(existingFeaturedImagesJson,
                        mapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class));
                if (existing != null) finalList.addAll(existing);
            } catch (Exception ex) {
                // ignore parse errors
            }
        }

        if (featuredImagesFiles != null && !featuredImagesFiles.isEmpty()) {
            try {
                java.util.List<String> uploaded = cloudinaryService.uploadFiles(featuredImagesFiles);
                if (uploaded != null && !uploaded.isEmpty()) {
                    finalList.addAll(uploaded);
                }
            } catch (Exception ex) {
                // ignore upload errors
            }
        }

        try {
            if (!finalList.isEmpty()) {
                updateDTO.setFeaturedImages(mapper.writeValueAsString(finalList));
            } else {
                // set to empty array string to indicate no images
                updateDTO.setFeaturedImages("[]");
            }
        } catch (Exception ex) {
            // ignore
        }

        storeService.updateStore(storeId, updateDTO, user.getId());
        
        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin cửa hàng thành công!");
        return "redirect:/vendor/store";
    }
    
    // ========================================
    // PRODUCT MANAGEMENT
    // ========================================
    
    @GetMapping("/products")
    public String productList(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Boolean isSelling,
                             @RequestParam(required = false) Boolean isActive,
                             @RequestParam(required = false) String categoryId,
                             Model model,
                             Authentication auth) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());
        
        Pageable pageable = PageRequest.of(page, size);
        
        // Support both 'search' and 'keyword' parameters
        String searchTerm = (search != null && !search.isEmpty()) ? search : keyword;
        
        // Get all products from store first
        List<VendorProductDTO> allProducts = productService.getMyProducts(storeId);
        
        // Apply filters
        List<VendorProductDTO> filtered = allProducts.stream()
            .filter(p -> {
                // Filter 1: Search by name (contains, case-insensitive)
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    if (!p.getName().toLowerCase().contains(searchTerm.toLowerCase().trim())) {
                        return false;
                    }
                }
                
                // Filter 2: isSelling status
                if (isSelling != null && !p.getIsSelling().equals(isSelling)) {
                    return false;
                }
                
                // Filter 3: isActive status (approved by admin)
                if (isActive != null && !p.getIsActive().equals(isActive)) {
                    return false;
                }
                
                // Filter 4: Category
                if (categoryId != null && !categoryId.isEmpty() && 
                    !categoryId.equals(p.getCategoryId())) {
                    return false;
                }
                
                return true;
            })
            .toList();
        
        // Pagination
        int start = Math.min(page * size, filtered.size());
        int end = Math.min(start + size, filtered.size());
        Page<VendorProductDTO> products = new org.springframework.data.domain.PageImpl<>(
            filtered.subList(start, end),
            pageable,
            filtered.size()
        );
        
        // Add to model
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("search", searchTerm);
        model.addAttribute("isSelling", isSelling);
        model.addAttribute("isActive", isActive);
        model.addAttribute("categoryId", categoryId);
        
        // Load categories for filter dropdown
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByNameAsc();
        model.addAttribute("categories", categories);
        
        return "vendor/products/list";
    }
    
    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("productDTO", new ProductCreateDTO());
        model.addAttribute("isEdit", false);
        
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByNameAsc();
        model.addAttribute("categories", categories);
        
        // Load styles và style values
        List<Style> styles = styleRepository.findByIsDeletedFalse();
        model.addAttribute("styles", styles);
        
        List<StyleValue> styleValues = styleValueRepository.findByIsDeletedFalse();
        model.addAttribute("styleValues", styleValues);
        
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
        
        redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công! Sản phẩm đã được kích hoạt và đang bán.");
        return "redirect:/vendor/products";
    }
    
    @GetMapping("/products/{id}")
    public String viewProductDetail(@PathVariable String id,
                                   Model model,
                                   Authentication auth) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String storeId = securityService.getCurrentVendorStoreId(user.getId());
        
        VendorProductDTO product = productService.getMyProduct(id, storeId);
        
        // Parse styleValueIds JSON and get names
        if (product.getStyleValueIds() != null && !product.getStyleValueIds().equals("[]")) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<String> styleValueIds = mapper.readValue(product.getStyleValueIds(), 
                        mapper.getTypeFactory().constructCollectionType(List.class, String.class));
                
                if (!styleValueIds.isEmpty()) {
                    List<String> styleValueNames = styleValueRepository.findAllById(styleValueIds).stream()
                            .map(sv -> sv.getStyle().getName() + ": " + sv.getName())
                            .collect(Collectors.toList());
                    model.addAttribute("styleValueNames", styleValueNames);
                }
            } catch (Exception e) {
                // Ignore JSON parse errors
            }
        }
        
        model.addAttribute("product", product);
        return "vendor/products/detail";
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
        
        // Load styles and style values for editing
        List<Style> styles = styleRepository.findByIsDeletedFalse();
        model.addAttribute("styles", styles);
        
        List<StyleValue> styleValues = styleValueRepository.findByIsDeletedFalse();
        model.addAttribute("styleValues", styleValues);
        
        // Parse selected styleValueIds
        if (product.getStyleValueIds() != null && !product.getStyleValueIds().equals("[]")) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<String> selectedStyleValueIds = mapper.readValue(product.getStyleValueIds(), 
                        mapper.getTypeFactory().constructCollectionType(List.class, String.class));
                model.addAttribute("selectedStyleValueIds", selectedStyleValueIds);
            } catch (Exception e) {
                model.addAttribute("selectedStyleValueIds", List.of());
            }
        } else {
            model.addAttribute("selectedStyleValueIds", List.of());
        }
        
        return "vendor/products/form";
    }
    
    @PostMapping("/products/{id}/update")
    public String updateProduct(@PathVariable String id,
                               @Valid @ModelAttribute("productDTO") ProductUpdateDTO updateDTO,
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
            updateDTO.setImageUrls(urls);
        }
        
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

    // Also provide three vendor zones: new (NOT_PROCESSED), processing (PROCESSING), assigned to shipper (has shipment)
    List<VendorOrderDTO> newOrders = orderService.getMyOrdersByStatus(storeId, Order.OrderStatus.NOT_PROCESSED);
    List<VendorOrderDTO> processingOrders = orderService.getMyOrdersByStatus(storeId, Order.OrderStatus.PROCESSING);
    List<VendorOrderDTO> assignedShipments = orderService.getMyAssignedShipments(storeId);

    model.addAttribute("newOrders", newOrders);
    model.addAttribute("processingOrders", processingOrders);
    model.addAttribute("assignedShipments", assignedShipments);
        
        // Order counts by status
        model.addAttribute("newCount", orderService.countOrdersByStatus(storeId, Order.OrderStatus.NOT_PROCESSED));
        model.addAttribute("processingCount", orderService.countOrdersByStatus(storeId, Order.OrderStatus.PROCESSING));
        model.addAttribute("shippingCount", orderService.countOrdersByStatus(storeId, Order.OrderStatus.DELIVERING));
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

        Shipment shipment = shipmentRepository.findByOrderId(id);
        model.addAttribute("shipment", shipment);

        
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
    
    @PostMapping("/orders/{id}/delivered")
    public String markAsDelivered(@PathVariable String id,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String storeId = securityService.getCurrentVendorStoreId(user.getId());
            
            orderService.markAsDelivered(id, storeId);
            
            redirectAttributes.addFlashAttribute("success", "Đơn hàng đã giao thành công! Tiền đã được chuyển vào ví.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/vendor/orders/" + id;
    }
}
