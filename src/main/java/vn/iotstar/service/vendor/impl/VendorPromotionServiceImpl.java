package vn.iotstar.service.vendor.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.iotstar.dto.vendor.PromotionCreateDTO;
import vn.iotstar.dto.vendor.VendorPromotionDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Promotion;
import vn.iotstar.entity.Store;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.PromotionRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.service.vendor.VendorPromotionService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of VendorPromotionService
 */
@Service
public class VendorPromotionServiceImpl implements VendorPromotionService {
    
    @Autowired
    private PromotionRepository promotionRepository;
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public List<VendorPromotionDTO> getMyPromotions(String storeId) {
        List<Promotion> promotions = promotionRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
        return promotions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<VendorPromotionDTO> getMyPromotions(String storeId, Pageable pageable) {
        List<VendorPromotionDTO> allPromotions = getMyPromotions(storeId);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allPromotions.size());
        
        List<VendorPromotionDTO> pageContent = allPromotions.subList(start, end);
        return new PageImpl<>(pageContent, pageable, allPromotions.size());
    }
    
    @Override
    public List<VendorPromotionDTO> getActivePromotions(String storeId) {
        List<Promotion> promotions = promotionRepository.findActivePromotions(storeId, LocalDateTime.now());
        return promotions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public VendorPromotionDTO getPromotionDetail(String promotionId, String storeId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        
        // Security check
        if (!promotion.getStore().getId().equals(storeId)) {
            throw new RuntimeException("Access denied");
        }
        
        return convertToDTO(promotion);
    }
    
    @Override
    @Transactional
    public VendorPromotionDTO createPromotion(PromotionCreateDTO dto, String storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));
        
        // Validate dates
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        
        Promotion promotion = new Promotion();
        promotion.setId(UUID.randomUUID().toString());
        promotion.setStore(store);
        promotion.setName(dto.getName());
        promotion.setDescription(dto.getDescription());
        promotion.setDiscountType(dto.getDiscountType());
        promotion.setDiscountValue(dto.getDiscountValue());
        promotion.setMaxDiscount(dto.getMaxDiscount());
        promotion.setMinOrderAmount(dto.getMinOrderAmount());
        promotion.setStartDate(dto.getStartDate());
        promotion.setEndDate(dto.getEndDate());
        promotion.setIsActive(true);
        promotion.setAppliesTo(dto.getAppliesTo());
        
        // Set products if specific products
        if (dto.getAppliesTo() == Promotion.AppliesTo.SPECIFIC_PRODUCTS && 
            dto.getProductIds() != null && !dto.getProductIds().isEmpty()) {
            List<Product> products = productRepository.findAllById(dto.getProductIds());
            promotion.setProducts(products);
        }
        
        promotionRepository.save(promotion);
        
