# Hướng dẫn đăng ký và phê duyệt Vendor

## Tổng quan
Hệ thống đã được cấu hình để **YÊU CẦU ADMIN PHÊ DUYỆT** trước khi user có thể trở thành vendor và bắt đầu bán hàng.

## Quy trình đăng ký Vendor

### 1. Đăng ký (USER role)
- User đăng nhập với role USER
- Truy cập `/vendor/register`
- Điền thông tin:
  - **Thông tin cửa hàng**: Tên, mô tả, SĐT, email, địa chỉ
  - **Thông tin pháp lý**: Loại hình kinh doanh, mã số thuế, giấy phép
  - **Thông tin thanh toán**: Ngân hàng, số TK, tên chủ TK
  - Upload logo và giấy phép (optional)
- Submit form

### 2. Trạng thái sau khi đăng ký
- Store được tạo với `isActive = false` (chưa được duyệt)
- User vẫn giữ role `USER` (chưa có quyền vendor)
- Hệ thống gửi email thông báo cho admin
- User thấy thông báo "Đang chờ admin phê duyệt"

### 3. Admin phê duyệt
Admin cần thực hiện 2 bước:

#### Bước 1: Kích hoạt Store
- Truy cập trang quản lý stores
- Xem thông tin cửa hàng đăng ký
- Kiểm tra thông tin trong `bio` field (chứa đầy đủ thông tin đăng ký)
- Set `isActive = true` để kích hoạt cửa hàng

#### Bước 2: Nâng cấp role User
- Truy cập trang quản lý users
- Tìm user tương ứng (owner của store)
- Đổi role từ `USER` sang `VENDOR`

### 4. Sau khi được duyệt
- User có role `VENDOR` và store `isActive = true`
- Có thể truy cập `/vendor/dashboard` và các chức năng vendor khác
- Bắt đầu thêm sản phẩm và bán hàng

## Cấu trúc Security

### SecurityConfig
```java
.requestMatchers("/vendor/register").hasAnyRole("USER", "ADMIN")  // Đăng ký
.requestMatchers("/vendor/**").hasAnyRole("VENDOR", "ADMIN")      // Quản lý vendor
```

### Quyền truy cập
- `/vendor/register`: USER, ADMIN
- `/vendor/dashboard`: VENDOR, ADMIN
- `/vendor/products`: VENDOR, ADMIN
- `/vendor/orders`: VENDOR, ADMIN
- Các endpoint vendor khác: VENDOR, ADMIN

## Files đã tạo/sửa

### 1. SecurityConfig.java (đã sửa)
- Thêm rule cho phép USER truy cập `/vendor/register`
- Các endpoint vendor khác vẫn yêu cầu role VENDOR

### 2. VendorRegistrationController.java (mới tạo)
- `GET /vendor/register`: Hiển thị form đăng ký
- `POST /vendor/register`: Xử lý đăng ký vendor
- Kiểm tra duplicate registration
- Gọi VendorRegistrationService để tạo store

### 3. vendor/register.html (đã cập nhật)
- Thêm thông báo khi có pending registration
- Thêm cảnh báo về quy trình phê duyệt
- Hiển thị lợi ích và yêu cầu để trở thành vendor

## Kiểm tra đăng ký trùng

VendorRegistrationService kiểm tra:
- User đã có store chưa (dù pending hay active)
- Nếu có → throw exception "Bạn đã đăng ký cửa hàng rồi!"
- Nếu chưa → Tạo store mới với `isActive = false`

## Dữ liệu được lưu

Store entity chứa:
- Thông tin cơ bản: name, bio (chứa full info), email, phone
- `featuredImages`: Logo (JSON array)
- `owner`: User ID
- `isActive`: false (chờ duyệt)
- `commission`: null (admin sẽ set sau)

Bio field format:
```
[Mô tả cửa hàng]

--- Thông tin liên hệ ---
Điện thoại: [phone]
Email: [email]
Địa chỉ: [address]

--- Thông tin doanh nghiệp ---
Loại hình: [businessType]
Mã số thuế: [taxCode]
Giấy phép KD: [licenseUrl]

--- Thông tin thanh toán ---
Ngân hàng: [bankName]
Số TK: [bankAccountNumber]
Chủ TK: [bankAccountName]
Chi nhánh: [bankBranch]
```

## Notification

Sau khi đăng ký thành công, hệ thống:
1. Tạo store với status pending
2. Gửi email thông báo cho admin (nếu SMTP được cấu hình)
3. Redirect user về profile với thông báo success

## Testing

### Test đăng ký
1. Đăng nhập với user thông thường (role USER)
2. Truy cập http://localhost:8080/vendor/register
3. Điền form và submit
4. Kiểm tra:
   - Có thông báo "Đăng ký thành công, đang chờ duyệt"
   - Database: Store mới với isActive = false
   - User vẫn có role USER

### Test truy cập vendor dashboard (trước khi duyệt)
1. Truy cập http://localhost:8080/vendor/dashboard
2. Kết quả: 403 Forbidden (đúng như mong muốn)

### Test sau khi admin duyệt
1. Admin: Set store.isActive = true
2. Admin: Set user.role = VENDOR
3. User login lại
4. Truy cập /vendor/dashboard → Success
5. Có thể thêm sản phẩm và quản lý orders

## Lưu ý quan trọng

⚠️ **Admin cần thực hiện cả 2 bước:**
1. Kích hoạt Store (`isActive = true`)
2. Nâng role User (`role = VENDOR`)

Nếu chỉ làm 1 trong 2:
- Chỉ set isActive: User vẫn không vào được vendor dashboard (403)
- Chỉ set role: User vào được dashboard nhưng không có store để quản lý

## Email thông báo

VendorRegistrationServiceImpl gửi email cho admin với:
- Subject: "🔔 Đăng ký người bán mới - [Tên shop]"
- Nội dung: Thông tin cơ bản của shop và link đến trang admin
- Link: http://localhost:8080/admin/stores/[storeId]/details

Nếu SMTP chưa cấu hình, email sẽ fail nhưng đăng ký vẫn thành công.
