Phụ thuộc suy luận từ khóa đơn giản: dễ sai ý định (ví dụ “áo thi đấu”), sai danh mục/budget.
“DB-first” chỉ tốt khi dữ liệu đầy đủ, có giá/khuyến mãi chuẩn; thiếu dữ liệu → trả lời nghèo nàn.
Không cá nhân hóa: chưa dùng lịch sử/gu của người dùng, dễ gợi ý chung chung.
Tìm kiếm sản phẩm hạn chế: không lọc theo size/màu/thương hiệu; không có fuzzy search/semantic search.



# AI Chat – Bad Shop

Tài liệu mô tả luồng hoạt động và các thành phần mã nguồn của tính năng Chat AI trong hệ thống Bad Shop.

## 1) Tổng quan luồng

1. Người dùng nhấn nút trợ lý AI nổi ở góc phải dưới và gửi câu hỏi (widget đặt trong `templates/fragments/footer.html`).
2. Frontend POST JSON `{ "message": "..." }` tới REST API nội bộ `/api/ai-chat`.
3. Backend `AiChatController` nhận request, kiểm tra input, gọi `AiChatService.chat(...)`.
4. `AiChatService` (RAG nhẹ):
   - Suy luận `categoryId` và `maxPrice` từ câu người dùng.
   - Truy vấn một danh sách sản phẩm phù hợp từ DB (`ProductRepository`).
   - Ghép “ngữ cảnh sản phẩm (nội bộ)” + system prompt để ràng buộc AI chỉ gợi ý link nội bộ.
   - Gọi API model (OpenRouter hoặc OpenAI) bằng `WebClient` và trả về câu trả lời văn bản.
5. Controller trả về `{ "reply": "..." }`. Widget hiển thị bong bóng chat của AI.

Sơ đồ rút gọn:

`Footer widget → POST /api/ai-chat → AiChatController → AiChatService (RAG + call model) → reply → Footer widget`.

## 2) Biến môi trường

- `OPENROUTER_API_KEY`: API key cho OpenRouter (ưu tiên nếu có).
- `OPENROUTER_MODEL`: tên model, mặc định `meta-llama/llama-3.1-8b-instruct:free` (có thể đổi sang `openrouter/auto` hoặc model hợp lệ khác).
- `OPENAI_API_KEY`: chỉ dùng khi không có `OPENROUTER_API_KEY` (base URL sẽ là OpenAI).

Thiết lập nhanh trên PowerShell (User scope):

```powershell
[System.Environment]::SetEnvironmentVariable("OPENROUTER_API_KEY","YOUR_KEY","User")
[System.Environment]::SetEnvironmentVariable("OPENROUTER_MODEL","openrouter/auto","User")
```

## 3) Thành phần Backend

### 3.1 `vn/iotstar/config/WebClientConfig.java`

- Tạo `WebClient` dùng chung cho AI:
  - Nếu có `OPENROUTER_API_KEY` → base URL `https://openrouter.ai/api/v1`, header `Authorization: Bearer <key>`, thêm `HTTP-Referer` và `X-Title: Bad Shop Chat`.
  - Ngược lại → dùng OpenAI `https://api.openai.com/v1` với `OPENAI_API_KEY`.

### 3.2 `vn/iotstar/controller/AiChatController.java`

- Endpoint:
  - `GET /api/ai-chat/ping` → kiểm tra sống, trả `{ reply: "pong" }`.
  - `POST /api/ai-chat` (JSON body `{ message: string }` hoặc map) → gọi `AiChatService.chat` và trả `{ reply }`.
- Có bắt lỗi/validate để tránh 500, luôn trả về 200 với thông báo an toàn khi lỗi.

### 3.3 `vn/iotstar/service/AiChatService.java`

