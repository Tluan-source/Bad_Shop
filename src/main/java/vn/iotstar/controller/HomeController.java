package vn.iotstar.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import vn.iotstar.entity.Product;
import vn.iotstar.service.ProductService;

@Controller
public class HomeController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping("/")
    public String home(Model model) {
        List<Product> topProducts = productService.getTop10Products();
        model.addAttribute("topProducts", topProducts);
        return "user/index";
    }
    
    @GetMapping("/products")
    public String products(
        @RequestParam(value = "category", required = false) String categoryId,
        @RequestParam(value = "minPrice", required = false) Double minPrice,
        @RequestParam(value = "maxPrice", required = false) Double maxPrice,
        @RequestParam(value = "brand", required = false) String brand,
        @RequestParam(value = "sort", required = false) String sort,
        Model model){
    
        List<Product> productList;
    
        // Logic l?c s?n ph?m
        if (categoryId != null || minPrice != null || maxPrice != null || brand != null) {
            // G?i service v?i c?c filter
            productList = productService.filterProducts(categoryId, minPrice, maxPrice, brand);
        } else {
            // Hi?n th? t?t c?
            productList = productService.getAllActiveProducts();
        }
        
        // S?p x?p s?n ph?m
        if (sort != null && !sort.isEmpty()) {
            switch (sort) {
                case "price-asc":
                    productList.sort((p1, p2) -> {
                        BigDecimal price1 = p1.getPromotionalPrice() != null ? p1.getPromotionalPrice() : p1.getPrice();
                        BigDecimal price2 = p2.getPromotionalPrice() != null ? p2.getPromotionalPrice() : p2.getPrice();
                        return price1.compareTo(price2);
                    });
                    break;
                case "price-desc":
                    productList.sort((p1, p2) -> {
                        BigDecimal price1 = p1.getPromotionalPrice() != null ? p1.getPromotionalPrice() : p1.getPrice();
                        BigDecimal price2 = p2.getPromotionalPrice() != null ? p2.getPromotionalPrice() : p2.getPrice();
                        return price2.compareTo(price1);
                    });
                    break;
                case "name-asc":
                    productList.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
                    break;
                case "name-desc":
                    productList.sort((p1, p2) -> p2.getName().compareToIgnoreCase(p1.getName()));
                    break;
            }
        }
        
        model.addAttribute("products", productList);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("selectedSort", sort);
        return "user/products";
    }

    
    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable String id, Model model) {
        Optional<Product> productOpt = productService.getProductById(id);
        
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            
            // L?y s?n ph?m li?n quan (c?ng category)
            List<Product> relatedProducts = productService.getProductsByCategory(product.getCategory().getId());
            relatedProducts.removeIf(p -> p.getId().equals(id)); // Lo?i b? s?n ph?m hi?n t?i
            if (relatedProducts.size() > 4) {
                relatedProducts = relatedProducts.subList(0, 4); // Ch? l?y 4 s?n ph?m
            }
            
            model.addAttribute("product", product);
            model.addAttribute("relatedProducts", relatedProducts);
            return "user/product-detail";
        }
        
        return "redirect:/products";
    }
}
