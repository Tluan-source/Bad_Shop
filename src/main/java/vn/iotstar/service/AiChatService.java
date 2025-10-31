package vn.iotstar.service;

import java.util.List;
import java.util.Map;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import vn.iotstar.entity.Product;
import vn.iotstar.entity.Category;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.CategoryRepository;

@Service
public class AiChatService {

    private final WebClient openAiClient;
    private final String apiKey;
    private final String openRouterModel;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public AiChatService(WebClient openAiClient, 
                         @Value("${OPENAI_API_KEY:}") String apiKey,
                         @Value("${OPENROUTER_MODEL:meta-llama/llama-3.1-8b-instruct:free}") String openRouterModel,
                         ProductRepository productRepository,
                         CategoryRepository categoryRepository) {
        this.openAiClient = openAiClient;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.openRouterModel = openRouterModel;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public String chat(String userMessage) {
        // Fallback if API key is not configured
        if (apiKey.isEmpty()) {
            return fallbackReply(userMessage, "Hệ thống chưa cấu hình AI. Tôi vẫn có thể gợi ý sản phẩm cơ bản.");
        }

        // Build small product context from DB when possible (RAG-light)
        String categoryId = inferCategoryId(userMessage);
        BigDecimal maxPrice = inferMaxPrice(userMessage);
        String productContext = buildProductContext(categoryId, maxPrice);

        // DB-first deterministic suggestions: if we can identify category, try to return curated list directly
        String curated = tryDbFirstSuggestion(categoryId, maxPrice, userMessage);
        if (curated != null && !curated.isBlank()) {
            return curated; // short-circuit without calling AI
        }

        // Build category info from database
        List<Category> allCategories = categoryRepository.findByIsActiveTrueOrderByNameAsc();
        StringBuilder categoryInfo = new StringBuilder();
        if (!allCategories.isEmpty()) {
            categoryInfo.append("Danh mục hiện có: ");
            for (int i = 0; i < allCategories.size(); i++) {
                Category cat = allCategories.get(i);
                categoryInfo.append(cat.getId()).append("=").append(cat.getName());
                if (i < allCategories.size() - 1) categoryInfo.append(", ");
            }
            categoryInfo.append(". ");
        }
        
        String systemPrompt = String.join(" ",
                "Bạn hãy trả lời tự nhiên, lịch sự và gọi khách hàng trước khi nói chuyện nhé",
                "Bạn là trợ lý mua sắm cho website Bad Shop (nội bộ), chỉ nói những sản phẩm có trong dữ liệu",
                "Trả lời BẰNG TIẾNG VIỆT, dùng thuật ngữ Việt. Không dùng từ tiếng Anh như 'Racket'.",
                categoryInfo.toString(),
                "Chỉ gợi ý sản phẩm/đường dẫn NỘI BỘ của website, KHÔNG đưa link ngoài.",
                "Nếu cần link, dùng các dạng: /products, /products/{id}, /products?category={categoryId},",
                "/products?category={categoryId}&maxPrice=50000 (nếu người dùng nói ngân sách), /stores/{id}.",
                "Luôn trả lời ngắn gọn, gợi ý mức giá trong cửa hàng nếu biết, hỏi rõ ngân sách khi cần.",
                "Tuyệt đối tránh mọi link ngoài hoặc tên sàn TMĐT khác."
        );

        Map<String, Object> body = Map.of(
                "model", openRouterModel,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.4
        );

        try {
            int[] backoffMs = new int[] { 400, 1000, 2000 };
            for (int attempt = 0; attempt < backoffMs.length; attempt++) {
                try {
                    Map<?, ?> response = openAiClient.post()
                            .uri("/chat/completions")
                            .bodyValue(body)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

                    if (response == null) {
                        return fallbackReply(userMessage, "Không nhận được phản hồi từ AI.");
                    }

                    Object choicesObj = response.get("choices");
                    if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
                        return fallbackReply(userMessage, "AI không có phản hồi.");
                    }
                    Object first = choices.get(0);
                    if (!(first instanceof Map<?, ?> firstMap)) {
                        return fallbackReply(userMessage, "AI không có phản hồi.");
                    }
                    Object messageObj = firstMap.get("message");
                    if (!(messageObj instanceof Map<?, ?> msg)) {
                        return fallbackReply(userMessage, "AI không có phản hồi.");
                    }
                    Object content = msg.get("content");
                    String text = content instanceof String s ? s : "";
                    if (text == null || text.isBlank()) {
                        return fallbackReply(userMessage, "AI phản hồi rỗng.");
                    }
                    // Tránh trả về 1 link đơn lẻ: chuẩn hóa gợi ý giàu thông tin và nhiều lựa chọn
                    String normalized = text.trim();
                    boolean looksLikeBareUrl = normalized.startsWith("/products");
                    boolean tooShort = normalized.length() < 80; // thiếu ngữ cảnh
                    // Nếu phản hồi quá ngắn hoặc chỉ là một URL, vẫn trả về như hiện tại (đã tối giản logic)
                    return normalized;
                } catch (WebClientResponseException ex) {
                    int code = ex.getStatusCode().value();
                    if (code == 429 || (code >= 500 && code < 600)) {
                        try { Thread.sleep(backoffMs[attempt]); } catch (InterruptedException ignored) {}
                        continue; // retry
                    }
                    String extra = "";
                    if (code == 400) {
                        extra = " (có thể do model không hợp lệ. Hãy đặt OPENROUTER_MODEL thành một model hợp lệ, ví dụ 'meta-llama/llama-3.1-8b-instruct:free' hoặc 'qwen/qwen-2.5-7b-instruct:free').";
                    }
                    return fallbackReply(userMessage, "Lỗi AI: " + ex.getStatusCode() + extra);
                }
            }
            return fallbackReply(userMessage, "AI đang quá tải, vui lòng thử lại sau.");
        } catch (Exception ex) {
            return fallbackReply(userMessage, "Không thể kết nối AI.");
        }
    }

