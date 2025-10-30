package vn.iotstar.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.Transient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;

import vn.iotstar.entity.Product;
import vn.iotstar.entity.Review;
import vn.iotstar.entity.StyleValue;
import vn.iotstar.repository.StyleRepository;
import vn.iotstar.repository.StyleValueRepository;
import vn.iotstar.service.CartService;
import vn.iotstar.service.FavoriteService;
import vn.iotstar.service.ProductService;
import vn.iotstar.repository.ReviewRepository;

@Controller
public class HomeController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private StyleRepository styleRepository;
    
    @Autowired
    private StyleValueRepository styleValueRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewRepository reviewRepository;

    @Transient
    private List<String> styleValueNames;
    
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
    public String productDetail(@PathVariable String id, Model model, Authentication auth) {
        // 1️⃣ Kiểm tra đăng nhập
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        // 2️⃣ Lấy sản phẩm
        Optional<Product> productOpt = productService.getProductById(id);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();

            // 3️⃣ Sản phẩm liên quan
            List<Product> relatedProducts = productService.getProductsByCategory(product.getCategory().getId());
            relatedProducts.removeIf(p -> p.getId().equals(id));
            if (relatedProducts.size() > 4) {
                relatedProducts = relatedProducts.subList(0, 4);
            }

            // 4️⃣ Kiểm tra yêu thích
            boolean isFavorite = favoriteService.isFavorite(id);

            // 5️⃣ Parse styleValueIds
            Map<String, List<StyleValue>> styleMap = new HashMap<>();
            try {
                if (product.getStyleValueIds() != null && !product.getStyleValueIds().equals("[]")) {
                    List<String> styleValueIds = objectMapper.readValue(
                        product.getStyleValueIds(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                    );

                    if (!styleValueIds.isEmpty()) {
                        List<StyleValue> styleValues = styleValueRepository.findAllById(styleValueIds);
                        for (StyleValue sv : styleValues) {
                            String styleName = sv.getStyle().getName();
                            styleMap.computeIfAbsent(styleName, k -> new ArrayList<>()).add(sv);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 🧩 Add dữ liệu cơ bản
            model.addAttribute("product", product);
            model.addAttribute("relatedProducts", relatedProducts);
            model.addAttribute("isFavorite", isFavorite);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("styleMap", styleMap);

            // ✅ 6️⃣ Thêm phần load reviews ở đây
            List<Review> reviews = reviewRepository.findByProduct_Id(product.getId());

            ObjectMapper mapper = new ObjectMapper();
            for (Review review : reviews) {
                try {
                    if (review.getOrderItem() != null && review.getOrderItem().getStyleValueIds() != null) {
                        List<String> styleValueIds = mapper.readValue(
                            review.getOrderItem().getStyleValueIds(),
                            mapper.getTypeFactory().constructCollectionType(List.class, String.class)
                        );
                        if (!styleValueIds.isEmpty()) {
                            List<StyleValue> styleValues = styleValueRepository.findAllById(styleValueIds);
                            List<String> styleValueNames = new ArrayList<>();
                            for (StyleValue sv : styleValues) {
                                styleValueNames.add(sv.getStyle().getName() + ": " + sv.getName());
                            }
                            review.getOrderItem().setStyleValueNames(styleValueNames);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Lỗi parse thuộc tính trong review: " + e.getMessage());
                }
            }

            model.addAttribute("reviews", reviews);

            // ✅ 7️⃣ Trả về view
            return "user/product-detail";
        }

        // ❌ Nếu không tìm thấy product
        return "redirect:/products";
    }
}
