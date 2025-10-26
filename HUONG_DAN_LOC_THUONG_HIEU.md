# 🏷️ HƯỚNG DẪN LỌC SẢN PHẨM THEO THƯƠNG HIỆU

## 🔍 TÌNH TRẠNG HIỆN TẠI

**Vấn đề:** Database không có cột `brand` (thương hiệu) riêng trong bảng `products`.

**Giải pháp hiện tại (TẠM THỜI):**
```java
// Filter theo thương hiệu (giả sử brand trong tên sản phẩm)
if (brand != null && !brand.isEmpty()) {
    products = products.stream()
        .filter(p -> p.getName().toLowerCase().contains(brand.toLowerCase()))
        .collect(Collectors.toList());
}
```

❌ **Nhược điểm:**
- Không chính xác (ví dụ: tìm "yonex" có thể match với sản phẩm có tên "nonyonex")
- Không có danh sách thương hiệu chuẩn
- Không thể hiển thị tất cả thương hiệu có sẵn
- Khó quản lý và mở rộng

---

## 💡 CÁC GIẢI PHÁP

### 📌 **GIẢI PHÁP 1: THÊM CỘT BRAND VÀO BẢNG PRODUCTS** (KHUYẾN NGHỊ)

#### **Bước 1: Thêm cột `brand` vào Entity**

```java
// Product.java
@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    private String id;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(500)")
    private String name;
    
    // ✨ THÊM FIELD MỚI
    @Column(columnDefinition = "NVARCHAR(100)")
    private String brand;  // Thương hiệu: Yonex, Victor, Li-Ning, Mizuno...
    
    // ... các field khác
}
```

#### **Bước 2: Tạo migration SQL để thêm cột**

```sql
-- Thêm cột brand vào bảng products
ALTER TABLE products 
ADD brand NVARCHAR(100) NULL;

-- Cập nhật brand cho các sản phẩm hiện có dựa trên tên
UPDATE products SET brand = N'Yonex' WHERE name LIKE N'%Yonex%';
UPDATE products SET brand = N'Victor' WHERE name LIKE N'%Victor%';
UPDATE products SET brand = N'Li-Ning' WHERE name LIKE N'%Li-Ning%';
UPDATE products SET brand = N'Mizuno' WHERE name LIKE N'%Mizuno%';
UPDATE products SET brand = N'Kumpoo' WHERE name LIKE N'%Kumpoo%';
UPDATE products SET brand = N'FZ Forza' WHERE name LIKE N'%Forza%';
UPDATE products SET brand = N'Apacs' WHERE name LIKE N'%Apacs%';
UPDATE products SET brand = N'Kawasaki' WHERE name LIKE N'%Kawasaki%';
UPDATE products SET brand = N'Fleet' WHERE name LIKE N'%Fleet%';
UPDATE products SET brand = N'Proace' WHERE name LIKE N'%Proace%';
UPDATE products SET brand = N'Adidas' WHERE name LIKE N'%Adidas%';
UPDATE products SET brand = N'Lining' WHERE name LIKE N'%Lining%';
-- Sản phẩm không có brand
UPDATE products SET brand = N'Other' WHERE brand IS NULL;
```

#### **Bước 3: Cập nhật ProductRepository**

```java
// ProductRepository.java
public interface ProductRepository extends JpaRepository<Product, String> {
    
    // Existing methods...
    List<Product> findByIsActiveTrueAndIsSellingTrue();
    List<Product> findByCategoryIdAndIsActiveTrueAndIsSellingTrue(String categoryId);
    
    // ✨ THÊM METHOD MỚI
    // Tìm theo brand
    List<Product> findByBrandAndIsActiveTrueAndIsSellingTrue(String brand);
    
    // Tìm theo brand và category
    List<Product> findByBrandAndCategoryIdAndIsActiveTrueAndIsSellingTrue(
        String brand, String categoryId);
    
    // Lấy danh sách tất cả brand có trong hệ thống
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.isActive = true AND p.isSelling = true ORDER BY p.brand")
    List<String> findAllDistinctBrands();
}
```

