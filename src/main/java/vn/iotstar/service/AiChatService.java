package vn.iotstar.service;

import java.util.List;
import java.util.Map;

import java.math.BigDecimal;
import java.util.ArrayList;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import vn.iotstar.entity.Product;
import vn.iotstar.repository.ProductRepository;

@Service
public class AiChatService {

    private final WebClient openAiClient;
    private final String apiKey;
    private final String openRouterModel;
    private final ProductRepository productRepository;

    public AiChatService(WebClient openAiClient, 
                         @Value("${OPENAI_API_KEY:}") String apiKey,
                         @Value("${OPENROUTER_MODEL:meta-llama/llama-3.1-8b-instruct:free}") String openRouterModel,
                         ProductRepository productRepository) {
        this.openAiClient = openAiClient;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.openRouterModel = openRouterModel;
        this.productRepository = productRepository;
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

        String systemPrompt = String.join(" ",
                "Bạn là trợ lý mua sắm cho website Bad Shop (nội bộ).",
                "Trả lời BẰNG TIẾNG VIỆT, dùng thuật ngữ Việt. Không dùng từ tiếng Anh như 'Racket'.",
                "Danh mục nội bộ: C1=Vợt, C2=Quả cầu & Phụ kiện, C3=Giày, C4=Phụ kiện.",
                "Chỉ gợi ý sản phẩm/đường dẫn NỘI BỘ của website, KHÔNG đưa link ngoài.",
                "Nếu cần link, dùng các dạng: /products, /products/{id}, /products?category=C1|C2|C3|C4,",
                "/products?category=C4&maxPrice=50000 (nếu người dùng nói ngân sách), /stores/{id}.",
                "Nếu không chắc ID, hãy dẫn về danh mục phù hợp (ví dụ phụ kiện: category=C4).",
                "Luôn trả lời ngắn gọn, gợi ý mức giá trong cửa hàng nếu biết, hỏi rõ ngân sách khi cần.",
                "Tuyệt đối tránh mọi link ngoài hoặc tên sàn TMĐT khác.",
                productContext
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
                    return text.isBlank() ? fallbackReply(userMessage, "AI phản hồi rỗng.") : text;
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
            return note + " Gợi ý: Với ngân sách ~20k, bạn có thể tham khảo phụ kiện nhỏ như quấn cán hoặc tất. Vợt chính hãng thường từ 300k+. Xem danh mục phụ kiện tại /products?category=C4.";
        }
        if (lower.contains("vợt")) {
            return note + " Bạn muốn vợt giá tầm nào? Phổ biến: 300k-700k (cơ bản), 700k-1.5m (trung cấp), 1.5m+ (cao cấp). Bạn có thể xem nhanh tại /products?category=C1.";
        }
        return note + " Bạn có thể hỏi về vợt, giày hoặc phụ kiện và ngân sách mong muốn.";
    }

    private String inferCategoryId(String message) {
        if (message == null) return null;
        String lower = message.toLowerCase();
        if (lower.contains("phụ kiện") || lower.contains("quấn cán") || lower.contains("bọc grip") || lower.contains("tất")) return "C4"; // accessories
        if (lower.contains("vợt")) return "C1"; // rackets
        if (lower.contains("giày")) return "C3"; // shoes
        if (lower.contains("quả cầu") || lower.contains("cầu lông")) return "C2"; // shuttles & apparel
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
}


