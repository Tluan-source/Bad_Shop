package vn.iotstar.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.iotstar.entity.Product;
import vn.iotstar.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getTop20Products() {
        Pageable pageable = PageRequest.of(0, 20);
        return productRepository.findTop20ByOrderBySoldDesc(pageable);
    }

    public List<Product> getAllActiveProducts() {
        return productRepository.findByIsActiveTrueAndIsSellingTrue();
    }

    // additional helpers
    public List<Product> findByCategory(String categoryId) {
        return productRepository.findByCategoryIdAndIsActiveTrueAndIsSellingTrue(categoryId);
    }

    public List<Product> filterProducts(String categoryId, Double minPrice, Double maxPrice, String brand) {
    // Bắt đầu với tất cả sản phẩm active
    List<Product> products = productRepository.findByIsActiveTrueAndIsSellingTrue();
    
    // Filter theo category
    if (categoryId != null && !categoryId.isEmpty()) {
        products = products.stream()
            .filter(p -> p.getCategory() != null && categoryId.equals(p.getCategory().getId()))
            .collect(Collectors.toList());
    }
    
    // Filter theo khoảng giá
    if (minPrice != null || maxPrice != null) {
        products = products.stream()
            .filter(p -> {
                Double price = p.getPromotionalPrice() != null ? 
                              p.getPromotionalPrice().doubleValue() : p.getPrice().doubleValue();
                boolean valid = true;
                if (minPrice != null) valid = valid && price >= minPrice;
                if (maxPrice != null) valid = valid && price <= maxPrice;
                return valid;
            })
            .collect(Collectors.toList());
    }
    
    // Filter theo thương hiệu (giả sử brand trong tên sản phẩm)
    if (brand != null && !brand.isEmpty()) {
        products = products.stream()
            .filter(p -> p.getName().toLowerCase().contains(brand.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    return products;
}

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByCategory(String categoryId) {
        return productRepository.findByCategoryIdAndIsActiveTrueAndIsSellingTrue(categoryId);
    }
    
    public List<Product> findByCategoryId(String categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndIsActiveTrueAndIsSellingTrue(categoryId)
                .stream()
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
    }
    public List<Product> searchProductsByName(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(keyword);
    }
    public List<Product> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }
}