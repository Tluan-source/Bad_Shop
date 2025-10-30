document.addEventListener("DOMContentLoaded", function () {
     const MAX_FILES = 5;
     const MAX_SIZE = 5 * 1024 * 1024; // 5MB

     function createPreview(file) {
          const url = URL.createObjectURL(file);
          const img = document.createElement("img");
          img.src = url;
          img.style.height = "80px";
          img.style.objectFit = "cover";
          img.style.borderRadius = "6px";
          img.style.marginRight = "6px";
          img.dataset._objectUrl = url;
          return img;
     }

     function validateFiles(files) {
          if (files.length > MAX_FILES) {
               return `Bạn chỉ được tải tối đa ${MAX_FILES} ảnh.`;
          }
          for (let i = 0; i < files.length; i++) {
               const f = files[i];
               if (!f.type.startsWith("image/")) {
                    return "Chỉ cho phép tệp ảnh (jpg, png, gif, ...).";
               }
               if (f.size > MAX_SIZE) {
                    return "Kích thước mỗi ảnh không được vượt quá 5MB.";
               }
          }
          return null;
     }

     function clearPreviews(container) {
          while (container.firstChild) {
               const node = container.firstChild;
               if (node.tagName === "IMG" && node.dataset._objectUrl) {
                    URL.revokeObjectURL(node.dataset._objectUrl);
               }
               container.removeChild(node);
          }
     }

     // Attach listeners to all file inputs for reviews (there may be multiple for each item)
     document.querySelectorAll(".review-images-input").forEach(function (input) {
          const previewContainer = input.closest("form").querySelector(".review-images-preview");
          const errorContainer = input.closest("form").querySelector(".review-images-error");
          const submitBtn = input.closest("form").querySelector(".review-submit-btn");

          input.addEventListener("change", function (evt) {
               const files = Array.from(evt.target.files || []);
               // clear previous
               clearPreviews(previewContainer);
               errorContainer.style.display = "none";
               errorContainer.textContent = "";

               const error = validateFiles(files);
               if (error) {
                    errorContainer.textContent = error;
                    errorContainer.style.display = "block";
                    if (submitBtn) submitBtn.disabled = true;
                    return;
               }

               // show previews
               files.forEach(function (f) {
                    const img = createPreview(f);
                    previewContainer.appendChild(img);
               });

               if (submitBtn) submitBtn.disabled = false;
          });

          // On form submit, final client-side validation
          const form = input.closest("form");
          if (form) {
               form.addEventListener("submit", function (e) {
                    const files = Array.from(input.files || []);
                    const error = validateFiles(files);
                    if (error) {
                         e.preventDefault();
                         errorContainer.textContent = error;
                         errorContainer.style.display = "block";
                    }
               });
          }
     });
});
