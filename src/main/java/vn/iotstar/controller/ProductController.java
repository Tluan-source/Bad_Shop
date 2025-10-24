package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.iotstar.entity.Product;
import vn.iotstar.repository.ProductRepository;

import java.util.Optional;

/**
 * Controller for public product views (guest access)
 */
@Controller
public class ProductController {
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Display product details
     */
    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable String id, Model model) {
        Optional<Product> productOpt = productRepository.findById(id);
        
        if (productOpt.isEmpty() || !productOpt.get().getIsActive()) {
            return "redirect:/products";
        }
        
        Product product = productOpt.get();
        model.addAttribute("product", product);
        return "user/product-detail";
    }
}
