// Checkout page functionality
// Checkout page functionality with Discount Support - FIXED VERSION

// Global variables for discount calculation
let selectedVoucher = null;
let selectedPromotions = {}; // { storeId: promotionData }
let originalTotal = 0;
let storeSubtotals = {}; // { storeId: amount }
let shippingFee = 0; // Global shipping fee

async function requestLocationPermission() {
     if (!navigator.permissions) {
          console.warn("Trình duyệt không hỗ trợ navigator.permissions");
          useGeolocation();
          return;
     }

     try {
          const result = await navigator.permissions.query({ name: "geolocation" });

          if (result.state === "granted") {
               console.log("Quyền geolocation: granted ✔️");
               useGeolocation();
          } else if (result.state === "prompt") {
               console.log("Quyền geolocation: prompt ❔");
               useGeolocation();
          } else if (result.state === "denied") {
               console.log("Quyền geolocation: denied ❌");
               showToast(
                    "Bạn đã từ chối quyền định vị! Vui lòng bật trong cài đặt trình duyệt.",
                    "warning"
               );
          }

          result.onchange = () => {
               console.log("Trạng thái quyền thay đổi:", result.state);
          };
     } catch (error) {
          console.error("Không kiểm tra được quyền:", error);
          useGeolocation();
     }
}

function useGeolocation() {
     navigator.geolocation.getCurrentPosition(
          (pos) => {
               const lat = pos.coords.latitude;
               const lng = pos.coords.longitude;
               console.log("LẤY ĐƯỢC VỊ TRÍ:", lat, lng);
          },
          (err) => {
               console.warn("Không lấy được vị trí:", err);

               if (err.code === err.PERMISSION_DENIED) {
                    showToast(
                         "Bạn đã từ chối quyền định vị. Vui lòng cho phép truy cập vị trí!",
                         "error"
                    );
               }
          },
          {
               enableHighAccuracy: true,
               timeout: 15000,
               maximumAge: 0,
          }
     );
}

document.getElementById("useCurrentLocationCheckout").addEventListener("click", function () {
     requestLocationPermission();
});

// Initialize on page load
document.addEventListener("DOMContentLoaded", function () {
     // Auto-fill default address
     const savedAddressSelect = document.getElementById("savedAddress");
     if (savedAddressSelect && savedAddressSelect.value) {
          fillAddress();
     }

     // Get original total from data attribute or text
     const subtotalElement = document.getElementById("subtotalAmount");
     if (subtotalElement) {
          let text = subtotalElement.textContent || subtotalElement.innerText;
          console.log("Raw text:", text);

          // Bỏ tất cả ký tự không phải số
          text = text.replace(/[^\d]/g, "");
          console.log("After cleanup:", text);

          originalTotal = parseInt(text, 10);

          if (isNaN(originalTotal)) originalTotal = 0;

          console.log("Original Total parsed:", originalTotal);
     }

     // Get store subtotals from promotion selects
     document.querySelectorAll(".promotion-select").forEach((select) => {
          const storeId = select.dataset.store;
          const total = parseFloat(select.dataset.total) || 0;
          storeSubtotals[storeId] = total;
     });

     console.log("Initialized - Original Total:", originalTotal);
     console.log("Store Subtotals:", storeSubtotals);
}); /* ===================== FILL SAVED ADDRESS ===================== */

function fillAddress() {
     const select = document.getElementById("savedAddress");
     const selectedOption = select.options[select.selectedIndex];

     if (selectedOption.value) {
          document.getElementById("fullName").value =
               selectedOption.getAttribute("data-fullname") || "";
          document.getElementById("phone").value = selectedOption.getAttribute("data-phone") || "";

          // Build full address from saved data
          const address = selectedOption.getAttribute("data-address") || "";
          const ward = selectedOption.getAttribute("data-ward") || "";
          const district = selectedOption.getAttribute("data-district") || "";
          const province = selectedOption.getAttribute("data-province") || "";

          // Combine all address parts
          const fullAddress = [address, ward, district, province].filter((part) => part).join(", ");
          document.getElementById("address").value = fullAddress;
     }
}

// ============================================
// VOUCHER FUNCTIONS
// ============================================

