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
import vn.iotstar.entity.Order;
import vn.iotstar.entity.User;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.Category;
import vn.iotstar.service.AdminService;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.OrderRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.math.BigDecimal;

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
        
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("shippedCount", shippedCount);
        model.addAttribute("deliveredCount", deliveredCount);
        
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
        return "admin/reports";
    }
    
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        return "admin/profile";
    }
    
    @GetMapping("/settings")
    public String settings(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        return "admin/settings";
    }
    
    // ==================== USER CRUD ====================
    
    /**
     * Create new user
     */
    @PostMapping("/users/create")
    public String createUser(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String role,
            RedirectAttributes redirectAttributes) {
        try {
            // Check if email already exists
            if (userRepository.findByEmail(email).isPresent()) {
                redirectAttributes.addFlashAttribute("message", "Email đã tồn tại!");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/users";
            }
            
            User user = new User();
            user.setId("U" + System.currentTimeMillis());
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            
            // Hash password
            user.setSalt(java.util.UUID.randomUUID().toString());
            user.setHashedPassword(org.springframework.security.crypto.bcrypt.BCrypt.hashpw(password, org.springframework.security.crypto.bcrypt.BCrypt.gensalt()));
            
            user.setRole(User.UserRole.valueOf(role));
            user.setStatus(User.UserStatus.ACTIVE);
            
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("message", "Đã thêm người dùng thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi khi thêm người dùng: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/users";
    }
    
    /**
     * Toggle user status (Active/Inactive)
     */
    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getStatus() == User.UserStatus.ACTIVE) {
                user.setStatus(User.UserStatus.INACTIVE);
                redirectAttributes.addFlashAttribute("message", "Đã khóa người dùng thành công!");
                redirectAttributes.addFlashAttribute("messageType", "warning");
            } else {
                user.setStatus(User.UserStatus.ACTIVE);
                redirectAttributes.addFlashAttribute("message", "Đã kích hoạt người dùng thành công!");
                redirectAttributes.addFlashAttribute("messageType", "success");
            }
            userRepository.save(user);
        } else {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy người dùng!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/users";
    }
    
    /**
     * Delete user
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy người dùng!");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/users";
            }
            
            User user = userOpt.get();
            
            // Check if user owns any stores
            if (user.getStores() != null && !user.getStores().isEmpty()) {
                redirectAttributes.addFlashAttribute("message", 
                    "Không thể xóa người dùng này vì đang sở hữu " + user.getStores().size() + " cửa hàng! Vui lòng xóa hoặc chuyển quyền sở hữu cửa hàng trước.");
                redirectAttributes.addFlashAttribute("messageType", "warning");
                return "redirect:/admin/users";
            }
            
            // Check if user has orders
            if (user.getOrders() != null && !user.getOrders().isEmpty()) {
                redirectAttributes.addFlashAttribute("message", 
                    "Không thể xóa người dùng này vì có " + user.getOrders().size() + " đơn hàng liên quan! Hãy xem xét vô hiệu hóa tài khoản thay vì xóa.");
                redirectAttributes.addFlashAttribute("messageType", "warning");
                return "redirect:/admin/users";
            }
            
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Đã xóa người dùng '" + user.getFullName() + "' thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", 
                "Không thể xóa người dùng: " + e.getMessage() + ". Người dùng này có thể đang có dữ liệu liên quan trong hệ thống.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/users";
    }
    
    /**
     * View user details
     */
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
}