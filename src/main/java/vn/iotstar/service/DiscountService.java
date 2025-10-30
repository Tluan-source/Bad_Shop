package vn.iotstar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.Promotion;
import vn.iotstar.entity.Voucher;
import vn.iotstar.repository.PromotionRepository;
import vn.iotstar.repository.VoucherRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service xử lý logic giảm giá cho voucher và promotion
 */
@Service
@RequiredArgsConstructor
public class DiscountService {
    
    private final VoucherRepository voucherRepository;
    private final PromotionRepository promotionRepository;
    
    /**
     * Validate và lấy voucher theo code
     * Logic mới: Kiểm tra theo TỔNG TẤT CẢ SHOP, chỉ cho phép PERCENTAGE
     */
    public Optional<Voucher> validateVoucher(String voucherCode, BigDecimal grandTotal) {
        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            return Optional.empty();
        }
        
        Optional<Voucher> voucherOpt = voucherRepository.findByCode(voucherCode.trim().toUpperCase());
        
        if (voucherOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Voucher voucher = voucherOpt.get();
        
        // CHỈ CHO PHÉP PERCENTAGE
        if (voucher.getDiscountType() != Voucher.DiscountType.PERCENTAGE) {
            throw new RuntimeException("Voucher chỉ hỗ trợ giảm theo phần trăm");
        }
        
        // Kiểm tra voucher có khả dụng không
        if (!voucher.isAvailable()) {
            return Optional.empty();
        }
        
        // Kiểm tra giá trị đơn hàng tối thiểu THEO TỔNG TẤT CẢ SHOP
        if (voucher.getMinOrderValue() != null && 
            grandTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            return Optional.empty();
        }
        
        return Optional.of(voucher);
    }
    