function selectVoucher(button) {
     const code = button.dataset.code;
     const name = button.dataset.name;
     const type = button.dataset.type;
     const value = parseFloat(button.dataset.value);
     const max = parseFloat(button.dataset.max) || 0;
     const min = parseFloat(button.dataset.min) || 0;

     // Check minimum order
     if (originalTotal < min) {
          showToast(`Đơn hàng tối thiểu ${formatCurrency(min)}`, "warning");
          return;
     }

     selectedVoucher = { code, name, type, value, max, min };

     // Update UI - Show voucher info
     document.getElementById("voucherCode").value = code;
     document.getElementById("voucherNameDisplay").textContent = name;
     document.getElementById("voucherDescDisplay").textContent = `Mã: ${code}`;
     document.getElementById("selectedVoucherInfo").style.display = "block";

     // Close modal
     const modalElement = document.getElementById("voucherModal");
     const modal = bootstrap.Modal.getInstance(modalElement);
     if (modal) {
          modal.hide();
     } else {
          const newModal = new bootstrap.Modal(modalElement);
          newModal.hide();
     }

     // Recalculate total
     calculateTotal();

     showToast("Đã áp dụng voucher " + code, "success");
}

function removeVoucher() {
     selectedVoucher = null;
     document.getElementById("voucherCode").value = "";
     document.getElementById("selectedVoucherInfo").style.display = "none";

     // Recalculate to remove voucher discount
     calculateTotal();

     showToast("Đã bỏ chọn voucher", "info");
}

// ============================================
// PROMOTION FUNCTIONS
// ============================================

function updatePromotionDiscount(selectElement) {
     const storeId = selectElement.dataset.store;
     const selectedOption = selectElement.options[selectElement.selectedIndex];

     if (!selectedOption.value) {
          // Remove promotion - RESET TO ORIGINAL
          delete selectedPromotions[storeId];

          // Hide promotion info below select
          const promoInfo = document.getElementById(`promoInfo-${storeId}`);
          if (promoInfo) {
               promoInfo.style.display = "none";
          }

          // Reset promotion discount display to 0
          const promoDiscountElement = document.getElementById(`promoDiscount-${storeId}`);
          if (promoDiscountElement) {
               promoDiscountElement.textContent = "0";
          }

          // Hide store promotion discount row in summary
          const storePromoRow = document.getElementById(`storePromoRow-${storeId}`);
          const storePromoDiscount = document.getElementById(`storePromoDiscount-${storeId}`);
          if (storePromoRow) {
               storePromoRow.style.removeProperty("display");
               storePromoRow.style.display = "none";
          }
          if (storePromoDiscount) {
               storePromoDiscount.textContent = "-0₫";
          }

          // Reset store total to original (no discount)
          const storeTotal = storeSubtotals[storeId] || 0;
          const storeTotalElement = document.getElementById(`storeTotal-${storeId}`);
          if (storeTotalElement) {
               storeTotalElement.textContent = formatCurrency(storeTotal);
          }

          showToast("Đã bỏ chọn khuyến mãi", "info");
     } else {
          // Add promotion
          const promotionData = {
               id: selectedOption.value,
               name: selectedOption.dataset.name,
               type: selectedOption.dataset.type,
               value: parseFloat(selectedOption.dataset.value),
               max: parseFloat(selectedOption.dataset.max) || 0,
               min: parseFloat(selectedOption.dataset.min) || 0,
          };

          const storeTotal = storeSubtotals[storeId] || 0;

          // Check minimum order for this store
          if (storeTotal < promotionData.min) {
               showToast(
                    `Shop này cần đơn tối thiểu ${formatCurrency(promotionData.min)}`,
                    "warning"
               );
               selectElement.value = "";
               return;
          }

          selectedPromotions[storeId] = promotionData;

          // Calculate and show discount for this store
          const discount = calculatePromotionDiscount(promotionData, storeTotal);

          // Update promotion info below select
          const promoDiscountElement = document.getElementById(`promoDiscount-${storeId}`);
          if (promoDiscountElement) {
               promoDiscountElement.textContent = formatCurrency(discount);
          }

          const promoInfo = document.getElementById(`promoInfo-${storeId}`);
          if (promoInfo) {
               promoInfo.style.display = "block";
          }

          // Show store promotion discount row in summary
          const storePromoRow = document.getElementById(`storePromoRow-${storeId}`);
          const storePromoDiscount = document.getElementById(`storePromoDiscount-${storeId}`);
          if (storePromoRow && storePromoDiscount) {
               storePromoDiscount.textContent = "-" + formatCurrency(discount);
               storePromoRow.style.removeProperty("display");
               storePromoRow.style.display = "flex";
          }

          // Update store total after promotion
          const storeTotalElement = document.getElementById(`storeTotal-${storeId}`);
          if (storeTotalElement) {
               storeTotalElement.textContent = formatCurrency(storeTotal - discount);
          }

          showToast(`Đã áp dụng: ${promotionData.name}`, "success");
     }

     // Always recalculate total
     calculateTotal();
}

