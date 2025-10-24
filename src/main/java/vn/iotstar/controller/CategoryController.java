package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

/**
 * Controller for public category views (guest access)
 */
@Controller
public class CategoryController {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Display all active categories
     */
    @GetMapping("/categories")
    public String listCategories(Model model) {
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByNameAsc();
        model.addAttribute("categories", categories);
        return "user/categories";
    }
    
    /**
     * Display products by category
     */
    @GetMapping("/categories/{id}")
    public String categoryDetail(@PathVariable String id, Model model) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        
        if (categoryOpt.isEmpty() || !categoryOpt.get().getIsActive()) {
            return "redirect:/categories";
        }
        
        Category category = categoryOpt.get();
        List<Product> products = productRepository.findByCategoryIdAndIsActiveTrueAndIsSellingTrue(id);
        
        model.addAttribute("category", category);
        model.addAttribute("products", products);
        return "user/category-detail";
    }
}
