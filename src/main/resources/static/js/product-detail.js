document.addEventListener("DOMContentLoaded", function () {
     initializeImages();
     setupStyleButtons();
});

/* ==================== IMAGE GALLERY ==================== */

let images = [];
let currentImageIndex = 0;
let selectedStyles = {};

function initializeImages() {
     try {
          if (
               productData.listImages &&
               productData.listImages.trim() !== "" &&
               productData.listImages !== "[]"
          ) {
               images = JSON.parse(productData.listImages);
          } else {
               const mainImg = document.getElementById("mainImage");
               if (mainImg && mainImg.src) images = [mainImg.src];
          }

          if (images.length > 0) {
               displayThumbnails();
               updateNavigationButtons();
          }
     } catch (error) {
          console.error("Error parsing images:", error);
     }
}

function displayThumbnails() {
     const container = document.getElementById("thumbnailsContainer");
     if (!container) return;
     container.innerHTML = "";

     images.forEach((url, index) => {
          const div = document.createElement("div");
          div.style.cssText = `
            flex-shrink: 0;
            width: 80px;
            height: 80px;
            border: 2px solid ${index === 0 ? "#0d6efd" : "#e8e8e8"};
            cursor: pointer;
            overflow: hidden;
            transition: all 0.3s;
        `;
          div.onclick = () => showImage(index);

          const img = document.createElement("img");
          img.src = url;
          img.alt = productData.name;
          img.style.cssText = "width: 100%; height: 100%; object-fit: cover;";

          div.appendChild(img);
          container.appendChild(div);
     });
}

function showImage(index, direction = 0) {
     if (index < 0 || index >= images.length) return;

     const mainImage = document.getElementById("mainImage");
     const oldIndex = currentImageIndex;
     currentImageIndex = index;

     // Chỉ apply animation khi có direction (click prev/next)
     if (direction !== 0) {
          // Xác định hướng animation
          const animationClass = direction > 0 ? "slide-from-right" : "slide-from-left";

          // Xóa tất cả animation classes
          mainImage.classList.remove("slide-from-right", "slide-from-left");

          // Force reflow để animation hoạt động
          void mainImage.offsetWidth;

          // Đổi ảnh và thêm animation
          mainImage.src = images[index];
          mainImage.classList.add(animationClass);

          // Xóa class sau khi animation xong
          setTimeout(() => {
               mainImage.classList.remove(animationClass);
          }, 400);
     } else {
          // Click thumbnail - chỉ đổi ảnh không có animation
          mainImage.src = images[index];
     }

     // Cập nhật thumbnails
     const thumbs = document.getElementById("thumbnailsContainer").children;
     for (let i = 0; i < thumbs.length; i++) {
          if (i === index) {
               thumbs[i].classList.add("active");
               thumbs[i].style.borderColor = "#0d6efd";
          } else {
               thumbs[i].classList.remove("active");
               thumbs[i].style.borderColor = "#e8e8e8";
          }
     }

     updateNavigationButtons();
}

function changeImage(direction) {
     if (images.length === 0) return;
     let newIndex = currentImageIndex + direction;
     if (newIndex < 0) newIndex = images.length - 1;
     else if (newIndex >= images.length) newIndex = 0;
     showImage(newIndex, direction);
}

function updateNavigationButtons() {
     const prevBtn = document.getElementById("prevBtn");
     const nextBtn = document.getElementById("nextBtn");
     if (images.length > 1) {
          prevBtn.style.display = "flex";
          nextBtn.style.display = "flex";
     }
}

/* ==================== STYLE SELECTION ==================== */

function setupStyleButtons() {
     document.querySelectorAll(".style-option-btn").forEach((button) => {
          button.addEventListener("mouseover", function () {
               if (!this.classList.contains("active")) {
                    this.style.borderColor = "#0d6efd";
                    this.style.color = "#0d6efd";
               }
          });
          button.addEventListener("mouseout", function () {
               if (!this.classList.contains("active")) {
                    this.style.borderColor = "#d8d8d8";
                    this.style.color = "#333";
               }
          });
     });
}

function selectStyle(button) {
     const styleName = button.dataset.styleName;
     const styleId = button.dataset.styleId;
     const styleValue = button.dataset.styleValue;

     const parent = button.parentElement;
     parent.querySelectorAll(".style-option-btn").forEach((btn) => {
          btn.classList.remove("active");
          btn.style.borderColor = "#d8d8d8";
          btn.style.color = "#333";
          btn.style.background = "#fff";
     });

     button.classList.add("active");
     button.style.borderColor = "#0d6efd";
     button.style.color = "#0d6efd";
     button.style.background = "#e7f1ff";

     selectedStyles[styleName] = { id: styleId, value: styleValue };
     hideStyleWarning();
}

function validateStyleSelection() {
     if (pageData.styleMapSize === 0) return true;
     const selectedCount = Object.keys(selectedStyles).length;
     if (selectedCount < pageData.styleMapSize) {
          showStyleWarning();
          return false;
     }
     return true;
}

function showStyleWarning() {
     const warning = document.getElementById("styleWarning");
     if (warning) {
          warning.style.display = "block";
          warning.scrollIntoView({ behavior: "smooth", block: "center" });
     }
}

function hideStyleWarning() {
     const warning = document.getElementById("styleWarning");
     if (warning) warning.style.display = "none";
}

/* ==================== QUANTITY ==================== */

function increaseQuantity() {
     const input = document.getElementById("quantity");
     const current = parseInt(input.value);
     const max = parseInt(input.max);
     if (current < max) input.value = current + 1;
     else showToast("Đã đạt số lượng tối đa", "warning");
}

