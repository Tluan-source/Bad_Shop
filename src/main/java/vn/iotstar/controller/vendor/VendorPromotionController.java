package vn.iotstar.controller.vendor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.Promotion;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.User;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.service.PromotionService;
import vn.iotstar.service.UserService;

import java.util.List;
import java.util.Map;

/**
 * Controller for Vendor to manage Promotions (Store-specific promotions)
 */
@Controller
@RequestMapping("/vendor/promotions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDOR')")
public class VendorPromotionController {
    
    private final PromotionService promotionService;
    private final StoreRepository storeRepository;
    private final UserService userService;
    
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
        return stores.get(0); // Get the first store (vendor usually has only one store)
    }
    
    /**
     * Display promotion management page
     */
    @GetMapping
    public String promotionManagement(Authentication auth, Model model) {
        Store store = getCurrentStore(auth);
        List<Promotion> promotions = promotionService.getPromotionsByStore(store.getId());
        
        model.addAttribute("promotions", promotions);
        model.addAttribute("store", store);
        return "vendor/promotions/list";
    }
    
    /**
     * Show create promotion form
     */
    @GetMapping("/create")
    public String showCreateForm(Authentication auth, Model model) {
        Store store = getCurrentStore(auth);
        
        model.addAttribute("promotion", new Promotion());
        model.addAttribute("discountTypes", Promotion.DiscountType.values());
        model.addAttribute("appliesTo", Promotion.AppliesTo.values());
        model.addAttribute("store", store);
        
        return "vendor/promotions/create";
    }
    
    /**
     * Create new promotion
     */
    @PostMapping("/create")
    public String createPromotion(Authentication auth,
                                  @ModelAttribute Promotion promotion,
                                  Model model) {
        try {
            Store store = getCurrentStore(auth);
            promotionService.createPromotion(promotion, store);
            return "redirect:/vendor/promotions?success=created";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("promotion", promotion);
            return "vendor/promotions/create";
        }
    }
    
    /**
     * Show edit promotion form
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(Authentication auth,
                              @PathVariable String id,
                              Model model) {
        try {
            Store store = getCurrentStore(auth);
            Promotion promotion = promotionService.getPromotionById(id, store.getId());
            
            model.addAttribute("promotion", promotion);
            model.addAttribute("discountTypes", Promotion.DiscountType.values());
            model.addAttribute("appliesTo", Promotion.AppliesTo.values());
            model.addAttribute("store", store);
            
            return "vendor/promotions/edit";
        } catch (Exception e) {
            return "redirect:/vendor/promotions?error=" + e.getMessage();
        }
    }
    
    /**
     * Update promotion
     */
    @PostMapping("/edit/{id}")
    public String updatePromotion(Authentication auth,
                                  @PathVariable String id,
                                  @ModelAttribute Promotion promotion,
                                  Model model) {
        try {
            Store store = getCurrentStore(auth);
            promotion.setStore(store);
            promotionService.updatePromotion(id, promotion);
            return "redirect:/vendor/promotions?success=updated";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("promotion", promotion);
            return "vendor/promotions/edit";
        }
    }
    
    /**
     * Delete promotion
     */
    @PostMapping("/delete/{id}")
    public String deletePromotion(Authentication auth,
                                  @PathVariable String id) {
        try {
            Store store = getCurrentStore(auth);
            // Verify ownership before deleting
            promotionService.getPromotionById(id, store.getId());
            promotionService.deletePromotion(id);
            return "redirect:/vendor/promotions?success=deleted";
        } catch (Exception e) {
            return "redirect:/vendor/promotions?error=" + e.getMessage();
        }
    }
    
    /**
     * View promotion detail
     */
    @GetMapping("/{id}")
    public String viewPromotion(Authentication auth,
                               @PathVariable String id,
                               Model model) {
        try {
            Store store = getCurrentStore(auth);
            Promotion promotion = promotionService.getPromotionById(id, store.getId());
            
            model.addAttribute("promotion", promotion);
            model.addAttribute("store", store);
            
            return "vendor/promotions/detail";
        } catch (Exception e) {
            return "redirect:/vendor/promotions?error=" + e.getMessage();
        }
    }
    
    /**
     * Toggle promotion active status
     */
    @PostMapping("/toggle/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleActive(Authentication auth,
                                         @PathVariable String id) {
        try {
            Store store = getCurrentStore(auth);
            Promotion promotion = promotionService.toggleActive(id, store.getId());
            return ResponseEntity.ok(promotion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get active promotions (API)
     */
    @GetMapping("/api/active")
    @ResponseBody
    public ResponseEntity<?> getActivePromotions(Authentication auth) {
        try {
            Store store = getCurrentStore(auth);
            List<Promotion> promotions = promotionService.getCurrentPromotions(store.getId());
            return ResponseEntity.ok(promotions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Add products to specific promotion
     */
    @PostMapping("/{id}/products")
    @ResponseBody
    public ResponseEntity<?> addProductsToPromotion(Authentication auth,
                                                    @PathVariable String id,
                                                    @RequestBody Map<String, List<String>> request) {
        try {
            Store store = getCurrentStore(auth);
            // Verify ownership
            promotionService.getPromotionById(id, store.getId());
            
            List<String> productIds = request.get("productIds");
            Promotion promotion = promotionService.addProductsToPromotion(id, productIds);
            
            return ResponseEntity.ok(promotion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
