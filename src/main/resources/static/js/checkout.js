// Checkout page functionality

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

function placeOrder(event) {
     if (event) event.preventDefault();
     const form = document.getElementById("checkoutForm");

     // Validate cơ bản
     if (!form.checkValidity()) {
          form.reportValidity();
          return;
     }

     // Lấy thông tin
     const fullName = document.getElementById("fullName").value.trim();
     const phone = document.getElementById("phone").value.trim();
     const address = document.getElementById("address").value.trim();
     const latitude = document.getElementById("latitude").value;
     const longitude = document.getElementById("longitude").value;
     const note = document.getElementById("note").value.trim();
     const payment = document.querySelector('input[name="paymentMethod"]:checked')?.value;

     // Kiểm tra chọn vị trí map
     if (!latitude || !longitude) {
          showToast("Vui lòng chọn vị trí giao hàng trên bản đồ!", "warning");
          return;
     }

     if (!payment) {
          showToast("Vui lòng chọn phương thức thanh toán!", "warning");
          return;
     }

     const formData = {
          fullName,
          phone,
          address,
          latitude,
          longitude,
          note,
          paymentMethod: payment,
     };

     // Setup headers
     const headers = { "Content-Type": "application/json" };
     headers[pageData.csrfHeader] = pageData.csrfToken;

     // Disable nút
     const btn = event ? event.target : document.querySelector('button[onclick*="placeOrder"]');
     const originalText = btn.innerHTML;
     btn.disabled = true;
     btn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang xử lý...';

     // Gửi request
     fetch("/checkout/place-order", {
          method: "POST",
          headers: headers,
          body: JSON.stringify(formData),
     })
          .then(async (res) => {
               const data = await res.json().catch(() => ({}));
               if (res.ok && data.success) {
                    if (data.paymentMethod === "VNPAY" && data.paymentUrl) {
                         window.location.href = data.paymentUrl;
                    } else {
                         window.location.href = "/checkout/success?orderId=" + data.orderId;
                    }
               } else {
                    showToast(data.message || "Có lỗi xảy ra khi đặt hàng!", "error");
                    btn.disabled = false;
                    btn.innerHTML = originalText;
               }
          })
          .catch((err) => {
               console.error("Place order error:", err);
               showToast("Không thể kết nối máy chủ!", "error");
               btn.disabled = false;
               btn.innerHTML = originalText;
          });
}

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

// Auto-fill default address on page load
document.addEventListener("DOMContentLoaded", function () {
     const savedAddressSelect = document.getElementById("savedAddress");
     if (savedAddressSelect && savedAddressSelect.value) {
          fillAddress();
     }
});
