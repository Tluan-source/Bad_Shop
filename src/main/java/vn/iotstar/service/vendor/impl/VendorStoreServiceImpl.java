package vn.iotstar.service.vendor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.vendor.StoreUpdateDTO;
import vn.iotstar.dto.vendor.VendorStoreDTO;
import vn.iotstar.entity.Store;
import vn.iotstar.exception.ResourceNotFoundException;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.service.vendor.VendorSecurityService;
import vn.iotstar.service.vendor.VendorStoreService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of VendorStoreService
 * @author Vendor Module
 * @since 2025-10-24
 */
@Service
public class VendorStoreServiceImpl implements VendorStoreService {
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private VendorSecurityService securityService;
    
    @Override
    @Transactional(readOnly = true)
    public VendorStoreDTO getMyStore(String storeId, String currentUserId) {
        securityService.checkStoreOwnership(storeId, currentUserId);
        
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> ResourceNotFoundException.store(storeId));
        
        return convertToDTO(store);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<VendorStoreDTO> getMyStores(String currentUserId) {
        List<Store> stores = storeRepository.findByOwnerId(currentUserId);
        return stores.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public VendorStoreDTO updateStore(String storeId, StoreUpdateDTO updateDTO, String currentUserId) {
        securityService.checkStoreOwnership(storeId, currentUserId);
        
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> ResourceNotFoundException.store(storeId));
        
        // Update fields
        store.setName(updateDTO.getName());
        store.setBio(updateDTO.getBio());
        
        if (updateDTO.getFeaturedImages() != null) {
            store.setFeaturedImages(updateDTO.getFeaturedImages());
        }
        
        store.setUpdatedAt(LocalDateTime.now());
        
        Store savedStore = storeRepository.save(store);
        return convertToDTO(savedStore);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Store getStoreEntity(String storeId) {
        return storeRepository.findById(storeId)
            .orElseThrow(() -> ResourceNotFoundException.store(storeId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isOwner(String storeId, String userId) {
        return storeRepository.existsByOwnerIdAndId(userId, storeId);
    }
    
    // Helper method to convert Store to DTO with statistics
    private VendorStoreDTO convertToDTO(Store store) {
        VendorStoreDTO dto = new VendorStoreDTO();
        
        // Basic info
        dto.setId(store.getId());
        dto.setName(store.getName());
        dto.setBio(store.getBio());
        dto.setSlug(store.getSlug());
        dto.setFeaturedImages(store.getFeaturedImages());
        dto.setIsActive(store.getIsActive());
        dto.setPoint(store.getPoint());
        dto.setRating(store.getRating());
        dto.setEWallet(store.getEWallet());
        dto.setCreatedAt(store.getCreatedAt());
        dto.setUpdatedAt(store.getUpdatedAt());
        
        // Owner info
        dto.setOwnerId(store.getOwner().getId());
        dto.setOwnerName(store.getOwner().getFullName());
        dto.setOwnerEmail(store.getOwner().getEmail());
        dto.setOwnerPhone(store.getOwner().getPhone());
        
        // Commission info
        if (store.getCommission() != null) {
            dto.setCommissionId(store.getCommission().getId());
            dto.setCommissionName(store.getCommission().getName());
            dto.setCommissionFeePercent(store.getCommission().getFeePercent());
        }
        
        // Statistics
        dto.setTotalProducts(productRepository.countByStoreId(store.getId()));
        dto.setActiveProducts(productRepository.countByStoreIdAndIsSelling(store.getId(), true));
        dto.setTotalOrders(orderRepository.countByStoreIdAndStatus(store.getId(), null));
        dto.setPendingOrders(orderRepository.countByStoreIdAndStatus(store.getId(), 
            vn.iotstar.entity.Order.OrderStatus.NOT_PROCESSED));
        
        // Monthly revenue
        LocalDateTime startOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
        BigDecimal monthlyRevenue = orderRepository.calculateRevenueByDateRange(store.getId(), startOfMonth, endOfMonth);
        dto.setMonthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);
        
        return dto;
    }
}
