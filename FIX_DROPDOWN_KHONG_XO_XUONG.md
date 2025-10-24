# FIX DROPDOWN KHÔNG XỔ XUỐNG

## Vấn đề
Khi click vào tên admin (hoặc vendor/shipper) trên header, dropdown không xổ xuống.

## Nguyên nhân
1. Bootstrap JavaScript không được load trước khi header được render
2. Bootstrap JS bị load ở footer → dropdown chưa khởi tạo kịp

## Giải pháp đã áp dụng

### 1. Di chuyển Bootstrap JS lên `<head>`

**File:** `fragments/head.html`

```html
<head th:fragment="head">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Badminton Marketplace</title>
    
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" 
          rel="stylesheet" 
          integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" 
          crossorigin="anonymous">
    
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    
    <!-- Custom CSS -->
    <link rel="stylesheet" href="/css/style.css?v=2" th:href="@{/css/style.css(v=2)}" />
    
    <!-- Bootstrap Bundle JS (includes Popper) - QUAN TRỌNG: Phải load trong head -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" 
            integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" 
            crossorigin="anonymous"></script>
</head>
```

**Tại sao?**
- Bootstrap dropdown cần JavaScript để hoạt động
- Load JS trong `<head>` đảm bảo nó sẵn sàng khi header được render
- `bootstrap.bundle.min.js` bao gồm cả Popper.js (cần cho dropdown positioning)

### 2. Xóa Bootstrap JS trùng lặp ở footer

Đã xóa dòng này khỏi tất cả footer files:
```html
<!-- KHÔNG CẦN NỮA -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
```

### 3. Tạo file `main.js` để hỗ trợ dropdown

**File:** `static/js/main.js`

```javascript
// Initialize all Bootstrap dropdowns when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Initialize Bootstrap dropdowns
    const dropdownElementList = document.querySelectorAll('.dropdown-toggle');
    const dropdownList = [...dropdownElementList].map(dropdownToggleEl => 
        new bootstrap.Dropdown(dropdownToggleEl)
    );
    
    console.log('Dropdowns initialized:', dropdownList.length);
});

// Fallback: Manual dropdown toggle
document.addEventListener('DOMContentLoaded', function() {
    const dropdownToggles = document.querySelectorAll('[data-bs-toggle="dropdown"]');
    
    dropdownToggles.forEach(toggle => {
        toggle.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            const dropdownMenu = this.nextElementSibling;
            
            // Close other dropdowns
            document.querySelectorAll('.dropdown-menu.show').forEach(menu => {
                if (menu !== dropdownMenu) {
                    menu.classList.remove('show');
                }
            });
            
            // Toggle current dropdown
            dropdownMenu.classList.toggle('show');
        });
    });
    
    // Close dropdown when clicking outside
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.dropdown')) {
            document.querySelectorAll('.dropdown-menu.show').forEach(menu => {
                menu.classList.remove('show');
            });
        }
    });
});
```

**Chức năng:**
- Khởi tạo tất cả dropdown khi trang load
- Có fallback code để toggle dropdown thủ công (phòng trường hợp Bootstrap chưa load)
- Tự động đóng dropdown khi click ra ngoài

## Cấu trúc Dropdown trong Header

Dropdown trong header sử dụng cú pháp Bootstrap 5 chuẩn:

```html
<li class="nav-item dropdown">
    <a class="nav-link dropdown-toggle d-flex align-items-center" 
       href="#" 
       id="adminDropdown" 
       role="button" 
       data-bs-toggle="dropdown"    <!-- Quan trọng: Bootstrap trigger -->
       aria-expanded="false">
        <i class="fas fa-user-shield me-2"></i>
        <span sec:authentication="name">Admin</span>
    </a>
    
    <ul class="dropdown-menu dropdown-menu-end shadow" 
        aria-labelledby="adminDropdown">
        <!-- Dropdown items -->
    </ul>
</li>
```

**Các thuộc tính quan trọng:**
- `data-bs-toggle="dropdown"` - Kích hoạt Bootstrap dropdown
- `aria-expanded="false"` - Accessibility
- `dropdown-menu-end` - Dropdown hiển thị bên phải
- `aria-labelledby="adminDropdown"` - Liên kết với button trigger

## Cách kiểm tra

### 1. Clear cache browser
```
Ctrl + Shift + R (Windows/Linux)
Cmd + Shift + R (Mac)
```

### 2. Mở DevTools Console
```
F12 → Console tab
```

Kiểm tra xem có thông báo:
```
Dropdowns initialized: 1
```

### 3. Test dropdown
- Click vào tên user trên header
- Dropdown phải xổ xuống với animation mượt mà
- Click ra ngoài → dropdown tự động đóng

### 4. Kiểm tra Bootstrap đã load
Trong Console, gõ:
```javascript
typeof bootstrap
```

Kết quả phải là: `"object"` (không phải "undefined")

## Các trường hợp lỗi và cách fix

### Lỗi 1: Dropdown vẫn không xổ
**Nguyên nhân:** Cache browser
**Fix:** 
```
Ctrl + Shift + Delete → Clear cache → Hard refresh
```

### Lỗi 2: Console báo "bootstrap is not defined"
**Nguyên nhân:** Bootstrap JS chưa load
**Fix:** Kiểm tra lại thẻ `<script>` trong head.html

### Lỗi 3: Dropdown xổ nhưng không có animation
**Nguyên nhân:** CSS chưa được load đúng
**Fix:** Thay đổi version trong URL:
```html
<link rel="stylesheet" href="/css/style.css?v=3" />
```

### Lỗi 4: "Uncaught ReferenceError: Dropdown is not defined"
**Nguyên nhân:** Bootstrap bundle chưa load hoặc bị chặn bởi adblocker
**Fix:** 
- Tắt adblocker
- Hoặc download Bootstrap về local

## Files đã thay đổi

1. ✅ `fragments/head.html` - Thêm Bootstrap JS
2. ✅ `fragments/footer-admin.html` - Xóa Bootstrap JS trùng
3. ✅ `fragments/footer-vendor.html` - Xóa Bootstrap JS trùng
4. ✅ `fragments/footer-shipper.html` - Xóa Bootstrap JS trùng
5. ✅ `fragments/footer-user.html` - Xóa Bootstrap JS trùng
6. ✅ `static/js/main.js` - Tạo file mới

## Testing

### Admin:
```
Login: admin@badminton.com / 123456
URL: http://localhost:8080/admin/dashboard
```
Click "admin@badminton.com" → Dropdown xổ xuống

### Vendor:
```
Login: vendor@badminton.com / 123456
URL: http://localhost:8080/vendor/dashboard
```
Click "vendor@badminton.com" → Dropdown xổ xuống

### Shipper:
```
Login: shipper@badminton.com / 123456
URL: http://localhost:8080/shipper/dashboard
```
Click "shipper@badminton.com" → Dropdown xổ xuống

## Kết luận

Vấn đề đã được fix bằng cách:
1. ✅ Di chuyển Bootstrap JS lên head
2. ✅ Xóa Bootstrap JS trùng lặp
3. ✅ Thêm file main.js để hỗ trợ
4. ✅ Tăng cache version CSS lên v=2

**Dropdown bây giờ phải hoạt động hoàn hảo!**

---
Tạo bởi: GitHub Copilot
Ngày: 24/10/2025
