# HƯỚNG DẪN TÍCH HỢP VNPAY

## 📌 Tổng quan

Hệ thống đã tích hợp sẵn VNPay để thanh toán trực tuyến. Người dùng có thể chọn:

-    **COD**: Thanh toán khi nhận hàng
-    **VNPay**: Thanh toán qua ATM, Visa, MasterCard, JCB, QR Code
-    **BANK_TRANSFER**: Chuyển khoản ngân hàng

## 🔧 Cấu hình VNPay

### Bước 1: Đăng ký tài khoản VNPay Sandbox (Test)

1. Truy cập: https://sandbox.vnpayment.vn/
2. Nhấn **"Đăng ký"** → Điền thông tin
3. Xác nhận email → Đăng nhập
4. Vào **Cấu hình** → Lấy:
     - **TMN Code** (Mã website)
     - **Secret Key** (Hash Secret)

### Bước 2: Cập nhật application.properties

Mở file `src/main/resources/application.properties` và cập nhật:

```properties
# VNPay Configuration
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.return-url=http://localhost:8080/payment/vnpay-return
vnpay.tmn-code=YOUR_TMN_CODE_HERE          # ← Thay bằng TMN Code của bạn
vnpay.hash-secret=YOUR_HASH_SECRET_HERE    # ← Thay bằng Secret Key của bạn
vnpay.version=2.1.0
vnpay.command=pay
vnpay.order-type=other
```

**⚠️ Lưu ý:**

-    `vnpay.return-url` phải khớp với domain bạn đăng ký trên VNPay
-    Nếu deploy lên server, đổi `localhost:8080` thành domain thật

### Bước 3: Kiểm tra các file đã tạo

✅ **Backend:**

-    `vn.iotstar.config.VNPayConfig` - Cấu hình VNPay
-    `vn.iotstar.service.VNPayService` - Interface
-    `vn.iotstar.service.impl.VNPayServiceImpl` - Logic tạo URL & validate
-    `vn.iotstar.controller.user.PaymentController` - Xử lý callback từ VNPay

✅ **Frontend:**

-    `templates/user/checkout.html` - Có radio button VNPay
-    `templates/user/payment-result.html` - Trang kết quả thanh toán
-    `static/js/checkout.js` - Xử lý redirect tới VNPay

## 🚀 Cách sử dụng

### 1. Khởi động ứng dụng

```bash
./mvnw spring-boot:run
```

### 2. Thanh toán thử nghiệm

1. Thêm sản phẩm vào giỏ hàng
2. Nhấn **"Thanh toán"**
3. Chọn **"Thanh toán qua VNPay"**
4. Nhấn **"Đặt hàng"**
5. Hệ thống redirect sang trang VNPay
6. Nhập thông tin thẻ test (xem bên dưới)
7. VNPay redirect về `/payment/vnpay-return`
8. Hiển thị kết quả thanh toán

### 3. Thẻ test VNPay Sandbox

VNPay cung cấp thẻ ảo để test:

**Thẻ ATM nội địa:**

-    Ngân hàng: NCB
-    Số thẻ: `9704198526191432198`
-    Tên chủ thẻ: `NGUYEN VAN A`
-    Ngày phát hành: `07/15`
-    Mật khẩu OTP: `123456`

**Thẻ quốc tế:**

-    Số thẻ: `4111111111111111`
-    Tên: `NGUYEN VAN A`
-    Ngày hết hạn: `12/25`
-    CVV: `123`

## 📊 Luồng thanh toán VNPay

```
[User] → Chọn sản phẩm → Checkout → Chọn VNPay
   ↓
[Backend] → Tạo Order → Tạo Payment URL (với hash)
   ↓
[VNPay] → User nhập thẻ → Xác thực → Trả kết quả
   ↓
[Backend] → Nhận callback → Validate hash → Cập nhật Order/Payment
   ↓
[User] → Xem kết quả thanh toán (Success/Failed)
```

## 🔐 Bảo mật

1. **Hash HMAC-SHA512:**

     - Mọi request tới VNPay đều được hash với Secret Key
     - Callback từ VNPay cũng được validate hash

2. **Transaction ID:**

     - Mỗi giao dịch có `vnp_TransactionNo` unique
     - Lưu vào database để đối soát

3. **IP Address:**
     - VNPay lưu IP của người thanh toán

## 🐛 Xử lý lỗi thường gặp

### Lỗi 1: "Chữ ký không hợp lệ"

**Nguyên nhân:** Secret Key sai hoặc format hash sai
**Giải pháp:**

-    Kiểm tra lại `vnpay.hash-secret` trong `application.properties`
-    Đảm bảo không có khoảng trắng thừa

### Lỗi 2: "Invalid TMN Code"

**Nguyên nhân:** `vnpay.tmn-code` sai
**Giải pháp:**

-    Lấy lại TMN Code từ VNPay Dashboard
-    Copy chính xác (phân biệt HOA/thường)

### Lỗi 3: Return URL không hoạt động

**Nguyên nhân:** URL không khớp với cấu hình trên VNPay
**Giải pháp:**

-    Vào VNPay Dashboard → Cấu hình → Thêm `http://localhost:8080/payment/vnpay-return`
-    Hoặc dùng ngrok để test: `ngrok http 8080`

### Lỗi 4: "Không tìm thấy đơn hàng"

**Nguyên nhân:** Order chưa được tạo hoặc bị xóa
**Giải pháp:**

-    Kiểm tra log: `log.info("VNPay return params: {}", params)`
-    Verify `orderId` trong database

## 📝 Lưu ý Production

Khi deploy lên môi trường thật:

1. **Đổi URL:**

```properties
vnpay.url=https://vnpayment.vn/paymentv2/vpcpay.html  # ← URL production
vnpay.return-url=https://yourdomain.com/payment/vnpay-return
```

2. **Đăng ký VNPay chính thức:**

     - Truy cập: https://vnpay.vn/
     - Chuẩn bị: Giấy phép ĐKKD, CMND, tài liệu pháp lý
     - Phí: VNPay thu % trên mỗi giao dịch

3. **Bảo mật Secret Key:**

```properties
# Không commit Secret Key vào Git
# Dùng biến môi trường:
vnpay.hash-secret=${VNPAY_SECRET_KEY}
```

4. **Logging:**

```java
log.info("VNPay payment for order {} - amount {}", orderId, amount);
```

## 🎯 Tính năng nâng cao (Optional)

### 1. Query API (Tra cứu giao dịch)

Thêm endpoint để tra cứu trạng thái giao dịch từ VNPay

### 2. Refund API (Hoàn tiền)

Tích hợp API hoàn tiền tự động khi hủy đơn

### 3. IPN (Instant Payment Notification)

VNPay gửi thông báo ngay khi thanh toán thành công (không chờ user redirect)

### 4. Multi-currency

Hỗ trợ USD, EUR (nếu có nhu cầu thanh toán quốc tế)

## 📞 Hỗ trợ

-    **VNPay Hotline:** 1900 555 577
-    **Email:** support@vnpay.vn
-    **Tài liệu:** https://sandbox.vnpayment.vn/apis/
-    **Telegram Support:** @vnpay_support

---

**🎉 Chúc bạn tích hợp thành công!**
