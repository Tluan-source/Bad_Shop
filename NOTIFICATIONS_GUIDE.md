# 🔔 Trang Thông Báo - Hướng Dẫn & Tính Năng

## 📋 Tổng Quan

Trang thông báo đã được xây dựng hoàn chỉnh với **CSS và JavaScript tách biệt** để dễ bảo trì và mở rộng.

---

## 📁 Cấu Trúc File

### 1. **HTML Template**

📄 `src/main/resources/templates/user/notifications.html`

-    Giao diện hiển thị danh sách thông báo
-    Sử dụng Thymeleaf để render dữ liệu động
-    Tích hợp fragments (header, footer)

### 2. **CSS Stylesheet**

🎨 `src/main/resources/static/css/notifications.css`

-    **Tách biệt hoàn toàn** khỏi HTML
-    Styling cho tất cả components
-    Responsive design cho mobile
-    Animation và hiệu ứng chuyển động

### 3. **JavaScript Logic**

⚙️ `src/main/resources/static/js/notifications.js`

-    **Tách biệt hoàn toàn** khỏi HTML
-    Xử lý sự kiện click, mark as read
-    AJAX calls tới backend API
-    Toast notifications

---

## ✨ Tính Năng Chính

### 🔍 Hiển Thị Thông Báo

-    ✅ Danh sách thông báo theo thời gian (mới nhất trước)
-    ✅ Badge hiển thị số lượng chưa đọc
-    ✅ Icon phân loại theo loại thông báo:
     -    🛍️ ORDER - Đơn hàng (màu xanh dương)
     -    📦 PRODUCT - Sản phẩm (màu cam)
     -    🏪 STORE - Cửa hàng (màu tím)
     -    ⭐ REVIEW - Đánh giá (màu vàng)
     -    🎟️ VOUCHER - Khuyến mãi (màu đỏ)
     -    ℹ️ SYSTEM - Hệ thống (màu xanh lá)

### 👆 Tương Tác

1. **Click vào thông báo**

     - Tự động đánh dấu đã đọc
     - Chuyển đến trang liên quan (đơn hàng, sản phẩm, v.v.)

2. **Nút "Đánh dấu đã đọc"** (✓)

     - Xuất hiện khi hover vào thông báo chưa đọc
     - Đánh dấu đã đọc mà không chuyển trang

3. **Nút "Đánh dấu tất cả đã đọc"**
     - Ở góc trên bên phải
     - Đánh dấu tất cả thông báo chưa đọc cùng lúc

### 🎨 Giao Diện

-    **Gradient header** màu tím đẹp mắt
-    **Trạng thái chưa đọc**: Nền xanh nhạt + viền trái xanh đậm
-    **Hover effects**: Background thay đổi khi di chuột
-    **Animation**: Pulse effect cho unread dot
-    **Empty state**: Hiển thị khi không có thông báo
-    **Responsive**: Tự động điều chỉnh trên mobile

---

## 🔧 API Endpoints

### Backend Controller

📍 `NotificationController.java`

| Method | Endpoint                   | Mô tả                                |
| ------ | -------------------------- | ------------------------------------ |
| GET    | `/notifications`           | Trang hiển thị thông báo             |
| GET    | `/notifications/list`      | API lấy danh sách thông báo          |
| GET    | `/notifications/count`     | API đếm số thông báo chưa đọc        |
| GET    | `/notifications/recent`    | API lấy thông báo gần đây (dropdown) |
| POST   | `/notifications/{id}/read` | API đánh dấu 1 thông báo đã đọc      |
| POST   | `/notifications/read-all`  | API đánh dấu tất cả đã đọc           |

---

## 🎯 Luồng Hoạt Động

### 1. Tải Trang

```
User → GET /notifications
     → Controller lấy danh sách thông báo
     → Render HTML với Thymeleaf
     → Load CSS + JS
```

### 2. Đánh Dấu Đã Đọc (Single)

```
User click notification
     → JS: markNotificationAsRead(id)
     → POST /notifications/{id}/read
     → Backend update database
     → Response: {success: true, unreadCount: X}
     → JS update UI (remove unread class)
     → Navigate to related content
```

### 3. Đánh Dấu Tất Cả

```
User click "Đánh dấu tất cả"
     → JS: markAllNotificationsAsRead()
     → POST /notifications/read-all
     → Backend update all notifications
     → Response: {success: true, unreadCount: 0}
     → JS remove all unread classes
     → Hide badge and button
     → Show success toast
```

