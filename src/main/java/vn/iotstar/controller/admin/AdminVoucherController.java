package vn.iotstar.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.dto.admin.VoucherDTO;
import vn.iotstar.entity.Voucher;
import vn.iotstar.service.VoucherService;

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
        return "admin/vouchers/list";
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
    public String createVoucher(@ModelAttribute VoucherDTO voucherDTO, Model model) {
        try {
            Voucher voucher = voucherDTO.toEntity();
            voucherService.createVoucher(voucher);
            return "redirect:/admin/vouchers?success=created";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("voucher", voucherDTO);
            return "admin/vouchers/create";
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
    @PostMapping("/delete/{id}")
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
    @PostMapping("/toggle/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleActive(@PathVariable String id) {
        try {
            Voucher voucher = voucherService.toggleActive(id);
            return ResponseEntity.ok(VoucherDTO.fromEntity(voucher));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
        return "admin/vouchers/list";
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