#### **Bước 4: Cập nhật ProductService**

```java
// ProductService.java
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> filterProducts(String categoryId, Double minPrice, Double maxPrice, String brand) {
        // Bắt đầu với tất cả sản phẩm active
        List<Product> products = productRepository.findByIsActiveTrueAndIsSellingTrue();
        
        // ✨ Filter theo brand CHÍNH XÁC
        if (brand != null && !brand.isEmpty()) {
            products = products.stream()
                .filter(p -> brand.equalsIgnoreCase(p.getBrand()))
                .collect(Collectors.toList());
        }
        
        // Filter theo category
        if (categoryId != null && !categoryId.isEmpty()) {
            products = products.stream()
                .filter(p -> p.getCategory() != null && categoryId.equals(p.getCategory().getId()))
                .collect(Collectors.toList());
        }
        
        // Filter theo khoảng giá
        if (minPrice != null || maxPrice != null) {
            products = products.stream()
                .filter(p -> {
                    Double price = p.getPromotionalPrice() != null ? 
                                  p.getPromotionalPrice().doubleValue() : p.getPrice().doubleValue();
                    boolean valid = true;
                    if (minPrice != null) valid = valid && price >= minPrice;
                    if (maxPrice != null) valid = valid && price <= maxPrice;
                    return valid;
                })
                .collect(Collectors.toList());
        }
        
        return products;
    }
    
    // ✨ THÊM METHOD MỚI
    public List<String> getAllBrands() {
        return productRepository.findAllDistinctBrands();
    }
    
    public List<Product> findByBrand(String brand) {
        return productRepository.findByBrandAndIsActiveTrueAndIsSellingTrue(brand);
    }
}
```

#### **Bước 5: Cập nhật Controller để lấy danh sách brands**

```java
// HomeController.java
@Controller
public class HomeController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping("/products")
    public String products(
        @RequestParam(value = "category", required = false) String categoryId,
        @RequestParam(value = "minPrice", required = false) Double minPrice,
        @RequestParam(value = "maxPrice", required = false) Double maxPrice,
        @RequestParam(value = "brand", required = false) String brand,
        @RequestParam(value = "sort", required = false) String sort,
        Model model){
    
        List<Product> productList;
    
        // Logic lọc sản phẩm
        if (categoryId != null || minPrice != null || maxPrice != null || brand != null) {
            productList = productService.filterProducts(categoryId, minPrice, maxPrice, brand);
        } else {
            productList = productService.getAllActiveProducts();
        }
        
        // Sắp xếp sản phẩm (giữ nguyên code cũ)
        // ...
        
        // ✨ THÊM DANH SÁCH BRANDS VÀO MODEL
        List<String> allBrands = productService.getAllBrands();
        model.addAttribute("allBrands", allBrands);
        
        model.addAttribute("products", productList);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("selectedSort", sort);
        return "user/products";
    }
}
```

#### **Bước 6: Cập nhật Template để hiển thị brands động**

```html
<!-- templates/user/products.html -->

<!-- Brand Filter -->
<div class="filter-box mb-4">
    <h5 class="filter-title">
        <i class="fas fa-tag me-2"></i>Thương hiệu
    </h5>
    <div class="filter-content">
        <!-- Tất cả -->
        <div class="form-check mb-2">
            <a
                th:href="@{/products(category=${selectedCategory}, minPrice=${selectedMinPrice}, maxPrice=${selectedMaxPrice})}"
                class="text-decoration-none"
                th:classappend="${selectedBrand == null ? 'fw-bold text-primary' : 'text-dark'}"
            >
                <i class="fas fa-chevron-right me-2"></i>Tất cả
            </a>
        </div>
        
        <!-- ✨ HIỂN THỊ BRANDS ĐỘNG TỪ DATABASE -->
        <div class="form-check mb-2" th:each="brandItem : ${allBrands}">
            <a
                th:href="@{/products(category=${selectedCategory}, minPrice=${selectedMinPrice}, maxPrice=${selectedMaxPrice}, brand=${brandItem})}"
                class="text-decoration-none"
                th:classappend="${selectedBrand == brandItem ? 'fw-bold text-primary' : 'text-dark'}"
            >
                <i class="fas fa-chevron-right me-2"></i>
                <span th:text="${brandItem}">Brand Name</span>
            </a>
        </div>
    </div>
</div>
```