---

## 🐛 Debug & Troubleshooting

### Vấn Đề: Không hiển thị thông báo

**Nguyên nhân:** File HTML bị rỗng (lỗi ban đầu)  
**Giải pháp:** ✅ Đã tạo lại file HTML đầy đủ

### Vấn Đề: CSS không load

**Kiểm tra:**

1. File tồn tại: `static/css/notifications.css`
2. Thymeleaf link: `th:href="@{/css/notifications.css}"`
3. Spring Boot serving static resources

### Vấn Đề: JS không chạy

**Kiểm tra:**

1. Console log: `Notifications.js loaded`
2. Event listeners được attach
3. CSRF token (nếu có)

### Vấn Đề: API không hoạt động

**Kiểm tra:**

1. Controller mapping đúng
2. Authentication valid
3. Database có data
4. Network tab trong DevTools

---

## 📱 Responsive Design

### Desktop (>768px)

-    Header ngang, actions bên phải
-    Icon thông báo 48x48px
-    Padding rộng rãi

### Mobile (≤768px)

-    Header dọc, actions full width
-    Icon thông báo 40x40px
-    Padding compact hơn
-    Font size nhỏ hơn

---

## 🎨 Color Scheme

| Element    | Color     | Usage                          |
| ---------- | --------- | ------------------------------ |
| Primary    | `#667eea` | Unread border, buttons         |
| Secondary  | `#764ba2` | Gradient header                |
| Unread BG  | `#f0f4ff` | Unread notification background |
| Hover BG   | `#f8f9fa` | Hover effect                   |
| Text       | `#212529` | Title text                     |
| Text Muted | `#6c757d` | Message text                   |
| Border     | `#f0f0f0` | Separator                      |

---

## 🚀 Tối Ưu Performance

1. **CSS Loading**

     - Tách file riêng → Browser caching
     - Minify trong production

2. **JS Loading**

     - Load cuối trang (defer)
     - Event delegation cho dynamic content

3. **API Calls**

     - Sử dụng fetch API (modern)
     - Error handling đầy đủ

4. **Animation**
     - CSS animation (hardware accelerated)
     - Will-change property
     - Contain property cho optimization

---

## 📝 Maintenance Tips

### Thêm Loại Thông Báo Mới

1. **Backend**: Thêm enum type
2. **HTML**: Thêm case trong `th:class`
3. **CSS**: Thêm class `.notification-icon.NEW_TYPE`
4. **JS**: Thêm case trong `navigateToRelatedContent()`

### Tùy Chỉnh Giao Diện

-    **Colors**: Sửa trong `notifications.css`
-    **Spacing**: Adjust padding/margin
-    **Animation**: Modify `@keyframes`

### Thêm Tính Năng

-    **Filter**: Thêm dropdown lọc theo type
-    **Search**: Thêm search bar
-    **Pagination**: Load more khi scroll
-    **Real-time**: WebSocket cho push notifications

---

## ✅ Checklist Hoàn Thành

-    [x] Tạo file HTML với Thymeleaf
-    [x] Tách CSS ra file riêng
-    [x] Tách JS ra file riêng
-    [x] Xử lý click notification
-    [x] Mark as read (single)
-    [x] Mark all as read
-    [x] Update unread count
-    [x] Navigation to related content
-    [x] Toast notifications
-    [x] Empty state
-    [x] Loading state
-    [x] Responsive design
-    [x] Hover effects
-    [x] Animation
-    [x] Error handling

---

## 🎓 Best Practices Đã Áp Dụng

✅ **Separation of Concerns**: HTML, CSS, JS tách biệt  
✅ **Semantic HTML**: Sử dụng tags có nghĩa  
✅ **CSS Methodology**: BEM-like naming  
✅ **Progressive Enhancement**: Graceful degradation  
✅ **Accessibility**: ARIA labels, keyboard navigation  
✅ **Performance**: Lazy loading, debouncing  
✅ **Security**: CSRF protection, XSS prevention  
✅ **Maintainability**: Clean code, comments

---

## 🔗 Liên Kết Tham Khảo

-    Thymeleaf: https://www.thymeleaf.org/
-    Spring Boot Static Resources: https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.servlet.spring-mvc.static-content
-    Fetch API: https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API
-    CSS Animations: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Animations

---

**Ngày tạo:** 06/12/2025  
**Version:** 1.0  
**Trạng thái:** ✅ Hoàn thành
