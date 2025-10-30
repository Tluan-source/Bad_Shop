package vn.iotstar.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.OrderItem;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Review;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OrderItemRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.ReviewImageRepository;
import vn.iotstar.repository.ReviewRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.UserService;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final ReviewImageRepository reviewImageRepository;

    // ==============================================================
    // ✅ GET - Hiển thị form đánh giá
    // ==============================================================
    @GetMapping("/submit-review")
    public String showReviewForm(@RequestParam("orderId") String orderId,
                                 Authentication auth,
                                 Model model) {

        System.out.println("🟢 [DEBUG] >>> Entered showReviewForm(), orderId = " + orderId);

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        var user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Lấy danh sách sản phẩm trong đơn hàng
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        if (items.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy sản phẩm nào trong đơn hàng này!");
            return "user/review";
        }

        // Kiểm tra sản phẩm đã được đánh giá chưa
         for (OrderItem item : items) {
            boolean reviewed = reviewRepository.existsByOrderItemIdAndUserId(item.getId(), user.getId());
            item.setReviewed(reviewed);
         }
        // 🟢 Parse JSON ảnh của product (ví dụ ["url1", "url2"]) → giữ ảnh đầu tiên
        ObjectMapper mapper = new ObjectMapper();
        for (OrderItem item : items) {
            String listImagesJson = item.getProduct().getListImages();
            if (listImagesJson != null && listImagesJson.startsWith("[")) {
                try {
                    List<String> imgs = mapper.readValue(listImagesJson, new TypeReference<List<String>>() {});
                    if (!imgs.isEmpty()) {
                        // Gán lại giá trị để Thymeleaf hiển thị được trực tiếp
                        item.getProduct().setListImages(imgs.get(0));
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Lỗi parse listImages: " + e.getMessage());
                }
            }
        }

        // Kiểm tra quyền sở hữu đơn hàng
        Order order = items.get(0).getOrder();
        if (!order.getUser().getId().equals(user.getId())) {
            return "redirect:/orders";
        }

        // Truyền dữ liệu sang view
        model.addAttribute("order", order);
        model.addAttribute("items", items);

        return "user/review";
    }

    // ==============================================================
    // ✅ POST - Gửi đánh giá
    // ==============================================================
    @PostMapping
    @Transactional
    public String submitReview(
            @RequestParam String orderItemId,
            @RequestParam String productId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false, name = "images") List<MultipartFile> images,
            Authentication auth,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Order item not found"));
        Order order = item.getOrder();

        if (!order.getUser().getId().equals(user.getId())
                || order.getStatus() != Order.OrderStatus.DELIVERED) {
            return "redirect:/orders/" + order.getId();
        }

        if (rating == null || rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("reviewError", "Rating phải trong khoảng 1 đến 5.");
            return "redirect:/orders/" + order.getId();
        }
        if (comment != null && comment.length() > 1000) {
            redirectAttributes.addFlashAttribute("reviewError", "Bình luận tối đa 1000 ký tự.");
            return "redirect:/orders/" + order.getId();
        }

        var existing = reviewRepository.findByOrderItem_IdAndUser_Id(orderItemId, user.getId());
        Review review;
        if (existing.isPresent()) {
            review = existing.get();
            review.setRating(BigDecimal.valueOf(rating));
            review.setComment(comment);
        } else {
            review = new Review();
            review.setId(UUID.randomUUID().toString());
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            review.setProduct(product);
            review.setUser(user);
            review.setOrderItem(item);
            review.setRating(BigDecimal.valueOf(rating));
            review.setComment(comment);
        }
        reviewRepository.save(review);

        // Upload ảnh Cloudinary
        if (images != null && !images.isEmpty()) {
            int uploadedCount = 0;
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) continue;
                if (uploadedCount >= 5) break;
                String ct = file.getContentType();
                long size = file.getSize();
                if (ct == null || !ct.toLowerCase().startsWith("image/")) continue;
                if (size > 5L * 1024 * 1024) continue;

                String url = cloudinaryService.uploadFile(file);
                if (url != null) {
                    vn.iotstar.entity.ReviewImage img = new vn.iotstar.entity.ReviewImage();
                    img.setId(UUID.randomUUID().toString());
                    img.setReview(review);
                    img.setUrl(url);
                    img.setType(vn.iotstar.entity.ReviewImage.MediaType.IMAGE);
                    reviewImageRepository.save(img);
                    uploadedCount++;
                }
            }
        }

        // Cập nhật rating trung bình
        List<Review> productReviews = reviewRepository.findByProduct_Id(productId);
        BigDecimal sum = BigDecimal.ZERO;
        for (Review r : productReviews) {
            sum = sum.add(r.getRating() == null ? BigDecimal.ZERO : r.getRating());
        }
        BigDecimal avg = productReviews.isEmpty() ? BigDecimal.ZERO
                : sum.divide(BigDecimal.valueOf(productReviews.size()), 1, RoundingMode.HALF_UP);

        Product product = productRepository.findById(productId).orElseThrow();
        product.setRating(avg);
        productRepository.save(product);

        // Cập nhật rating trung bình cửa hàng
        Store store = product.getStore();
        List<Product> products = productRepository.findByStoreIdAndIsActiveTrue(store.getId());
        BigDecimal sumStore = BigDecimal.ZERO;
        int count = 0;
        for (Product p : products) {
            if (p.getRating() != null) {
                sumStore = sumStore.add(p.getRating());
                count++;
            }
        }
        BigDecimal storeAvg = count == 0 ? BigDecimal.ZERO
                : sumStore.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);
        store.setRating(storeAvg);
        storeRepository.save(store);

        redirectAttributes.addFlashAttribute("reviewSuccess", "Gửi đánh giá thành công.");
        return "redirect:/orders/" + order.getId();
    }
}
