# 📚 HƯỚNG DẪN SỬ DỤNG CLOUDINARY

## 🎯 Tổng Quan

Cloudinary là dịch vụ lưu trữ và quản lý ảnh trên cloud. Dự án của bạn đã được cấu hình sẵn để sử dụng Cloudinary.

### 📋 Thông tin tài khoản hiện tại:
- **Cloud Name:** `duzkugddg`
- **API Key:** `318697823826668`
- **API Secret:** `7ESbMuV-DENDpSf2QaHLpel9Ndw`

---

## 🏗️ Kiến Trúc Đã Có

### 1. **CloudinaryConfig.java** (Configuration)
```java
@Configuration
public class CloudinaryConfig {
    @Value("${cloudinary.cloud-name}")
    private String cloudName;
    
    @Value("${cloudinary.api-key}")
    private String apiKey;
    
    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        ));
    }
}
```
- Đọc thông tin từ `application.properties`
- Tạo bean Cloudinary để inject vào các service

### 2. **CloudinaryService.java** (Service Layer)
```java
@Service
public class CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    // Upload ảnh
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
            ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image"
            ));
        return uploadResult.get("secure_url").toString();
    }
    
    // Xóa ảnh
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
```

---

## 📝 CÁCH SỬ DỤNG

### ✅ **Bước 1: Tạo Form Upload trong HTML**

#### Ví dụ: Upload Avatar User
```html
<!-- templates/user/profile.html -->
<form th:action="@{/user/upload-avatar}" method="post" enctype="multipart/form-data">
    <div class="mb-3">
        <label for="avatarFile" class="form-label">
            <i class="fas fa-image me-2"></i>Chọn ảnh đại diện
        </label>
        <input type="file" 
               class="form-control" 
               id="avatarFile" 
               name="avatarFile" 
               accept="image/*" 
               required>
    </div>
    
    <!-- Preview ảnh -->
    <div class="mb-3">
        <img id="avatarPreview" 
             src="#" 
             alt="Preview" 
             style="max-width: 200px; display: none;" 
             class="img-thumbnail">
    </div>
    
    <button type="submit" class="btn btn-primary">
        <i class="fas fa-upload me-2"></i>Upload Avatar
    </button>
</form>

<script>
    // Preview ảnh trước khi upload
    document.getElementById('avatarFile').addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                const preview = document.getElementById('avatarPreview');
                preview.src = e.target.result;
                preview.style.display = 'block';
            }
            reader.readAsDataURL(file);
        }
    });
</script>
```

#### Ví dụ: Upload Ảnh Sản Phẩm (Multiple Images)
```html
<!-- templates/vendor/product-form.html -->
<form th:action="@{/vendor/products/create}" method="post" enctype="multipart/form-data">
    <div class="mb-3">
        <label class="form-label">Tên sản phẩm</label>
        <input type="text" name="name" class="form-control" required>
    </div>
    
    <div class="mb-3">
        <label class="form-label">
            <i class="fas fa-images me-2"></i>Hình ảnh sản phẩm (Tối đa 5 ảnh)
        </label>
        <input type="file" 
               class="form-control" 
               name="productImages" 
               accept="image/*" 
               multiple 
               required>
        <small class="text-muted">Chọn nhiều ảnh bằng Ctrl + Click</small>
    </div>
    
    <button type="submit" class="btn btn-success">
        <i class="fas fa-plus me-2"></i>Tạo sản phẩm
    </button>
</form>
```

---

### ✅ **Bước 2: Tạo Controller Xử Lý Upload**

