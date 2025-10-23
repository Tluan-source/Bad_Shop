# Hướng dẫn chạy dự án Badminton Marketplace

## 1. Cấu hình Database

### Tạo database SQL Server:
```sql
CREATE DATABASE BadmintonMarketplace;
```

### Chạy file data.sql để thêm dữ liệu mẫu:
File `data.sql` đã được cấu hình với câu lệnh **MERGE** - có thể chạy nhiều lần mà không bị lỗi duplicate key.

**Cách 1: Chạy trực tiếp trong SQL Server Management Studio**
1. Mở SQL Server Management Studio
2. Kết nối đến database `BadmintonMarketplace`
3. Mở file `src/main/resources/data.sql`
4. Nhấn Execute (F5)

**Cách 2: Để Spring Boot tự động chạy**
1. Mở file `application.properties`
2. Đảm bảo có cấu hình: `spring.sql.init.mode=always`
3. Chạy ứng dụng, dữ liệu sẽ tự động được thêm vào

## 2. Chạy ứng dụng

```bash
mvn spring-boot:run
```

Hoặc trong IDE (Eclipse/IntelliJ):
- Right click vào project → Run As → Spring Boot App

## 3. Truy cập ứng dụng

- URL: http://localhost:8080
- Trang đăng nhập: http://localhost:8080/login

## 4. Tài khoản demo

| Role | Email | Password | Dashboard URL |
|------|-------|----------|---------------|
| Admin | admin@badminton.com | 123456 | /admin/dashboard |
| Vendor | vendor@badminton.com | 123456 | /vendor/dashboard |
| Shipper | shipper@badminton.com | 123456 | /shipper/dashboard |
| User | user@badminton.com | 123456 | / (trang chủ) |

## 5. Tính năng đã hoàn thành

### ✅ Đăng nhập với thông báo
- Thông báo lỗi khi sai email/password
- Thông báo thành công khi đăng xuất
- Tự động redirect đúng trang theo role

### ✅ Dashboard theo từng role

**Admin Dashboard** (`/admin/dashboard`)
- Tổng quan hệ thống
- Quản lý người dùng, sản phẩm, cửa hàng
- Thống kê đơn hàng

**Vendor Dashboard** (`/vendor/dashboard`)
- Quản lý sản phẩm của cửa hàng
- Xử lý đơn hàng
- Thống kê doanh thu
- Đánh giá của khách hàng

**Shipper Dashboard** (`/shipper/dashboard`)
- Đơn hàng cần giao
- Đơn đang giao
- Lịch sử giao hàng
- Thống kê thu nhập

**User** (Trang chủ `/`)
- Xem sản phẩm
- Mua hàng
- Quản lý đơn hàng cá nhân

## 6. Cấu trúc dữ liệu mẫu

- 4 users (Admin, Vendor, Shipper, User)
- 4 categories (Vợt, Quả cầu, Giày, Phụ kiện)
- 2 stores (Yonex VN, Victor Sports)
- 8 products
- 2 commissions
- 4 shipping providers
- 4 vouchers

## 7. Lưu ý

- File `data.sql` sử dụng MERGE nên có thể chạy nhiều lần
- Nếu muốn reset dữ liệu: DROP DATABASE và tạo lại
- Password đã được hash bằng BCrypt (123456)

## 8. Troubleshooting

**Lỗi: Could not connect to database**
- Kiểm tra SQL Server đang chạy
- Kiểm tra username/password trong `application.properties`

**Lỗi: Table not found**
- Chạy ứng dụng lần đầu để Hibernate tạo tables
- Sau đó chạy data.sql

**Lỗi: Duplicate key**
- File data.sql đã dùng MERGE, không nên bị lỗi này
- Nếu vẫn lỗi, kiểm tra lại file data.sql
