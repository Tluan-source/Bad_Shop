# Bad Shop – Hệ thống thương mại điện tử cầu lông (Spring Boot)

Ứng dụng web bán hàng cầu lông được xây dựng bằng Spring Boot, Thymeleaf và SQL Server. Bao gồm giỏ hàng, đặt hàng, đánh giá, khu vực người bán, thanh toán, nhắn tin trực tiếp realtime giữa người bán và mua hàng, và trợ lý chat AI.

## Công nghệ sử dụng
- Backend: Spring Boot 3 (Web, Security, JPA, Validation, WebSocket)
- Giao diện: Thymeleaf, Bootstrap 5, Font Awesome
- CSDL: Microsoft SQL Server (JPA/Hibernate)
- Xác thực: Spring Security (Form Login)
- Lưu trữ ảnh: Cloudinary
- Thanh toán: VNPay (đã cấu hình), VietQR (chưa tự động kiểm tra thanh toán)
- AI: OpenRouter/OpenAI qua WebClient (RAG nhẹ trên dữ liệu sản phẩm)

## Cấu trúc thư mục
- `src/main/java/vn/iotstar`
  - `controller/` controller web (user, admin, vendor, API chat)
  - `service/` lớp nghiệp vụ (cart, order, product, AI chat)
  - `repository/` Spring Data JPA repositories
  - `config/` security, webclient, websocket, thanh toán
  - `entity/` các thực thể JPA
- `src/main/resources/templates/` giao diện Thymeleaf (trong `fragments/` có widget chat AI)
- `ai-chat.md` tài liệu chi tiết luồng chat AI

## Yêu cầu môi trường
- JDK 17+, Maven 3.9+
- SQL Server 2019+
- (Tuỳ chọn) Tài khoản Cloudinary

## Cấu hình
Tạo `src/main/resources/application.properties` hoặc đặt qua biến môi trường.

CSDL
```
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=badshop;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YourStrong!Passw0rd
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Cloudinary (tuỳ chọn)
```
cloudinary.cloud-name=xxxx
cloudinary.api-key=xxxx
cloudinary.api-secret=xxxx
```

### Trợ lý AI
OpenRouter (khuyến nghị):
```
OPENROUTER_API_KEY=your_key
OPENROUTER_MODEL=openrouter/auto   # hoặc model :free hợp lệ
```
OpenAI:
```
OPENAI_API_KEY=your_key
```
WebClient tự chọn OpenRouter nếu có `OPENROUTER_API_KEY`.

## Chạy dự án
```
# Windows
dev\mvnw.cmd spring-boot:run   # hoặc mvnw.cmd spring-boot:run
# macOS/Linux
./mvnw spring-boot:run
```
Ứng dụng: `http://localhost:8080`.

## Build
```
./mvnw clean package
```
File jar nằm trong `target/`.

# Tính năng dự án (Bad Shop)
- Người dùng (khách mua)
    --Đăng ký/đăng nhập, cập nhật hồ sơ
    --Duyệt danh mục, tìm kiếm sản phẩm, xem chi tiết
    --Giỏ hàng: thêm/xoá/sửa số lượng, mua ngay
    --Mã giảm giá/khuyến mãi (áp dụng ở checkout)
    --Thanh toán
    --VNPay (đã tích hợp)
    --VietQR (chuyển khoản QR – chưa tự động đối soát)
    --Đặt hàng: tạo đơn nhiều sản phẩm/cửa hàng, theo dõi trạng thái
    --Đánh giá sản phẩm (rating, ảnh)
    --Yêu thích (favorites), lịch sử xem (nếu có)
    --Trò chuyện realtime với người bán
    --Trợ lý AI chat (gợi ý theo dữ liệu shop)
- Người bán (Vendor)
    --Quản lý sản phẩm: thêm/sửa/xoá, hình ảnh, thuộc tính/biến thể
    --Quản lý đơn hàng: xem, xác nhận, cập nhật trạng thái giao hàng
    --Quản lý khuyến mãi/mã giảm giá
    --Thống kê cơ bản: doanh thu, đơn bán chạy (tùy phạm vi)
    --Chat realtime với người mua
- Quản trị (Admin)
    --Quản lý danh mục, cửa hàng, người dùng
    --Duyệt/kiểm soát nội dung (sản phẩm, đánh giá)
    --Cấu hình khuyến mãi chung, phí/hoa hồng (nếu bật)
    --Theo dõi hoạt động, log sự kiện (nếu bật)
- Hệ thống/khác
    --Xác thực Spring Security (Form Login)
    --WebSocket chat realtime
    --Lưu trữ ảnh Cloudinary
    --Giao diện Thymeleaf + Bootstrap 5, responsive
    --AI Chat: API /api/ai-chat, widget nổi ở footer, chiến lược DB-first + model
    --Tích hợp vận chuyển (hiển thị provider), trạng thái đơn (PENDING → DELIVERED…)
    --Email/OTP (nếu cấu hình)
    --Cấu hình đa phân hệ header/footer cho user/vendor/admin/shipper

## Xử lý sự cố (Troubleshooting)
- Kết nối SQL Server: bật SQL auth, cổng 1433; dùng `encrypt=true;trustServerCertificate=true` khi chạy local.
- 400 từ OpenRouter: sai `OPENROUTER_MODEL` → đặt `openrouter/auto` hoặc model `:free` hợp lệ.
- 429/5xx từ model: hết hạn mức hoặc lỗi tạm thời → đã có retry, thử model khác.
- 403 với `/api/ai-chat`: endpoint này đã bỏ CSRF; thử hard refresh nếu cookie thay đổi.

## Ghi chú bảo mật
- `SecurityConfig` mở một số route public và mở `/api/ai-chat` cho mục đích demo; cần siết chặt khi lên production.
- Khuyến nghị bổ sung rate‑limit và audit log cho API chat.

## Giấy phép
Phục vụ mục đích học tập/nội bộ. Tuỳ biến thêm khi triển khai production.
