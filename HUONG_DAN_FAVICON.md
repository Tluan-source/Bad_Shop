# Hướng dẫn thêm Favicon cho Bad_Shop

## Vấn đề
Trình duyệt tự động yêu cầu file `/favicon.ico` nhưng ứng dụng không có file này, gây ra lỗi 500.

## Giải pháp đã thực hiện

### 1. Đã cập nhật SecurityConfig
- Thêm `/favicon.ico` vào danh sách URL được phép truy cập không cần xác thực
- Tránh lỗi 403 Forbidden khi truy cập favicon

### 2. Cách thêm favicon vào project

#### Tùy chọn 1: Sử dụng favicon có sẵn
1. Tải một file favicon.ico từ internet (kích thước 16x16 hoặc 32x32 pixels)
2. Đổi tên thành `favicon.ico`
3. Copy vào thư mục: `D:\laptrinhweb\Bad_Shop\src\main\resources\static\`

#### Tùy chọn 2: Tạo favicon online
1. Truy cập: https://favicon.io/favicon-generator/
2. Tạo favicon với chữ "B" (viết tắt của Badminton)
3. Tải xuống file `favicon.ico`
4. Copy vào thư mục: `D:\laptrinhweb\Bad_Shop\src\main\resources\static\`

#### Tùy chọn 3: Sử dụng PNG thay vì ICO
1. Tìm một icon badminton PNG (32x32 hoặc 64x64)
2. Đổi tên thành `favicon.png`
3. Copy vào: `D:\laptrinhweb\Bad_Shop\src\main\resources\static\`
4. Thêm vào file HTML head:
```html
<link rel="icon" type="image/png" th:href="@{/favicon.png}">
```

#### Tùy chọn 4: Tạm thời disable favicon (cho development)
Không làm gì, lỗi 500 đã được fix bởi cấu hình SecurityConfig.
Favicon sẽ không hiển thị nhưng không còn lỗi nữa.

## Khởi động lại ứng dụng
Sau khi thêm favicon.ico, khởi động lại ứng dụng Spring Boot để thay đổi có hiệu lực.

## Kiểm tra
Truy cập: http://localhost:8080/favicon.ico
- Nếu thấy icon: Thành công ✓
- Nếu thấy 404: File chưa được đặt đúng vị trí
- Nếu thấy 500: Cần kiểm tra lại SecurityConfig
