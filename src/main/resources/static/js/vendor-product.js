/**
 * Vendor Product Management JavaScript
 * Handles product-related functionality
 */

document.addEventListener('DOMContentLoaded', function() {
    initProductForm();
    initImageUpload();
    initStyleValueSelection();
    initPriceCalculation();
});

// ========================================
// PRODUCT FORM
// ========================================

function initProductForm() {
    const form = document.getElementById('productForm');
    if (!form) return;
    
    // Validate form before submit
    form.addEventListener('submit', function(e) {
        if (!validateProductForm()) {
            e.preventDefault();
            return false;
        }
    });
    
    // Auto-save draft (optional)
    const autoSave = document.getElementById('autoSaveDraft');
    if (autoSave && autoSave.checked) {
        setInterval(saveDraft, 30000); // Save every 30 seconds
    }
}

function validateProductForm() {
    let isValid = true;
    const errors = [];
    
    // Validate name
    const name = document.getElementById('name');
    if (name && name.value.trim().length < 3) {
        errors.push('Tên sản phẩm phải có ít nhất 3 ký tự');
        isValid = false;
    }
    
    // Validate price
    const price = document.getElementById('price');
    if (price && parseFloat(price.value) <= 0) {
        errors.push('Giá sản phẩm phải lớn hơn 0');
        isValid = false;
    }
    
    // Validate promotional price
    const promotionalPrice = document.getElementById('promotionalPrice');
    if (promotionalPrice && parseFloat(promotionalPrice.value) > 0) {
        if (parseFloat(promotionalPrice.value) >= parseFloat(price.value)) {
            errors.push('Giá khuyến mãi phải nhỏ hơn giá gốc');
            isValid = false;
        }
    }
    
    // Validate quantity
    const quantity = document.getElementById('quantity');
    if (quantity && parseInt(quantity.value) < 0) {
        errors.push('Số lượng không thể âm');
        isValid = false;
    }
    
    // Validate category
    const category = document.getElementById('categoryId');
    if (category && !category.value) {
        errors.push('Vui lòng chọn danh mục sản phẩm');
        isValid = false;
    }
    
    // Show errors
    if (!isValid) {
        const errorDiv = document.getElementById('formErrors');
        if (errorDiv) {
            errorDiv.innerHTML = '<div class="alert alert-danger"><ul class="mb-0">' + 
                errors.map(err => '<li>' + err + '</li>').join('') + 
                '</ul></div>';
            errorDiv.scrollIntoView({ behavior: 'smooth' });
        } else {
            alert(errors.join('\n'));
        }
    }
    
    return isValid;
}

// ========================================
// IMAGE UPLOAD
// ========================================

function initImageUpload() {
    const imageInput = document.getElementById('imageFiles');
    if (imageInput) {
        imageInput.addEventListener('change', handleImageUpload);
    }
    
    // Handle image URL input
    const imageUrlsText = document.getElementById('imageUrlsText');
    if (imageUrlsText) {
        imageUrlsText.addEventListener('input', function() {
            previewImageUrls(this.value);
        });
    }
    
    // Initialize existing images preview
    previewExistingImages();
}

function handleImageUpload(event) {
    const files = event.target.files;
    const preview = document.getElementById('imagePreview');
    
    if (!preview) return;
    
    preview.innerHTML = '';
    
    if (files.length === 0) return;
    
    Array.from(files).forEach((file, index) => {
        if (!file.type.match('image.*')) {
            return;
        }
        
        const reader = new FileReader();
        reader.onload = function(e) {
            const col = document.createElement('div');
            col.className = 'col-md-3 mb-3';
            col.innerHTML = `
                <div class="card">
                    <img src="${e.target.result}" class="card-img-top" alt="Preview ${index + 1}" style="height: 200px; object-fit: cover;">
                    <div class="card-body p-2">
                        <button type="button" class="btn btn-sm btn-danger w-100" onclick="removeImagePreview(this)">
                            <i class="fas fa-trash"></i> Xóa
                        </button>
                    </div>
                </div>
            `;
            preview.appendChild(col);
        };
        reader.readAsDataURL(file);
    });
}