#### **Bước 7: Cập nhật data.sql cho sản phẩm mới**

```sql
-- Khi thêm sản phẩm mới, nhớ thêm brand
INSERT INTO products (id, name, brand, description, price, ...) VALUES
(N'P26', N'Vợt Yonex Arcsaber 11', N'Yonex', N'Vợt cầu lông...', 4500000, ...),
(N'P27', N'Giày Victor P9200', N'Victor', N'Giày cầu lông...', 2800000, ...);
```

---

### 📌 **GIẢI PHÁP 2: TẠO BẢNG BRANDS RIÊNG** (CHUYÊN NGHIỆP HƠN)

#### **Ưu điểm:**
- Quản lý thương hiệu tập trung
- Có thể thêm thông tin brand: logo, description, country...
- Dễ mở rộng cho admin quản lý
- Có thể thêm brand mới mà không cần sửa code

#### **Bước 1: Tạo Entity Brand**

```java
// Brand.java
@Entity
@Table(name = "brands")
@Data
public class Brand {
    @Id
    private String id;  // BR1, BR2, BR3...
    
    @Column(nullable = false, unique = true, columnDefinition = "NVARCHAR(100)")
    private String name;  // Yonex, Victor, Li-Ning...
    
    @Column(columnDefinition = "NVARCHAR(500)")
    private String logo;  // URL logo
    
    @Column(columnDefinition = "NVARCHAR(1000)")
    private String description;  // Mô tả thương hiệu
    
    @Column(columnDefinition = "NVARCHAR(100)")
    private String country;  // Quốc gia
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "brand")
    @JsonIgnore
    private List<Product> products;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

#### **Bước 2: Cập nhật Entity Product**

```java
// Product.java
@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    private String id;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(500)")
    private String name;
    
    // ✨ THÊM RELATIONSHIP VỚI BRAND
    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;
    
    // ... các field khác
}
```

#### **Bước 3: Tạo SQL Schema**

```sql
-- Tạo bảng brands
CREATE TABLE brands (
    id NVARCHAR(50) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    logo NVARCHAR(500),
    description NVARCHAR(1000),
    country NVARCHAR(100),
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- Thêm dữ liệu brands
INSERT INTO brands (id, name, country, description) VALUES
(N'BR1', N'Yonex', N'Japan', N'Thương hiệu số 1 thế giới về cầu lông'),
(N'BR2', N'Victor', N'Taiwan', N'Thương hiệu cầu lông nổi tiếng từ Đài Loan'),
(N'BR3', N'Li-Ning', N'China', N'Thương hiệu thể thao hàng đầu Trung Quốc'),
(N'BR4', N'Mizuno', N'Japan', N'Thương hiệu thể thao Nhật Bản'),
(N'BR5', N'Kumpoo', N'China', N'Thương hiệu cầu lông phổ biến'),
(N'BR6', N'FZ Forza', N'Denmark', N'Thương hiệu cầu lông Đan Mạch'),
(N'BR7', N'Apacs', N'Singapore', N'Thương hiệu Singapore'),
(N'BR8', N'Kawasaki', N'Japan', N'Thương hiệu thể thao Nhật Bản'),
(N'BR9', N'Fleet', N'Taiwan', N'Thương hiệu Đài Loan'),
(N'BR10', N'Other', N'', N'Các thương hiệu khác');

-- Thêm cột brand_id vào bảng products
ALTER TABLE products ADD brand_id NVARCHAR(50);
ALTER TABLE products ADD FOREIGN KEY (brand_id) REFERENCES brands(id);

-- Cập nhật brand_id cho sản phẩm hiện có
UPDATE products SET brand_id = N'BR1' WHERE name LIKE N'%Yonex%';
UPDATE products SET brand_id = N'BR2' WHERE name LIKE N'%Victor%';
UPDATE products SET brand_id = N'BR3' WHERE name LIKE N'%Li-Ning%' OR name LIKE N'%Lining%';
UPDATE products SET brand_id = N'BR4' WHERE name LIKE N'%Mizuno%';
UPDATE products SET brand_id = N'BR5' WHERE name LIKE N'%Kumpoo%';
UPDATE products SET brand_id = N'BR6' WHERE name LIKE N'%Forza%';
UPDATE products SET brand_id = N'BR7' WHERE name LIKE N'%Apacs%';
UPDATE products SET brand_id = N'BR8' WHERE name LIKE N'%Kawasaki%';
UPDATE products SET brand_id = N'BR9' WHERE name LIKE N'%Fleet%';
UPDATE products SET brand_id = N'BR10' WHERE brand_id IS NULL;
```

#### **Bước 4: Tạo BrandRepository**

```java
// BrandRepository.java
public interface BrandRepository extends JpaRepository<Brand, String> {
    List<Brand> findByIsActiveTrue();
    Optional<Brand> findByName(String name);
}
```

#### **Bước 5: Tạo BrandService**

```java
// BrandService.java
@Service
public class BrandService {
    
    @Autowired
    private BrandRepository brandRepository;
    
    public List<Brand> getAllActiveBrands() {
        return brandRepository.findByIsActiveTrue();
    }
    
    public Optional<Brand> findById(String id) {
        return brandRepository.findById(id);
    }
    
    public Optional<Brand> findByName(String name) {
        return brandRepository.findByName(name);
    }
}
```

#### **Bước 6: Cập nhật ProductRepository**

```java
// ProductRepository.java
public interface ProductRepository extends JpaRepository<Product, String> {
    
    // Filter theo brand
    List<Product> findByBrandIdAndIsActiveTrueAndIsSellingTrue(String brandId);
    
    // Filter theo brand và category
    List<Product> findByBrandIdAndCategoryIdAndIsActiveTrueAndIsSellingTrue(
        String brandId, String categoryId);
}
```

#### **Bước 7: Cập nhật ProductService**

```java
// ProductService.java
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> filterProducts(String categoryId, Double minPrice, 
                                       Double maxPrice, String brandId) {
        List<Product> products = productRepository.findByIsActiveTrueAndIsSellingTrue();
        
        // ✨ Filter theo brand_id
        if (brandId != null && !brandId.isEmpty()) {
            products = products.stream()
                .filter(p -> p.getBrand() != null && brandId.equals(p.getBrand().getId()))
                .collect(Collectors.toList());
        }
        
        // Filter theo category
        if (categoryId != null && !categoryId.isEmpty()) {
            products = products.stream()
                .filter(p -> p.getCategory() != null && categoryId.equals(p.getCategory().getId()))
                .collect(Collectors.toList());
        }
        
        // Filter theo khoảng giá
        if (minPrice != null || maxPrice != null) {
            products = products.stream()
                .filter(p -> {
                    Double price = p.getPromotionalPrice() != null ? 
                                  p.getPromotionalPrice().doubleValue() : p.getPrice().doubleValue();
                    boolean valid = true;
                    if (minPrice != null) valid = valid && price >= minPrice;
                    if (maxPrice != null) valid = valid && price <= maxPrice;
                    return valid;
                })
                .collect(Collectors.toList());
        }
        
        return products;
    }
}
```

#### **Bước 8: Cập nhật Controller**

```java
// HomeController.java
@Controller
public class HomeController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private BrandService brandService;
    
    @GetMapping("/products")
    public String products(
        @RequestParam(value = "category", required = false) String categoryId,
        @RequestParam(value = "minPrice", required = false) Double minPrice,
        @RequestParam(value = "maxPrice", required = false) Double maxPrice,
        @RequestParam(value = "brand", required = false) String brandId,  // Đổi từ brand thành brandId
        @RequestParam(value = "sort", required = false) String sort,
        Model model){
    
        List<Product> productList;
    
        if (categoryId != null || minPrice != null || maxPrice != null || brandId != null) {
            productList = productService.filterProducts(categoryId, minPrice, maxPrice, brandId);
        } else {
            productList = productService.getAllActiveProducts();
        }
        
        // Sắp xếp (giữ nguyên)
        // ...
        
        // ✨ Lấy danh sách brands
        List<Brand> allBrands = brandService.getAllActiveBrands();
        model.addAttribute("allBrands", allBrands);
        
        model.addAttribute("products", productList);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedBrand", brandId);
        model.addAttribute("selectedSort", sort);
        return "user/products";
    }
}
```

#### **Bước 9: Cập nhật Template**

```html
<!-- templates/user/products.html -->