function calculatePromotionDiscount(promotion, amount) {
     let discount = 0;

     if (promotion.type === "PERCENTAGE") {
          discount = (amount * promotion.value) / 100;
          if (promotion.max && promotion.max > 0 && discount > promotion.max) {
               discount = promotion.max;
          }
     } else if (promotion.type === "FIXED_AMOUNT") {
          discount = promotion.value;
     }

     if (discount > amount) discount = amount;
     return Math.round(discount);
}

function calculateVoucherDiscount(voucher, amount) {
     let discount = 0;

     // LOGIC MỚI: CHỈ HỖ TRỢ PERCENTAGE
     if (voucher.type === "PERCENTAGE") {
          discount = (amount * voucher.value) / 100;
          // Không có max discount nữa - voucher áp dụng % cho từng shop
     } else {
          // Không hỗ trợ FIXED nữa
          console.warn("Voucher chỉ hỗ trợ giảm theo phần trăm");
          return 0;
     }

     if (discount > amount) discount = amount;
     return Math.round(discount);
}

// ============================================
// TOTAL CALCULATION - REAL TIME UPDATE
// ============================================

function calculateTotal() {
     let totalPromotionDiscount = 0;
     let totalVoucherDiscount = 0;

     // Object to store each store's amount after promotion
     const storeAmountsAfterPromotion = {};

     // Calculate promotion discounts for each store
     Object.keys(selectedPromotions).forEach((storeId) => {
          const promotion = selectedPromotions[storeId];
          const storeTotal = storeSubtotals[storeId] || 0;
          const discount = calculatePromotionDiscount(promotion, storeTotal);
          totalPromotionDiscount += discount;

          // Store amount after promotion for this store
          storeAmountsAfterPromotion[storeId] = storeTotal - discount;
     });

     // For stores without promotion, use original amount
     Object.keys(storeSubtotals).forEach((storeId) => {
          if (!storeAmountsAfterPromotion[storeId]) {
               storeAmountsAfterPromotion[storeId] = storeSubtotals[storeId] || 0;
          }
     });

     // Amount after promotion discount (all stores)
     let afterPromotion = originalTotal - totalPromotionDiscount;

     // === LOGIC MỚI: ÁP DỤNG VOUCHER % CHO TỪNG SHOP VỚI MAXDISCOUNT ===
     if (selectedVoucher) {
          // Bước 1: Tính % voucher cho TỪNG SHOP
          const voucherDiscountsByStore = {};
          let totalVoucherBeforeMax = 0;

          Object.keys(storeAmountsAfterPromotion).forEach((storeId) => {
               const storeAmount = storeAmountsAfterPromotion[storeId];
               const storeVoucherDiscount = calculateVoucherDiscount(selectedVoucher, storeAmount);
               voucherDiscountsByStore[storeId] = storeVoucherDiscount;
               totalVoucherBeforeMax += storeVoucherDiscount;
          });

          // Bước 2: Kiểm tra maxDiscount
          const maxDiscount = selectedVoucher.max ? parseFloat(selectedVoucher.max) : 0;

          if (maxDiscount > 0 && totalVoucherBeforeMax > maxDiscount) {
               // Tổng voucher vượt quá max, cần scale down theo tỷ lệ
               const scaleFactor = maxDiscount / totalVoucherBeforeMax;

               Object.keys(voucherDiscountsByStore).forEach((storeId) => {
                    voucherDiscountsByStore[storeId] = Math.round(
                         voucherDiscountsByStore[storeId] * scaleFactor
                    );
               });

               totalVoucherDiscount = maxDiscount;
          } else {
               // Không vượt quá max, dùng tổng ban đầu
               totalVoucherDiscount = totalVoucherBeforeMax;
          }
     }

     // Final amount
     const selectedProvider = document.querySelector('input[name="shippingProvider"]:checked');
     shippingFee = selectedProvider ? parseFloat(selectedProvider.dataset.fee) : 0;

     const shipEl = document.getElementById("shippingFeeDisplay");
     if (shipEl) {
          if (selectedProvider) {
               shipEl.textContent = formatCurrency(shippingFee);
               shipEl.classList.remove("text-muted");
          } else {
               shipEl.textContent = "Chọn đơn vị vận chuyển";
               shipEl.classList.add("text-muted");
          }
     }

     // Tổng cuối cùng - ĐẢM BẢO CỘNG SỐ KHÔNG PHẢI STRING
     const finalAmount = Math.round(afterPromotion - totalVoucherDiscount + shippingFee);

     // Update UI - Total promotion row
     const totalPromotionRow = document.getElementById("totalPromotionDiscountRow");
     if (totalPromotionRow) {
          if (totalPromotionDiscount > 0) {
               const totalPromotionAmount = document.getElementById("totalPromotionDiscountAmount");
               if (totalPromotionAmount) {
                    totalPromotionAmount.textContent = "-" + formatCurrency(totalPromotionDiscount);
               }
               totalPromotionRow.style.removeProperty("display");
               totalPromotionRow.style.display = "flex";
          } else {
               totalPromotionRow.style.removeProperty("display");
               totalPromotionRow.style.display = "none";
          }
     }

     // Update UI - Voucher row
     const voucherRow = document.getElementById("voucherDiscountRow");
     if (voucherRow) {
          if (totalVoucherDiscount > 0) {
               const voucherAmount = document.getElementById("voucherDiscountAmount");
               if (voucherAmount) {
                    voucherAmount.textContent = "-" + formatCurrency(totalVoucherDiscount);
               }
               voucherRow.style.removeProperty("display");
               voucherRow.style.display = "flex";
          } else {
               voucherRow.style.removeProperty("display");
               voucherRow.style.display = "none";
          }
     }

     // Update final amount - IMPORTANT: Always update this!
     const finalAmountElement = document.getElementById("finalAmount");
     if (finalAmountElement) {
          finalAmountElement.textContent = formatCurrency(finalAmount);
     }

     console.log("=== CALCULATION RESULT ===");
     console.log("Original Total:", originalTotal);
     console.log("Promotion Discount:", totalPromotionDiscount);
     console.log("After Promotion:", afterPromotion);
     console.log("Voucher Discount:", totalVoucherDiscount);
     console.log("Shipping Fee:", shippingFee);
     console.log("Final Amount:", finalAmount);
     console.log("========================");
}