function previewImageUrls(urls) {
    const preview = document.getElementById('imagePreview');
    if (!preview) return;
    
    preview.innerHTML = '';
    
    const urlList = urls.split('\n').filter(url => url.trim() !== '');
    
    urlList.forEach((url, index) => {
        const col = document.createElement('div');
        col.className = 'col-md-3 mb-3';
        col.innerHTML = `
            <div class="card">
                <img src="${url.trim()}" class="card-img-top" alt="Preview ${index + 1}" 
                     style="height: 200px; object-fit: cover;"
                     onerror="this.src='https://via.placeholder.com/300x200?text=Invalid+URL'">
                <div class="card-body p-2 text-center">
                    <small class="text-muted">Ảnh ${index + 1}</small>
                </div>
            </div>
        `;
        preview.appendChild(col);
    });
}

function previewExistingImages() {
    const existingImages = document.getElementById('existingImages');
    if (!existingImages) return;
    
    try {
        const images = JSON.parse(existingImages.value || '[]');
        if (images.length > 0) {
            const preview = document.getElementById('imagePreview');
            if (preview) {
                preview.innerHTML = '';
                images.forEach((url, index) => {
                    const col = document.createElement('div');
                    col.className = 'col-md-3 mb-3';
                    col.innerHTML = `
                        <div class="card">
                            <img src="${url}" class="card-img-top" alt="Image ${index + 1}" style="height: 200px; object-fit: cover;">
                            <div class="card-body p-2 text-center">
                                <small class="text-muted">Ảnh hiện tại ${index + 1}</small>
                            </div>
                        </div>
                    `;
                    preview.appendChild(col);
                });
            }
        }
    } catch (e) {
        console.error('Error parsing existing images:', e);
    }
}

function removeImagePreview(button) {
    const col = button.closest('.col-md-3');
    if (col) {
        col.remove();
    }
}

// ========================================
// STYLE VALUE SELECTION
// ========================================

function initStyleValueSelection() {
    const styleCheckboxes = document.querySelectorAll('.style-value-checkbox');
    const selectedStylesDiv = document.getElementById('selectedStyles');
    
    if (!styleCheckboxes.length) return;
    
    styleCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            updateSelectedStyles();
        });
    });
    
    // Initialize display
    updateSelectedStyles();
}

function updateSelectedStyles() {
    const selectedStylesDiv = document.getElementById('selectedStyles');
    if (!selectedStylesDiv) return;
    
    const checkedBoxes = document.querySelectorAll('.style-value-checkbox:checked');
    
    if (checkedBoxes.length === 0) {
        selectedStylesDiv.innerHTML = '<div class="text-muted">Chưa chọn style nào</div>';
        return;
    }
    
    let html = '<div class="d-flex flex-wrap gap-2">';
    checkedBoxes.forEach(checkbox => {
        const label = checkbox.closest('label');
        const text = label ? label.textContent.trim() : checkbox.value;
        html += `<span class="badge bg-primary">${text}</span>`;
    });
    html += '</div>';
    
    selectedStylesDiv.innerHTML = html;
}

// ========================================
// PRICE CALCULATION
// ========================================

function initPriceCalculation() {
    const priceInput = document.getElementById('price');
    const promoInput = document.getElementById('promotionalPrice');
    const discountDisplay = document.getElementById('discountPercent');
    
    if (!priceInput || !promoInput) return;
    
    function calculateDiscount() {
        const price = parseFloat(priceInput.value) || 0;
        const promoPrice = parseFloat(promoInput.value) || 0;
        
        if (price > 0 && promoPrice > 0 && promoPrice < price) {
            const discount = ((price - promoPrice) / price * 100).toFixed(0);
            if (discountDisplay) {
                discountDisplay.textContent = `Giảm ${discount}%`;
                discountDisplay.className = 'badge bg-success ms-2';
            }
        } else {
            if (discountDisplay) {
                discountDisplay.textContent = '';
            }
        }
    }
    
    priceInput.addEventListener('input', calculateDiscount);
    promoInput.addEventListener('input', calculateDiscount);
    
    // Initial calculation
    calculateDiscount();
}

