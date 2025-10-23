# HƯỚNG DẪN CHẠY DATA.SQL CHO BẠN BÈ

## ⚡ Cách chạy nhanh nhất

### Bước 1: Tạo Database
```sql
CREATE DATABASE BadmintonMarketplace;
```

### Bước 2: Chạy Spring Boot
```bash
mvn spring-boot:run
```

**Xong!** Dữ liệu sẽ tự động được thêm vào khi ứng dụng khởi động lần đầu.

## 📝 File data.sql đã được cải tiến

✅ **Sử dụng câu lệnh MERGE** thay vì INSERT
- Có thể chạy nhiều lần không bị lỗi duplicate key
- Tự động kiểm tra: nếu dữ liệu đã tồn tại thì bỏ qua, chưa có thì thêm mới

### Ví dụ cấu trúc MERGE:
```sql
MERGE INTO users AS target
USING (SELECT 'U_ADMIN' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (...) VALUES (...);
```

## 🎯 Tài khoản demo có sẵn

| Role | Email | Password |
|------|-------|----------|
| 👨‍💼 Admin | admin@badminton.com | 123456 |
| 🏪 Vendor | vendor@badminton.com | 123456 |
| 🚚 Shipper | shipper@badminton.com | 123456 |
| 👤 User | user@badminton.com | 123456 |

## 🎨 Tính năng đăng nhập mới

### ✅ Thông báo đầy đủ:
- **Đăng nhập sai:** Hiện thông báo đỏ "Email/Số điện thoại hoặc mật khẩu không đúng!"
- **Đăng xuất:** Hiện thông báo xanh "Bạn đã đăng xuất thành công!"
- **Kích hoạt tài khoản:** Thông báo xanh sau khi verify OTP
- **Reset mật khẩu:** Thông báo xanh sau khi đặt lại mật khẩu

### 🔄 Tự động redirect theo role:
- **Admin** → `/admin/dashboard` (Trang quản trị hệ thống)
- **Vendor** → `/vendor/dashboard` (Trang quản lý cửa hàng)
- **Shipper** → `/shipper/dashboard` (Trang quản lý giao hàng)
- **User** → `/` (Trang chủ mua sắm)

## 📁 Các file đã tạo/cập nhật

### Controllers mới:
- `AdminController.java` - Xử lý trang admin
- `VendorController.java` - Xử lý trang vendor
- `ShipperController.java` - Xử lý trang shipper
- `CustomAuthenticationSuccessHandler.java` - Điều hướng sau đăng nhập

### Templates mới:
- `templates/admin/dashboard.html` - Dashboard admin với thống kê
- `templates/vendor/dashboard.html` - Dashboard vendor với quản lý sản phẩm
- `templates/shipper/dashboard.html` - Dashboard shipper với đơn hàng
- `templates/auth/login.html` - Trang đăng nhập với thông báo

### Cập nhật:
- `SecurityConfig.java` - Thêm custom success handler
- `data.sql` - Đổi từ INSERT sang MERGE

## 🚀 Kiểm tra hoạt động

1. Chạy ứng dụng: `mvn spring-boot:run`
2. Truy cập: http://localhost:8080/login
3. Đăng nhập bằng bất kỳ tài khoản nào ở trên
4. Xem thông báo thành công và được redirect đến trang tương ứng

## ❓ Nếu gặp lỗi

**"Duplicate key error"** - Không thể xảy ra vì đã dùng MERGE
- Nếu vẫn bị: Xóa database và tạo lại

**"Table not found"** - Chạy app lần đầu để Hibernate tạo bảng
- Spring Boot sẽ tự động tạo các bảng từ Entity classes

**"Login failed"** - Kiểm tra lại username/password
- Nhớ: username là **EMAIL** (không phải số điện thoại)
