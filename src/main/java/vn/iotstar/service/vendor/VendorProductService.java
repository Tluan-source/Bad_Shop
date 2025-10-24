package vn.iotstar.service.vendor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.iotstar.dto.vendor.ProductCreateDTO;
import vn.iotstar.dto.vendor.ProductUpdateDTO;
import vn.iotstar.dto.vendor.VendorProductDTO;
import vn.iotstar.entity.Product;

import java.util.List;

/**
 * Service interface for Vendor Product Management
 * Use this for VENDOR to manage their products (CRUD operations)
 * For public product browsing, use ProductService instead
 * 
 * @author Vendor Module
 * @since 2025-10-24
 */
public interface VendorProductService {
    
    /**
     * Get all products of vendor's store
     */
    List<VendorProductDTO> getMyProducts(String storeId);
    
    /**
     * Get products with pagination
     */
    Page<VendorProductDTO> getMyProducts(String storeId, Pageable pageable);
    
    /**
     * Get product by ID (with security check)
     */
    VendorProductDTO getMyProduct(String productId, String storeId);
    
    /**
     * Create new product
     */
    VendorProductDTO createProduct(ProductCreateDTO createDTO, String storeId);
    
    /**
     * Update existing product
     */
    VendorProductDTO updateProduct(String productId, ProductUpdateDTO updateDTO, String storeId);
    
    /**
     * Delete product (soft delete)
     */
    void deleteProduct(String productId, String storeId);
    
    /**
     * Toggle product selling status
     */
    void toggleSelling(String productId, String storeId);
    
    /**
     * Search products in store
     */
    List<VendorProductDTO> searchMyProducts(String storeId, String keyword);
    
    Page<VendorProductDTO> searchMyProducts(String storeId, String keyword, Pageable pageable);
    
    /**
     * Get products by status
     */
    List<VendorProductDTO> getMyProductsByStatus(String storeId, Boolean isSelling, Boolean isActive);
    
    /**
     * Get low stock products
     */
    List<VendorProductDTO> getLowStockProducts(String storeId, Integer threshold);
    
    /**
     * Count products
     */
    Long countMyProducts(String storeId);
    
    Long countMySellingProducts(String storeId);
}
