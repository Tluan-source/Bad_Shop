package vn.iotstar.service.vendor.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.vendor.ProductCreateDTO;
import vn.iotstar.dto.vendor.ProductUpdateDTO;
import vn.iotstar.dto.vendor.VendorProductDTO;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Store;
import vn.iotstar.exception.ResourceNotFoundException;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.vendor.VendorProductService;
import vn.iotstar.service.vendor.VendorSecurityService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of VendorProductService
 * @author Vendor Module
 * @since 2025-10-24
 */
@Service
public class VendorProductServiceImpl implements VendorProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private VendorSecurityService securityService;

    @Autowired
    private CloudinaryService cloudinaryService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    @Transactional(readOnly = true)
    public List<VendorProductDTO> getMyProducts(String storeId) {
        List<Product> products = productRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
        return products.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<VendorProductDTO> getMyProducts(String storeId, Pageable pageable) {
        Page<Product> products = productRepository.findByStoreId(storeId, pageable);
        List<VendorProductDTO> dtos = products.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, products.getTotalElements());
    }
    
    @Override
    @Transactional(readOnly = true)
    public VendorProductDTO getMyProduct(String productId, String storeId) {
        securityService.checkProductOwnership(productId, storeId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> ResourceNotFoundException.product(productId));
        
        return convertToDTO(product);
    }
    
    @Override
    @Transactional
    public VendorProductDTO createProduct(ProductCreateDTO createDTO, String storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> ResourceNotFoundException.store(storeId));
        
        Category category = categoryRepository.findById(createDTO.getCategoryId())
            .orElseThrow(() -> ResourceNotFoundException.category(createDTO.getCategoryId()));
        
        Product product = new Product();
        product.setId("P_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        product.setName(createDTO.getName());
        product.setSlug(generateSlug(createDTO.getName()));
        product.setDescription(createDTO.getDescription());
        product.setPrice(createDTO.getPrice());
        product.setPromotionalPrice(createDTO.getPromotionalPrice());
        product.setQuantity(createDTO.getQuantity());
        product.setSold(0);
        product.setIsActive(false); // Admin needs to approve
        product.setIsSelling(createDTO.getIsSelling() != null ? createDTO.getIsSelling() : true);
        product.setCategory(category);
        product.setStore(store);
        product.setRating(BigDecimal.ZERO);
        product.setViewCount(0);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        
        // Handle images: prefer uploaded MultipartFile images (Cloudinary), then provided imageUrls
        if (createDTO.getImages() != null && !createDTO.getImages().isEmpty()) {
            try {
                List<String> uploadedUrls = cloudinaryService.uploadFiles(createDTO.getImages());
                product.setListImages(objectMapper.writeValueAsString(uploadedUrls));
            } catch (Exception e) {
                try {
                    product.setListImages(objectMapper.writeValueAsString(new ArrayList<String>()));
                } catch (Exception ex) {
                    product.setListImages("[]");
                }
            }
        } else if (createDTO.getImageUrls() != null && !createDTO.getImageUrls().isEmpty()) {
            try {
                product.setListImages(objectMapper.writeValueAsString(createDTO.getImageUrls()));
            } catch (Exception e) {
                product.setListImages("[]");
            }
        } else {
            product.setListImages("[]");
        }
        
        // Handle style values (màu sắc, kích thước...)
        if (createDTO.getStyleValueIds() != null && !createDTO.getStyleValueIds().isEmpty()) {
            try {
                product.setStyleValueIds(objectMapper.writeValueAsString(createDTO.getStyleValueIds()));
            } catch (Exception e) {
                product.setStyleValueIds("[]");
            }
        } else {
            product.setStyleValueIds("[]");
        }
        
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }
    
    @Override
    @Transactional
    public VendorProductDTO updateProduct(String productId, ProductUpdateDTO updateDTO, String storeId) {
        securityService.checkProductOwnership(productId, storeId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> ResourceNotFoundException.product(productId));
        
        Category category = categoryRepository.findById(updateDTO.getCategoryId())
            .orElseThrow(() -> ResourceNotFoundException.category(updateDTO.getCategoryId()));
        
        // Update fields
        product.setName(updateDTO.getName());
        product.setSlug(generateSlug(updateDTO.getName()));
        product.setDescription(updateDTO.getDescription());
        product.setPrice(updateDTO.getPrice());
        product.setPromotionalPrice(updateDTO.getPromotionalPrice());
        product.setQuantity(updateDTO.getQuantity());
        product.setCategory(category);
        product.setIsSelling(updateDTO.getIsSelling());
        product.setUpdatedAt(LocalDateTime.now());
        
        // Update images: priority order - newImages (upload to Cloudinary) > listImages (keep existing) > imageUrls (replace with new URLs)
        if (updateDTO.getNewImages() != null && !updateDTO.getNewImages().isEmpty()) {
            try {
                List<String> uploadedUrls = cloudinaryService.uploadFiles(updateDTO.getNewImages());
                if (!uploadedUrls.isEmpty()) {
                    product.setListImages(objectMapper.writeValueAsString(uploadedUrls));
                }
            } catch (Exception e) {
                // Keep existing images on upload failure
            }
        } else if (updateDTO.getListImages() != null) {
            product.setListImages(updateDTO.getListImages());
        } else if (updateDTO.getImageUrls() != null && !updateDTO.getImageUrls().isEmpty()) {
            try {
                product.setListImages(objectMapper.writeValueAsString(updateDTO.getImageUrls()));
            } catch (Exception e) {
                // Keep existing images
            }
        }
        
        // Update style values
        if (updateDTO.getStyleValueIds() != null && !updateDTO.getStyleValueIds().isEmpty()) {
            try {
                product.setStyleValueIds(objectMapper.writeValueAsString(updateDTO.getStyleValueIds()));
            } catch (Exception e) {
                product.setStyleValueIds("[]");
            }
        } else {
            product.setStyleValueIds("[]");
        }
        
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }
    
    @Override
    @Transactional
    public void deleteProduct(String productId, String storeId) {
        securityService.checkProductOwnership(productId, storeId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> ResourceNotFoundException.product(productId));
        
        // Soft delete
        product.setIsActive(false);
        product.setIsSelling(false);
        product.setUpdatedAt(LocalDateTime.now());
        
        productRepository.save(product);
    }
    
    @Override
    @Transactional
    public void toggleSelling(String productId, String storeId) {
        securityService.checkProductOwnership(productId, storeId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> ResourceNotFoundException.product(productId));
        
        product.setIsSelling(!product.getIsSelling());
        product.setUpdatedAt(LocalDateTime.now());
        
        productRepository.save(product);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<VendorProductDTO> searchMyProducts(String storeId, String keyword) {
        List<Product> products = productRepository.searchByStoreAndName(storeId, keyword);
        return products.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<VendorProductDTO> searchMyProducts(String storeId, String keyword, Pageable pageable) {
        Page<Product> products = productRepository.searchByStoreAndName(storeId, keyword, pageable);
        List<VendorProductDTO> dtos = products.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, products.getTotalElements());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<VendorProductDTO> getMyProductsByStatus(String storeId, Boolean isSelling, Boolean isActive) {
        List<Product> products;
        
        if (isSelling != null && isActive != null) {
            products = productRepository.findByStoreIdAndIsSelling(storeId, isSelling)
                .stream()
                .filter(p -> p.getIsActive().equals(isActive))
                .collect(Collectors.toList());
        } else if (isSelling != null) {
            products = productRepository.findByStoreIdAndIsSelling(storeId, isSelling);
        } else if (isActive != null) {
            products = productRepository.findByStoreIdAndIsActive(storeId, isActive);
        } else {
            products = productRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
        }
        
        return products.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<VendorProductDTO> getLowStockProducts(String storeId, Integer threshold) {
        List<Product> products = productRepository.findLowStockProducts(storeId, threshold);
        return products.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countMyProducts(String storeId) {
        return productRepository.countByStoreId(storeId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countMySellingProducts(String storeId) {
        return productRepository.countByStoreIdAndIsSelling(storeId, true);
    }
    
    // Helper methods
    private VendorProductDTO convertToDTO(Product product) {
        VendorProductDTO dto = new VendorProductDTO();
        
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setPromotionalPrice(product.getPromotionalPrice());
        dto.setQuantity(product.getQuantity());
        dto.setSold(product.getSold());
        dto.setIsActive(product.getIsActive());
        dto.setIsSelling(product.getIsSelling());
        dto.setListImages(product.getListImages());
        dto.setStyleValueIds(product.getStyleValueIds());
        dto.setRating(product.getRating());
        dto.setViewCount(product.getViewCount());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        // Category
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        // Store
        dto.setStoreId(product.getStore().getId());
        dto.setStoreName(product.getStore().getName());
        
        // Calculated fields
        BigDecimal effectivePrice = product.getPromotionalPrice() != null && 
            product.getPromotionalPrice().compareTo(BigDecimal.ZERO) > 0 
            ? product.getPromotionalPrice() : product.getPrice();
        dto.setTotalRevenue(effectivePrice.multiply(BigDecimal.valueOf(product.getSold())));
        
        // Stock status
        if (product.getQuantity() == 0) {
            dto.setStockStatus("Hết hàng");
        } else if (product.getQuantity() < 10) {
            dto.setStockStatus("Sắp hết");
        } else {
            dto.setStockStatus("Còn hàng");
        }
        
        return dto;
    }
    
    private String generateSlug(String name) {
        return name.toLowerCase()
            .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
            .replaceAll("[èéẹẻẽêềếệểễ]", "e")
            .replaceAll("[ìíịỉĩ]", "i")
            .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
            .replaceAll("[ùúụủũưừứựửữ]", "u")
            .replaceAll("[ỳýỵỷỹ]", "y")
            .replaceAll("[đ]", "d")
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");
    }
}