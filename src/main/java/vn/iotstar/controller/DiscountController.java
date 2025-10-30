package vn.iotstar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.dto.ValidateDiscountRequest;
import vn.iotstar.dto.ValidateDiscountResponse;
import vn.iotstar.entity.Promotion;
import vn.iotstar.entity.Voucher;
import vn.iotstar.repository.PromotionRepository;
import vn.iotstar.repository.VoucherRepository;
import vn.iotstar.service.DiscountService;
import vn.iotstar.service.DiscountService.DiscountResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST API Controller để xử lý các yêu cầu liên quan đến giảm giá
 */
@RestController
@RequestMapping("/api/discount")
@RequiredArgsConstructor
public class DiscountController {
    
    private final DiscountService discountService;
    private final VoucherRepository voucherRepository;
    private final PromotionRepository promotionRepository;
    
    /**
     * Validate và tính toán giảm giá
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidateDiscountResponse> validateDiscount(@RequestBody ValidateDiscountRequest request) {
        
        if (request.getOrderAmount() == null || request.getOrderAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                    .body(ValidateDiscountResponse.error("Số tiền đơn hàng không hợp lệ"));
        }
        
        ValidateDiscountResponse response = new ValidateDiscountResponse();
        response.setSuccess(true);
        response.setOriginalAmount(request.getOrderAmount());
        response.setVoucherDiscount(BigDecimal.ZERO);
        response.setPromotionDiscount(BigDecimal.ZERO);
        response.setTotalDiscount(BigDecimal.ZERO);
        response.setFinalAmount(request.getOrderAmount());
        
        Voucher voucher = null;
        Promotion promotion = null;
        
        // Validate voucher
        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            Optional<Voucher> voucherOpt = discountService.validateVoucher(
                    request.getVoucherCode(), 
                    request.getOrderAmount()
            );
            
            if (voucherOpt.isPresent()) {
                voucher = voucherOpt.get();
                response.setVoucherValid(true);
                response.setVoucherCode(voucher.getCode());
            } else {
                response.setVoucherValid(false);
                response.setVoucherError(getVoucherErrorMessage(request.getVoucherCode(), request.getOrderAmount()));
            }
        }
        
        // Validate promotion
        if (request.getPromotionId() != null && !request.getPromotionId().trim().isEmpty()) {
            if (request.getStoreId() == null || request.getStoreId().trim().isEmpty()) {
                response.setPromotionValid(false);
                response.setPromotionError("Thiếu thông tin shop");
            } else {
                Optional<Promotion> promotionOpt = discountService.validatePromotion(
                        request.getPromotionId(), 
                        request.getStoreId(), 
                        request.getOrderAmount()
                );
                
                if (promotionOpt.isPresent()) {
                    promotion = promotionOpt.get();
                    response.setPromotionValid(true);
                    response.setPromotionId(promotion.getId());
                    response.setPromotionName(promotion.getName());
                } else {
                    response.setPromotionValid(false);
                    response.setPromotionError(getPromotionErrorMessage(request.getPromotionId(), request.getOrderAmount()));
                }
            }
        }
        
        // Tính toán giảm giá
        DiscountResult result = discountService.calculateTotalDiscount(
                request.getOrderAmount(), 
                promotion, 
                voucher
        );
        
        response.setPromotionDiscount(result.getPromotionDiscount());
        response.setVoucherDiscount(result.getVoucherDiscount());
        response.setTotalDiscount(result.getTotalDiscount());
        response.setFinalAmount(result.getFinalAmount());
        response.setMessage("Tính toán giảm giá thành công");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy danh sách voucher khả dụng
     */
    @GetMapping("/vouchers/available")
    public ResponseEntity<List<Voucher>> getAvailableVouchers() {
        List<Voucher> vouchers = voucherRepository.findAvailableVouchers(LocalDateTime.now());
        return ResponseEntity.ok(vouchers);
    }
    