<!-- Brand Filter -->
<div class="filter-box mb-4">
    <h5 class="filter-title">
        <i class="fas fa-tag me-2"></i>Thương hiệu
    </h5>
    <div class="filter-content">
        <!-- Tất cả -->
        <div class="form-check mb-2">
            <a
                th:href="@{/products(category=${selectedCategory}, minPrice=${selectedMinPrice}, maxPrice=${selectedMaxPrice})}"
                class="text-decoration-none"
                th:classappend="${selectedBrand == null ? 'fw-bold text-primary' : 'text-dark'}"
            >
                <i class="fas fa-chevron-right me-2"></i>Tất cả
            </a>
        </div>
        
        <!-- ✨ HIỂN THỊ BRANDS TỪ BẢNG BRANDS -->
        <div class="form-check mb-2" th:each="brandItem : ${allBrands}">
            <a
                th:href="@{/products(category=${selectedCategory}, minPrice=${selectedMinPrice}, maxPrice=${selectedMaxPrice}, brand=${brandItem.id})}"
                class="text-decoration-none"
                th:classappend="${selectedBrand == brandItem.id ? 'fw-bold text-primary' : 'text-dark'}"
            >
                <i class="fas fa-chevron-right me-2"></i>
                <span th:text="${brandItem.name}">Brand Name</span>
            </a>
        </div>
    </div>