- Nhiệm vụ chính:
  - RAG nhẹ: suy luận `categoryId` (C1=Vợt, C2=Quả cầu & Phụ kiện, C3=Giày, C4=Phụ kiện) và `maxPrice` (bắt số như 50k/50000).
  - Query sản phẩm từ DB, build context ngắn: `Tên | Giá | /products/{id}` (tối đa 6 mục).
  - Ràng buộc bằng system prompt: bắt buộc tiếng Việt, chỉ link nội bộ, không link ngoài, dùng danh mục nội bộ.
  - Gọi API model qua `WebClient` với body Chat Completions:

    ```json
    {
      "model": "<OPENROUTER_MODEL>",
      "messages": [
        {"role": "system", "content": "...prompt ràng buộc..."},
        {"role": "user",   "content": "<user message>"}
      ],
      "temperature": 0.4
    }
    ```

  - Retry đơn giản khi 429/5xx: backoff 400ms → 1000ms → 2000ms.
  - Fallback thân thiện khi thiếu API key/quá tải/lỗi JSON.

### 3.4 `vn/iotstar/repository/ProductRepository.java`

- Truy vấn phục vụ RAG:

```java
@Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isSelling = true " +
       "AND p.category.id = :categoryId AND COALESCE(p.promotionalPrice, p.price) <= :maxPrice " +
       "ORDER BY COALESCE(p.promotionalPrice, p.price) ASC")
List<Product> findTopByCategoryAndMaxPrice(String categoryId, BigDecimal maxPrice, Pageable pageable);
```

## 4) Bảo mật (Security)

- `SecurityConfig`:
  - Mở quyền `permitAll()` cho `POST /api/ai-chat` để dùng không cần đăng nhập (tùy chọn chỉnh lại nếu muốn).
  - Bỏ qua CSRF cho `/api/ai-chat` để tránh 403 khi gọi AJAX.

## 5) Thành phần Frontend

### 5.1 Widget ở `templates/fragments/footer.html`

- Nút nổi mở chat (hiển thị icon/bot). Panel chat gồm:
  - Header: tên “Trợ lý AI Bad Shop”.
  - Body: vùng hiển thị tin nhắn, bong bóng `.bubble.user` và `.bubble.ai` (CSS wrap tốt, giữ xuống dòng).
  - Footer: form gửi tin (`#ai-form`), input và nút “Gửi”.
- Script chính:
  - Lắng nghe submit, thêm bong bóng user, hiển thị trạng thái “Đang phản hồi…”.
  - Gọi `fetch('/api/ai-chat', { method: 'POST', headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': <cookie> }, body: JSON.stringify({ message }) })`.
  - Xử lý `res.ok`, parse JSON an toàn, fallback khi rỗng/lỗi.
  - Tối ưu UI: auto-scroll, mobile responsive, nút và input co giãn hợp lý.

## 6) Cấu hình nhanh – Checklist

1. Đặt biến môi trường:
   - `OPENROUTER_API_KEY` (khuyến nghị) và `OPENROUTER_MODEL` (ví dụ `openrouter/auto` hoặc một model `:free`).
   - Hoặc `OPENAI_API_KEY` nếu dùng OpenAI.
2. Khởi động lại IDE/app.
3. Mở trang chủ, nhấn nút AI → gửi câu hỏi.
4. Kiểm tra `GET /api/ai-chat/ping` nếu cần chẩn đoán.

## 7) Chẩn đoán lỗi thường gặp

- 403 (trước đây): do CSRF hoặc chưa permitAll → đã cấu hình bỏ qua CSRF cho `/api/ai-chat`.
- 429: hết quota/đang quá tải → service có retry; nếu vẫn lỗi, xem lại credit hoặc dùng model free.
- 400 từ OpenRouter: tên model không hợp lệ → đặt `OPENROUTER_MODEL=openrouter/auto` hoặc một model hợp lệ `...:free`.
- Reply rỗng/không liên quan: kiểm tra system prompt, model, hoặc tăng số lượng sản phẩm ngữ cảnh.

## 8) Tùy biến/Nâng cao

- Debounce gửi câu hỏi (1–1.5s) để giảm gọi API.
- Streaming (SSE) để hiển thị dần từng token (cần đổi `WebClient` sang xử lý stream và JS lắng nghe).
- Rich context: đưa thêm giá khuyến mãi, tồn kho, điểm rating; hoặc truy vấn theo nhiều tiêu chí.
- Bộ lọc UI: thêm preset nút bấm (Vợt/Phụ kiện/Giày + khoảng giá) để gửi prompt chuẩn hóa.

---