    /**
     * Validate và lấy promotion của shop
     */
    public Optional<Promotion> validatePromotion(String promotionId, String storeId, BigDecimal orderAmount) {
        if (promotionId == null || promotionId.trim().isEmpty()) {
            return Optional.empty();
        }
        
        Optional<Promotion> promotionOpt = promotionRepository.findById(promotionId);
        
        if (promotionOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Promotion promotion = promotionOpt.get();
        
        // Kiểm tra promotion có thuộc shop không
        if (!promotion.getStore().getId().equals(storeId)) {
            return Optional.empty();
        }
        
        // Kiểm tra promotion có khả dụng không (bao gồm cả quantity)
        if (!promotion.isAvailable()) {
            return Optional.empty();
        }
        
        // Kiểm tra giá trị đơn hàng tối thiểu
        if (promotion.getMinOrderAmount() != null && 
            orderAmount.compareTo(promotion.getMinOrderAmount()) < 0) {
            return Optional.empty();
        }
        
        return Optional.of(promotion);
    }
    
    /**
     * Tính số tiền giảm giá từ voucher cho MỘT SHOP
     * Logic: Voucher chỉ có PERCENTAGE, kiểm tra theo tổng tất cả shop, áp dụng % cho từng shop
     * Có hỗ trợ maxDiscount - sẽ được phân bổ theo tỷ lệ từng shop
     * 
     * @param voucher Voucher to apply
     * @param grandTotal Tổng tiền TẤT CẢ shop (để check min order)
     * @param storeAmount Tổng tiền SHOP HIỆN TẠI (để tính % discount)
     * @param totalDiscountAllStores Tổng discount đã tính cho TẤT CẢ shop (để check maxDiscount)
     * @return Discount amount cho shop này
     */
    public BigDecimal calculateVoucherDiscountForStore(Voucher voucher, BigDecimal grandTotal, 
                                                        BigDecimal storeAmount, BigDecimal totalDiscountAllStores) {
        if (voucher == null || grandTotal == null || storeAmount == null) {
            return BigDecimal.ZERO;
        }
        
        // CHỈ HỖ TRỢ PERCENTAGE
        if (voucher.getDiscountType() != Voucher.DiscountType.PERCENTAGE) {
            throw new RuntimeException("Voucher chỉ hỗ trợ giảm theo phần trăm");
        }
        
        BigDecimal discount = BigDecimal.ZERO;
        
        // Giảm theo phần trăm cho SHOP NÀY
        discount = storeAmount.multiply(voucher.getDiscountValue())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        // Đảm bảo discount không vượt quá storeAmount
        if (discount.compareTo(storeAmount) > 0) {
            discount = storeAmount;
        }
        
        // ÁP DỤNG MAX DISCOUNT (nếu có)
        // Nếu tổng discount của tất cả shop + discount shop này > maxDiscount
        // thì giảm discount của shop này xuống
        if (voucher.getMaxDiscount() != null && voucher.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0) {
            if (totalDiscountAllStores != null) {
                BigDecimal projectedTotal = totalDiscountAllStores.add(discount);
                if (projectedTotal.compareTo(voucher.getMaxDiscount()) > 0) {
                    // Discount vượt quá max, chỉ lấy phần còn lại
                    BigDecimal remaining = voucher.getMaxDiscount().subtract(totalDiscountAllStores);
                    if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                        discount = remaining;
                    } else {
                        discount = BigDecimal.ZERO;
                    }
                }
            }
        }
        
        return discount.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Tính tổng voucher discount cho TẤT CẢ shop cùng lúc (đảm bảo không vượt maxDiscount)
     * 
     * @param voucher Voucher to apply
     * @param grandTotal Tổng tiền TẤT CẢ shop
     * @param storeAmountsAfterPromotion Map<storeId, amount> sau khi đã trừ promotion
     * @return Map<storeId, voucherDiscount>
     */
    public java.util.Map<String, BigDecimal> calculateVoucherDiscountsForAllStores(
            Voucher voucher, BigDecimal grandTotal, java.util.Map<String, BigDecimal> storeAmountsAfterPromotion) {
        
        java.util.Map<String, BigDecimal> voucherDiscounts = new java.util.HashMap<>();
        
        if (voucher == null || storeAmountsAfterPromotion == null || storeAmountsAfterPromotion.isEmpty()) {
            return voucherDiscounts;
        }
        
        // CHỈ HỖ TRỢ PERCENTAGE
        if (voucher.getDiscountType() != Voucher.DiscountType.PERCENTAGE) {
            throw new RuntimeException("Voucher chỉ hỗ trợ giảm theo phần trăm");
        }
        
        // Tính discount % cho từng shop
        BigDecimal totalDiscount = BigDecimal.ZERO;
        
        for (java.util.Map.Entry<String, BigDecimal> entry : storeAmountsAfterPromotion.entrySet()) {
            String storeId = entry.getKey();
            BigDecimal storeAmount = entry.getValue();
            
            // Tính % discount cho shop này
            BigDecimal discount = storeAmount.multiply(voucher.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            
            // Đảm bảo không vượt quá storeAmount
            if (discount.compareTo(storeAmount) > 0) {
                discount = storeAmount;
            }
            
            voucherDiscounts.put(storeId, discount);
            totalDiscount = totalDiscount.add(discount);
        }
        
        // ÁP DỤNG MAX DISCOUNT (nếu tổng vượt quá)
        if (voucher.getMaxDiscount() != null && 
            voucher.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0 &&
            totalDiscount.compareTo(voucher.getMaxDiscount()) > 0) {
            
            // Tổng discount vượt quá max, cần scale down theo tỷ lệ
            BigDecimal scaleFactor = voucher.getMaxDiscount()
                    .divide(totalDiscount, 4, RoundingMode.HALF_UP);
            
            for (String storeId : voucherDiscounts.keySet()) {
                BigDecimal originalDiscount = voucherDiscounts.get(storeId);
                BigDecimal scaledDiscount = originalDiscount.multiply(scaleFactor)
                        .setScale(2, RoundingMode.HALF_UP);
                voucherDiscounts.put(storeId, scaledDiscount);
            }
        }
        
        return voucherDiscounts;
    }
    
    /**
     * DEPRECATED - Giữ lại để tương thích ngược, nhưng nên dùng calculateVoucherDiscountForStore
     */
    @Deprecated
    public BigDecimal calculateVoucherDiscount(Voucher voucher, BigDecimal orderAmount) {
        if (voucher == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discount = BigDecimal.ZERO;
        
        if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
            // Giảm theo phần trăm
            discount = orderAmount.multiply(voucher.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else if (voucher.getDiscountType() == Voucher.DiscountType.FIXED) {
            // Giảm theo số tiền cố định
            discount = voucher.getDiscountValue();
        }
        
        // Đảm bảo discount không vượt quá orderAmount
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }
        
        return discount.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Tính số tiền giảm giá từ promotion
     */
    public BigDecimal calculatePromotionDiscount(Promotion promotion, BigDecimal orderAmount) {
        if (promotion == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discount = BigDecimal.ZERO;
        
        if (promotion.getDiscountType() == Promotion.DiscountType.PERCENTAGE) {
            // Giảm theo phần trăm
            discount = orderAmount.multiply(promotion.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            
            // Áp dụng giảm giá tối đa nếu có
            if (promotion.getMaxDiscount() != null && 
                promotion.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0 &&
                discount.compareTo(promotion.getMaxDiscount()) > 0) {
                discount = promotion.getMaxDiscount();
            }
        } else if (promotion.getDiscountType() == Promotion.DiscountType.FIXED_AMOUNT) {
            // Giảm theo số tiền cố định
            discount = promotion.getDiscountValue();
        }
        // FREE_SHIPPING sẽ được xử lý riêng ở shipping fee
        
        // Đảm bảo discount không vượt quá orderAmount
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }
        
        return discount.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Tính tổng số tiền sau khi áp dụng tất cả giảm giá
     * Ưu tiên: Promotion (của shop) trước, sau đó Voucher (toàn sàn)
     */
    public DiscountResult calculateTotalDiscount(BigDecimal originalAmount, 
                                                   Promotion promotion, 
                                                   Voucher voucher) {
        DiscountResult result = new DiscountResult();
        result.setOriginalAmount(originalAmount);
        
        BigDecimal currentAmount = originalAmount;
        
        // Áp dụng promotion trước (giảm giá của shop)
        if (promotion != null) {
            BigDecimal promotionDiscount = calculatePromotionDiscount(promotion, currentAmount);
            result.setPromotionDiscount(promotionDiscount);
            currentAmount = currentAmount.subtract(promotionDiscount);
        }
        
        // Sau đó áp dụng voucher (giảm giá toàn sàn)
        if (voucher != null) {
            BigDecimal voucherDiscount = calculateVoucherDiscount(voucher, currentAmount);
            result.setVoucherDiscount(voucherDiscount);
            currentAmount = currentAmount.subtract(voucherDiscount);
        }
        
        result.setTotalDiscount(result.getPromotionDiscount().add(result.getVoucherDiscount()));
        result.setFinalAmount(currentAmount);
        
        return result;
    }
    
    /**
     * Class chứa kết quả tính toán giảm giá
     */
    public static class DiscountResult {
        private BigDecimal originalAmount = BigDecimal.ZERO;
        private BigDecimal promotionDiscount = BigDecimal.ZERO;
        private BigDecimal voucherDiscount = BigDecimal.ZERO;
        private BigDecimal totalDiscount = BigDecimal.ZERO;
        private BigDecimal finalAmount = BigDecimal.ZERO;
        
        // Getters and Setters
        public BigDecimal getOriginalAmount() {
            return originalAmount;
        }
        
        public void setOriginalAmount(BigDecimal originalAmount) {
            this.originalAmount = originalAmount;
        }
        
        public BigDecimal getPromotionDiscount() {
            return promotionDiscount;
        }
        
        public void setPromotionDiscount(BigDecimal promotionDiscount) {
            this.promotionDiscount = promotionDiscount;
        }
        
        public BigDecimal getVoucherDiscount() {
            return voucherDiscount;
        }
        
        public void setVoucherDiscount(BigDecimal voucherDiscount) {
            this.voucherDiscount = voucherDiscount;
        }
        
        public BigDecimal getTotalDiscount() {
            return totalDiscount;
        }
        
        public void setTotalDiscount(BigDecimal totalDiscount) {
            this.totalDiscount = totalDiscount;
        }
        
        public BigDecimal getFinalAmount() {
            return finalAmount;
        }
        
        public void setFinalAmount(BigDecimal finalAmount) {
            this.finalAmount = finalAmount;
        }
    }
}