function decreaseQuantity() {
     const input = document.getElementById("quantity");
     const current = parseInt(input.value);
     const min = parseInt(input.min);
     if (current > min) input.value = current - 1;
}

/* ==================== CART & BUY ==================== */

function addToCart() {
     if (!productData.isSelling) {
          showToast("Sản phẩm hiện không bán", "error");
          return;
     }
     if (!validateStyleSelection()) return;

     const quantity = parseInt(document.getElementById("quantity").value);
     const styleValueIds = Object.values(selectedStyles).map((s) => s.id);

     const headers = { "Content-Type": "application/json" };
     headers[pageData.csrfHeader] = pageData.csrfToken;

     fetch("/cart/add", {
          method: "POST",
          headers,
          body: JSON.stringify({
               productId: productData.id,
               quantity,
               styleValueIds,
          }),
     })
          .then((res) => res.json())
          .then((data) => {
               if (data.success) {
                    showToast("Đã thêm vào giỏ hàng!", "success");
                    updateCartCount(data.cartCount);
               } else {
                    showToast(data.message || "Có lỗi xảy ra", "error");
               }
          })
          .catch((err) => {
               console.error("Add to cart error:", err);
               showToast("Không thể thêm vào giỏ hàng", "error");
          });
}

function buyNow() {
     if (!productData.isSelling) {
          showToast("Sản phẩm hiện không bán", "error");
          return;
     }
     if (!validateStyleSelection()) return;

     const quantity = parseInt(document.getElementById("quantity").value);
     const styleValueIds = Object.values(selectedStyles).map((s) => s.id);

     const headers = { "Content-Type": "application/json" };
     headers[pageData.csrfHeader] = pageData.csrfToken;

     fetch("/checkout/buy-now", {
          method: "POST",
          headers,
          body: JSON.stringify({
               productId: productData.id,
               quantity,
               styleValueIds,
          }),
     })
          .then((res) => res.json())
          .then((data) => {
               if (data.success && data.redirect) {
                    window.location.href = data.redirect;
               } else if (data.error) {
                    showToast(data.error, "error");
                    if (data.redirect) {
                         setTimeout(() => {
                              window.location.href = data.redirect;
                         }, 1500);
                    }
               } else {
                    showToast("Có lỗi xảy ra", "error");
               }
          })
          .catch((err) => {
               console.error("Buy now error:", err);
               showToast("Không thể mua ngay. Vui lòng thử lại!", "error");
          });
}

/* ==================== TOAST ==================== */

function showToast(message, type = "info") {
     const toast = document.getElementById("notificationToast");
     const toastMessage = document.getElementById("toastMessage");
     if (!toast || !toastMessage) return;

     toastMessage.textContent = message;
     toast.classList.remove("bg-success", "bg-danger", "bg-warning", "bg-info");

     switch (type) {
          case "success":
               toast.classList.add("bg-success");
               break;
          case "error":
               toast.classList.add("bg-danger");
               break;
          case "warning":
               toast.classList.add("bg-warning");
               break;
          default:
               toast.classList.add("bg-info");
     }

     const bsToast = new bootstrap.Toast(toast, { delay: 3000 });
     bsToast.show();
}

function updateCartCount(count) {
     const badge = document.querySelector(".cart-badge");
     if (badge) {
          badge.textContent = count;
          badge.style.display = count > 0 ? "inline-block" : "none";
     }
}

/* ==================== FAVORITE FUNCTIONALITY ==================== */

// Check if product is in favorites on page load
document.addEventListener("DOMContentLoaded", function () {
     checkFavoriteStatus();
});

function checkFavoriteStatus() {
     fetch(`/favorites/check/${productData.id}`)
          .then((response) => response.json())
          .then((data) => {
               updateFavoriteButton(data.isFavorite);
          })
          .catch((error) => {
               console.error("Error checking favorite status:", error);
          });
}

function toggleFavorite() {
     // Check if all required styles are selected
     if (pageData.styleMapSize > 0) {
          const selectedCount = Object.keys(selectedStyles).length;
          if (selectedCount < pageData.styleMapSize) {
               showToast(
                    "Vui lòng chọn đầy đủ thuộc tính sản phẩm trước khi thêm vào yêu thích!",
                    "warning"
               );
               return;
          }
     }

     const formData = new URLSearchParams();
     formData.append("productId", productData.id);

     fetch("/favorites/toggle", {
          method: "POST",
          headers: {
               "Content-Type": "application/x-www-form-urlencoded",
               [pageData.csrfHeader]: pageData.csrfToken,
          },
          body: formData,
     })
          .then((response) => response.json())
          .then((data) => {
               if (data.success) {
                    updateFavoriteButton(data.added);
                    showToast(data.message, "success");
                    updateFavoriteCount(data.favoriteCount);
               } else {
                    showToast(data.message, "error");
               }
          })
          .catch((error) => {
               console.error("Error toggling favorite:", error);
               showToast("Có lỗi xảy ra. Vui lòng thử lại!", "error");
          });
}

function updateFavoriteButton(isFavorite) {
     const btn = document.getElementById("favoriteBtn");
     const icon = document.getElementById("favoriteIcon");
     const text = document.getElementById("favoriteText");

     if (isFavorite) {
          btn.style.background = "#dc3545";
          btn.style.color = "white";
          icon.className = "fas fa-heart me-2";
          text.textContent = "Đã thích";
     } else {
          btn.style.background = "white";
          btn.style.color = "#dc3545";
          icon.className = "far fa-heart me-2";
          text.textContent = "Yêu thích";
     }
}

function updateFavoriteCount(count) {
     const badge = document.querySelector(".favorite-badge");
     if (badge) {
          badge.textContent = count;
          badge.style.display = count > 0 ? "inline-block" : "none";
     }
}