Nếu cần switching nhanh giữa OpenRouter/OpenAI, chỉ cần thay biến môi trường. Code tự động chọn OpenRouter khi có `OPENROUTER_API_KEY`.

## 3) Trỏ chính xác vào mã nguồn (click vào đâu – chạy gì)

### 3.1 Nút mở chat, đóng chat, gửi tin – `templates/fragments/footer.html`

- Mở panel khi bấm nút (hiện `#ai-panel`, ẩn nút tròn):
```162:164:src/main/resources/templates/fragments/footer.html
openBtn.addEventListener('click', () => { panel.style.display = 'block'; openBtn.style.display = 'none'; });
closeBtn.addEventListener('click', () => { panel.style.display = 'none'; openBtn.style.display = 'inline-flex'; });
```

- Gửi tin nhắn (submit form), thêm bong bóng người dùng, gọi API, hiển thị phản hồi/ lỗi:
```181:214:src/main/resources/templates/fragments/footer.html
form.addEventListener('submit', async (e) => {
     e.preventDefault();
     const text = input.value.trim();
     if (!text) return;
     addMsg(text, 'user');
     input.value = '';

     addMsg('Đang phản hồi...', 'ai');
     try {
          const csrf = getCsrfToken();
          const res = await fetch('/api/ai-chat', {
               method: 'POST',
               headers: Object.assign({ 'Content-Type': 'application/json' }, csrf ? { 'X-XSRF-TOKEN': csrf } : {}),
               body: JSON.stringify({ message: text })
          });
          if (!res.ok) {
               messages.lastChild.querySelector('span').textContent = 'Lỗi (' + res.status + '). Vui lòng thử lại.';
               return;
          }
          let data;
          try { data = await res.json(); } catch (e) {
               messages.lastChild.querySelector('span').textContent = 'Phản hồi không hợp lệ.'; return; }
          const reply = (data && typeof data.reply === 'string') ? data.reply.trim() : '';
          messages.lastChild.querySelector('span').textContent = reply && reply.length > 0
               ? reply
               : 'Gợi ý: Bạn có thể nói "mua vợt tầm 700k" hoặc "phụ kiện dưới 50k" để mình tư vấn.';
     } catch (err) {
          messages.lastChild.querySelector('span').textContent = 'Lỗi kết nối. Vui lòng thử lại.';
     }
});
```

- Tạo bong bóng chat (giữ xuống dòng, tự wrap):
```165:173:src/main/resources/templates/fragments/footer.html
function addMsg(text, role) {
     const div = document.createElement('div');
     div.className = role === 'user' ? 'text-end mb-2' : 'mb-2';
     const span = document.createElement('span');
     span.className = role === 'user' ? 'bubble user' : 'bubble ai';
     span.textContent = text;
     div.appendChild(span);
     messages.appendChild(div);
}
```

### 3.2 REST API – `vn/iotstar/controller/AiChatController.java`

- Ping kiểm tra nhanh:
```27:30:src/main/java/vn/iotstar/controller/AiChatController.java
@GetMapping(path = "/ping")
public ResponseEntity<ChatResponse> ping() {
    return ResponseEntity.ok(new ChatResponse("pong"));
}
```

- Nhận câu hỏi, gọi service, trả về `{ reply }` (đã chống lỗi 500 và input rỗng):
```32:51:src/main/java/vn/iotstar/controller/AiChatController.java
@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<ChatResponse> chat(@RequestBody(required = false) Map<String, Object> req) {
    try {
        String message = null;
        if (req != null) {
            Object raw = req.get("message");
            if (raw != null) message = raw.toString();
        }
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.ok(new ChatResponse("Bạn hãy nhập câu hỏi để mình tư vấn."));
        }
        String reply = aiChatService.chat(message.trim());
        if (reply == null || reply.isBlank()) {
            reply = "Hiện chưa có phản hồi. Bạn có thể nói 'mua vợt tầm 700k' hoặc 'phụ kiện dưới 50k' để mình tư vấn.";
        }
        return ResponseEntity.ok(new ChatResponse(reply));
    } catch (Exception ex) {
        return ResponseEntity.ok(new ChatResponse("Hệ thống đang bận. Vui lòng thử lại sau."));
    }
}
```