</div>
```

---

### 📌 **GIẢI PHÁP 3: GIỮ NGUYÊN (KHÔNG KHUYẾN NGHỊ)**

Nếu bạn muốn giữ nguyên cách hiện tại (tìm trong tên sản phẩm), ít nhất nên cải thiện:

#### **Cải thiện filter trong ProductService:**

```java
public List<Product> filterProducts(String categoryId, Double minPrice, 
                                   Double maxPrice, String brand) {
    List<Product> products = productRepository.findByIsActiveTrueAndIsSellingTrue();
    
    // Filter theo thương hiệu - CẢI THIỆN
    if (brand != null && !brand.isEmpty()) {
        final String searchBrand = brand.toLowerCase().trim();
        products = products.stream()
            .filter(p -> {
                String productName = p.getName().toLowerCase();
                // Tìm chính xác từ (không phải substring)
                return productName.matches(".*\\b" + searchBrand + "\\b.*");
            })
            .collect(Collectors.toList());
    }
    
    // ... các filter khác
    
    return products;
}
```

#### **Tạo danh sách brands cố định:**

```java
// HomeController.java
@GetMapping("/products")
public String products(...) {
    // ...
    
    // Danh sách brands cố định
    List<String> allBrands = Arrays.asList(
        "Yonex", "Victor", "Li-Ning", "Mizuno", "Kumpoo", 
        "FZ Forza", "Apacs", "Kawasaki", "Fleet", "Proace"
    );
    model.addAttribute("allBrands", allBrands);
    
    // ...
}
```

---

## 📊 SO SÁNH CÁC GIẢI PHÁP

| Tiêu chí | Giải pháp 1 (Brand Column) | Giải pháp 2 (Brand Table) | Giải pháp 3 (Hiện tại) |
|----------|---------------------------|--------------------------|----------------------|
| **Độ chính xác** | ✅ Cao | ✅ Rất cao | ❌ Thấp |
| **Dễ implement** | ✅ Dễ | ⚠️ Trung bình | ✅ Rất dễ |
| **Khả năng mở rộng** | ⚠️ Trung bình | ✅ Cao | ❌ Thấp |
| **Quản lý** | ⚠️ Cần update code | ✅ Admin panel | ❌ Hardcode |
| **Performance** | ✅ Tốt | ✅ Tốt | ⚠️ Trung bình |
| **Chi phí** | ⚠️ Migration DB | ⚠️ Migration DB + Code | ✅ Không có |

---

## 🎯 KHUYẾN NGHỊ

### 🥇 **Dự án nhỏ, đơn giản → GIẢI PHÁP 1**
- Nhanh chóng implement
- Đủ chính xác cho nhu cầu cơ bản
- Ít phức tạp

### 🥇 **Dự án lớn, chuyên nghiệp → GIẢI PHÁP 2**
- Mở rộng tốt cho tương lai
- Admin có thể quản lý brands
- Có thể thêm thông tin chi tiết (logo, country...)
- Phù hợp cho e-commerce thực tế

### 🚫 **KHÔNG nên dùng GIẢI PHÁP 3**
- Chỉ dùng tạm thời nếu deadline gấp
- Plan migrate sang Giải pháp 1 hoặc 2 sau

---

## 🚀 BƯỚC TIẾP THEO (Chọn Giải pháp 1 - Đơn giản nhất)

```sql
-- 1. Thêm cột brand vào products
ALTER TABLE products ADD brand NVARCHAR(100) NULL;

