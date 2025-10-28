# 🚚 THAY ĐỔI LOGIC SHIPMENT - ĐỌC TRỰC TIẾP TỪ ORDER

## 📝 Tóm tắt thay đổi

**Shipper không dùng trạng thái PENDING/ACCEPTED nữa**, thay vào đó **đọc trực tiếp các Order có status = PROCESSING**

### ❌ Logic CŨ (đã bỏ):
```
Order: PROCESSING → Shipment: ASSIGNED/ACCEPTED → DELIVERING → DELIVERED/FAILED
```

### ✅ Logic MỚI:
```
Order: PROCESSING (shipper xem danh sách này)
        ↓ (shipper nhận đơn)
Shipment: DELIVERING → DELIVERED/FAILED
```

---

## 🎯 Lý do thay đổi

- **Tab "Chờ nhận"** của shipper sẽ **lấy danh sách Order có status = PROCESSING**
- **KHÔNG tạo Shipment** cho đến khi shipper nhận đơn
- Khi shipper nhận đơn → **tạo Shipment mới** với status = `DELIVERING`
- **Đơn giản hóa**: Shipment chỉ còn 3 trạng thái thay vì 5

---

## 📊 Các trạng thái Shipment hiện tại

| Trạng thái | Mô tả | Màu sắc |
|-----------|-------|---------|
| **DELIVERING** | Đang giao hàng | 🔵 Primary |
| **DELIVERED** | Đã giao thành công | 🟢 Success |
| **FAILED** | Giao hàng thất bại | 🔴 Danger |

**Lưu ý:** Không còn trạng thái ASSIGNED, ACCEPTED, PENDING nữa!

---

## 🔧 Các file đã thay đổi

### 1. **Entity Shipment**
- ✅ Bỏ enum `ASSIGNED`, `ACCEPTED`, `PENDING`
- ✅ Chỉ giữ 3 trạng thái: `DELIVERING`, `DELIVERED`, `FAILED`
- ✅ Status mặc định khi tạo: `DELIVERING`
- ✅ Giữ nguyên trường `assignedAt` (thời điểm shipper nhận đơn)

### 2. **Repository**
- ✅ `OrderRepository.java`: Thêm các method mới
  - `findByStatus()` - Lấy Order theo status (PROCESSING)
  - `countByStatus()` - Đếm Order theo status
  - `findByStatusAndCreatedAtBetween()` - Lọc theo ngày
  - `findByStatusAndKeyword()` - Tìm kiếm theo keyword

### 3. **Controller**
- ✅ `ShipperController.java`:
  - **Tab "Chờ nhận"**: Hiển thị danh sách Order PROCESSING (không phải Shipment)
  - **Action nhận đơn**: Tạo Shipment mới với status `DELIVERING`
  - Thêm endpoint `/shipper/order/{id}` để xem chi tiết Order

### 4. **Templates/Views**
- ✅ `shipper/dashboard.html`: Hiển thị `pendingOrders` (Order) thay vì `pendingShipments`
- ✅ `shipper/order_detail.html`: **Tạo mới** - Xem chi tiết Order chưa có Shipment
- ✅ `shipper/profile.html`: Bỏ badge PENDING
- ✅ `vendor/orders/list.html`: Hiển thị "Chưa giao" khi shipmentStatus = null
- ✅ `fragments/footer-shipper.html`: Link vẫn dùng status=PENDING (logic filter)

---

## 🗄️ Migration Database

**KHÔNG CẦN chạy migration SQL** vì:
- Không thay đổi cấu trúc bảng `shipments`
- Chỉ thay đổi logic hiển thị và tạo mới

Tuy nhiên, nếu có dữ liệu cũ với status ASSIGNED/ACCEPTED, bạn có thể:
```sql
-- Cập nhật các shipment cũ thành DELIVERING hoặc xóa đi
UPDATE shipments SET status = 'DELIVERING' WHERE status IN ('ASSIGNED', 'ACCEPTED');
-- Hoặc
DELETE FROM shipments WHERE status IN ('ASSIGNED', 'ACCEPTED');
```

---

## 🔄 Workflow mới của Shipper

### 1. **Tab "Chờ nhận"**
- Shipper xem danh sách **Order có status = PROCESSING**
- Các Order này **CHƯA có Shipment**
- Hiển thị: Mã đơn, Khách hàng, Địa chỉ, COD, Ngày tạo

### 2. **Shipper nhận đơn**
```java
// ShipperController.acceptOrder()
Order order = orderRepository.findById(orderId);

// Tạo Shipment mới
Shipment shipment = new Shipment();
shipment.setId("SH" + System.currentTimeMillis());
shipment.setOrder(order);
shipment.setShipper(shipper);
shipment.setStatus(ShipmentStatus.DELIVERING);
shipment.setAssignedAt(LocalDateTime.now());
shipment.setShippingFee(order.getShippingFee());

// Cập nhật Order
order.setStatus(Order.OrderStatus.SHIPPED);
```

### 3. **Tab "Đang giao"**
- Hiển thị các **Shipment có status = DELIVERING**
- Thuộc về shipper hiện tại

### 4. **Hoàn thành**
- **Thành công**: `DELIVERED` → Order status = `DELIVERED`
- **Thất bại**: `FAILED` → Order status = `CANCELLED`

---

## 📍 Điểm khác biệt quan trọng

| Trước đây | Bây giờ |
|-----------|---------|
| Shipment được tạo sẵn với status ASSIGNED | **Shipment chỉ tạo khi shipper nhận đơn** |
| Tab "Chờ nhận" hiển thị Shipment PENDING/ACCEPTED | **Tab "Chờ nhận" hiển thị Order PROCESSING** |
| Cần update Shipment từ PENDING → DELIVERING | **Tạo mới Shipment với status DELIVERING** |
| 5 trạng thái Shipment | **3 trạng thái Shipment** |

---

## ⚠️ Lưu ý quan trọng

1. **Không thay đổi database schema** - Chỉ thay đổi logic code
2. **Order.shipment có thể null** - Khi Order đang ở trạng thái PROCESSING
3. **ID Shipment tự động**: Dùng `"SH" + System.currentTimeMillis()` hoặc UUID
4. **Kiểm tra null**: Luôn check `order.getShipment() != null` trước khi dùng

---

## ✅ Testing Checklist

- [ ] Shipper có thể xem danh sách Order PROCESSING trong tab "Chờ nhận"
- [ ] Shipper có thể xem chi tiết Order (chưa có Shipment)
- [ ] Shipper nhận đơn → tạo Shipment mới với status DELIVERING
- [ ] Order chuyển từ PROCESSING → SHIPPED khi shipper nhận
- [ ] Tab "Đang giao" hiển thị đúng Shipment của shipper
- [ ] Filter/Search theo keyword hoạt động
- [ ] Filter theo ngày hoạt động
- [ ] Giao hàng thành công/thất bại hoạt động đúng

---

## 🎨 UI Changes

**Tab "Chờ nhận":**
- Hiển thị dữ liệu từ `Order` object
- Button "Nhận" gọi `/shipper/accept/{orderId}` (không phải shipmentId)
- Link "Xem" đến `/shipper/order/{orderId}` (trang mới)

**Tab "Đang giao", "Đã giao", "Thất bại":**
- Vẫn hiển thị dữ liệu từ `Shipment` object
- Không thay đổi

---

## 📅 Ngày thay đổi
**28/10/2025**

## 👤 Người thực hiện
Cập nhật logic để shipper đọc trực tiếp Order PROCESSING thay vì dùng Shipment PENDING
