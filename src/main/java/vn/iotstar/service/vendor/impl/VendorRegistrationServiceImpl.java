package vn.iotstar.service.vendor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import vn.iotstar.dto.vendor.StoreRegistrationDTO;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.User;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.MailService;
import vn.iotstar.service.vendor.VendorRegistrationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of VendorRegistrationService
 */
@Service
public class VendorRegistrationServiceImpl implements VendorRegistrationService {
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private MailService mailService;
    
    @Override
    @Transactional
    public String registerVendor(User user, StoreRegistrationDTO dto, 
                                 MultipartFile logoFile, MultipartFile licenseFile) {
        
        // Check if user already has a store - findByOwnerId returns List
        List<Store> existingStores = storeRepository.findByOwnerId(user.getId());
        if (!existingStores.isEmpty()) {
            throw new RuntimeException("Bạn đã đăng ký cửa hàng rồi!");
        }
        
        // Upload files if provided
        String logoUrl = null;
        String licenseUrl = null;
        
        try {
            if (logoFile != null && !logoFile.isEmpty()) {
                logoUrl = cloudinaryService.uploadFile(logoFile);
            }
            
            if (licenseFile != null && !licenseFile.isEmpty()) {
                licenseUrl = cloudinaryService.uploadFile(licenseFile);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi upload file: " + e.getMessage());
        }
        
        // Create store with PENDING status
        Store store = new Store();
        store.setId(UUID.randomUUID().toString());
        store.setName(dto.getStoreName());
        store.setBio(dto.getDescription());
        // Store uses featuredImages instead of avatar
        if (logoUrl != null) {
            store.setFeaturedImages("[\"" + logoUrl + "\"]"); // Store as JSON array
        }
        store.setOwner(user);
        store.setIsActive(false); // Waiting for admin approval
        store.setPoint(0);
        store.setRating(BigDecimal.ZERO);
        store.setEWallet(BigDecimal.ZERO);
        store.setCreatedAt(LocalDateTime.now());
        store.setUpdatedAt(LocalDateTime.now());
        
        // Store contact info and business details in JSON or separate fields
        // For now, we'll use the bio field to store additional info
        StringBuilder additionalInfo = new StringBuilder(dto.getDescription());
        additionalInfo.append("\n\n--- Thông tin liên hệ ---");
        additionalInfo.append("\nĐiện thoại: ").append(dto.getPhone());
        additionalInfo.append("\nEmail: ").append(dto.getEmail());
        additionalInfo.append("\nĐịa chỉ: ").append(dto.getAddress())
                      .append(", ").append(dto.getWard())
                      .append(", ").append(dto.getDistrict())
                      .append(", ").append(dto.getCity());
        additionalInfo.append("\n\n--- Thông tin doanh nghiệp ---");
        additionalInfo.append("\nLoại hình: ").append(dto.getBusinessType());
        if (dto.getTaxCode() != null && !dto.getTaxCode().isEmpty()) {
            additionalInfo.append("\nMã số thuế: ").append(dto.getTaxCode());
        }
        if (licenseUrl != null) {
            additionalInfo.append("\nGiấy phép KD: ").append(licenseUrl);
        }
        additionalInfo.append("\n\n--- Thông tin thanh toán ---");
        additionalInfo.append("\nNgân hàng: ").append(dto.getBankName());
        additionalInfo.append("\nSố TK: ").append(dto.getBankAccountNumber());
        additionalInfo.append("\nChủ TK: ").append(dto.getBankAccountName());
        if (dto.getBankBranch() != null && !dto.getBankBranch().isEmpty()) {
            additionalInfo.append("\nChi nhánh: ").append(dto.getBankBranch());
        }
        
        store.setBio(additionalInfo.toString());
        
        // Save store
        storeRepository.save(store);
        
        // Send notification email to admin
        try {
            sendAdminNotification(user, store);
        } catch (Exception e) {
            // Log error but don't fail the registration
            System.err.println("Failed to send admin notification: " + e.getMessage());
        }
        
        return store.getId();
    }
    
    @Override
    public boolean hasPendingRegistration(String userId) {
        List<Store> stores = storeRepository.findByOwnerId(userId);
        // Check if any store is not active (pending)
        return stores.stream().anyMatch(store -> !store.getIsActive());
    }
    
    @Override
    public String getPendingStoreId(String userId) {
        List<Store> stores = storeRepository.findByOwnerId(userId);
        // Find first store that is not active (pending)
        return stores.stream()
                .filter(store -> !store.getIsActive())
                .map(Store::getId)
                .findFirst()
                .orElse(null);
    }
    
    private void sendAdminNotification(User user, Store store) {
        String subject = "🔔 Đăng ký người bán mới - " + store.getName();
        
        StringBuilder body = new StringBuilder();
        body.append("Thông báo đăng ký người bán mới\n\n");
        body.append("Có một đăng ký người bán mới cần được duyệt:\n\n");
        body.append("Tên cửa hàng: ").append(store.getName()).append("\n");
        body.append("Chủ shop: ").append(user.getFullName()).append("\n");
        body.append("Email: ").append(user.getEmail()).append("\n");
        body.append("Thời gian đăng ký: ").append(store.getCreatedAt()).append("\n\n");
        body.append("Vui lòng vào trang quản trị để xem chi tiết và phê duyệt.\n");
        body.append("Link: http://localhost:8080/admin/stores/").append(store.getId()).append("/details");
        
        // Use MailService's sendSimpleMessage method
        mailService.sendSimpleMessage("admin@badmintonmarket.com", subject, body.toString());
    }
}