// ============================================
// PLACE ORDER
// ============================================

function placeOrder(event) {
     if (event) event.preventDefault();
     const form = document.getElementById("checkoutForm");

     if (!form.checkValidity()) {
          form.reportValidity();
          return;
     }

     // Validate shipping provider selection
     const selectedProvider = document.querySelector('input[name="shippingProvider"]:checked');
     if (!selectedProvider) {
          showToast("Vui lòng chọn đơn vị vận chuyển", "error");
          return;
     }

     // Build promotions by store
     const promotionsByStore = {};
     Object.keys(selectedPromotions).forEach((storeId) => {
          promotionsByStore[storeId] = selectedPromotions[storeId].id;
     });

     // Build sending data
     const data = {
          fullName: document.getElementById("fullName").value.trim(),
          phone: document.getElementById("phone").value.trim(),
          address: document.getElementById("address").value.trim(),
          latitude: document.getElementById("latitude").value,
          longitude: document.getElementById("longitude").value,
          note: document.getElementById("note").value.trim(),
          paymentMethod: document.querySelector('input[name="paymentMethod"]:checked')?.value,
          shippingProviderId: selectedProvider.value,
          voucherCode: selectedVoucher ? selectedVoucher.code : null,
          promotionsByStore,
     };

     // Validate map pick
     if (!data.latitude || !data.longitude)
          return showToast("Vui lòng chọn vị trí giao hàng trên bản đồ!", "warning");

     if (!data.paymentMethod) return showToast("Vui lòng chọn phương thức thanh toán!", "warning");

     const headers = {
          "Content-Type": "application/json",
     };
     headers[pageData.csrfHeader] = pageData.csrfToken;

     const btn = event.target;
     const originalText = btn.innerHTML;
     btn.disabled = true;
     btn.innerHTML = `<i class="fas fa-spinner fa-spin me-2"></i>Đang xử lý...`;

     fetch("/checkout/place-order", {
          method: "POST",
          headers,
          body: JSON.stringify(data),
     })
          .then((res) => res.json())
          .then((data) => {
               if (!data.success) {
                    showToast(data.message || "Lỗi đặt hàng!", "error");
                    return;
               }

               if (data.paymentMethod === "VNPAY") {
                    window.location.href = data.paymentUrl;
                    return;
               }

               if (data.paymentMethod === "BANK_QR") {
                    window.currentOrderId = data.orderId;
                    document.getElementById("qrImage").src = data.qrImage;
                    document.getElementById("qrAmount").innerText = new Intl.NumberFormat(
                         "vi-VN"
                    ).format(data.amount);
                    document.getElementById("qrDesc").innerText = data.description;
                    new bootstrap.Modal(document.getElementById("bankQrModal")).show();
                    return;
               }

               window.location.href = "/checkout/success?orderId=" + data.orderId;
          })
          .catch(() => showToast("Không thể kết nối máy chủ!", "error"))
          .finally(() => {
               btn.disabled = false;
               btn.innerHTML = originalText;
          });
}

