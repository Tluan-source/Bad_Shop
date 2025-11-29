package vn.iotstar.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.OrderItem;
import vn.iotstar.entity.Review;
import vn.iotstar.entity.StyleValue;
import vn.iotstar.repository.ReviewRepository;
import vn.iotstar.repository.StyleValueRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StyleValueRepository styleValueRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Lấy danh sách đánh giá theo Product ID
     * Đồng thời parse JSON styleValueIds trong OrderItem
     * để hiển thị "Màu sắc: Đỏ", "Size: L", ...
     */
    public List<Review> getReviewsByProductId(String productId) {
        List<Review> reviews = reviewRepository.findByProductIdWithImages(productId);

        for (Review rv : reviews) {
            try {
                OrderItem oi = rv.getOrderItem();
                if (oi == null || oi.getStyleValueIds() == null || oi.getStyleValueIds().isBlank())
                    continue;

                // Parse JSON: ["SV_1","SV_2"]
                List<String> styleIds = mapper.readValue(
                        oi.getStyleValueIds(),
                        new TypeReference<List<String>>() {}
                );

                if (!styleIds.isEmpty()) {
                    // Lấy danh sách StyleValue tương ứng
                    List<StyleValue> styleValues = styleValueRepository.findAllById(styleIds);

                    // Ghép dạng "Tên thuộc tính: Giá trị"
                    List<String> styleNames = styleValues.stream()
                            .map(sv -> sv.getStyle().getName() + ": " + sv.getName())
                            .toList();

                    oi.setStyleValueNames(styleNames);
                }

            } catch (Exception e) {
                // Log but don't break the loop - continue to next review
                System.err.println("Error parsing styleValueIds for review " + rv.getId() + ": " + e.getMessage());
            }
        }

        return reviews;
    }
    
    /**
     * Get review by ID
     */
    public Review getReviewById(String reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
    }
    
    /**
     * Add vendor reply to review
     */
    public void addVendorReply(String reviewId, String vendorReply, String storeId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        // Verify that this review belongs to a product in this store
        if (!review.getProduct().getStore().getId().equals(storeId)) {
            throw new RuntimeException("Bạn không có quyền trả lời đánh giá này");
        }
        
        review.setVendorReply(vendorReply);
        review.setVendorReplyAt(java.time.LocalDateTime.now());
        reviewRepository.save(review);
    }
}