    private String fallbackReply(String userMessage, String note) {
        String lower = userMessage == null ? "" : userMessage.toLowerCase();
        if (lower.contains("20k") || lower.contains("20 k") || lower.contains("20000")) {
            // Tìm category phụ kiện trong database
            List<Category> categories = categoryRepository.findByIsActiveTrueOrderByNameAsc();
            String accessoryCategoryId = null;
            for (Category cat : categories) {
                String catName = cat.getName().toLowerCase();
                if (catName.contains("phụ kiện") || catName.contains("accessory")) {
                    accessoryCategoryId = cat.getId();
                    break;
                }
            }
            String categoryLink = accessoryCategoryId != null ? "/products?category=" + accessoryCategoryId : "/products";
            return note + " Gợi ý: Với ngân sách ~20k, bạn có thể tham khảo phụ kiện nhỏ như quấn cán hoặc tất. Vợt chính hãng thường từ 300k+. Xem danh mục phụ kiện tại " + categoryLink + ".";
        }
        if (lower.contains("vợt")) {
            // Tìm category vợt trong database
            List<Category> categories = categoryRepository.findByIsActiveTrueOrderByNameAsc();
            String racketCategoryId = null;
            for (Category cat : categories) {
                String catName = cat.getName().toLowerCase();
                if (catName.contains("vợt") || catName.contains("racket")) {
                    racketCategoryId = cat.getId();
                    break;
                }
            }
            String categoryLink = racketCategoryId != null ? "/products?category=" + racketCategoryId : "/products";
            return note + " Bạn muốn vợt giá tầm nào? Phổ biến: 300k-700k (cơ bản), 700k-1.5m (trung cấp), 1.5m+ (cao cấp). Bạn có thể xem nhanh tại " + categoryLink + ".";
        }
        return note + " Bạn có thể hỏi về vợt, giày hoặc phụ kiện và ngân sách mong muốn.";
    }

    private String inferCategoryId(String message) {
        if (message == null) return null;
        String lower = message.toLowerCase();
        
        // Lấy tất cả category active từ database
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByNameAsc();
        
        // Tìm category theo từ khóa trong message
        for (Category category : categories) {
            String categoryName = category.getName().toLowerCase();
            
            // Kiểm tra từ khóa "vợt"
            if (lower.contains("vợt") && (categoryName.contains("vợt") || categoryName.contains("racket"))) {
                return category.getId();
            }
            
            // Kiểm tra từ khóa "giày"
            if (lower.contains("giày") && (categoryName.contains("giày") || categoryName.contains("shoe"))) {
                return category.getId();
            }
            
            // Kiểm tra từ khóa "quả cầu" hoặc "cầu lông"
            if ((lower.contains("quả cầu") || lower.contains("cầu lông")) && 
                (categoryName.contains("quả cầu") || categoryName.contains("cầu lông") || categoryName.contains("shuttle"))) {
                return category.getId();
            }
            
            // Kiểm tra từ khóa "phụ kiện"
            if ((lower.contains("phụ kiện") || lower.contains("quấn cán") || lower.contains("bọc grip") || lower.contains("tất")) && 
                (categoryName.contains("phụ kiện") || categoryName.contains("accessory"))) {
                return category.getId();
            }
            
            // Kiểm tra nếu message chứa tên category
            if (categoryName.length() > 2 && lower.contains(categoryName)) {
                return category.getId();
            }
        }
        
        return null;
    }