        return convertToDTO(promotion);
    }
    
    @Override
    @Transactional
    public VendorPromotionDTO updatePromotion(String promotionId, PromotionCreateDTO dto, String storeId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        
        // Security check
        if (!promotion.getStore().getId().equals(storeId)) {
            throw new RuntimeException("Access denied");
        }
        
        // Validate dates
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        
        promotion.setName(dto.getName());
        promotion.setDescription(dto.getDescription());
        promotion.setDiscountType(dto.getDiscountType());
        promotion.setDiscountValue(dto.getDiscountValue());
        promotion.setMaxDiscount(dto.getMaxDiscount());
        promotion.setMinOrderAmount(dto.getMinOrderAmount());
        promotion.setStartDate(dto.getStartDate());
        promotion.setEndDate(dto.getEndDate());
        promotion.setAppliesTo(dto.getAppliesTo());
        
        // Update products
        if (dto.getAppliesTo() == Promotion.AppliesTo.SPECIFIC_PRODUCTS && 
            dto.getProductIds() != null && !dto.getProductIds().isEmpty()) {
            List<Product> products = productRepository.findAllById(dto.getProductIds());
            promotion.setProducts(products);
        } else {
            promotion.setProducts(new ArrayList<>());
        }
        
        promotionRepository.save(promotion);
        
        return convertToDTO(promotion);
    }
    
    @Override
    @Transactional
    public void togglePromotionStatus(String promotionId, String storeId, boolean isActive) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        
        // Security check
        if (!promotion.getStore().getId().equals(storeId)) {
            throw new RuntimeException("Access denied");
        }
        
        promotion.setIsActive(isActive);
        promotionRepository.save(promotion);
    }
    
    @Override
    @Transactional
    public void deletePromotion(String promotionId, String storeId) {
        System.out.println("=== START DELETE PROMOTION ===");
        System.out.println("Promotion ID: " + promotionId);
        System.out.println("Store ID: " + storeId);
        
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        
        System.out.println("Found promotion: " + promotion.getName());
        
        // Security check
        if (!promotion.getStore().getId().equals(storeId)) {
            System.err.println("Access denied! Store ID mismatch");
            throw new RuntimeException("Access denied");
        }
        
        System.out.println("Security check passed");
        
        try {
            System.out.println("Step 1: Detaching entity...");
            entityManager.detach(promotion);
            
            System.out.println("Step 2: Clearing products list...");
            if (promotion.getProducts() != null) {
                System.out.println("Products count: " + promotion.getProducts().size());
                promotion.getProducts().clear();
                System.out.println("Products cleared");
            } else {
                System.out.println("No products to clear");
            }
            
            System.out.println("Step 3: Merging entity...");
            promotion = entityManager.merge(promotion);
            System.out.println("Entity merged");
            
            System.out.println("Step 4: Flushing changes...");
            entityManager.flush();
            System.out.println("Changes flushed");
            
            System.out.println("Step 5: Removing promotion...");
            entityManager.remove(promotion);
            System.out.println("Promotion removed from context");
            
            System.out.println("Step 6: Final flush...");
            entityManager.flush();
            System.out.println("Final flush completed");
            
            System.out.println("=== DELETE PROMOTION SUCCESS ===");
            
        } catch (Exception e) {
            System.err.println("=== ERROR DELETING PROMOTION ===");
            System.err.println("Promotion ID: " + promotionId);
            System.err.println("Store ID: " + storeId);
            System.err.println("Error Type: " + e.getClass().getName());
            System.err.println("Error Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getClass().getName());
                System.err.println("Cause Message: " + e.getCause().getMessage());
                if (e.getCause().getCause() != null) {
                    System.err.println("Root cause: " + e.getCause().getCause().getClass().getName());
                    System.err.println("Root cause message: " + e.getCause().getCause().getMessage());
                }
            }
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.err.println("=== END ERROR ===");
            throw new RuntimeException("Không thể xóa chương trình khuyến mãi: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isPromotionNameExists(String storeId, String name, String excludeId) {
        List<Promotion> promotions = promotionRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
        return promotions.stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name) && 
                              (excludeId == null || !p.getId().equals(excludeId)));
    }
    
    private VendorPromotionDTO convertToDTO(Promotion promotion) {
        VendorPromotionDTO dto = new VendorPromotionDTO();
        dto.setId(promotion.getId());
        dto.setName(promotion.getName());
        dto.setDescription(promotion.getDescription());
        dto.setDiscountType(promotion.getDiscountType());
        dto.setDiscountValue(promotion.getDiscountValue());
        dto.setMaxDiscount(promotion.getMaxDiscount());
        dto.setMinOrderAmount(promotion.getMinOrderAmount());
        dto.setStartDate(promotion.getStartDate());
        dto.setEndDate(promotion.getEndDate());
        dto.setIsActive(promotion.getIsActive());
        dto.setAppliesTo(promotion.getAppliesTo());
        dto.setCreatedAt(promotion.getCreatedAt());
        dto.setUpdatedAt(promotion.getUpdatedAt());
        
        // Set product count for specific products
        if (promotion.getProducts() != null) {
            dto.setProductCount(promotion.getProducts().size());
        } else {
            dto.setProductCount(0);
        }
        
        return dto;
    }
}