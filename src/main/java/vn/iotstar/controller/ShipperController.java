package vn.iotstar.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Shipment;
import vn.iotstar.entity.Shipment.ShipmentStatus;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.ShipmentRepository;
import vn.iotstar.repository.UserRepository;

@Controller
@RequestMapping("/shipper")
@RequiredArgsConstructor
public class ShipperController {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;
    
    /** 🏠 Trang dashboard chính của Shipper */
    @GetMapping("/dashboard")
    public String dashboard(
            Model model,
            Authentication authentication,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page
    ) {
        // Lấy user ID thực sự từ database (U100, U101,...) thay vì dùng email
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        String shipperId = currentUser.getId();
        
        // Kiểm tra xem shipper có thuộc nhà vận chuyển nào không
        if (currentUser.getShippingProvider() == null) {
            model.addAttribute("error", "Bạn chưa được gán vào nhà vận chuyển nào. Vui lòng liên hệ admin.");
            return "shipper/dashboard";
        }
        
        String shippingProviderId = currentUser.getShippingProvider().getId();
        String shippingProviderName = currentUser.getShippingProvider().getName();
        
        // Debug logging
        System.out.println("=== SHIPPER DEBUG ===");
        System.out.println("Shipper ID: " + shipperId);
        System.out.println("Shipper Email: " + email);
        System.out.println("Shipping Provider ID: " + shippingProviderId);
        System.out.println("Shipping Provider Name: " + shippingProviderName);
        
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        
        // 🔄 Xử lý status cũ (backward compatibility)
        if ("ACCEPTED".equals(status) || "ASSIGNED".equals(status)) {
            status = "PROCESSING";
        }
        
        // 📊 Tính tổng số cho các trạng thái
        // Chờ nhận = Orders có status PROCESSING VÀ shipping_provider_id trùng với shipper
        long totalPending = orderRepository.countByStatusAndShippingProviderId(
                Order.OrderStatus.PROCESSING, shippingProviderId);
        long totalDelivering = shipmentRepository.countByShipper_IdAndStatus(shipperId, ShipmentStatus.DELIVERING);
        long totalDelivered = shipmentRepository.countByShipper_IdAndStatus(shipperId, ShipmentStatus.DELIVERED);
        long totalFailed = shipmentRepository.countByShipper_IdAndStatus(shipperId, ShipmentStatus.FAILED);

        Page<Order> pendingOrders = Page.empty();
        Page<Shipment> delivering = Page.empty();
        Page<Shipment> delivered = Page.empty();
        Page<Shipment> failed = Page.empty();
        int totalPages = 0;

        // ✅ Có chọn trạng thái cụ thể → chỉ hiển thị bảng tương ứng
        if (status != null && !status.isEmpty()) {
            switch (status) {
                case "PROCESSING" -> {
                    // Load Orders có status PROCESSING và shipping_provider_id trùng
                    pendingOrders = getFilteredPendingOrders(shippingProviderId, keyword, fromDate, toDate, pageable);
                    totalPages = pendingOrders.getTotalPages();
                }
                case "DELIVERING" -> {
                    delivering = getFilteredShipments(shipperId, ShipmentStatus.DELIVERING, keyword, fromDate, toDate, pageable);
                    totalPages = delivering.getTotalPages();
                }
                case "DELIVERED" -> {
                    delivered = getFilteredShipments(shipperId, ShipmentStatus.DELIVERED, keyword, fromDate, toDate, pageable);
                    totalPages = delivered.getTotalPages();
                }
                case "FAILED" -> {
                    failed = getFilteredShipments(shipperId, ShipmentStatus.FAILED, keyword, fromDate, toDate, pageable);
                    totalPages = failed.getTotalPages();
                }
            }
        }
        // ✅ Không chọn trạng thái → mặc định hiển thị tab "Chờ nhận"
        else {
            pendingOrders = getFilteredPendingOrders(shippingProviderId, keyword, fromDate, toDate, pageable);
            totalPages = pendingOrders.getTotalPages();
            status = "PROCESSING"; // Set default active tab
        }

        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("delivering", delivering);
        model.addAttribute("delivered", delivered);
        model.addAttribute("failed", failed);
        model.addAttribute("totalPending", totalPending);
        model.addAttribute("totalDelivering", totalDelivering);
        model.addAttribute("totalDelivered", totalDelivered);
        model.addAttribute("totalFailed", totalFailed);
        model.addAttribute("status", status);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("username", shipperId);
        model.addAttribute("shippingProviderName", shippingProviderName);
        model.addAttribute("currentUser", currentUser);

        return "shipper/dashboard";
    }
    
