# 🛒 HƯỚNG DẪN: TRIỂN KHAI GIỎ HÀNG VÀ YÊU THÍCH

## 📋 Tổng quan

Hướng dẫn này sẽ giúp bạn triển khai đầy đủ tính năng:

-    ✅ Thêm/xóa sản phẩm vào giỏ hàng
-    ✅ Cập nhật số lượng trong giỏ
-    ✅ Thêm/xóa sản phẩm yêu thích
-    ✅ Hiển thị badge số lượng trên header
-    ✅ Trang giỏ hàng với checkout

---

## 🗂️ Cấu trúc Files

```
src/main/java/vn/iotstar/
├── repository/
│   ├── CartItemRepository.java      ← TẠO MỚI
│   └── FavoriteRepository.java      ← TẠO MỚI
├── service/
│   ├── CartService.java             ← TẠO MỚI
│   └── FavoriteService.java         ← TẠO MỚI
├── controller/
│   ├── CartController.java          ← TẠO MỚI
│   └── FavoriteController.java      ← TẠO MỚI
└── dto/
    └── CartItemDTO.java             ← TẠO MỚI (Optional)

src/main/resources/templates/
├── user/
│   ├── cart.html                    ← TẠO MỚI
│   ├── favorites.html               ← TẠO MỚI
│   └── product-detail.html          ← SỬA
└── fragments/
    └── header-user.html             ← SỬA (thêm cart badge)
```

---

## 📝 BƯỚC 1: Tạo Repositories

### File: `repository/CartItemRepository.java`

```java
package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.CartItem;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {

    List<CartItem> findByUser(User user);

    List<CartItem> findByUserOrderByCreatedAtDesc(User user);

    Optional<CartItem> findByUserAndProduct(User user, Product product);

    void deleteByUser(User user);

    void deleteByUserAndProduct(User user, Product product);

    int countByUser(User user);

    boolean existsByUserAndProduct(User user, Product product);
}
```

### File: `repository/FavoriteRepository.java`

```java
package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Favorite;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, String> {

    List<Favorite> findByUserOrderByCreatedAtDesc(User user);

    Optional<Favorite> findByUserAndProduct(User user, Product product);

    boolean existsByUserAndProduct(User user, Product product);

    void deleteByUserAndProduct(User user, Product product);

    int countByUser(User user);
}
```

---

## 📝 BƯỚC 2: Tạo Services

### File: `service/CartService.java`

**Chú ý quan trọng:** File này rất dài (150+ lines). Tôi sẽ tạo file riêng để bạn copy.

Xem file: **`SERVICE_CART_CODE.txt`** (tôi sẽ tạo ở bước sau)

### File: `service/FavoriteService.java`

Xem file: **`SERVICE_FAVORITE_CODE.txt`** (tôi sẽ tạo ở bước sau)

---

## 📝 BƯỚC 3: Tạo Controllers

### File: `controller/CartController.java`

```java
package vn.iotstar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.CartItem;
import vn.iotstar.service.CartService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * View cart page
     */
    @GetMapping
    public String viewCart(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        List<CartItem> cartItems = cartService.getCartItems();
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", cartService.getCartTotal());
        model.addAttribute("cartCount", cartItems.size());

        return "user/cart";
    }

    /**
     * Add to cart (AJAX)
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam String productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Authentication auth) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để thêm vào giỏ hàng");
                return ResponseEntity.ok(response);
            }

            CartItem cartItem = cartService.addToCart(productId, quantity);

            response.put("success", true);
            response.put("message", "Đã thêm vào giỏ hàng");
            response.put("cartCount", cartService.getCartCount());
            response.put("cartItem", cartItem);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Update quantity (AJAX)
     */
    @PostMapping("/update/{cartItemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @PathVariable String cartItemId,
            @RequestParam Integer quantity) {

        Map<String, Object> response = new HashMap<>();

        try {
            CartItem cartItem = cartService.updateQuantity(cartItemId, quantity);

            response.put("success", true);
            response.put("message", "Đã cập nhật số lượng");
            response.put("cartItem", cartItem);
            response.put("cartTotal", cartService.getCartTotal());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Remove from cart (AJAX)
     */
    @DeleteMapping("/remove/{cartItemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable String cartItemId) {

        Map<String, Object> response = new HashMap<>();

        try {
            cartService.removeFromCart(cartItemId);

            response.put("success", true);
            response.put("message", "Đã xóa khỏi giỏ hàng");
            response.put("cartCount", cartService.getCartCount());
            response.put("cartTotal", cartService.getCartTotal());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Clear cart
     */
    @PostMapping("/clear")
    public String clearCart() {
        cartService.clearCart();
        return "redirect:/cart";
    }
}
```

### File: `controller/FavoriteController.java`

```java
package vn.iotstar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.Favorite;
import vn.iotstar.service.FavoriteService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * View favorites page
     */
    @GetMapping
    public String viewFavorites(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        List<Favorite> favorites = favoriteService.getFavorites();
        model.addAttribute("favorites", favorites);
        model.addAttribute("favoriteCount", favorites.size());

        return "user/favorites";
    }

    /**
     * Toggle favorite (AJAX)
     */
    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @RequestParam String productId,
            Authentication auth) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return ResponseEntity.ok(response);
            }

            boolean added = favoriteService.toggleFavorite(productId);

            response.put("success", true);
            response.put("added", added);
            response.put("message", added ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích");
            response.put("favoriteCount", favoriteService.getFavoriteCount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Check if product is favorite (AJAX)
     */
    @GetMapping("/check/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkFavorite(@PathVariable String productId) {
        Map<String, Object> response = new HashMap<>();
        response.put("isFavorite", favoriteService.isFavorite(productId));
        return ResponseEntity.ok(response);
    }
}
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

Vì công cụ tạo file tự động thường gặp lỗi BOM encoding, bạn nên:

1. **Tạo file thủ công trong IntelliJ IDEA hoặc VS Code**
2. **Copy code từ hướng dẫn này**
3. **Paste vào file mới tạo**

Tôi đang tạo thêm file hướng dẫn chi tiết cho Service code và HTML...

**Tiếp tục ở phần 2!** 👇