// ============================================
// UTILITY FUNCTIONS
// ============================================

function formatCurrency(amount) {
     const num = parseFloat(amount) || 0;
     return (
          num.toLocaleString("vi-VN", {
               minimumFractionDigits: 0,
               maximumFractionDigits: 0,
          }) + "đ"
     );
}

/* ===================== SHOW TOAST ===================== */
function showToast(message, type = "info") {
     const toast = document.getElementById("notificationToast");
     const toastMessage = document.getElementById("toastMessage");

     toastMessage.textContent = message;
     toast.classList.remove("bg-success", "bg-danger", "bg-warning", "bg-info");

     toast.classList.add(
          type === "success"
               ? "bg-success"
               : type === "error"
               ? "bg-danger"
               : type === "warning"
               ? "bg-warning"
               : "bg-info"
     );

     new bootstrap.Toast(toast, { delay: 3000 }).show();
}

/* ✅ Xác nhận đã chuyển khoản */
function confirmQR() {
     window.location.href = `/checkout/success?orderId=${window.currentOrderId}`;
}

/* ===================== MAP PICKER ===================== */
let map = L.map("map").setView([10.762622, 106.660172], 13);

L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
     maxZoom: 19,
}).addTo(map);

let marker = null;

map.on("click", function (e) {
     const lat = e.latlng.lat;
     const lng = e.latlng.lng;

     if (marker) map.removeLayer(marker);
     marker = L.marker([lat, lng]).addTo(map);

     document.getElementById("latitude").value = lat;
     document.getElementById("longitude").value = lng;

     fetch(
          `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&accept-language=vi`
     )
          .then((res) => res.json())
          .then(
               (data) =>
                    (document.getElementById("address").value =
                         data.display_name || "Không xác định")
          );
});

/* ===================== ADDRESS AUTOCOMPLETE ===================== */
const searchInput = document.getElementById("addressSearch");
const suggestionBox = document.getElementById("addressSuggestions");
let searchTimeout;

if (searchInput) {
     searchInput.addEventListener("input", () => {
          const query = searchInput.value.trim();

          if (query.length < 3) {
               suggestionBox.style.display = "none";
               suggestionBox.innerHTML = "";
               return;
          }

          clearTimeout(searchTimeout);

          searchTimeout = setTimeout(async () => {
               try {
                    fetch(
                         `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(
                              query + " Vietnam"
                         )}&format=json&addressdetails=1&limit=5&accept-language=vi`,
                         {
                              headers: {
                                   "User-Agent": "BadmintonMarketplace/1.0",
                              },
                         }
                    )
                         .then((res) => res.json())
                         .then((data) => {
                              suggestionBox.innerHTML = "";
                              suggestionBox.style.display = "block";

                              data.forEach((item) => {
                                   const div = document.createElement("div");
                                   div.className = "suggestion-item";
                                   div.innerHTML = `
                    <i class="fas fa-map-marker-alt me-2"></i> ${item.display_name}
               `;

                                   div.onclick = () => {
                                        const lat = parseFloat(item.lat);
                                        const lon = parseFloat(item.lon);

                                        // fill input
                                        document.getElementById("address").value =
                                             item.display_name;
                                        document.getElementById("latitude").value = lat;
                                        document.getElementById("longitude").value = lon;

                                        // di chuyển map
                                        map.flyTo([lat, lon], 17, { duration: 1.2 });

                                        // đặt marker
                                        if (marker) map.removeLayer(marker);
                                        marker = L.marker([lat, lon]).addTo(map);

                                        suggestionBox.style.display = "none";
                                   };

                                   suggestionBox.appendChild(div);
                              });
                         });
               } catch (error) {
                    console.error("Error fetching addresses:", error);
                    suggestionBox.innerHTML =
                         '<div class="suggestion-item error"><i class="fas fa-exclamation-triangle me-2"></i>Lỗi tìm kiếm, vui lòng thử lại</div>';
                    suggestionBox.style.display = "block";
               }
          }, 400); // debounce 400ms
     });

     // Close suggestions when clicking outside
     document.addEventListener("mousedown", (e) => {
          if (!suggestionBox.contains(e.target) && e.target !== searchInput) {
               suggestionBox.style.display = "none";
          }
     });
}

