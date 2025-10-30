/* ===================== FILL SAVED ADDRESS ===================== */
function fillAddress() {
     const select = document.getElementById("savedAddress");
     const selectedOption = select.options[select.selectedIndex];

     if (selectedOption.value) {
          document.getElementById("fullName").value =
               selectedOption.getAttribute("data-fullname") || "";
          document.getElementById("phone").value = selectedOption.getAttribute("data-phone") || "";
          document.getElementById("address").value =
               selectedOption.getAttribute("data-address") || "";
          document.getElementById("province").value =
               selectedOption.getAttribute("data-province") || "";
          document.getElementById("district").value =
               selectedOption.getAttribute("data-district") || "";
          document.getElementById("ward").value = selectedOption.getAttribute("data-ward") || "";
     }
}

/* ===================== PLACE ORDER ===================== */
function placeOrder(event) {
     if (event) event.preventDefault();
     const form = document.getElementById("checkoutForm");

     if (!form.checkValidity()) {
          form.reportValidity();
          return;
     }

     const data = {
          fullName: document.getElementById("fullName").value.trim(),
          phone: document.getElementById("phone").value.trim(),
          address: document.getElementById("address").value.trim(),
          latitude: document.getElementById("latitude").value,
          longitude: document.getElementById("longitude").value,
          note: document.getElementById("note").value.trim(),
          paymentMethod: document.querySelector('input[name="paymentMethod"]:checked')?.value,
     };

     if (!data.latitude || !data.longitude)
          return showToast("Vui lòng chọn vị trí giao hàng trên bản đồ!", "warning");

     if (!data.paymentMethod) return showToast("Vui lòng chọn phương thức thanh toán!", "warning");

     const headers = { "Content-Type": "application/json" };
     headers[pageData.csrfHeader] = pageData.csrfToken;

     const btn = event.target;
     const originalText = btn.innerHTML;
     btn.disabled = true;
     btn.innerHTML = `<i class="fas fa-spinner fa-spin me-2"></i>Đang xử lý...`;

     fetch("/checkout/place-order", {
          method: "POST",
          headers: headers,
          body: JSON.stringify(data),
     })
          .then((res) => res.json())
          .then((data) => {
               if (!data.success) {
                    showToast(data.message || "Lỗi đặt hàng!", "error");
                    btn.disabled = false;
                    btn.innerHTML = originalText;
                    return;
               }

               // ✅ VNPAY
               if (data.paymentMethod === "VNPAY") {
                    window.location.href = data.paymentUrl;
                    return;
               }

               // ✅ BANK QR
               if (data.paymentMethod === "BANK_QR") {
                    window.currentOrderId = data.orderId;
                    document.getElementById("qrImage").src = data.qrImage;
                    document.getElementById("qrAmount").innerText = new Intl.NumberFormat(
                         "vi-VN"
                    ).format(data.amount);
                    document.getElementById("qrDesc").innerText = data.description;

                    new bootstrap.Modal(document.getElementById("bankQrModal")).show();

                    btn.disabled = false;
                    btn.innerHTML = originalText;
                    return;
               }

               // ✅ COD
               window.location.href = "/checkout/success?orderId=" + data.orderId;
          })
          .catch(() => showToast("Không thể kết nối máy chủ!", "error"))
          .finally(() => {
               btn.disabled = false;
               btn.innerHTML = originalText;
          });
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

/* ===================== AUTO FILL SAVED ADDRESS ===================== */
document.addEventListener("DOMContentLoaded", () => {
     const savedAddressSelect = document.getElementById("savedAddress");
     if (savedAddressSelect && savedAddressSelect.value) fillAddress();
});
