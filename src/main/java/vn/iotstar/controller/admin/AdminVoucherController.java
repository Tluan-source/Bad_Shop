package vn.iotstar.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.admin.VoucherDTO;
import vn.iotstar.entity.Voucher;
import vn.iotstar.service.VoucherService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for Admin to manage Vouchers (System-wide discount codes)
 */
@Controller
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminVoucherController {
    
    private final VoucherService voucherService;
    
    /**
     * Display voucher management page
     */
    @GetMapping
    public String voucherManagement(Model model) {
        List<Voucher> vouchers = voucherService.getAllVouchers();
        List<VoucherDTO> voucherDTOs = vouchers.stream()
            .map(VoucherDTO::fromEntity)
            .collect(Collectors.toList());
        
        VoucherService.VoucherStats stats = voucherService.getStatistics();
        
        model.addAttribute("vouchers", voucherDTOs);
        model.addAttribute("stats", stats);
        return "admin/vouchers";
    }
    
    /**
     * Show create voucher form
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("voucher", new VoucherDTO());
        model.addAttribute("discountTypes", Voucher.DiscountType.values());
        return "admin/vouchers/create";
    }
    
    /**
     * Create new voucher
     */
    @PostMapping("/create")
    public String createVoucher(
            @RequestParam String code,
            @RequestParam String description,
            @RequestParam Voucher.DiscountType discountType,
            @RequestParam BigDecimal discountValue,
            @RequestParam(required = false) BigDecimal maxDiscount,
            @RequestParam(required = false) BigDecimal minOrderValue,
            @RequestParam Integer quantity,
            @RequestParam String startDate,
            @RequestParam String endDate,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Voucher voucher = new Voucher();
            voucher.setCode(code);
            voucher.setDescription(description);
            voucher.setDiscountType(discountType);
            voucher.setDiscountValue(discountValue);
            voucher.setMaxDiscount(maxDiscount);
            voucher.setMinOrderValue(minOrderValue);
            voucher.setQuantity(quantity);
            voucher.setIsActive(true);
            
            // Convert string dates to LocalDateTime
            voucher.setStartDate(LocalDate.parse(startDate).atStartOfDay());
            voucher.setEndDate(LocalDate.parse(endDate).atTime(23, 59, 59));
            
            voucherService.createVoucher(voucher);
            
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Tạo voucher thành công!");
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("messageType", "danger");
            redirectAttributes.addFlashAttribute("message", "Lỗi khi tạo voucher: " + e.getMessage());
            return "redirect:/admin/vouchers";
        }
    }
    
    /**
     * Show edit voucher form
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model) {
        try {
            Voucher voucher = voucherService.findByCode(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
            model.addAttribute("voucher", VoucherDTO.fromEntity(voucher));
            model.addAttribute("discountTypes", Voucher.DiscountType.values());
            return "admin/vouchers/edit";
        } catch (Exception e) {
            return "redirect:/admin/vouchers?error=" + e.getMessage();
        }
    }
    
    /**
     * Update voucher
     */
    @PostMapping("/edit/{id}")
    public String updateVoucher(@PathVariable String id, 
                                @ModelAttribute VoucherDTO voucherDTO, 
                                Model model) {
        try {
            Voucher voucher = voucherDTO.toEntity();
            voucherService.updateVoucher(id, voucher);
            return "redirect:/admin/vouchers?success=updated";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("voucher", voucherDTO);
            return "admin/vouchers/edit";
        }
    }
    
    /**
     * Delete voucher
     */
    @PostMapping("/{id}/delete")
    public String deleteVoucher(@PathVariable String id) {
        try {
            voucherService.deleteVoucher(id);
            return "redirect:/admin/vouchers?success=deleted";
        } catch (Exception e) {
            return "redirect:/admin/vouchers?error=" + e.getMessage();
        }
    }
    
    /**
     * Toggle voucher active status
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleActive(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Voucher voucher = voucherService.toggleActive(id);
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", 
                voucher.getIsActive() ? "Đã kích hoạt voucher!" : "Đã vô hiệu hóa voucher!");
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("messageType", "danger");
            redirectAttributes.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            return "redirect:/admin/vouchers";
        }
    }
    
    /**
     * Search vouchers
     */
    @GetMapping("/search")
    public String searchVouchers(@RequestParam String keyword, Model model) {
        List<Voucher> vouchers = voucherService.searchVouchers(keyword);
        List<VoucherDTO> voucherDTOs = vouchers.stream()
            .map(VoucherDTO::fromEntity)
            .collect(Collectors.toList());
        
        model.addAttribute("vouchers", voucherDTOs);
        model.addAttribute("keyword", keyword);
        return "admin/vouchers";
    }
    
    /**
     * Validate voucher code (API for user)
     */
    @PostMapping("/api/validate")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> validateVoucher(@RequestBody Map<String, Object> request) {
        try {
            String code = (String) request.get("code");
            Double orderAmount = ((Number) request.get("orderAmount")).doubleValue();
            
            boolean valid = voucherService.validateVoucher(code, 
                java.math.BigDecimal.valueOf(orderAmount));
            
            if (valid) {
                Voucher voucher = voucherService.findByCode(code).get();
                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("voucher", VoucherDTO.fromEntity(voucher));
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.ok(Map.of("valid", false, 
                    "message", "Voucher không hợp lệ hoặc không đủ điều kiện"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get available vouchers for users
     */
    @GetMapping("/api/available")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAvailableVouchers() {
        try {
            List<Voucher> vouchers = voucherService.getAvailableVouchers();
            List<VoucherDTO> voucherDTOs = vouchers.stream()
                .map(VoucherDTO::fromEntity)
                .collect(Collectors.toList());
            return ResponseEntity.ok(voucherDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