#### Ví dụ 1: Upload Avatar User
```java
@Controller
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/upload-avatar")
    public String uploadAvatar(
            @RequestParam("avatarFile") MultipartFile file,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Lấy thông tin user đang đăng nhập
            String username = principal.getName();
            User user = userService.findByUsername(username);
            
            // Xóa ảnh cũ nếu có
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                // Extract publicId từ URL cũ
                String oldPublicId = extractPublicId(user.getAvatar());
                cloudinaryService.deleteImage(oldPublicId);
            }
            
            // Upload ảnh mới lên Cloudinary
            String avatarUrl = cloudinaryService.uploadImage(file, "badminton/avatars");
            
            // Cập nhật URL vào database
            user.setAvatar(avatarUrl);
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Upload avatar thành công!");
            
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi khi upload avatar: " + e.getMessage());
        }
        
        return "redirect:/user/profile";
    }
    
    // Helper method: Extract publicId từ Cloudinary URL
    private String extractPublicId(String cloudinaryUrl) {
        // URL format: https://res.cloudinary.com/duzkugddg/image/upload/v123456/badminton/avatars/abc123.jpg
        // PublicId: badminton/avatars/abc123
        
        String[] parts = cloudinaryUrl.split("/upload/");
        if (parts.length == 2) {
            String pathWithVersion = parts[1];
            // Bỏ version (v123456)
            String path = pathWithVersion.substring(pathWithVersion.indexOf("/") + 1);
            // Bỏ extension (.jpg)
            return path.substring(0, path.lastIndexOf("."));
        }
        return "";
    }
}
```

#### Ví dụ 2: Upload Nhiều Ảnh Sản Phẩm
```java
@Controller
@RequestMapping("/vendor/products")
public class VendorProductController {
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private ProductService productService;
    
    @PostMapping("/create")
    public String createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("productImages") MultipartFile[] images,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Tạo sản phẩm mới
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            
            // Upload tất cả ảnh lên Cloudinary
            List<String> imageUrls = new ArrayList<>();
            
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String imageUrl = cloudinaryService.uploadImage(
                        image, 
                        "badminton/products"
                    );
                    imageUrls.add(imageUrl);
                }
            }
            
            // Lưu danh sách URL dưới dạng JSON
            ObjectMapper mapper = new ObjectMapper();
            String jsonImageUrls = mapper.writeValueAsString(imageUrls);
            product.setListImages(jsonImageUrls);
            
            // Lưu sản phẩm vào database
            productService.save(product);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Tạo sản phẩm thành công!");
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi khi tạo sản phẩm: " + e.getMessage());
        }
        
        return "redirect:/vendor/products";
    }
}
```

#### Ví dụ 3: Xóa Ảnh Sản Phẩm
```java
@Controller
@RequestMapping("/vendor/products")
public class VendorProductController {
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private ProductService productService;
    
    @PostMapping("/{id}/delete")
    public String deleteProduct(
            @PathVariable String id,
            RedirectAttributes redirectAttributes) {
        
        try {
            Product product = productService.findById(id).orElseThrow();
            
            // Parse JSON để lấy danh sách URL ảnh
            ObjectMapper mapper = new ObjectMapper();
            List<String> imageUrls = mapper.readValue(
                product.getListImages(), 
                new TypeReference<List<String>>(){}
            );
            
            // Xóa tất cả ảnh trên Cloudinary
            for (String imageUrl : imageUrls) {
                String publicId = extractPublicId(imageUrl);
                cloudinaryService.deleteImage(publicId);
            }
            
            // Xóa sản phẩm khỏi database
            productService.delete(product);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Xóa sản phẩm thành công!");
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
        
        return "redirect:/vendor/products";
    }
}
```

---

## 🎨 CẤU TRÚC FOLDER TRÊN CLOUDINARY

Nên tổ chức folder theo cấu trúc rõ ràng:

```
badminton/
├── avatars/          # Avatar người dùng
├── products/         # Ảnh sản phẩm
├── stores/           # Logo/Banner cửa hàng
├── banners/          # Banner quảng cáo
└── categories/       # Icon danh mục
```

**Ví dụ upload với folder cụ thể:**
```java
// Avatar
cloudinaryService.uploadImage(file, "badminton/avatars");

// Product
cloudinaryService.uploadImage(file, "badminton/products");

// Store logo
cloudinaryService.uploadImage(file, "badminton/stores");
```

