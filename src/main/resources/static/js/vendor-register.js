console.log(">>> vendor-register.js LOADED <<<");

document.addEventListener("DOMContentLoaded", () => {
     const mapDiv = document.getElementById("map");
     if (!mapDiv) return console.warn("Map div not found!");

     const defaultLat = 10.762622;
     const defaultLng = 106.660172;

     const latInput = document.getElementById("latitude");
     const lngInput = document.getElementById("longitude");
     const addressInput = document.getElementById("address");
     const fullAddressDisplay = document.getElementById("fullAddress");
     const cityInput = document.getElementById("city");
     const districtInput = document.getElementById("district");
     const wardInput = document.getElementById("ward");

     console.log({
          latInput,
          lngInput,
          addressInput,
          fullAddressDisplay,
     });

     let map = L.map("map").setView([defaultLat, defaultLng], 13);
     let marker;

     // FREE TILE OSM
     L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
          maxZoom: 19,
          attribution: "&copy; OpenStreetMap contributors",
     }).addTo(map);

     setTimeout(() => map.invalidateSize(), 500);

     function buildCleanAddress(display, ward, district, city) {
          if (!display) return "";

          // Tách thành từng phần theo dấu phẩy
          let parts = display
               .split(",")
               .map((p) => p.trim())
               .filter(Boolean);

          const lower = (s) => (s || "").toLowerCase();

          parts = parts.filter((p) => {
               const lp = lower(p);

               // Bỏ ward / district / city đã tách riêng
               if (ward && lp === lower(ward)) return false;
               if (district && lp === lower(district)) return false;
               if (city && lp === lower(city)) return false;

               // Bỏ mã bưu điện kiểu 72415
               if (/^\d{4,6}$/.test(p)) return false;

               // Bỏ country
               if (lp === "việt nam" || lp === "vietnam") return false;

               return true;
          });

          // Ghép lại – không còn , , , nữa
          return parts.join(", ");
     }

     async function setAddress(lat, lng) {
          const res = await fetch(
               `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&accept-language=vi`
          );
          const data = await res.json();
          const display = data.display_name || "Không xác định";

          if (fullAddressDisplay) fullAddressDisplay.value = display;

          let city = "";
          let district = "";
          let ward = "";

          if (data.address) {
               city = data.address.city || data.address.state || data.address.province || "";
               district =
                    data.address.district ||
                    data.address.town ||
                    data.address.suburb ||
                    data.address.county ||
                    "";
               ward =
                    data.address.quarter ||
                    data.address.neighbourhood ||
                    data.address.village ||
                    data.address.hamlet ||
                    "";

               if (cityInput) cityInput.value = city;
               if (districtInput) districtInput.value = district;
               if (wardInput) wardInput.value = ward;
          }

          const cleanAddress = buildCleanAddress(display, ward, district, city);
          if (addressInput) addressInput.value = cleanAddress || display;
     }

     map.on("click", (e) => {
          const lat = e.latlng.lat;
          const lng = e.latlng.lng;

          if (marker) map.removeLayer(marker);
          marker = L.marker([lat, lng]).addTo(map);

          if (latInput) latInput.value = lat;
          if (lngInput) lngInput.value = lng;

          setAddress(lat, lng);
     });
     // ----------------------
     // AUTOCOMPLETE ĐỊA CHỈ
     // ----------------------

     const searchInput = document.getElementById("addressSearch");
     const suggestionBox = document.getElementById("addressSuggestions");

     let searchTimeout;

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
                    const res = await fetch(
                         `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(
                              query
                         )}&format=json&addressdetails=1&limit=5&accept-language=vi`,
                         {
                              headers: {
                                   "User-Agent": "BadmintonMarketplace/1.0",
                              },
                         }
                    );

                    const data = await res.json();

                    suggestionBox.innerHTML = "";

                    if (data.length === 0) {
                         const noResult = document.createElement("div");
                         noResult.className = "suggestion-item no-result";
                         noResult.innerHTML =
                              '<i class="fas fa-info-circle me-2"></i>Không tìm thấy địa chỉ phù hợp';
                         suggestionBox.appendChild(noResult);
                         suggestionBox.style.display = "block";
                         return;
                    }

                    suggestionBox.style.display = "block";

                    data.forEach((item, index) => {
                         const li = document.createElement("div");
                         li.className = "suggestion-item";

                         // Add icon and text
                         li.innerHTML = `
                              <i class="fas fa-map-marker-alt me-2"></i>
                              <span>${item.display_name}</span>
                         `;

                         li.addEventListener("click", () => {
                              const display = item.display_name;
                              if (fullAddressDisplay) fullAddressDisplay.value = display;

                              const a = item.address || {};

                              const city = a.city || a.state || a.province || "";
                              const district = a.district || a.town || a.suburb || a.county || "";
                              const ward =
                                   a.quarter || a.neighbourhood || a.village || a.hamlet || "";

                              if (cityInput) cityInput.value = city;
                              if (districtInput) districtInput.value = district;
                              if (wardInput) wardInput.value = ward;

                              const cleanAddress = buildCleanAddress(display, ward, district, city);
                              if (addressInput) addressInput.value = cleanAddress || display;

                              if (latInput) latInput.value = item.lat;
                              if (lngInput) lngInput.value = item.lon;

                              if (marker) map.removeLayer(marker);
                              marker = L.marker([item.lat, item.lon]).addTo(map);
                              map.setView([item.lat, item.lon], 16);

                              suggestionBox.style.display = "none";
                         });

                         suggestionBox.appendChild(li);
                    });
               } catch (error) {
                    console.error("Error fetching addresses:", error);
                    suggestionBox.innerHTML =
                         '<div class="suggestion-item error"><i class="fas fa-exclamation-triangle me-2"></i>Lỗi tìm kiếm, vui lòng thử lại</div>';
                    suggestionBox.style.display = "block";
               }
          }, 400); // debounce 400ms
     });

     // ----------------------
     // CURRENT LOCATION BUTTON
     // ----------------------
     const currentLocationBtn = document.getElementById("useCurrentLocation");
     const locationSpinner = document.getElementById("locationSpinner");

     if (currentLocationBtn) {
          currentLocationBtn.addEventListener("click", () => {
               if (!navigator.geolocation) {
                    alert("Trình duyệt của bạn không hỗ trợ định vị!");
                    return;
               }

               // Show loading
               currentLocationBtn.disabled = true;
               locationSpinner.classList.remove("d-none");
               currentLocationBtn.querySelector("span").textContent = "Đang lấy vị trí...";

               navigator.geolocation.getCurrentPosition(
                    (position) => {
                         const lat = position.coords.latitude;
                         const lng = position.coords.longitude;

                         // Update map
                         if (marker) map.removeLayer(marker);
                         marker = L.marker([lat, lng]).addTo(map);
                         map.setView([lat, lng], 16);

                         // Update hidden inputs
                         if (latInput) latInput.value = lat;
                         if (lngInput) lngInput.value = lng;

                         // Fetch address
                         setAddress(lat, lng);

                         // Reset button
                         currentLocationBtn.disabled = false;
                         locationSpinner.classList.add("d-none");
                         currentLocationBtn.querySelector("span").textContent =
                              "Sử dụng vị trí hiện tại của tôi";
                    },
                    (error) => {
                         console.error("Geolocation error:", error);
                         let message = "Không thể lấy vị trí của bạn!";

                         switch (error.code) {
                              case error.PERMISSION_DENIED:
                                   message =
                                        "Bạn đã từ chối quyền truy cập vị trí. Vui lòng cho phép trong cài đặt trình duyệt.";
                                   break;
                              case error.POSITION_UNAVAILABLE:
                                   message = "Thông tin vị trí không khả dụng.";
                                   break;
                              case error.TIMEOUT:
                                   message = "Yêu cầu lấy vị trí hết thời gian.";
                                   break;
                         }

                         alert(message);

                         // Reset button
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

     // Auto-detect location removed - uncomment below if needed
     /*
     navigator.geolocation.getCurrentPosition((pos) => {
          const lat = pos.coords.latitude;
          const lng = pos.coords.longitude;

          map.setView([lat, lng], 15);

          if (marker) map.removeLayer(marker);
          marker = L.marker([lat, lng]).addTo(map);

          latInput.value = lat;
          lngInput.value = lng;
          setAddress(lat, lng);
     });
     */

     // Image preview functionality
     const logoInput = document.getElementById("logo");
     const logoPreview = document.getElementById("logo-preview");
     const previewImage = document.getElementById("preview-image");
     const removeLogo = document.getElementById("remove-logo");
     const fileLabelText = document.getElementById("file-label-text");

     if (logoInput) {
          logoInput.addEventListener("change", function (e) {
               const file = e.target.files[0];
               if (file) {
                    // Check file size (2MB = 2 * 1024 * 1024 bytes)
                    if (file.size > 2 * 1024 * 1024) {
                         alert("Kích thước file không được vượt quá 2MB!");
                         logoInput.value = "";
                         return;
                    }

                    // Check file type
                    if (!file.type.startsWith("image/")) {
                         alert("Vui lòng chọn file ảnh!");
                         logoInput.value = "";
                         return;
                    }

                    // Show preview
                    const reader = new FileReader();
                    reader.onload = function (e) {
                         previewImage.src = e.target.result;
                         logoPreview.style.display = "block";
                         fileLabelText.textContent = file.name;
                    };
                    reader.readAsDataURL(file);
               }
          });
     }

     if (removeLogo) {
          removeLogo.addEventListener("click", function () {
               logoInput.value = "";
               previewImage.src = "";
               logoPreview.style.display = "none";
               fileLabelText.textContent = "Chọn ảnh";
          });
     }
});
