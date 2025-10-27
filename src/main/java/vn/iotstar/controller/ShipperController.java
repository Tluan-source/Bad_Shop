package vn.iotstar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Shipment;
import vn.iotstar.entity.Shipment.ShipmentStatus;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.ShipmentRepository;
import vn.iotstar.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/shipper")
@RequiredArgsConstructor
public class ShipperController {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        
        Pageable pageable = PageRequest.of(page, 3, Sort.by("createdAt").descending());
        boolean showAll = (status == null || status.isEmpty());

        Page<Shipment> pending = Page.empty();
        Page<Shipment> delivering = Page.empty();
        Page<Shipment> delivered = Page.empty();
        Page<Shipment> failed = Page.empty();

        // ✅ Không lọc trạng thái → hiển thị cả 4 danh sách
        if (showAll) {
            pending = getFilteredShipments(null, ShipmentStatus.ACCEPTED, keyword, fromDate, toDate, pageable);
            delivering = getFilteredShipments(shipperId, ShipmentStatus.DELIVERING, keyword, fromDate, toDate, pageable);
            delivered = getFilteredShipments(shipperId, ShipmentStatus.DELIVERED, keyword, fromDate, toDate, pageable);
            failed = getFilteredShipments(shipperId, ShipmentStatus.FAILED, keyword, fromDate, toDate, pageable);
        }
        // ✅ Có chọn trạng thái cụ thể → chỉ hiển thị bảng tương ứng
        else {
            ShipmentStatus selectedStatus = ShipmentStatus.valueOf(status);
            switch (selectedStatus) {
                case ACCEPTED -> pending = getFilteredShipments(null, selectedStatus, keyword, fromDate, toDate, pageable);
                case DELIVERING -> delivering = getFilteredShipments(shipperId, selectedStatus, keyword, fromDate, toDate, pageable);
                case DELIVERED -> delivered = getFilteredShipments(shipperId, selectedStatus, keyword, fromDate, toDate, pageable);
                case FAILED -> failed = getFilteredShipments(shipperId, selectedStatus, keyword, fromDate, toDate, pageable);
            }
        }

        int totalPages = Math.max(
                Math.max(pending.getTotalPages(), delivering.getTotalPages()),
                Math.max(delivered.getTotalPages(), failed.getTotalPages())
        );

        model.addAttribute("pendingShipments", pending);
        model.addAttribute("delivering", delivering);
        model.addAttribute("delivered", delivered);
        model.addAttribute("failed", failed);
        model.addAttribute("status", status);
        model.addAttribute("showAll", showAll);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("username", shipperId);

        return "shipper/dashboard";
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

    /** ✅ Nhận đơn (assign shipper & đổi trạng thái) */
    @PostMapping("/accept/{id}")
    public String acceptShipment(@PathVariable String id, Authentication auth) {
        Shipment shipment = shipmentRepository.findById(id).orElse(null);
        if (shipment != null) {
            // Lấy user ID thực sự từ database
            String email = auth.getName();
            User shipper = userRepository.findByEmail(email).orElseThrow();
            
            shipment.setStatus(ShipmentStatus.DELIVERING);
            shipment.setAcceptedAt(LocalDateTime.now());
            shipment.setShipper(shipper);

            shipmentRepository.save(shipment);

            Order order = shipment.getOrder();
            order.setStatus(Order.OrderStatus.SHIPPED);
            orderRepository.save(order);
        }
        return "redirect:/shipper/dashboard";
    }

    /** 🟢 Giao hàng thành công */
    @PostMapping("/delivered/{id}")
    public String delivered(@PathVariable String id) {
        Shipment shipment = shipmentRepository.findById(id).orElse(null);
        if (shipment != null) {
            shipment.setStatus(ShipmentStatus.DELIVERED);
            shipment.setDeliveredAt(LocalDateTime.now());
            shipmentRepository.save(shipment);

            Order order = shipment.getOrder();
            order.setStatus(Order.OrderStatus.DELIVERED);
            orderRepository.save(order);
        }
        return "redirect:/shipper/dashboard";
    }

    /** 🔴 Giao hàng thất bại (ghi chú nguyên nhân) */
    @PostMapping("/failed/{id}")
    public String failed(@PathVariable String id, @RequestParam(value = "note", required = false) String note) {
        Shipment shipment = shipmentRepository.findById(id).orElse(null);
        if (shipment != null) {
            shipment.setStatus(ShipmentStatus.FAILED);
            shipment.setDeliveredAt(LocalDateTime.now());
            shipment.setNote(note);
            shipmentRepository.save(shipment);

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
