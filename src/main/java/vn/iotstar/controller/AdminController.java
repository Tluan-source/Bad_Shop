package vn.iotstar.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.iotstar.entity.ActivityLog;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.User;
import vn.iotstar.entity.Voucher;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.repository.VoucherRepository;
import vn.iotstar.service.ActivityLogService;
import vn.iotstar.service.AdminService;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CommissionService;
import vn.iotstar.service.MailService;
import vn.iotstar.service.PasswordService;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordService passwordService;
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    @Autowired
    private ActivityLogService activityLogService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CommissionService commissionService;
    
    @Autowired
    private MailService mailService;
    
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
    
    @PostMapping("/users/create")
    public String createUser(@RequestParam String fullName,
                            @RequestParam String email,
                            @RequestParam String phone,
                            @RequestParam String password,
                            @RequestParam String role,
                            RedirectAttributes redirectAttributes) {
        try {
            // Check if email already exists
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                redirectAttributes.addFlashAttribute("messageType", "danger");
                redirectAttributes.addFlashAttribute("message", "Email đã tồn tại trong hệ thống!");
                return "redirect:/admin/users";
            }
            
            // Create new user
            User newUser = new User();
            newUser.setId("U_" + System.currentTimeMillis());
            newUser.setFullName(fullName);
            newUser.setEmail(email);
            newUser.setPhone(phone);
            
            // Hash password (PasswordService không cần salt nữa)
            newUser.setHashedPassword(passwordService.hashPassword(password));
            
            // Set role
            newUser.setRole(User.UserRole.valueOf(role));
            newUser.setStatus(User.UserStatus.ACTIVE);
            newUser.setPoint(0);
            newUser.setEWallet(BigDecimal.ZERO);
            
            // Save user
            userRepository.save(newUser);
            
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Tạo người dùng thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("messageType", "danger");
            redirectAttributes.addFlashAttribute("message", "Lỗi khi tạo người dùng: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @GetMapping("/stores")
    public String stores(@RequestParam(required = false) String search, 
                        Model model, 
                        Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        // Get stores based on search parameter
        List<Store> stores;
        if (search != null && !search.trim().isEmpty()) {
            stores = storeRepository.searchStoresByNameOrOwner(search.trim());
            model.addAttribute("search", search);
        } else {
            stores = storeRepository.findAll();
        }
        
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
    public String vouchers(@RequestParam(required = false) String search, 
                          Model model, 
                          Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        // Get vouchers based on search parameter
        List<Voucher> vouchers;
        if (search != null && !search.trim().isEmpty()) {
            vouchers = voucherRepository.searchVouchers(search.trim());
            model.addAttribute("search", search);
        } else {
            vouchers = voucherRepository.findAll();
        }
        
        model.addAttribute("vouchers", vouchers);
        
        // Calculate statistics
        long totalVouchers = vouchers.size();
        long activeVouchers = vouchers.stream().filter(v -> v.getIsActive() && !v.isExpired()).count();
        int totalUsageCount = vouchers.stream().mapToInt(Voucher::getUsageCount).sum();
        
        // Calculate total discount given (estimated)
        BigDecimal totalDiscount = vouchers.stream()
            .map(v -> {
                if (v.getDiscountType() == Voucher.DiscountType.FIXED) {
                    return v.getDiscountValue().multiply(new BigDecimal(v.getUsageCount()));
                } else {
                    // For percentage, use max discount as estimate
                    return v.getMaxDiscount().multiply(new BigDecimal(v.getUsageCount()));
                }
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("totalVouchers", totalVouchers);
        model.addAttribute("activeVouchers", activeVouchers);
        model.addAttribute("totalUsageCount", totalUsageCount);
        model.addAttribute("totalDiscount", totalDiscount);
        
        return "admin/vouchers";
    }
    
    @GetMapping("/reports")
    public String reports(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model, 
            Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        LocalDateTime start = null;
        LocalDateTime end = null;
        
        // Calculate date range based on period
        if (period != null) {
            LocalDateTime now = LocalDateTime.now();
            switch (period) {
                case "7":
                    start = now.minusDays(7);
                    end = now;
                    break;
                case "30":
                    start = now.minusDays(30);
                    end = now;
                    break;
                case "90":
                    start = now.minusDays(90);
                    end = now;
                    break;
                case "365":
                    start = now.minusDays(365);
                    end = now;
                    break;
                case "custom":
                    if (startDate != null && endDate != null) {
                        try {
                            start = LocalDate.parse(startDate).atStartOfDay();
                            end = LocalDate.parse(endDate).atTime(23, 59, 59);
                        } catch (Exception e) {
                            // If parsing fails, use all time
                        }
                    }
                    break;
            }
        }
        
        // Get report statistics with date filter
        Map<String, Object> reportStats;
        if (start != null && end != null) {
            reportStats = adminService.getReportStatsByDateRange(start, end);
            model.addAttribute("startDate", start);
            model.addAttribute("endDate", end);
        } else {
            reportStats = adminService.getReportStats();
        }
        
        model.addAttribute("period", period);
        model.addAttribute("topProducts", reportStats.get("topProducts"));
        model.addAttribute("categorySales", reportStats.get("categorySales"));
        model.addAttribute("topStores", reportStats.get("topStores"));
        model.addAttribute("totalOrders", reportStats.get("totalOrders"));
        model.addAttribute("completedOrders", reportStats.get("completedOrders"));
        model.addAttribute("cancelledOrders", reportStats.get("cancelledOrders"));
        model.addAttribute("totalRevenue", reportStats.get("totalRevenue"));
        
        return "admin/reports";
    }
    
    @GetMapping("/settings")
    public String settings(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        return "admin/settings";
    }
    
    @GetMapping("/reports/export")
    public void exportReport(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        
        LocalDateTime start = null;
        LocalDateTime end = null;
        
        // Calculate date range
        if (period != null) {
            LocalDateTime now = LocalDateTime.now();
            switch (period) {
                case "7":
                    start = now.minusDays(7);
                    end = now;
                    break;
                case "30":
                    start = now.minusDays(30);
                    end = now;
                    break;
                case "90":
                    start = now.minusDays(90);
                    end = now;
                    break;
                case "365":
                    start = now.minusDays(365);
                    end = now;
                    break;
                case "custom":
                    if (startDate != null && endDate != null) {
                        try {
                            start = LocalDate.parse(startDate).atStartOfDay();
                            end = LocalDate.parse(endDate).atTime(23, 59, 59);
                        } catch (Exception e) {
                            // If parsing fails, use all time
                        }
                    }
                    break;
            }
        }
        
        // Get report data
        Map<String, Object> reportStats;
        if (start != null && end != null) {
            reportStats = adminService.getReportStatsByDateRange(start, end);
        } else {
            reportStats = adminService.getReportStats();
        }
        
        // Generate CSV report
        String fileName = "BaoCao_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        
        PrintWriter writer = response.getWriter();
        
        // Write BOM for UTF-8
        writer.write('\ufeff');
        
        // Write header
        writer.println("BÁO CÁO THỐNG KÊ - BADMINTON MARKETPLACE");
        if (start != null && end != null) {
            writer.println("Thời gian: " + start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                          " - " + end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            writer.println("Thời gian: Tất cả");
        }
        writer.println("Ngày xuất: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        writer.println();
        
        // Summary statistics
        writer.println("TỔNG QUAN");
        writer.println("Tổng đơn hàng," + reportStats.get("totalOrders"));
        writer.println("Đơn hoàn thành," + reportStats.get("completedOrders"));
        writer.println("Đơn hủy," + reportStats.get("cancelledOrders"));
        writer.println("Tổng doanh thu," + reportStats.get("totalRevenue") + " VNĐ");
        writer.println();
        
        // Top products
        writer.println("TOP SẢN PHẨM BÁN CHẠY");
        writer.println("STT,Tên sản phẩm,Số lượng bán,Doanh thu (VNĐ)");
        List<Map<String, Object>> topProducts = (List<Map<String, Object>>) reportStats.get("topProducts");
        if (topProducts != null) {
            int index = 1;
            for (Map<String, Object> item : topProducts) {
                Product product = (Product) item.get("product");
                writer.println(index++ + "," + 
                    product.getName() + "," + 
                    item.get("quantity") + "," + 
                    item.get("revenue"));
            }
        }
        writer.println();
        
        // Top stores
        writer.println("TOP CỬA HÀNG XUẤT SẮC");
        writer.println("STT,Tên cửa hàng,Số lượng bán,Doanh thu (VNĐ),Rating");
        List<Map<String, Object>> topStores = (List<Map<String, Object>>) reportStats.get("topStores");
        if (topStores != null) {
            int index = 1;
            for (Map<String, Object> item : topStores) {
                Store store = (Store) item.get("store");
                writer.println(index++ + "," + 
                    store.getName() + "," + 
                    item.get("quantity") + "," + 
                    item.get("revenue") + "," + 
                    store.getRating());
            }
        }
        writer.println();
        
        // Category sales
        writer.println("THỐNG KÊ THEO DANH MỤC");
        writer.println("STT,Danh mục,Số lượng bán,Doanh thu (VNĐ)");
        List<Map<String, Object>> categorySales = (List<Map<String, Object>>) reportStats.get("categorySales");
        if (categorySales != null) {
            int index = 1;
            for (Map<String, Object> item : categorySales) {
                Category category = (Category) item.get("category");
                writer.println(index++ + "," + 
                    category.getName() + "," + 
                    item.get("quantity") + "," + 
                    item.get("revenue"));
            }
        }
        
        writer.flush();
    }
    
    // ==================== VOUCHER CRUD ====================
    
    /**
     * Create new voucher
     */
    @PostMapping("/vouchers/create")
    public String createVoucher(
            @RequestParam String code,
            @RequestParam(required = false) String description,
            @RequestParam String discountType,
            @RequestParam BigDecimal discountValue,
            @RequestParam(required = false) BigDecimal maxDiscount,
            @RequestParam(required = false) BigDecimal minOrderValue,
            @RequestParam Integer quantity,
            @RequestParam String startDate,
            @RequestParam String endDate,
            RedirectAttributes redirectAttributes) {
        try {
            // Check if code already exists
            if (voucherRepository.findByCode(code).isPresent()) {
                redirectAttributes.addFlashAttribute("message", "Mã voucher '" + code + "' đã tồn tại!");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/vouchers";
            }
            
            Voucher voucher = new Voucher();
            voucher.setId("V" + System.currentTimeMillis());
            voucher.setCode(code.toUpperCase());
            voucher.setDescription(description);
            voucher.setDiscountType(Voucher.DiscountType.valueOf(discountType));
            voucher.setDiscountValue(discountValue);
            voucher.setMaxDiscount(maxDiscount != null ? maxDiscount : BigDecimal.ZERO);
            voucher.setMinOrderValue(minOrderValue != null ? minOrderValue : BigDecimal.ZERO);
            voucher.setQuantity(quantity);
            voucher.setUsageCount(0);
            voucher.setStartDate(LocalDateTime.parse(startDate + "T00:00:00"));
            voucher.setEndDate(LocalDateTime.parse(endDate + "T23:59:59"));
            voucher.setIsActive(true);
            
            voucherRepository.save(voucher);
            
            redirectAttributes.addFlashAttribute("message", "Đã tạo voucher thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Lỗi khi tạo voucher: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/vouchers";
    }
    
    /**
     * Toggle voucher status
     */
    @PostMapping("/vouchers/{id}/toggle-status")
    public String toggleVoucherStatus(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Optional<Voucher> voucherOpt = voucherRepository.findById(id);
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            voucher.setIsActive(!voucher.getIsActive());
            voucherRepository.save(voucher);
            
            String status = voucher.getIsActive() ? "kích hoạt" : "vô hiệu hóa";
            redirectAttributes.addFlashAttribute("message", "Đã " + status + " voucher thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy voucher!");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/vouchers";
    }
    
    /**
     * Delete voucher
     */
    @PostMapping("/vouchers/{id}/delete")
    public String deleteVoucher(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Voucher> voucherOpt = voucherRepository.findById(id);
            if (!voucherOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy voucher!");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/vouchers";
            }
            
            Voucher voucher = voucherOpt.get();
            
            // Check if voucher has been used
            if (voucher.getUsageCount() > 0) {
                redirectAttributes.addFlashAttribute("message", 
                    "Không thể xóa voucher '" + voucher.getCode() + "' vì đã có " + voucher.getUsageCount() + " lượt sử dụng!");
                redirectAttributes.addFlashAttribute("messageType", "warning");
                return "redirect:/admin/vouchers";
            }
            
            voucherRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Đã xóa voucher '" + voucher.getCode() + "' thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", 
                "Không thể xóa voucher: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/vouchers";
    }
    
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication, HttpServletRequest request) {
        model.addAttribute("username", authentication.getName());

        // Load current user by email (authentication name assumed to be email)
        String email = authentication != null ? authentication.getName() : null;
        if (email != null) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("user", user);
                
                // Lấy lịch sử hoạt động gần đây (10 hoạt động gần nhất)
                List<ActivityLog> recentActivities = activityLogService.getRecentActivities(user, 10);
                model.addAttribute("recentActivities", recentActivities);
                
                // Ghi log xem profile
                activityLogService.logActivity(user, ActivityLog.ActivityType.VIEW_DASHBOARD, 
                    "Xem trang thông tin cá nhân", request);
            }
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
            // Upload to Cloudinary and set secure URL as avatar
            String url = cloudinaryService.uploadFile(avatar);
            if (url == null) {
                redirectAttributes.addFlashAttribute("message", "Không thể tải ảnh lên Cloudinary.");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/profile";
            }

            user.setAvatar(url);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("message", "Đã cập nhật ảnh đại diện thành công.");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception ex) {
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
        
        // Force initialization of lazy collections to avoid LazyInitializationException
        List<Product> products = store.getProducts();
        int productCount = 0;
        if (products != null) {
            productCount = products.size(); // This forces Hibernate to load the collection
        }
        
        List<Order> orders = store.getOrders();
        int orderCount = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (orders != null) {
            orderCount = orders.size(); // This forces Hibernate to load the collection
            totalRevenue = orders.stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getAmountFromStore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        model.addAttribute("store", store);
        model.addAttribute("productCount", productCount);
        model.addAttribute("products", products);
        model.addAttribute("orderCount", orderCount);
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
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/stores";
    }
    
    /**
     * Approve vendor registration - activate store and grant VENDOR role
     */
    @PostMapping("/stores/{id}/approve")
    public String approveVendorRegistration(@PathVariable String id,
                                           RedirectAttributes redirectAttributes,
                                           Authentication authentication) {
        try {
            Optional<Store> storeOpt = storeRepository.findById(id);
            if (!storeOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy cửa hàng!");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/stores";
            }
            
            Store store = storeOpt.get();
            User owner = store.getOwner();
            
            // Activate store
            store.setIsActive(true);
            store.setUpdatedAt(LocalDateTime.now());
            storeRepository.save(store);
            
            // Grant VENDOR role to owner
            owner.setRole(User.UserRole.VENDOR);
            userRepository.save(owner);
            
            // Send approval email
            try {
                String subject = "✅ Đăng ký cửa hàng được phê duyệt - " + store.getName();
                StringBuilder body = new StringBuilder();
                body.append("Chúc mừng! Đăng ký của bạn đã được phê duyệt\n\n");
                body.append("Xin chào ").append(owner.getFullName()).append(",\n\n");
                body.append("Cửa hàng ").append(store.getName()).append(" của bạn đã được phê duyệt.\n");
                body.append("Bạn có thể bắt đầu quản lý cửa hàng và đăng bán sản phẩm ngay bây giờ!\n\n");
                body.append("Vào trang quản lý: http://localhost:8080/vendor/dashboard\n\n");
                body.append("Chúc bạn kinh doanh thành công!");
                
                mailService.sendSimpleMessage(owner.getEmail(), subject, body.toString());
            } catch (Exception e) {
                System.err.println("Failed to send approval email: " + e.getMessage());
            }
            
            // Log activity - Get current admin user
            Optional<User> adminUserOpt = userRepository.findByEmail(authentication.getName());
            if (adminUserOpt.isPresent()) {
                activityLogService.logActivity(
                    adminUserOpt.get(),
                    ActivityLog.ActivityType.ADMIN_ACTION,
                    "Admin phê duyệt cửa hàng: " + store.getName() + " (ID: " + store.getId() + ")",
                    null
                );
            }
            
            redirectAttributes.addFlashAttribute("message", 
                "Đã phê duyệt cửa hàng " + store.getName() + " thành công! User đã được cấp quyền VENDOR.");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/admin/stores/" + id + "/details";
    }
    
    /**
     * Reject vendor registration - delete store and notify user
     */
    @PostMapping("/stores/{id}/reject")
    public String rejectVendorRegistration(@PathVariable String id,
                                          @RequestParam(required = false) String reason,
                                          RedirectAttributes redirectAttributes,
                                          Authentication authentication) {
        try {
            Optional<Store> storeOpt = storeRepository.findById(id);
            if (!storeOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy cửa hàng!");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/stores";
            }
            
            Store store = storeOpt.get();
            User owner = store.getOwner();
            String storeName = store.getName();
            
            // Send rejection email
            try {
                String subject = "❌ Đăng ký cửa hàng không được phê duyệt";
                StringBuilder body = new StringBuilder();
                body.append("Thông báo từ chối đăng ký\n\n");
                body.append("Xin chào ").append(owner.getFullName()).append(",\n\n");
                body.append("Rất tiếc, đăng ký cửa hàng ").append(storeName).append(" của bạn không được phê duyệt.\n\n");
                
                if (reason != null && !reason.isEmpty()) {
                    body.append("Lý do: ").append(reason).append("\n\n");
                }
                
                body.append("Bạn có thể đăng ký lại với thông tin đầy đủ và chính xác hơn.\n");
                body.append("Nếu có thắc mắc, vui lòng liên hệ: support@badmintonmarket.com");
                
                mailService.sendSimpleMessage(owner.getEmail(), subject, body.toString());
            } catch (Exception e) {
                System.err.println("Failed to send rejection email: " + e.getMessage());
            }
            
            // Delete store
            storeRepository.delete(store);
            
            // Log activity - Get current admin user
            Optional<User> adminUserOpt = userRepository.findByEmail(authentication.getName());
            if (adminUserOpt.isPresent()) {
                activityLogService.logActivity(
                    adminUserOpt.get(),
                    ActivityLog.ActivityType.ADMIN_ACTION,
                    "Admin từ chối cửa hàng: " + storeName + " (Lý do: " + (reason != null ? reason : "Không ghi") + ")",
                    null
                );
            }
            
            redirectAttributes.addFlashAttribute("message", 
                "Đã từ chối đăng ký cửa hàng " + storeName + ". Email thông báo đã được gửi.");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/admin/stores";
    }
    
    // ==================== COMMISSION CRUD ====================
    
    /**
     * View commissions page
     */
    @GetMapping("/commissions")
    public String commissions(@RequestParam(required = false) String search,
                             Model model,
                             Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        List<vn.iotstar.entity.Commission> commissions;
        if (search != null && !search.trim().isEmpty()) {
            commissions = commissionService.searchCommissions(search.trim());
            model.addAttribute("search", search);
        } else {
            commissions = commissionService.getAllCommissions();
        }
        
        model.addAttribute("commissions", commissions);
        model.addAttribute("totalCommissions", commissions.size());
        
        return "admin/commissions";
    }
    
    /**
     * Create commission
     */
    @PostMapping("/commissions/create")
    public String createCommission(@RequestParam String name,
                                  @RequestParam BigDecimal feePercent,
                                  @RequestParam(required = false) String description,
                                  RedirectAttributes redirectAttributes) {
        try {
            commissionService.createCommission(name, feePercent, description);
            redirectAttributes.addFlashAttribute("message", "Tạo chiết khấu thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/commissions";
    }
    
    /**
     * Update commission
     */
    @PostMapping("/commissions/{id}/update")
    public String updateCommission(@PathVariable String id,
                                  @RequestParam String name,
                                  @RequestParam BigDecimal feePercent,
                                  @RequestParam(required = false) String description,
                                  RedirectAttributes redirectAttributes) {
        try {
            commissionService.updateCommission(id, name, feePercent, description);
            redirectAttributes.addFlashAttribute("message", "Cập nhật chiết khấu thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/commissions";
    }
    
    /**
     * Delete commission
     */
    @PostMapping("/commissions/{id}/delete")
    public String deleteCommission(@PathVariable String id,
                                  RedirectAttributes redirectAttributes) {
        try {
            commissionService.deleteCommission(id);
            redirectAttributes.addFlashAttribute("message", "Xóa chiết khấu thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/commissions";
    }
    
    /**
     * Assign commission to store
     */
    @PostMapping("/stores/{storeId}/assign-commission")
    public String assignCommissionToStore(@PathVariable String storeId,
                                         @RequestParam String commissionId,
                                         RedirectAttributes redirectAttributes) {
        try {
            Optional<Store> storeOpt = storeRepository.findById(storeId);
            if (!storeOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy cửa hàng!");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/stores";
            }
            
            vn.iotstar.entity.Commission commission = commissionService.getCommissionById(commissionId);
            Store store = storeOpt.get();
            store.setCommission(commission);
            storeRepository.save(store);
            
            redirectAttributes.addFlashAttribute("message", 
                "Đã gán chiết khấu " + commission.getName() + " cho cửa hàng " + store.getName());
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/stores/" + storeId + "/details";
    }
    
    // ==================== SHIPPING PROVIDER CRUD ====================
    
    /**
     * View shipping providers page
     */
    @GetMapping("/shipping-providers")
    public String shippingProviders(@RequestParam(required = false) String search,
                                   Model model,
                                   Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        List<vn.iotstar.entity.ShippingProvider> providers;
        if (search != null && !search.trim().isEmpty()) {
            vn.iotstar.repository.ShippingProviderRepository repo = 
                (vn.iotstar.repository.ShippingProviderRepository) 
                org.springframework.beans.factory.BeanFactoryUtils.beanOfTypeIncludingAncestors(
                    org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext(),
                    vn.iotstar.repository.ShippingProviderRepository.class);
            providers = repo.searchByName(search.trim());
            model.addAttribute("search", search);
        } else {
            vn.iotstar.repository.ShippingProviderRepository repo = 
                (vn.iotstar.repository.ShippingProviderRepository) 
                org.springframework.beans.factory.BeanFactoryUtils.beanOfTypeIncludingAncestors(
                    org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext(),
                    vn.iotstar.repository.ShippingProviderRepository.class);
            providers = repo.findAll();
        }
        
        model.addAttribute("providers", providers);
        model.addAttribute("totalProviders", providers.size());
        
        // Count active providers
        long activeCount = providers.stream().filter(vn.iotstar.entity.ShippingProvider::getIsActive).count();
        model.addAttribute("activeProviders", activeCount);
        
        return "admin/shipping-providers";
    }
    
    /**
     * Create shipping provider
     */
    @PostMapping("/shipping-providers/create")
    public String createShippingProvider(@RequestParam String name,
                                        @RequestParam BigDecimal shippingFee,
                                        @RequestParam(required = false) String description,
                                        RedirectAttributes redirectAttributes) {
        try {
            vn.iotstar.repository.ShippingProviderRepository repo = 
                (vn.iotstar.repository.ShippingProviderRepository) 
                org.springframework.beans.factory.BeanFactoryUtils.beanOfTypeIncludingAncestors(
                    org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext(),
                    vn.iotstar.repository.ShippingProviderRepository.class);
            
            // Check if name exists
            if (repo.findByName(name).isPresent()) {
                redirectAttributes.addFlashAttribute("message", "Tên nhà vận chuyển đã tồn tại!");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/shipping-providers";
            }
            
            vn.iotstar.entity.ShippingProvider provider = new vn.iotstar.entity.ShippingProvider();
            provider.setId("SP" + System.currentTimeMillis());
            provider.setName(name);
            provider.setShippingFee(shippingFee);
            provider.setDescription(description);
            provider.setIsActive(true);
            
            repo.save(provider);
            
            redirectAttributes.addFlashAttribute("message", "Tạo nhà vận chuyển thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/shipping-providers";
    }
    
    /**
     * Update shipping provider
     */
    @PostMapping("/shipping-providers/{id}/update")
    public String updateShippingProvider(@PathVariable String id,
                                        @RequestParam String name,
                                        @RequestParam BigDecimal shippingFee,
                                        @RequestParam(required = false) String description,
                                        RedirectAttributes redirectAttributes) {
        try {
            vn.iotstar.repository.ShippingProviderRepository repo = 
                (vn.iotstar.repository.ShippingProviderRepository) 
                org.springframework.beans.factory.BeanFactoryUtils.beanOfTypeIncludingAncestors(
                    org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext(),
                    vn.iotstar.repository.ShippingProviderRepository.class);
            
            Optional<vn.iotstar.entity.ShippingProvider> providerOpt = repo.findById(id);
            if (!providerOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy nhà vận chuyển!");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/shipping-providers";
            }
            
            vn.iotstar.entity.ShippingProvider provider = providerOpt.get();
            provider.setName(name);
            provider.setShippingFee(shippingFee);
            provider.setDescription(description);
            
            repo.save(provider);
            
            redirectAttributes.addFlashAttribute("message", "Cập nhật nhà vận chuyển thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/shipping-providers";
    }
    
    /**
     * Toggle shipping provider status
     */
    @PostMapping("/shipping-providers/{id}/toggle")
    public String toggleShippingProviderStatus(@PathVariable String id,
                                              RedirectAttributes redirectAttributes) {
        try {
            vn.iotstar.repository.ShippingProviderRepository repo = 
                (vn.iotstar.repository.ShippingProviderRepository) 
                org.springframework.beans.factory.BeanFactoryUtils.beanOfTypeIncludingAncestors(
                    org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext(),
                    vn.iotstar.repository.ShippingProviderRepository.class);
            
            Optional<vn.iotstar.entity.ShippingProvider> providerOpt = repo.findById(id);
            if (!providerOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy nhà vận chuyển!");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/shipping-providers";
            }
            
            vn.iotstar.entity.ShippingProvider provider = providerOpt.get();
            provider.setIsActive(!provider.getIsActive());
            repo.save(provider);
            
            String status = provider.getIsActive() ? "kích hoạt" : "vô hiệu hóa";
            redirectAttributes.addFlashAttribute("message", "Đã " + status + " nhà vận chuyển thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/shipping-providers";
    }
    
    /**
     * Delete shipping provider
     */
    @PostMapping("/shipping-providers/{id}/delete")
    public String deleteShippingProvider(@PathVariable String id,
                                        RedirectAttributes redirectAttributes) {
        try {
            vn.iotstar.repository.ShippingProviderRepository repo = 
                (vn.iotstar.repository.ShippingProviderRepository) 
                org.springframework.beans.factory.BeanFactoryUtils.beanOfTypeIncludingAncestors(
                    org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext(),
                    vn.iotstar.repository.ShippingProviderRepository.class);
            
            Optional<vn.iotstar.entity.ShippingProvider> providerOpt = repo.findById(id);
            if (!providerOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy nhà vận chuyển!");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/shipping-providers";
            }
            
            repo.deleteById(id);
            
            redirectAttributes.addFlashAttribute("message", "Xóa nhà vận chuyển thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/admin/shipping-providers";
    }
    
    // ==================== PROMOTION MANAGEMENT (Admin view all promotions) ====================
    
    /**
     * View all promotions from all stores
     */
    @GetMapping("/promotions")
    public String allPromotions(@RequestParam(required = false) String search,
                               @RequestParam(required = false) String storeId,
                               Model model,
                               Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        vn.iotstar.repository.PromotionRepository promotionRepo = 
            (vn.iotstar.repository.PromotionRepository) 
            org.springframework.beans.factory.BeanFactoryUtils.beanOfTypeIncludingAncestors(
                org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext(),
                vn.iotstar.repository.PromotionRepository.class);
        
        List<vn.iotstar.entity.Promotion> promotions;
        if (storeId != null && !storeId.isEmpty()) {
            promotions = promotionRepo.findByStoreIdOrderByCreatedAtDesc(storeId);
            model.addAttribute("selectedStoreId", storeId);
        } else {
            promotions = promotionRepo.findAll();
            promotions.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
        }
        
        model.addAttribute("promotions", promotions);
        model.addAttribute("totalPromotions", promotions.size());
        
        // Get all stores for filter
        List<Store> stores = storeRepository.findAll();
        model.addAttribute("stores", stores);
        
        // Count active promotions
        long activeCount = promotions.stream()
            .filter(p -> p.getIsActive() && 
                        p.getStartDate().isBefore(LocalDateTime.now()) && 
                        p.getEndDate().isAfter(LocalDateTime.now()))
            .count();
        model.addAttribute("activePromotions", activeCount);
        
        return "admin/promotions";
    }
}