-- 2. Cập nhật brand cho sản phẩm hiện có
UPDATE products SET brand = N'Yonex' WHERE name LIKE N'%Yonex%';
UPDATE products SET brand = N'Victor' WHERE name LIKE N'%Victor%';
UPDATE products SET brand = N'Li-Ning' WHERE name LIKE N'%Li-Ning%' OR name LIKE N'%Lining%';
UPDATE products SET brand = N'Mizuno' WHERE name LIKE N'%Mizuno%';
UPDATE products SET brand = N'Other' WHERE brand IS NULL;
```

```java
// 3. Thêm field vào Product.java
@Column(columnDefinition = "NVARCHAR(100)")
private String brand;
```

```java
// 4. Thêm vào ProductRepository.java
@Query("SELECT DISTINCT p.brand FROM Product p WHERE p.isActive = true AND p.isSelling = true ORDER BY p.brand")
List<String> findAllDistinctBrands();
```

```java
// 5. Cập nhật ProductService.filterProducts()
if (brand != null && !brand.isEmpty()) {
    products = products.stream()
        .filter(p -> brand.equalsIgnoreCase(p.getBrand()))
        .collect(Collectors.toList());
}
```

```java
// 6. Thêm vào HomeController
List<String> allBrands = productService.getAllBrands();
model.addAttribute("allBrands", allBrands);
```

```html
<!-- 7. Cập nhật products.html -->
<div class="form-check mb-2" th:each="brandItem : ${allBrands}">
    <a th:href="@{/products(..., brand=${brandItem})}" ...>
        <span th:text="${brandItem}">Brand</span>
    </a>
</div>
```

---

✨ **Chúc bạn thành công!**
