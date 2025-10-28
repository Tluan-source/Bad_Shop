package vn.iotstar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Voucher;
import vn.iotstar.repository.VoucherRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing Vouchers (Admin-managed, system-wide discount codes)
 */
@Service
@RequiredArgsConstructor
public class VoucherService {
    
    private final VoucherRepository voucherRepository;
    
    /**
     * Create a new voucher
     */
    @Transactional
    public Voucher createVoucher(Voucher voucher) {
        voucher.setId(UUID.randomUUID().toString());
        voucher.setUsageCount(0);
        voucher.setCreatedAt(LocalDateTime.now());
        return voucherRepository.save(voucher);
    }
    
    /**
     * Update existing voucher
     */
    @Transactional
    public Voucher updateVoucher(String id, Voucher voucherDetails) {
        Voucher voucher = voucherRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Voucher not found"));
        
        voucher.setCode(voucherDetails.getCode());
        voucher.setDescription(voucherDetails.getDescription());
        voucher.setDiscountType(voucherDetails.getDiscountType());
        voucher.setDiscountValue(voucherDetails.getDiscountValue());
        voucher.setMaxDiscount(voucherDetails.getMaxDiscount());
        voucher.setMinOrderValue(voucherDetails.getMinOrderValue());
        voucher.setQuantity(voucherDetails.getQuantity());
        voucher.setStartDate(voucherDetails.getStartDate());
        voucher.setEndDate(voucherDetails.getEndDate());
        voucher.setIsActive(voucherDetails.getIsActive());
        
        return voucherRepository.save(voucher);
    }
    
    /**
     * Delete voucher
     */
    @Transactional
    public void deleteVoucher(String id) {
        voucherRepository.deleteById(id);
    }
    
    /**
     * Find voucher by code
     */
    public Optional<Voucher> findByCode(String code) {
        return voucherRepository.findByCode(code);
    }
    
    /**
     * Get all vouchers
     */
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }
    
    /**
     * Get active vouchers
     */
    public List<Voucher> getActiveVouchers() {
        return voucherRepository.findByIsActiveTrue();
    }
    
    /**
     * Get available vouchers (active, not expired, has remaining usage)
     */
    public List<Voucher> getAvailableVouchers() {
        return voucherRepository.findAvailableVouchers(LocalDateTime.now());
    }
    
    /**
     * Validate voucher for order
     */
    public boolean validateVoucher(String code, BigDecimal orderAmount) {
        Optional<Voucher> voucherOpt = voucherRepository.findByCode(code);
        
        if (voucherOpt.isEmpty()) {
            return false;
        }
        
        Voucher voucher = voucherOpt.get();
        
        // Check if voucher is available
        if (!voucher.isAvailable()) {
            return false;
        }
        
        // Check minimum order value
        if (voucher.getMinOrderValue() != null && 
            orderAmount.compareTo(voucher.getMinOrderValue()) < 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Apply voucher to order and increment usage count
     */
    @Transactional
    public BigDecimal applyVoucher(String code, BigDecimal orderAmount) {
        Voucher voucher = voucherRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Voucher not found"));
        
        if (!validateVoucher(code, orderAmount)) {
            throw new RuntimeException("Voucher is not valid for this order");
        }
        
        BigDecimal discount = BigDecimal.ZERO;
        
        switch (voucher.getDiscountType()) {
            case PERCENTAGE:
                discount = orderAmount.multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));
                
                // Apply max discount limit
                if (voucher.getMaxDiscount() != null && 
                    voucher.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0) {
                    discount = discount.min(voucher.getMaxDiscount());
                }
                break;
                
            case FIXED:
                discount = voucher.getDiscountValue();
                // Don't exceed order amount
                discount = discount.min(orderAmount);
                break;
        }
        
        // Increment usage count
        voucher.setUsageCount(voucher.getUsageCount() + 1);
        voucherRepository.save(voucher);
        
        return discount;
    }
    
    /**
     * Rollback voucher usage (when order is cancelled)
     */
    @Transactional
    public void rollbackVoucherUsage(String voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new RuntimeException("Voucher not found"));
        
        if (voucher.getUsageCount() > 0) {
            voucher.setUsageCount(voucher.getUsageCount() - 1);
            voucherRepository.save(voucher);
        }
    }
    
    /**
     * Search vouchers by keyword
     */
    public List<Voucher> searchVouchers(String keyword) {
        return voucherRepository.searchVouchers(keyword);
    }
    
    /**
     * Toggle voucher active status
     */
    @Transactional
    public Voucher toggleActive(String id) {
        Voucher voucher = voucherRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Voucher not found"));
        
        voucher.setIsActive(!voucher.getIsActive());
        return voucherRepository.save(voucher);
    }
    
    /**
     * Get voucher statistics
     */
    public VoucherStats getStatistics() {
        long totalVouchers = voucherRepository.count();
        long activeVouchers = voucherRepository.countByIsActiveTrue();
        long expiredVouchers = voucherRepository.countExpiredVouchers(LocalDateTime.now());
        
        return new VoucherStats(totalVouchers, activeVouchers, expiredVouchers);
    }
    
    // Inner class for statistics
    public static class VoucherStats {
        public final long total;
        public final long active;
        public final long expired;
        
        public VoucherStats(long total, long active, long expired) {
            this.total = total;
            this.active = active;
            this.expired = expired;
        }
    }
}
