package vn.iotstar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.Product;
import vn.iotstar.repository.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getTop10Products() {
        return productRepository.findTop10ByOrderBySoldDesc();
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
}