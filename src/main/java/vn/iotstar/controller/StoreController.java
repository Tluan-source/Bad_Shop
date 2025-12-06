package vn.iotstar.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import vn.iotstar.entity.Product;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.StoreRepository;

/**
 * Controller for public store views (guest access)
 */
@Controller
public class StoreController {
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Display all active stores
     */
    @GetMapping("/stores")
    public String listStores(@RequestParam(required = false) String search, Model model) {
        List<Store> stores;
        
        if (search != null && !search.trim().isEmpty()) {
            stores = storeRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(search);
            model.addAttribute("search", search);
        } else {
            stores = storeRepository.findByIsActiveTrue();
        }
        
        model.addAttribute("stores", stores);
        return "user/stores";
    }
    
    /**
     * Display store details with products
     */
    @GetMapping("/stores/{id}")
    public String storeDetail(@PathVariable String id, Model model) {
        Optional<Store> storeOpt = storeRepository.findById(id);
        
        if (storeOpt.isEmpty() || !storeOpt.get().getIsActive()) {
            return "redirect:/stores";
        }
        
        Store store = storeOpt.get();
        // Chỉ lấy sản phẩm đã duyệt và đang bán
        List<Product> products = productRepository.findByStoreIdAndApprovedAndSelling(id);
        
        model.addAttribute("store", store);
        model.addAttribute("products", products);
        return "user/store-detail";
    }
}