---

## 🔧 CẢI TIẾN SERVICE (Tùy Chọn)

### Thêm các method hữu ích vào CloudinaryService:

```java
@Service
public class CloudinaryService {
    
    @Autowired
    private Cloudinary cloudinary;

    /**
     * Upload ảnh với tùy chọn resize
     */
    public String uploadImageWithResize(MultipartFile file, String folder, 
                                       int width, int height) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
            ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image",
                "transformation", new Transformation()
                    .width(width)
                    .height(height)
                    .crop("fill")
            ));
        return uploadResult.get("secure_url").toString();
    }
    
    /**
     * Upload ảnh với quality compress
     */
    public String uploadImageWithQuality(MultipartFile file, String folder, 
                                        int quality) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
            ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image",
                "quality", quality  // 1-100
            ));
        return uploadResult.get("secure_url").toString();
    }
    
    /**
     * Validate file trước khi upload
     */
    public boolean isValidImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            return false;
        }
        
        // Check file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            return false;
        }
        
        // Check content type
        String contentType = file.getContentType();
        return contentType != null && 
               (contentType.equals("image/jpeg") || 
                contentType.equals("image/png") || 
                contentType.equals("image/jpg") ||
                contentType.equals("image/webp"));
    }
    
    /**
     * Extract publicId từ Cloudinary URL
     */
    public String extractPublicId(String cloudinaryUrl) {
        String[] parts = cloudinaryUrl.split("/upload/");
        if (parts.length == 2) {
            String pathWithVersion = parts[1];
            String path = pathWithVersion.substring(pathWithVersion.indexOf("/") + 1);
            return path.substring(0, path.lastIndexOf("."));
        }
        return "";
    }
    
    /**
     * Xóa ảnh
     */
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
    
    /**
     * Xóa nhiều ảnh cùng lúc
     */
    public void deleteImages(List<String> publicIds) throws IOException {
        for (String publicId : publicIds) {
            deleteImage(publicId);
        }
    }
}
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

### 1. **Xử lý Exception**
```java
try {
    String url = cloudinaryService.uploadImage(file, "badminton/products");
} catch (IOException e) {
    // Log lỗi
    e.printStackTrace();
    // Thông báo cho user
    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi upload ảnh");
}
```

### 2. **Validate File**
```java
// Check file không rỗng
if (file.isEmpty()) {
    throw new IllegalArgumentException("File không được để trống");
}

// Check size (max 10MB)
if (file.getSize() > 10 * 1024 * 1024) {
    throw new IllegalArgumentException("File không được vượt quá 10MB");
}

// Check định dạng
String contentType = file.getContentType();
if (!contentType.startsWith("image/")) {
    throw new IllegalArgumentException("Chỉ chấp nhận file ảnh");
}
```

### 3. **Xóa Ảnh Cũ Khi Update**
```java
// Luôn xóa ảnh cũ trước khi upload ảnh mới
if (user.getAvatar() != null) {
    String oldPublicId = extractPublicId(user.getAvatar());
    cloudinaryService.deleteImage(oldPublicId);
}

// Upload ảnh mới
String newAvatarUrl = cloudinaryService.uploadImage(file, "badminton/avatars");
user.setAvatar(newAvatarUrl);
```

### 4. **Transaction Management**
```java
@Transactional
public void updateProductImages(String productId, MultipartFile[] newImages) {
    Product product = productRepository.findById(productId).orElseThrow();
    
    // Xóa ảnh cũ
    List<String> oldUrls = parseImageUrls(product.getListImages());
    for (String url : oldUrls) {
        cloudinaryService.deleteImage(extractPublicId(url));
    }
    
    // Upload ảnh mới
    List<String> newUrls = new ArrayList<>();
    for (MultipartFile image : newImages) {
        String url = cloudinaryService.uploadImage(image, "badminton/products");
        newUrls.add(url);
    }
    
    // Cập nhật database
    product.setListImages(toJson(newUrls));
    productRepository.save(product);
}
```

---

## 📊 GIỚI HẠN TÀI KHOẢN FREE

Cloudinary Free Plan có giới hạn:
- ✅ 25 GB lưu trữ
- ✅ 25 GB bandwidth/tháng
- ✅ Unlimited transformations

**Theo dõi usage tại:** https://cloudinary.com/console

---

## 🎯 TỔNG KẾT LUỒNG HOẠT ĐỘNG

```
1. User chọn file từ form HTML
   └─> <input type="file" name="avatarFile">