// ========================================
// DRAFT SAVING
// ========================================

function saveDraft() {
    const form = document.getElementById('productForm');
    if (!form) return;
    
    const formData = new FormData(form);
    const draft = {};
    
    for (let [key, value] of formData.entries()) {
        draft[key] = value;
    }
    
    localStorage.setItem('productDraft', JSON.stringify(draft));
    
    // Show saved indicator
    const indicator = document.getElementById('draftSavedIndicator');
    if (indicator) {
        indicator.textContent = 'Đã lưu nháp lúc ' + new Date().toLocaleTimeString();
        indicator.classList.remove('d-none');
        setTimeout(() => {
            indicator.classList.add('d-none');
        }, 3000);
    }
}

function loadDraft() {
    const draft = localStorage.getItem('productDraft');
    if (!draft) return;
    
    try {
        const data = JSON.parse(draft);
        Object.keys(data).forEach(key => {
            const input = document.querySelector(`[name="${key}"]`);
            if (input) {
                input.value = data[key];
            }
        });
        
        alert('Đã tải nháp đã lưu');
    } catch (e) {
        console.error('Error loading draft:', e);
    }
}

function clearDraft() {
    localStorage.removeItem('productDraft');
}

// ========================================
// BULK ACTIONS
// ========================================

function initBulkActions() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const productCheckboxes = document.querySelectorAll('.product-checkbox');
    const bulkActionBtn = document.getElementById('bulkActionBtn');
    
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            productCheckboxes.forEach(cb => {
                cb.checked = this.checked;
            });
            updateBulkActionButton();
        });
    }
    
    productCheckboxes.forEach(cb => {
        cb.addEventListener('change', updateBulkActionButton);
    });
    
    if (bulkActionBtn) {
        bulkActionBtn.addEventListener('click', executeBulkAction);
    }
}

function updateBulkActionButton() {
    const checkedBoxes = document.querySelectorAll('.product-checkbox:checked');
    const bulkActionBtn = document.getElementById('bulkActionBtn');
    
    if (bulkActionBtn) {
        if (checkedBoxes.length > 0) {
            bulkActionBtn.disabled = false;
            bulkActionBtn.textContent = `Thao tác (${checkedBoxes.length})`;
        } else {
            bulkActionBtn.disabled = true;
            bulkActionBtn.textContent = 'Thao tác';
        }
    }
}

function executeBulkAction() {
    const action = document.getElementById('bulkAction').value;
    const checkedBoxes = document.querySelectorAll('.product-checkbox:checked');
    
    if (!action || checkedBoxes.length === 0) {
        alert('Vui lòng chọn hành động và ít nhất một sản phẩm');
        return;
    }
    
    const productIds = Array.from(checkedBoxes).map(cb => cb.value);
    
    if (confirm(`Bạn có chắc muốn ${action} ${productIds.length} sản phẩm?`)) {
        // Submit form or make API call
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = `/vendor/products/bulk-${action}`;
        
        productIds.forEach(id => {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'productIds';
            input.value = id;
            form.appendChild(input);
        });
        
        // Add CSRF token
        const csrfToken = document.querySelector('meta[name="_csrf"]');
        if (csrfToken) {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = '_csrf';
            input.value = csrfToken.content;
            form.appendChild(input);
        }
        
        document.body.appendChild(form);
        form.submit();
    }
}

// Export functions
window.vendorProduct = {
    validateProductForm,
    saveDraft,
    loadDraft,
    clearDraft,
    removeImagePreview
};