### 3.3 Service RAG nhẹ + gọi model – `vn/iotstar/service/AiChatService.java`

- Suy luận category/giá, build system prompt + context sản phẩm, gọi OpenRouter/OpenAI, retry 429/5xx:
```42:68:src/main/java/vn/iotstar/service/AiChatService.java
String categoryId = inferCategoryId(userMessage);
BigDecimal maxPrice = inferMaxPrice(userMessage);
String productContext = buildProductContext(categoryId, maxPrice);
String systemPrompt = String.join(" ", /* ràng buộc tiếng Việt + link nội bộ + context */ );
Map<String, Object> body = Map.of(
    "model", openRouterModel,
    "messages", List.of(
        Map.of("role", "system", "content", systemPrompt),
        Map.of("role", "user", "content", userMessage)
    ),
    "temperature", 0.4
);
```

- Gọi API và retry:
```69:113:src/main/java/vn/iotstar/service/AiChatService.java
int[] backoffMs = new int[] { 400, 1000, 2000 };
for (int attempt = 0; attempt < backoffMs.length; attempt++) {
    try {
        Map<?, ?> response = openAiClient.post()
            .uri("/chat/completions")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(Map.class)
            .block();
        // ... kiểm tra response và trích content ...
        return text.isBlank() ? fallbackReply(userMessage, "AI phản hồi rỗng.") : text;
    } catch (WebClientResponseException ex) {
        int code = ex.getStatusCode().value();
        if (code == 429 || (code >= 500 && code < 600)) { Thread.sleep(backoffMs[attempt]); continue; }
        return fallbackReply(userMessage, "Lỗi AI: " + ex.getStatusCode());
    }
}
```

- Suy luận category/giá và dựng context sản phẩm (trích từ DB):
```129:153:src/main/java/vn/iotstar/service/AiChatService.java
private String inferCategoryId(String message) { /* C1/C2/C3/C4 theo từ khóa */ }
private BigDecimal inferMaxPrice(String message) { /* bắt số: 50k, 50000, 50.000 */ }
```
```155:181:src/main/java/vn/iotstar/service/AiChatService.java
private String buildProductContext(String categoryId, BigDecimal maxPrice) {
    // Query repo, lấy tối đa 6 sp, format: "Tên | Giá | /products/{id}"
}
```

### 3.4 WebClient cấu hình endpoint – `vn/iotstar/config/WebClientConfig.java`

- Chọn OpenRouter nếu có `OPENROUTER_API_KEY` (base URL + headers), ngược lại dùng OpenAI:
```14:36:src/main/java/vn/iotstar/config/WebClientConfig.java
boolean useOpenRouter = openRouterKey != null && !openRouterKey.isBlank();
String baseUrl = useOpenRouter ? "https://openrouter.ai/api/v1" : "https://api.openai.com/v1";
String token = useOpenRouter ? openRouterKey : (openAiKey == null ? "" : openAiKey);
return WebClient.builder().baseUrl(baseUrl)
    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    // + metadata cho OpenRouter
    .build();
```

## 4) Bảo mật

- `SecurityConfig` đã `permitAll()` và bỏ qua CSRF cho `POST /api/ai-chat` để widget JS gọi được mà không lỗi 403.

## 5) Cách test nhanh

- Ping backend: mở trình duyệt `GET /api/ai-chat/ping` → `{ "reply": "pong" }`.
- Gửi câu hỏi qua PowerShell:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/ai-chat" -Method Post -ContentType "application/json" -Body (@{message="phụ kiện dưới 50k"} | ConvertTo-Json)
```
- Trên UI: bấm nút tròn AI ở góc phải → gõ câu hỏi → Enter hoặc bấm "Gửi".

## 6) Gỡ lỗi

- 400 từ OpenRouter: sai `OPENROUTER_MODEL` → đặt `openrouter/auto` hoặc model `:free` hợp lệ.
- 429/5xx: service đã retry; nếu vẫn lỗi, kiểm tra quota/model.
- Trả lời lẫn link ngoài: xem lại phần “systemPrompt” trong `AiChatService` và `productContext` có được dựng hay chưa.


