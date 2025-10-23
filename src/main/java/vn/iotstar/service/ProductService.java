package vn.iotstar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.Product;
import vn.iotstar.repository.ProductRepository;

import java.util.List;

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

}