    /** 📦 Hàm lọc Orders chờ nhận (status = PROCESSING) - CHỈ LẤY ĐƠN THUỘC NHÀ VẬN CHUYỂN CỦA SHIPPER */
    private Page<Order> getFilteredPendingOrders(
            String shippingProviderId,
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        System.out.println("=== getFilteredPendingOrders DEBUG ===");
        System.out.println("Shipping Provider ID: " + shippingProviderId);
        System.out.println("Keyword: " + keyword);
        System.out.println("From Date: " + fromDate);
        System.out.println("To Date: " + toDate);
        
        Page<Order> result;
        
        // 🔍 Lọc theo thời gian
        if (fromDate != null && toDate != null) {
            System.out.println("Query: findByStatusAndShippingProviderIdAndCreatedAtBetween");
            result = orderRepository.findByStatusAndShippingProviderIdAndCreatedAtBetween(
                    Order.OrderStatus.PROCESSING,
                    shippingProviderId,
                    fromDate.atStartOfDay(), 
                    toDate.atTime(23, 59, 59), 
                    pageable);
        }
        // 🔍 Lọc theo keyword
        else if (!keyword.isEmpty()) {
            System.out.println("Query: searchByStatusAndShippingProviderIdAndKeyword");
            result = orderRepository.searchByStatusAndShippingProviderIdAndKeyword(
                    Order.OrderStatus.PROCESSING,
                    shippingProviderId,
                    keyword, 
                    pageable);
        }
        // Mặc định lọc theo shipping_provider_id
        else {
            System.out.println("Query: findByStatusAndShippingProviderId");
            result = orderRepository.findByStatusAndShippingProviderId(
                    Order.OrderStatus.PROCESSING,
                    shippingProviderId,
                    pageable);
        }
        
        System.out.println("Found " + result.getTotalElements() + " orders");
        result.getContent().forEach(order -> {
            System.out.println("  - Order ID: " + order.getId() + 
                             ", Shipping Provider: " + (order.getShippingProvider() != null ? order.getShippingProvider().getName() : "NULL"));
        });
        
        return result;
    }

    /** 📦 Hàm lọc / tìm kiếm / phân trang */
    private Page<Shipment> getFilteredShipments(
            String shipperId,
            ShipmentStatus status,
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        // 🔍 Lọc theo thời gian
        if (fromDate != null && toDate != null) {
            if (shipperId == null) {
                return shipmentRepository.findByStatusAndShipperIsNullAndCreatedAtBetween(
                        status, fromDate.atStartOfDay(), toDate.atTime(23, 59, 59), pageable);
            } else {
                return shipmentRepository.findByShipper_IdAndStatusAndCreatedAtBetween(
                        shipperId, status, fromDate.atStartOfDay(), toDate.atTime(23, 59, 59), pageable);
            }
        }
        // 🔍 Lọc theo keyword
        else if (!keyword.isEmpty()) {
            if (shipperId == null) {
                return shipmentRepository.searchByStatusAndKeywordAndShipperIsNull(status, keyword, pageable);
            } else {
                return shipmentRepository.searchByShipperAndStatusAndKeyword(shipperId, status, keyword, pageable);
            }
        }
        // Mặc định không có điều kiện đặc biệt
        else {
            if (shipperId == null) {
                return shipmentRepository.findByStatusAndShipperIsNull(status, pageable);
            } else {
                return shipmentRepository.findByShipper_IdAndStatus(shipperId, status, pageable);
            }
        }
    }

