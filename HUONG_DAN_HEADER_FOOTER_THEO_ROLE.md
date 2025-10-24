# HƯỚNG DẪN SỬ DỤNG HEADER VÀ FOOTER THEO ROLE

## Tổng quan
Dự án đã được cải tiến với header và footer riêng biệt cho từng role (ADMIN, VENDOR, SHIPPER, USER).
Mỗi role có giao diện và chức năng phù hợp với vai trò của họ.

## Cấu trúc file fragments

### 1. **ADMIN** - Quản trị viên hệ thống
- **Header**: `fragments/header-admin.html`
  - Màu đỏ (bg-danger) để nổi bật quyền hạn cao nhất
  - Menu: Dashboard, Users, Stores, Categories, Orders, Vouchers, Reports
  
- **Footer**: `fragments/footer-admin.html`
  - Thông tin hỗ trợ admin
  - Liên kết công cụ quản trị

- **Cách sử dụng trong template**:
```html
<div th:replace="~{fragments/header-admin :: header-admin}"></div>
<!-- Nội dung trang -->
<div th:replace="~{fragments/footer-admin :: footer-admin}"></div>
```

### 2. **VENDOR** - Nhà cung cấp/Người bán
- **Header**: `fragments/header-vendor.html`
  - Màu xanh lá (bg-success) đại diện cho kinh doanh
  - Menu: Dashboard, My Store, Products, Orders, Revenue, Wallet
  
- **Footer**: `fragments/footer-vendor.html`
  - Thông tin hỗ trợ bán hàng
  - Hiển thị phí hoa hồng

- **Cách sử dụng trong template**:
```html
<div th:replace="~{fragments/header-vendor :: header-vendor}"></div>
<!-- Nội dung trang -->
<div th:replace="~{fragments/footer-vendor :: footer-vendor}"></div>
```

### 3. **SHIPPER** - Nhân viên giao hàng
- **Header**: `fragments/header-shipper.html`
  - Màu xanh dương nhạt (bg-info) dễ nhìn
  - Menu: Dashboard, Pending Orders, Delivering, Completed, Income, Wallet
  
- **Footer**: `fragments/footer-shipper.html`
  - Thông tin hỗ trợ shipper
  - Hướng dẫn giao hàng

- **Cách sử dụng trong template**:
```html
<div th:replace="~{fragments/header-shipper :: header-shipper}"></div>
<!-- Nội dung trang -->
<div th:replace="~{fragments/footer-shipper :: footer-shipper}"></div>
```

### 4. **USER** - Người dùng thường
- **Header**: `fragments/header-user.html`
  - Màu xanh dương (bg-primary) thân thiện
  - Menu: Home, Products, Stores, Categories, Cart
  - Có thanh tìm kiếm sản phẩm
  
- **Footer**: `fragments/footer-user.html`
  - Thông tin công ty đầy đủ
  - Liên kết mạng xã hội
  - Chính sách và điều khoản

- **Cách sử dụng trong template**:
```html
<div th:replace="~{fragments/header-user :: header-user}"></div>
<!-- Nội dung trang -->
<div th:replace="~{fragments/footer-user :: footer-user}"></div>
```

## Các trang đã được cập nhật

### Admin
- ✅ `/admin/index.html` - header-admin + footer-admin
- ✅ `/admin/dashboard.html` - header-admin + footer-admin

### Vendor
- ✅ `/vendor/index.html` - header-vendor + footer-vendor
- ✅ `/vendor/dashboard.html` - header-vendor + footer-vendor

### Shipper
- ✅ `/shipper/dashboard.html` - header-shipper + footer-shipper

### User
- ✅ `/user/index.html` - header-user + footer-user
- ✅ `/user/products.html` - header-user + footer-user

## Lợi ích của thiết kế này

1. **Trải nghiệm người dùng tốt hơn**
   - Mỗi role có giao diện phù hợp với công việc của họ
   - Màu sắc riêng biệt giúp nhận diện vai trò ngay lập tức

2. **Dễ bảo trì**
   - Thay đổi header/footer của một role không ảnh hưởng role khác
   - Code rõ ràng, dễ hiểu

3. **Bảo mật tốt hơn**
   - Mỗi role chỉ thấy menu phù hợp với quyền hạn
   - Giảm thiểu rủi ro truy cập không đúng quyền

4. **Chuyên nghiệp**
   - Tách biệt rõ ràng giữa các role
   - Giao diện nhất quán trong mỗi role

## Khi tạo trang mới

### Cho Admin:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head th:replace="~{fragments/head :: head}">
    <title>Tiêu đề trang - Badminton Marketplace</title>
</head>
<body>
    <div th:replace="~{fragments/header-admin :: header-admin}"></div>
    
    <!-- Nội dung của bạn -->
    
    <div th:replace="~{fragments/footer-admin :: footer-admin}"></div>
</body>
</html>
```

### Cho Vendor:
Thay `header-admin` và `footer-admin` bằng `header-vendor` và `footer-vendor`

### Cho Shipper:
Thay thành `header-shipper` và `footer-shipper`

### Cho User:
Thay thành `header-user` và `footer-user`

## Lưu ý quan trọng

1. **Header cũ** (`fragments/header.html`) và **Footer cũ** (`fragments/footer.html`) vẫn được giữ lại để tương thích với các trang auth (login, register, forgot password)

2. **Màu sắc phân biệt**:
   - Admin: Đỏ (danger) - Quyền cao nhất
   - Vendor: Xanh lá (success) - Kinh doanh
   - Shipper: Xanh dương nhạt (info) - Vận chuyển
   - User: Xanh dương (primary) - Người dùng thường

3. Tất cả header đều có:
   - Logo/Brand name
   - Menu điều hướng phù hợp với role
   - Dropdown account với thông tin user
   - Nút đăng xuất

4. Tất cả footer đều có:
   - Thông tin liên hệ
   - Quick links
   - Copyright

## Testing

Để kiểm tra, đăng nhập với các tài khoản:
- Admin: admin@badminton.com / 123456
- Vendor: vendor@badminton.com / 123456
- Shipper: shipper@badminton.com / 123456
- User: user@badminton.com / 123456

Mỗi role sẽ thấy header và footer khác nhau phù hợp với vai trò của mình.

---
Tạo bởi: GitHub Copilot
Ngày: 24/10/2025
