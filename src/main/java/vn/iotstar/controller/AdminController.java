package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.User;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.Category;
import vn.iotstar.service.AdminService;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.service.PasswordService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordService passwordService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        // Get dashboard statistics from database
        Map<String, Object> stats = adminService.getDashboardStats();
        model.addAttribute("totalUsers", stats.get("totalUsers"));
        model.addAttribute("totalProducts", stats.get("totalProducts"));
        model.addAttribute("totalOrders", stats.get("totalOrders"));
        model.addAttribute("totalStores", stats.get("totalStores"));
        model.addAttribute("totalRevenue", stats.get("totalRevenue"));
        model.addAttribute("shippedCount", stats.get("shippedCount"));
        model.addAttribute("deliveredToday", stats.get("deliveredToday"));
        
        // Get recent orders
        List<Order> recentOrders = adminService.getRecentOrders();
        model.addAttribute("recentOrders", recentOrders);
        
        return "admin/dashboard";
    }
    
    @GetMapping("/users")
    public String users(Model model, Authentication authentication) {
        try {
            model.addAttribute("username", authentication.getName());
            
            // Get all users
            List<User> users = userRepository.findAll();
            
            // Ensure users list is never null
            if (users == null) {
                users = new java.util.ArrayList<>();
            }
            
            model.addAttribute("users", users);
            
            // Count by role - with null safety
            long adminCount = users.stream()
                .filter(u -> u != null && u.getRole() != null && u.getRole() == User.UserRole.ADMIN)
                .count();
            long vendorCount = users.stream()
                .filter(u -> u != null && u.getRole() != null && u.getRole() == User.UserRole.VENDOR)
                .count();
            long userCount = users.stream()
                .filter(u -> u != null && u.getRole() != null && u.getRole() == User.UserRole.USER)
                .count();
            
            model.addAttribute("totalUsers", users.size());
            model.addAttribute("adminCount", adminCount);
            model.addAttribute("vendorCount", vendorCount);
            model.addAttribute("userCount", userCount);
            
            return "admin/users";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách người dùng: " + e.getMessage());
            model.addAttribute("users", new java.util.ArrayList<>());
            model.addAttribute("totalUsers", 0);
            model.addAttribute("adminCount", 0);
            model.addAttribute("vendorCount", 0);
            model.addAttribute("userCount", 0);
            return "admin/users";
        }
    }
    
    @GetMapping("/stores")
    public String stores(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        // Get all stores
        List<Store> stores = storeRepository.findAll();
        model.addAttribute("stores", stores);
        
        // Get all vendors for dropdown in add store modal
        List<User> vendors = userRepository.findAll().stream()
            .filter(u -> u.getRole() == User.UserRole.VENDOR || u.getRole() == User.UserRole.ADMIN)
            .toList();
        model.addAttribute("vendors", vendors);
        
        // Count by status
        long activeCount = stores.stream().filter(Store::getIsActive).count();
        long inactiveCount = stores.size() - activeCount;
        
        model.addAttribute("totalStores", stores.size());
        model.addAttribute("activeStores", activeCount);
        model.addAttribute("inactiveStores", inactiveCount);
        
        return "admin/stores";
    }
    
    @GetMapping("/categories")
    public String categories(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        // Get all categories
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        
        return "admin/categories";
    }
    
    @GetMapping("/orders")
    public String orders(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        // Get all orders
        List<Order> orders = orderRepository.findAll();
        orders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
        model.addAttribute("orders", orders);
        
        // Count by status
        long pendingCount = orders.stream().filter(o -> o.getStatus() == Order.OrderStatus.NOT_PROCESSED).count();
        long processingCount = orders.stream().filter(o -> o.getStatus() == Order.OrderStatus.PROCESSING).count();
        long shippedCount = orders.stream().filter(o -> o.getStatus() == Order.OrderStatus.SHIPPED).count();
        long deliveredCount = orders.stream().filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED).count();
        long cancelledCount = orders.stream().filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED).count();
        
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("processingCount", processingCount);
        model.addAttribute("shippedCount", shippedCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("cancelledCount", cancelledCount);
        
        return "admin/orders";
    }
    
    @GetMapping("/vouchers")
    public String vouchers(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        return "admin/vouchers";
    }
    
    @GetMapping("/reports")
    public String reports(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        // Get report statistics from database
        Map<String, Object> reportStats = adminService.getReportStats();
        model.addAttribute("topProducts", reportStats.get("topProducts"));
        model.addAttribute("categorySales", reportStats.get("categorySales"));
        model.addAttribute("topStores", reportStats.get("topStores"));
        model.addAttribute("totalOrders", reportStats.get("totalOrders"));
        model.addAttribute("completedOrders", reportStats.get("completedOrders"));
        model.addAttribute("cancelledOrders", reportStats.get("cancelledOrders"));
        model.addAttribute("totalRevenue", reportStats.get("totalRevenue"));
        
        return "admin/reports";
    }
    
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());

        // Load current user by email (authentication name assumed to be email)
        String email = authentication != null ? authentication.getName() : null;
        if (email != null) {
            userRepository.findByEmail(email).ifPresent(u -> model.addAttribute("user", u));
        }

        return "admin/profile";
    }

    /**
     * Update profile information
     */
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String birthday,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String bio,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        String authEmail = authentication != null ? authentication.getName() : null;
        if (authEmail == null) {
            redirectAttributes.addFlashAttribute("message", "Người dùng chưa xác thực.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/profile";
        }

        Optional<User> userOpt = userRepository.findByEmail(authEmail);
        if (!userOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy người dùng.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/profile";
        }

        User user = userOpt.get();

        // Validate email uniqueness if changed
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("message", "Email đã được sử dụng bởi người dùng khác.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/profile";
        }

        // Validate phone uniqueness if provided and changed
        if (phone != null && !phone.isBlank()) {
            if (user.getPhone() == null || !user.getPhone().equals(phone)) {
                if (userRepository.existsByPhone(phone)) {
                    redirectAttributes.addFlashAttribute("message", "Số điện thoại đã được sử dụng bởi người khác.");
                    redirectAttributes.addFlashAttribute("messageType", "danger");
                    return "redirect:/admin/profile";
                }
            }
        }

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone((phone != null && !phone.isBlank()) ? phone : null);
        user.setAddresses((address != null && !address.isBlank()) ? address : null);
        if (birthday != null && !birthday.isBlank()) {
            try {
                user.setBirthday(LocalDate.parse(birthday));
            } catch (Exception ex) {
                // ignore parse error
            }
        } else {
            user.setBirthday(null);
        }
        user.setBio(bio);

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message", "Đã cập nhật thông tin cá nhân thành công.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/profile";
    }

    /**
     * Change password
     */
    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        String authEmail = authentication != null ? authentication.getName() : null;
        if (authEmail == null) {
            redirectAttributes.addFlashAttribute("message", "Người dùng chưa xác thực.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/profile";
        }

        Optional<User> userOpt = userRepository.findByEmail(authEmail);
        if (!userOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy người dùng.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/profile";
        }

        User user = userOpt.get();

        if (!passwordService.verifyPassword(currentPassword, user.getHashedPassword())) {
            redirectAttributes.addFlashAttribute("message", "Mật khẩu hiện tại không chính xác.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/profile";
        }

        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("message", "Mật khẩu mới phải có ít nhất 6 ký tự.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("message", "Mật khẩu xác nhận không khớp.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/profile";
        }

        user.setHashedPassword(passwordService.hashPassword(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/profile";
    }

    /**
     * Upload avatar
     */
    @PostMapping("/profile/upload-avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile avatar,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {
        String authEmail = authentication != null ? authentication.getName() : null;
        if (authEmail == null) {
            redirectAttributes.addFlashAttribute("message", "Người dùng chưa xác thực.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/profile";
        }

        Optional<User> userOpt = userRepository.findByEmail(authEmail);
        if (!userOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy người dùng.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/profile";
        }

        User user = userOpt.get();

        if (avatar == null || avatar.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Vui lòng chọn tệp ảnh.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/profile";
        }

        try {
            // Use system temp directory or a dedicated upload folder outside the project
            String userHome = System.getProperty("user.home");
            String uploadsDir = userHome + File.separator + "bad-shop-uploads" + File.separator + "avatars";
            Path uploadPath = Paths.get(uploadsDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String original = avatar.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String filename = user.getId() + "_" + System.currentTimeMillis() + ext;

            Path filePath = uploadPath.resolve(filename);
            avatar.transferTo(filePath.toFile());

            // Set avatar URL path for serving (we'll need a controller to serve this)
            user.setAvatar("/uploads/avatars/" + filename);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("message", "Đã cập nhật ảnh đại diện thành công.");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (IOException ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Lỗi khi tải ảnh lên: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }

        return "redirect:/admin/profile";
    }

    @GetMapping("/users/{id}/details")
    public String userDetails(@PathVariable String id, Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            model.addAttribute("error", "Không tìm thấy người dùng!");
            return "redirect:/admin/users";
        }
        
        User user = userOpt.get();
        model.addAttribute("user", user);
        
        // Get user's stores
        int storeCount = user.getStores() != null ? user.getStores().size() : 0;
        model.addAttribute("storeCount", storeCount);
        
        // Get user's orders
        int orderCount = user.getOrders() != null ? user.getOrders().size() : 0;
        model.addAttribute("orderCount", orderCount);
        
        // Get user's cart items count
        int cartItemCount = user.getCartItems() != null ? user.getCartItems().size() : 0;
        model.addAttribute("cartItemCount", cartItemCount);
        
        return "admin/user-details";
    }
    
    // ==================== STORE CRUD ====================
    
    /**
     * View store details
     */
    @GetMapping("/stores/{id}/details")
    public String storeDetails(@PathVariable String id, Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        Optional<Store> storeOpt = storeRepository.findById(id);
        if (!storeOpt.isPresent()) {
            model.addAttribute("error", "Không tìm thấy cửa hàng!");
            return "redirect:/admin/stores";
        }
        
        Store store = storeOpt.get();
        model.addAttribute("store", store);
        
        // Get store's products
        int productCount = store.getProducts() != null ? store.getProducts().size() : 0;
        model.addAttribute("productCount", productCount);
        model.addAttribute("products", store.getProducts());
        
        // Get store's orders
        int orderCount = store.getOrders() != null ? store.getOrders().size() : 0;
        model.addAttribute("orderCount", orderCount);
        
        // Calculate total revenue
        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (store.getOrders() != null) {
            totalRevenue = store.getOrders().stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getAmountFromStore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        model.addAttribute("totalRevenue", totalRevenue);
        
        return "admin/store-details";
    }
    
    /**
     * Create new store
     */
    @PostMapping("/stores/create")
    public String createStore(
            @RequestParam String name,
            @RequestParam String bio,
            @RequestParam String slug,
            @RequestParam String ownerId,
            RedirectAttributes redirectAttributes) {
        try {
            // Check if owner exists
            Optional<User> ownerOpt = userRepository.findById(ownerId);
            if (!ownerOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy chủ cửa hàng!");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/stores";
            }
            
            Store store = new Store();
            store.setId("S" + System.currentTimeMillis());
            store.setName(name);
            store.setBio(bio);
            store.setSlug(slug);
            store.setOwner(ownerOpt.get());
            store.setIsActive(true);
            store.setRating(BigDecimal.ZERO);
            
            storeRepository.save(store);
            
            redirectAttributes.addFlashAttribute("message", "Đã thêm cửa hàng thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi khi thêm cửa hàng: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/stores";
    }
    
    /**
     * Toggle store status (Active/Inactive)
     */
    @PostMapping("/stores/{id}/toggle-status")
    public String toggleStoreStatus(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Optional<Store> storeOpt = storeRepository.findById(id);
        if (storeOpt.isPresent()) {
            Store store = storeOpt.get();
            store.setIsActive(!store.getIsActive());
            storeRepository.save(store);
            
            String status = store.getIsActive() ? "kích hoạt" : "vô hiệu hóa";
            redirectAttributes.addFlashAttribute("message", "Đã " + status + " cửa hàng thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy cửa hàng!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/stores";
    }
    
    /**
     * Delete store
     */
    @PostMapping("/stores/{id}/delete")
    public String deleteStore(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Store> storeOpt = storeRepository.findById(id);
            if (!storeOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy cửa hàng!");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/stores";
            }
            
            Store store = storeOpt.get();
            
            // Check if store has products
            if (store.getProducts() != null && !store.getProducts().isEmpty()) {
                redirectAttributes.addFlashAttribute("message", 
                    "Không thể xóa cửa hàng '" + store.getName() + "' vì có " + store.getProducts().size() + " sản phẩm! Vui lòng xóa tất cả sản phẩm trước.");
                redirectAttributes.addFlashAttribute("messageType", "warning");
                return "redirect:/admin/stores";
            }
            
            // Check if store has orders
            if (store.getOrders() != null && !store.getOrders().isEmpty()) {
                redirectAttributes.addFlashAttribute("message", 
                    "Không thể xóa cửa hàng '" + store.getName() + "' vì có " + store.getOrders().size() + " đơn hàng liên quan! Hãy xem xét vô hiệu hóa cửa hàng thay vì xóa.");
                redirectAttributes.addFlashAttribute("messageType", "warning");
                return "redirect:/admin/stores";
            }
            
            storeRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Đã xóa cửa hàng '" + store.getName() + "' thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", 
                "Không thể xóa cửa hàng: " + e.getMessage() + ". Cửa hàng này có thể đang có dữ liệu liên quan trong hệ thống.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/stores";
    }
    
    // ==================== CATEGORY CRUD ====================
    
    /**
     * Create new category
     */
    @PostMapping("/categories/create")
    public String createCategory(
            @RequestParam String name,
            @RequestParam String slug,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {
        try {
            Category category = new Category();
            category.setId("C" + System.currentTimeMillis());
            category.setName(name);
            category.setSlug(slug);
            category.setDescription(description);
            category.setIsActive(true);
            
            categoryRepository.save(category);
            redirectAttributes.addFlashAttribute("message", "Đã tạo danh mục thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Không thể tạo danh mục: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/categories";
    }
    
    /**
     * Update category
     */
    @PostMapping("/categories/{id}/update")
    public String updateCategory(
            @PathVariable String id,
            @RequestParam String name,
            @RequestParam String slug,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            category.setName(name);
            category.setSlug(slug);
            category.setDescription(description);
            
            categoryRepository.save(category);
            redirectAttributes.addFlashAttribute("message", "Đã cập nhật danh mục thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy danh mục!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/categories";
    }
    
    /**
     * Toggle category status
     */
    @PostMapping("/categories/{id}/toggle-status")
    public String toggleCategoryStatus(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            category.setIsActive(!category.getIsActive());
            categoryRepository.save(category);
            
            String status = category.getIsActive() ? "kích hoạt" : "vô hiệu hóa";
            redirectAttributes.addFlashAttribute("message", "Đã " + status + " danh mục thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy danh mục!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/categories";
    }
    
    /**
     * Delete category
     */
    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            categoryRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Đã xóa danh mục thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Không thể xóa danh mục: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/categories";
    }
    
    // ==================== ORDER ACTIONS ====================
    
    /**
     * Update order status
     */
    @PostMapping("/orders/{id}/update-status")
    public String updateOrderStatus(
            @PathVariable String id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(Order.OrderStatus.valueOf(status));
            orderRepository.save(order);
            
            redirectAttributes.addFlashAttribute("message", "Đã cập nhật trạng thái đơn hàng thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy đơn hàng!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/orders";
    }

    /**
     * Change user role
     */
    @PostMapping("/users/{id}/change-role")
    public String changeUserRole(
            @PathVariable String id,
            @RequestParam String role,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy người dùng!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/users";
        }

        User user = userOpt.get();

        // Prevent changing role to an invalid value
        User.UserRole newRole;
        try {
            newRole = User.UserRole.valueOf(role);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("message", "Vai trò không hợp lệ: " + role);
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/users";
        }

        // Prevent demoting the last admin
        if (user.getRole() == User.UserRole.ADMIN && newRole != User.UserRole.ADMIN) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u != null && u.getRole() == User.UserRole.ADMIN)
                    .count();
            if (adminCount <= 1) {
                redirectAttributes.addFlashAttribute("message", "Không thể gỡ quyền ADMIN: hệ thống chỉ còn một ADMIN duy nhất.");
                redirectAttributes.addFlashAttribute("messageType", "warning");
                return "redirect:/admin/users";
            }

            // Prevent self-demotion
            if (authentication != null && authentication.getName() != null && authentication.getName().equals(user.getEmail())) {
                redirectAttributes.addFlashAttribute("message", "Bạn không thể tự gỡ quyền ADMIN của chính mình.");
                redirectAttributes.addFlashAttribute("messageType", "warning");
                return "redirect:/admin/users";
            }
        }

        user.setRole(newRole);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message", "Đã cập nhật vai trò cho người dùng thành công!");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/users";
    }

    /**
     * Remove ADMIN role (set to USER)
     */
    @PostMapping("/users/{id}/remove-admin-role")
    public String removeAdminRole(
            @PathVariable String id,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy người dùng!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/users";
        }

        User user = userOpt.get();

        if (user.getRole() != User.UserRole.ADMIN) {
            redirectAttributes.addFlashAttribute("message", "Người dùng không có quyền ADMIN.");
            redirectAttributes.addFlashAttribute("messageType", "info");
            return "redirect:/admin/users";
        }

        // Prevent removing last admin
        long adminCount = userRepository.findAll().stream()
                .filter(u -> u != null && u.getRole() == User.UserRole.ADMIN)
                .count();
        if (adminCount <= 1) {
            redirectAttributes.addFlashAttribute("message", "Không thể gỡ quyền ADMIN: hệ thống chỉ còn một ADMIN duy nhất.");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/admin/users";
        }

        // Prevent self-removal
        if (authentication != null && authentication.getName() != null && authentication.getName().equals(user.getEmail())) {
            redirectAttributes.addFlashAttribute("message", "Bạn không thể tự gỡ quyền ADMIN của chính mình.");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/admin/users";
        }

        user.setRole(User.UserRole.USER);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message", "Đã gỡ vai trò ADMIN của người dùng thành công.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/users";
    }
}