// ===================== CURRENT LOCATION (checkout) =====================
const currentLocationBtn = document.getElementById("useCurrentLocationCheckout");
const locationSpinner = document.getElementById("locationSpinnerCheckout");

async function setAddressCheckout(lat, lng) {
     const res = await fetch(
          `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&accept-language=vi`
     );
     const data = await res.json();
     const display = data.display_name || "";

     document.getElementById("address").value = display;
}

if (currentLocationBtn) {
     currentLocationBtn.addEventListener("click", () => {
          if (!navigator.geolocation) {
               alert("Trình duyệt không hỗ trợ GPS!");
               return;
          }

          // loading UI
          currentLocationBtn.disabled = true;
          locationSpinner.classList.remove("d-none");
          currentLocationBtn.querySelector("span").textContent = "Đang xác định vị trí...";

          navigator.geolocation.getCurrentPosition(
               (pos) => {
                    const lat = pos.coords.latitude;
                    const lng = pos.coords.longitude;

                    // set marker
                    if (marker) map.removeLayer(marker);
                    marker = L.marker([lat, lng]).addTo(map);

                    // move map
                    map.flyTo([lat, lng], 17, {
                         duration: 1.2,
                    });

                    // update hidden inputs
                    document.getElementById("latitude").value = lat;
                    document.getElementById("longitude").value = lng;

                    // reverse geocode
                    setAddressCheckout(lat, lng);

                    // reset UI
                    currentLocationBtn.disabled = false;
                    locationSpinner.classList.add("d-none");
                    currentLocationBtn.querySelector("span").textContent =
                         "Sử dụng vị trí hiện tại của tôi";
               },
               (error) => {
                    alert("Không thể lấy vị trí GPS!");

                    currentLocationBtn.disabled = false;
                    locationSpinner.classList.add("d-none");
                    currentLocationBtn.querySelector("span").textContent =
                         "Sử dụng vị trí hiện tại của tôi";
               },
               {
                    enableHighAccuracy: true,
                    timeout: 10000,
                    maximumAge: 0,
               }
          );
     });
}

/* ===================== AUTO FILL SAVED ADDRESS ===================== */
document.addEventListener("DOMContentLoaded", () => {
     const savedAddressSelect = document.getElementById("savedAddress");
     if (savedAddressSelect && savedAddressSelect.value) fillAddress();
});

// function submitCheckout() {
//      const payload = {};

//      // Collect shipping provider ID
//      const shippingProviderSelect = document.getElementById("shippingProviderSelect");
//      if (shippingProviderSelect) {
//           payload.shippingProviderId = shippingProviderSelect.value;
//      }

//      // Collect other checkout data
//      payload.fullName = document.getElementById("fullName").value;
//      payload.phone = document.getElementById("phone").value;
//      payload.address = document.getElementById("address").value;
//      payload.voucherCode = document.getElementById("voucherCode").value;

//      // Send the payload to the backend
//      fetch("/api/checkout", {
//           method: "POST",
//           headers: {
//                "Content-Type": "application/json",
//           },
//           body: JSON.stringify(payload),
//      })
//           .then((response) => {
//                if (response.ok) {
//                     return response.json();
//                } else {
//                     throw new Error("Checkout failed");
//                }
//           })
//           .then((data) => {
//                console.log("Checkout successful", data);
//                window.location.href = "/checkout/success";
//           })
//           .catch((error) => {
//                console.error("Error during checkout:", error);
//                alert("Checkout failed. Please try again.");
//           });
// }

// // Attach event listener to the checkout button
// document.getElementById("checkoutButton").addEventListener("click", submitCheckout);
// Note: Button already has onclick="placeOrder(event)" in HTML, no need to attach here