2. Submit form lên Controller
   └─> @PostMapping("/upload-avatar")
   └─> @RequestParam("avatarFile") MultipartFile file

3. Controller gọi CloudinaryService
   └─> cloudinaryService.uploadImage(file, "badminton/avatars")

4. CloudinaryService upload file lên Cloudinary
   └─> cloudinary.uploader().upload(file.getBytes(), options)
   └─> Trả về URL: https://res.cloudinary.com/.../image.jpg

5. Controller lưu URL vào database
   └─> user.setAvatar(url)
   └─> userService.save(user)

6. Hiển thị ảnh trong template
   └─> <img th:src="${user.avatar}">
```

---

## 📝 CHECKLIST TRIỂN KHAI

- [ ] Đã có CloudinaryConfig.java ✅
- [ ] Đã có CloudinaryService.java ✅
- [ ] Đã cấu hình application.properties ✅
- [ ] Tạo form upload trong HTML
- [ ] Tạo Controller xử lý upload
- [ ] Validate file trước khi upload
- [ ] Xử lý exception khi upload
- [ ] Xóa ảnh cũ khi update
- [ ] Hiển thị ảnh trong template
- [ ] Test chức năng upload/delete

---

## 🚀 ĐỀ XUẤT CHỨC NĂNG CẦN LÀM

### 1. **Upload Avatar User** (Priority: HIGH)
- Form: `templates/user/profile.html`
- Controller: `UserController.uploadAvatar()`
- Folder: `badminton/avatars`

### 2. **Upload Ảnh Sản Phẩm** (Priority: HIGH)
- Form: `templates/vendor/product-create.html`
- Controller: `VendorProductController.createProduct()`
- Folder: `badminton/products`
- Support: Multiple images

### 3. **Upload Store Logo/Banner** (Priority: MEDIUM)
- Form: `templates/vendor/store-settings.html`
- Controller: `VendorStoreController.updateStore()`
- Folder: `badminton/stores`

### 4. **Upload Category Icon** (Priority: LOW)
- Admin only
- Folder: `badminton/categories`

---

## ❓ CÂU HỎI THƯỜNG GẶP

**Q: Làm sao để resize ảnh tự động?**
A: Thêm `transformation` vào upload options:
```java
"transformation", new Transformation().width(800).height(600).crop("fill")
```

**Q: Làm sao để compress ảnh giảm dung lượng?**
A: Thêm `quality` vào upload options:
```java
"quality", 80  // 1-100, 80 là tốt nhất
```

**Q: Làm sao để upload video?**
A: Đổi `resource_type` từ `"image"` thành `"video"`:
```java
"resource_type", "video"
```

**Q: Làm sao để lấy thumbnail của video?**
A: Cloudinary tự động tạo thumbnail, thêm `.jpg` vào URL video:
```
Video: https://res.cloudinary.com/.../video.mp4
Thumbnail: https://res.cloudinary.com/.../video.jpg
```

---

## 📞 HỖ TRỢ

- **Cloudinary Docs:** https://cloudinary.com/documentation
- **Java SDK:** https://cloudinary.com/documentation/java_integration
- **Admin Console:** https://cloudinary.com/console

---

✨ **Chúc bạn code thành công!**