    /** ✅ Nhận đơn (tạo shipment mới hoặc cập nhật shipment có sẵn & đổi trạng thái) */
    @PostMapping("/accept/{id}")
    public String acceptOrder(@PathVariable String id, Authentication auth) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null && order.getStatus() == Order.OrderStatus.PROCESSING) {
            String email = auth.getName();
            User shipper = userRepository.findByEmail(email).orElseThrow();

            Shipment shipment = order.getShipment();
            if (shipment == null) {
                shipment = new Shipment();
                shipment.setId(java.util.UUID.randomUUID().toString());
                shipment.setOrder(order);
                shipment.setShippingFee(order.getShippingFee());
            }

            shipment.setShipper(shipper);
            shipment.setStatus(ShipmentStatus.DELIVERING);
            shipment.setAssignedAt(LocalDateTime.now());
            shipmentRepository.save(shipment);

            // ✅ Order -> DELIVERING (đang giao)
            order.setStatus(Order.OrderStatus.DELIVERING);
            orderRepository.save(order);
        }
        return "redirect:/shipper/dashboard";
    }


    /** 🟢 Giao hàng thành công */
    @PostMapping("/delivered/{id}")
    public String delivered(@PathVariable String id,
                            @RequestParam("image") MultipartFile image,
                            Authentication auth) {
    
        Shipment shipment = shipmentRepository.findById(id).orElse(null);
        if (shipment != null) {
            String email = auth.getName();
            User currentUser = userRepository.findByEmail(email).orElseThrow();
            if (!shipment.getShipper().getId().equals(currentUser.getId())) {
                return "redirect:/shipper/dashboard?error=not_allowed";
            }
    
            try {
                Map uploadResult = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap());
                String imageUrl = uploadResult.get("secure_url").toString();
    
                shipment.setDeliveryImageUrl(imageUrl);
                shipment.setStatus(ShipmentStatus.DELIVERED);
                shipment.setDeliveredAt(LocalDateTime.now());
                shipmentRepository.save(shipment);
    
                Order order = shipment.getOrder();
    
                if (Boolean.TRUE.equals(order.getIsPaidBefore())) {
                    // ✅ Thanh toán trước → hoàn tất luôn
                    order.setStatus(Order.OrderStatus.DELIVERED);
                    order.setConfirmedByUserAt(LocalDateTime.now());
                } else {
                    // ✅ COD → user cần xác nhận
                    order.setStatus(Order.OrderStatus.AWAITING_CONFIRMATION);
                }
    
                orderRepository.save(order);
    
            } catch (Exception e) {
                System.out.println("Upload ảnh lỗi: " + e.getMessage());
            }
        }
        return "redirect:/shipper/dashboard";
    }
    


    /** 🔴 Giao hàng thất bại (ghi chú nguyên nhân) */
    @PostMapping("/failed/{id}")
    public String failed(@PathVariable String id, @RequestParam(value = "note", required = false) String note) {
        Shipment shipment = shipmentRepository.findById(id).orElse(null);
        if (shipment != null) {
            // Shipment -> FAILED
            shipment.setStatus(ShipmentStatus.FAILED);
            shipment.setDeliveredAt(LocalDateTime.now());
            shipment.setNote(note);
            shipmentRepository.save(shipment);

            // Order -> CANCELLED
            Order order = shipment.getOrder();
            order.setStatus(Order.OrderStatus.CANCELLED);
            orderRepository.save(order);
        }
        return "redirect:/shipper/dashboard";
    }

    /** 🔍 Xem chi tiết đơn giao */
    @GetMapping("/shipment/{id}")
    public String shipmentDetail(@PathVariable String id, Model model) {
        Shipment shipment = shipmentRepository.findById(id).orElse(null);
        if (shipment == null) return "redirect:/shipper/dashboard";

        model.addAttribute("shipment", shipment);
        model.addAttribute("order", shipment.getOrder());
        model.addAttribute("orderItems", shipment.getOrder().getOrderItems());
        return "shipper/shipment_detail";
    }
    
    /** 🔍 Xem chi tiết đơn hàng (Order chưa có Shipment) */
    @GetMapping("/order/{id}")
    public String orderDetail(@PathVariable String id, Model model) {
        Order order = orderRepository.findByIdWithItems(id);
        if (order == null) return "redirect:/shipper/dashboard";

        model.addAttribute("order", order);
        model.addAttribute("orderItems", order.getOrderItems());
        model.addAttribute("shipment", order.getShipment()); // Có thể null
        return "shipper/order_detail";
    }

    /** 📊 Trang báo cáo thu nhập */
    @GetMapping("/report")
    public String report(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String period
    ) {
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        String shipperId = currentUser.getId();

        // Xử lý period (TODAY, WEEK, MONTH, YEAR)
        if (period != null && !period.isEmpty()) {
            LocalDate now = LocalDate.now();
            switch (period) {
                case "TODAY" -> {
                    fromDate = now;
                    toDate = now;
                }
                case "WEEK" -> {
                    fromDate = now.minusDays(7);
                    toDate = now;
                }
                case "MONTH" -> {
                    fromDate = now.withDayOfMonth(1);
                    toDate = now;
                }
                case "YEAR" -> {
                    fromDate = now.withDayOfYear(1);
                    toDate = now;
                }
            }
        }

        // Lấy danh sách shipments trong khoảng thời gian
        List<Shipment> shipmentList;
        if (fromDate != null && toDate != null) {
            shipmentList = shipmentRepository.findByShipper_IdAndCreatedAtBetween(
                shipperId, 
                fromDate.atStartOfDay(), 
                toDate.atTime(23, 59, 59)
            );
        } else {
            shipmentList = shipmentRepository.findByShipper_Id(shipperId);
        }

        // Tính toán thống kê
        long totalShipments = shipmentList.size();
        long successCount = shipmentList.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .count();
        long failedCount = shipmentList.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.FAILED)
            .count();
        long deliveringCount = shipmentList.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERING)
            .count();

        double totalEarnings = shipmentList.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .mapToDouble(s -> s.getShippingFee().doubleValue())
            .sum();

        // Dữ liệu cho biểu đồ theo ngày (7 ngày gần nhất)
        String[] dailyLabels = new String[7];
        int[] dailyData = new int[7];
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dailyLabels[6 - i] = date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(23, 59, 59);
            dailyData[6 - i] = (int) shipmentList.stream()
                .filter(s -> s.getCreatedAt().isAfter(start) && s.getCreatedAt().isBefore(end))
                .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                .count();
        }

        model.addAttribute("shipmentList", shipmentList);
        model.addAttribute("totalShipments", totalShipments);
        model.addAttribute("successCount", successCount);
        model.addAttribute("failedCount", failedCount);
        model.addAttribute("deliveringCount", deliveringCount);
        model.addAttribute("totalEarnings", totalEarnings);
        model.addAttribute("dailyLabels", String.format("['%s']", String.join("','", dailyLabels)));
        model.addAttribute("dailyData", java.util.Arrays.toString(dailyData));
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("period", period);

        return "shipper/report";
    }

    /** 👤 Trang profile */
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        // Thống kê nhanh
        List<Shipment> allShipments = shipmentRepository.findByShipper_Id(user.getId());
        long totalShipments = allShipments.size();
        long successCount = allShipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .count();
        long failedCount = allShipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.FAILED)
            .count();
        double totalEarnings = allShipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .mapToDouble(s -> s.getShippingFee().doubleValue())
            .sum();

        // Hoạt động gần đây (10 đơn gần nhất)
        List<Shipment> recentActivities = shipmentRepository.findTop10ByShipper_IdOrderByCreatedAtDesc(user.getId());

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalShipments", totalShipments);
        stats.put("successCount", successCount);
        stats.put("failedCount", failedCount);
        stats.put("totalEarnings", totalEarnings);

        model.addAttribute("user", user);
        model.addAttribute("stats", stats);
        model.addAttribute("recentActivities", recentActivities);

        return "shipper/profile";
    }

    /** 💾 Cập nhật profile */
    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String currentEmail = authentication.getName();
            User user = userRepository.findByEmail(currentEmail).orElseThrow();

            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("message", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/shipper/profile";
    }

    /** ⚙️ Trang settings */
    @GetMapping("/settings")
    public String settings(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        
        model.addAttribute("user", user);
        model.addAttribute("lastLogin", user.getCreatedAt());
        
        return "shipper/settings";
    }

    /** 🔑 Đổi mật khẩu */
    @PostMapping("/settings/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Kiểm tra mật khẩu mới và xác nhận có khớp không
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận không khớp!");
                return "redirect:/shipper/settings";
            }

            // Kiểm tra độ dài mật khẩu mới
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự!");
                return "redirect:/shipper/settings";
            }

            // Lấy thông tin user hiện tại
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            // Kiểm tra mật khẩu hiện tại có đúng không
            if (!passwordEncoder.matches(currentPassword, user.getHashedPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
                return "redirect:/shipper/settings";
            }

            // Mã hóa và cập nhật mật khẩu mới
            String newHashedPassword = passwordEncoder.encode(newPassword);
            user.setHashedPassword(newHashedPassword);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/shipper/settings";
    }
}