    /**
     * Lấy danh sách promotion của một shop
     */
    @GetMapping("/promotions/store/{storeId}")
    public ResponseEntity<List<Promotion>> getStorePromotions(@PathVariable String storeId) {
        List<Promotion> promotions = promotionRepository.findActivePromotions(storeId, LocalDateTime.now());
        return ResponseEntity.ok(promotions);
    }
    
    /**
     * Kiểm tra mã voucher
     */
    @GetMapping("/voucher/check/{code}")
    public ResponseEntity<?> checkVoucher(@PathVariable String code, 
                                           @RequestParam(required = false) BigDecimal orderAmount) {
        
        Optional<Voucher> voucherOpt = voucherRepository.findByCode(code.trim().toUpperCase());
        
        if (voucherOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Mã voucher không tồn tại"));
        }
        
        Voucher voucher = voucherOpt.get();
        
        if (!voucher.isAvailable()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(getVoucherUnavailableReason(voucher)));
        }
        
        if (orderAmount != null && voucher.getMinOrderValue() != null 
                && orderAmount.compareTo(voucher.getMinOrderValue()) < 0) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Đơn hàng tối thiểu " + voucher.getMinOrderValue() + "đ"));
        }
        
        return ResponseEntity.ok(voucher);
    }
    
    // Helper methods
    private String getVoucherErrorMessage(String code, BigDecimal orderAmount) {
        Optional<Voucher> voucherOpt = voucherRepository.findByCode(code.trim().toUpperCase());
        
        if (voucherOpt.isEmpty()) {
            return "Mã voucher không tồn tại";
        }
        
        Voucher voucher = voucherOpt.get();
        
        if (!voucher.getIsActive()) {
            return "Mã voucher đã bị vô hiệu hóa";
        }
        
        if (voucher.isExpired()) {
            return "Mã voucher đã hết hạn";
        }
        
        if (voucher.isNotStarted()) {
            return "Mã voucher chưa bắt đầu";
        }
        
        if (voucher.getUsageCount() >= voucher.getQuantity()) {
            return "Mã voucher đã hết lượt sử dụng";
        }
        
        if (voucher.getMinOrderValue() != null && orderAmount.compareTo(voucher.getMinOrderValue()) < 0) {
            return "Đơn hàng tối thiểu " + voucher.getMinOrderValue() + "đ";
        }
        
        return "Mã voucher không hợp lệ";
    }
    
    private String getPromotionErrorMessage(String promotionId, BigDecimal orderAmount) {
        Optional<Promotion> promotionOpt = promotionRepository.findById(promotionId);
        
        if (promotionOpt.isEmpty()) {
            return "Khuyến mãi không tồn tại";
        }
        
        Promotion promotion = promotionOpt.get();
        
        if (!promotion.getIsActive()) {
            return "Khuyến mãi đã bị vô hiệu hóa";
        }
        
        if (promotion.isExpired()) {
            return "Khuyến mãi đã hết hạn";
        }
        
        if (promotion.isNotStarted()) {
            return "Khuyến mãi chưa bắt đầu";
        }
        
        if (promotion.getMinOrderAmount() != null && orderAmount.compareTo(promotion.getMinOrderAmount()) < 0) {
            return "Đơn hàng tối thiểu " + promotion.getMinOrderAmount() + "đ";
        }
        
        return "Khuyến mãi không hợp lệ";
    }
    
    private String getVoucherUnavailableReason(Voucher voucher) {
        if (!voucher.getIsActive()) {
            return "Mã voucher đã bị vô hiệu hóa";
        }
        if (voucher.isExpired()) {
            return "Mã voucher đã hết hạn";
        }
        if (voucher.isNotStarted()) {
            return "Mã voucher chưa bắt đầu";
        }
        if (voucher.getUsageCount() >= voucher.getQuantity()) {
            return "Mã voucher đã hết lượt sử dụng";
        }
        return "Mã voucher không khả dụng";
    }
    
    // Error response class
    private static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
