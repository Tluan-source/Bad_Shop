package vn.iotstar.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.Transient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.ObjectMapper;

import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Review;
import vn.iotstar.entity.Style;
import vn.iotstar.entity.StyleValue;
import vn.iotstar.repository.StyleRepository;
import vn.iotstar.repository.StyleValueRepository;
import vn.iotstar.service.CartService;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.FavoriteService;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.StyleService;
import vn.iotstar.repository.ReviewRepository;
import vn.iotstar.repository.ProductRepository;

@Controller
public class HomeController {
    
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;
    
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

    @Autowired
    private StyleService styleService;

    @Autowired
    private CategoryService categoryService;
    
    @GetMapping("/")
    public String home(Model model) {

        // ✅ Top 20 sản phẩm bán chạy
        Pageable pageable = PageRequest.of(0, 20);
        List<Product> topProducts = productRepository.findTop20ByOrderBySoldDesc(pageable);
        model.addAttribute("topProducts", topProducts);

        // ✅ Lấy danh mục từ DB
        List<Category> categories = categoryService.getActiveCategories();
        model.addAttribute("categories", categories);

        // ✅ Đếm sản phẩm theo Category
        Map<String, Long> categoryCounts = new HashMap<>();
        categories.forEach(c ->
            categoryCounts.put(c.getId(), productRepository.countByCategory_Id(c.getId()))
        );
        model.addAttribute("categoryCounts", categoryCounts);

        return "user/index";
    }

    
    @GetMapping("/products")
    public String products(
            @RequestParam(value = "category", required = false) String categoryId,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", required = false) String sort,
            Model model){

        // ✅ Lọc sản phẩm
        List<Product> productList;
        if (keyword != null && !keyword.trim().isEmpty()) {
        productList = productService.searchProductsByName(keyword.trim());
        } 
        else if (categoryId != null || minPrice != null || maxPrice != null || brand != null) {
            productList = productService.filterProducts(categoryId, minPrice, maxPrice, brand);
        } else {
            productList = productService.getAllActiveProducts();
        }

        // ✅ Sắp xếp
        if (sort != null && !sort.isEmpty()) {
            switch (sort) {
                case "price-asc" -> productList.sort(Comparator.comparing(p -> 
                    p.getPromotionalPrice() != null ? p.getPromotionalPrice() : p.getPrice()
                ));
                case "price-desc" -> productList.sort((p1, p2) -> {
                    BigDecimal price1 = p1.getPromotionalPrice() != null ? p1.getPromotionalPrice() : p1.getPrice();
                    BigDecimal price2 = p2.getPromotionalPrice() != null ? p2.getPromotionalPrice() : p2.getPrice();
                    return price2.compareTo(price1);
                });
                case "name-asc" -> productList.sort(Comparator.comparing(Product::getName));
                case "name-desc" -> productList.sort((p1, p2) -> p2.getName().compareToIgnoreCase(p1.getName()));
            }
        }

        // ✅ Lấy categories từ DB (để load menu danh mục)
        List<Category> categories = categoryService.getActiveCategories();
        model.addAttribute("categories", categories);

        // ✅ Lấy styles theo category (nếu cần lọc theo style)
        List<Style> styles = styleService.getStylesByCategory(categoryId);
        model.addAttribute("styles", styles);

        // ✅ Truyền selected values
        model.addAttribute("products", productList);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("keyword", keyword);

        return "user/products";
    }
    @GetMapping("/api/search")
    public ResponseEntity<List<String>> searchSuggestions(
            @RequestParam("keyword") String keyword) {

        List<Product> products = productService.searchByKeyword(keyword);
        
        List<String> names = products.stream()
                .map(Product::getName)
                .limit(8) // hạn chế 8 gợi ý
                .toList();

        return ResponseEntity.ok(names);
    }
    @GetMapping("/api/products/search")
    public ResponseEntity<List<Map<String, Object>>> realtimeSearch(
            @RequestParam("keyword") String keyword) {

        List<Product> products = productService.searchProductsByName(keyword);

        List<Map<String, Object>> result = products.stream().map(p -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getName());
            item.put("price", p.getPromotionalPrice() != null ? p.getPromotionalPrice() : p.getPrice());
            item.put("image", p.getFirstImage());
            item.put("rating", p.getRating());
            return item;
        }).limit(20).toList(); // giới hạn 20 sp

        return ResponseEntity.ok(result);
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
