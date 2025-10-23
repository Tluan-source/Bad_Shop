package vn.iotstar.controller;

import vn.iotstar.entity.Product;
import vn.iotstar.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

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
    public String products(Model model) {
        List<Product> allProducts = productService.getAllActiveProducts();
        model.addAttribute("products", allProducts);
        return "user/products";
    }
}