    private BigDecimal inferMaxPrice(String message) {
        if (message == null) return null;
        String lower = message.toLowerCase();
        // Find patterns like 50k, 50 k, 50000, 50.000
        try {
            String digits = lower.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) return null;
            long value = Long.parseLong(digits);
            if (lower.contains("k") && value < 1000) value = value * 1000; // 50k -> 50000
            if (value <= 0) return null;
            return BigDecimal.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildProductContext(String categoryId, BigDecimal maxPrice) {
        try {
            if (categoryId == null && maxPrice == null) return "";
            List<Product> products;
            if (categoryId != null && maxPrice != null) {
                products = productRepository.findTopByCategoryAndMaxPrice(categoryId, maxPrice, PageRequest.of(0, 6));
            } else if (categoryId != null) {
                products = productRepository.findByCategoryIdAndIsActiveTrueAndIsSellingTrue(categoryId);
                if (products.size() > 6) products = new ArrayList<>(products.subList(0, 6));
            } else {
                products = new ArrayList<>();
            }
            if (products.isEmpty()) return "";

            StringBuilder sb = new StringBuilder("\nNgữ cảnh sản phẩm (nội bộ):\n");
            for (Product p : products) {
                java.math.BigDecimal effective = p.getPromotionalPrice() != null ? p.getPromotionalPrice() : p.getPrice();
                sb.append("- ").append(p.getName())
                  .append(" | ").append(effective != null ? effective.longValue() : 0).append(" đ")
                  .append(" | link: /products/").append(p.getId())
                  .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String tryDbFirstSuggestion(String categoryId, BigDecimal budget, String message) {
        try {
            if (categoryId == null) return null;
            List<Product> picked = new ArrayList<>();
            
            // Nếu có budget: tìm sản phẩm theo budget
            if (budget != null) {
                // 1) exact/under budget, closest first
                picked = productRepository.findTopClosestUnderBudget(categoryId, budget, PageRequest.of(0, 3));
                // 2) nearest within ±20% nếu không tìm thấy
                if (picked == null || picked.isEmpty()) {
                    BigDecimal min = budget.subtract(budget.multiply(new BigDecimal("0.20")));
                    if (min.compareTo(BigDecimal.ZERO) < 0) min = BigDecimal.ZERO;
                    BigDecimal max = budget.add(budget.multiply(new BigDecimal("0.20")));
                    picked = productRepository.findNearestWithinRange(categoryId, budget, min, max, PageRequest.of(0, 3));
                }
            }
            
            // Nếu không có budget hoặc chưa tìm thấy: lấy top sản phẩm trong category
            if (picked == null || picked.isEmpty()) {
                // Lấy top sản phẩm bán chạy hoặc sản phẩm trong category (tối đa 5 sản phẩm)
                List<Product> categoryProducts = productRepository.findByCategoryIdAndIsActiveTrueAndIsSellingTrue(categoryId);
                if (categoryProducts != null && !categoryProducts.isEmpty()) {
                    // Sắp xếp theo sold (bán chạy) hoặc rating, sau đó lấy top 5
                    picked = categoryProducts.stream()
                        .sorted((p1, p2) -> {
                            // Ưu tiên sản phẩm bán chạy, sau đó là rating
                            int soldCompare = Integer.compare(
                                p2.getSold() != null ? p2.getSold() : 0,
                                p1.getSold() != null ? p1.getSold() : 0
                            );
                            if (soldCompare != 0) return soldCompare;
                            
                            // Nếu sold bằng nhau, sắp xếp theo rating
                            BigDecimal r1 = p1.getRating() != null ? p1.getRating() : BigDecimal.ZERO;
                            BigDecimal r2 = p2.getRating() != null ? p2.getRating() : BigDecimal.ZERO;
                            return r2.compareTo(r1);
                        })
                        .limit(5)
                        .collect(Collectors.toList());
                }
            }
            
            // Fallback cuối cùng: lấy sản phẩm rẻ nhất nếu vẫn không có
            if (picked == null || picked.isEmpty()) {
                picked = productRepository.findCheapestByCategory(categoryId, PageRequest.of(0, 3));
            }
            
            // Nếu vẫn không có sản phẩm trong category
            if (picked == null || picked.isEmpty()) {
                return null; // Trả về null để gọi AI thay vì trả về message lỗi
            }

            // Lấy tên category từ database
            String categoryName = "sản phẩm";
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                categoryName = category.getName();
            }
            
            String intro = "Chào bạn! Dưới đây là các " + categoryName + " đang bán chạy tại Bad Shop:";

            StringBuilder sb = new StringBuilder(intro).append("\n\n");
            int index = 1;
            for (Product p : picked) {
                java.math.BigDecimal effective = p.getPromotionalPrice() != null ? p.getPromotionalPrice() : p.getPrice();
                String priceStr = effective != null ? String.format("%,d", effective.longValue()) : "0";
                sb.append(index).append(". ")
                  .append(p.getName())
                  .append(" - ")
                  .append(priceStr).append(" đ");
                if (p.getPromotionalPrice() != null && p.getPromotionalPrice().compareTo(p.getPrice()) < 0) {
                    sb.append(" (Giảm giá từ ").append(String.format("%,d", p.getPrice().longValue())).append(" đ)");
                }
                if (p.getRating() != null && p.getRating().compareTo(BigDecimal.ZERO) > 0) {
                    sb.append(" ⭐ ").append(p.getRating());
                }
                sb.append("\n   Xem chi tiết: /products/").append(p.getId()).append("\n");
                index++;
            }

            // add follow-up question
            sb.append("\nBạn có muốn tìm theo ngân sách cụ thể, màu sắc, hay thương hiệu nào không?");
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    // Removed curated suggestion builder to keep service concise
}


