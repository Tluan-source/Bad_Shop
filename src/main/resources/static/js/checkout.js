// Checkout page functionality

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
          const fullAddress = [address, ward, district, province].filter(part => part).join(", ");
          document.getElementById("address").value = fullAddress;
     }
}

function placeOrder(event) {
     if (event) event.preventDefault();
     const form = document.getElementById("checkoutForm");

     // Validate form
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

     // Get form data
     const formData = {
          fullName: document.getElementById("fullName").value,
          phone: document.getElementById("phone").value,
          address: document.getElementById("address").value,
          province: "", // Not needed anymore with simplified address
          district: "",
          ward: "",
          note: document.getElementById("note").value,
          paymentMethod: document.querySelector('input[name="paymentMethod"]:checked').value,
          shippingProviderId: selectedProvider.value,
     };

     // Setup headers
     const headers = {
          "Content-Type": "application/json",
     };
     headers[pageData.csrfHeader] = pageData.csrfToken;

     // Show loading
     const btn = event ? event.target : document.querySelector('button[onclick*="placeOrder"]');
     const originalText = btn.innerHTML;
     btn.disabled = true;
     btn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang xử lý...';

     // Send request
     fetch("/checkout/place-order", {
          method: "POST",
          headers: headers,
          body: JSON.stringify(formData),
     })
          .then((res) => res.json())
          .then((data) => {
               if (data.success) {
                    // Check if VNPay payment
                    if (data.paymentMethod === "VNPAY" && data.paymentUrl) {
                         // Redirect to VNPay
                         window.location.href = data.paymentUrl;
                    } else {
                         // Redirect to success page for COD or other methods
                         window.location.href = "/checkout/success?orderId=" + data.orderId;
                    }
               } else {
                    showToast(data.message || "Có lỗi xảy ra", "error");
                    btn.disabled = false;
                    btn.innerHTML = originalText;
               }
          })
          .catch((err) => {
               console.error("Place order error:", err);
               showToast("Không thể đặt hàng. Vui lòng thử lại.", "error");
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