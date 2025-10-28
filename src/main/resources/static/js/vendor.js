/**
 * Vendor Dashboard JavaScript
 * Handles vendor-specific functionality
 */

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    initOrderManagement();
    initProductManagement();
    initNotifications();
    initCharts();
    initFilters();
});

// ========================================
// ORDER MANAGEMENT
// ========================================

function initOrderManagement() {
    // Auto-refresh order counts every 30 seconds
    setInterval(refreshOrderCounts, 30000);
    
    // Handle order status tabs
    const statusTabs = document.querySelectorAll('.order-status-tab');
    statusTabs.forEach(tab => {
        tab.addEventListener('click', function(e) {
            e.preventDefault();
            const status = this.dataset.status;
            filterOrdersByStatus(status);
        });
    });
    
    // Handle quick actions on orders
    const confirmButtons = document.querySelectorAll('.btn-confirm-order');
    confirmButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            if (!confirm('Xác nhận đơn hàng này? Số lượng sản phẩm sẽ được trừ khỏi kho.')) {
                e.preventDefault();
            }
        });
    });
    
    // Handle cancel order
    const cancelButtons = document.querySelectorAll('.btn-cancel-order');
    cancelButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            if (!confirm('Bạn có chắc muốn hủy đơn hàng này?')) {
                e.preventDefault();
            }
        });
    });
}

function refreshOrderCounts() {
    // Only refresh on order list page
    if (!document.querySelector('.order-list-page')) return;
    
    fetch('/vendor/api/order-counts')
        .then(response => response.json())
        .then(data => {
            updateOrderBadges(data);
        })
        .catch(error => console.error('Error refreshing order counts:', error));
}

function updateOrderBadges(counts) {
    const badges = {
        'new': counts.newOrders || 0,
        'processing': counts.processingOrders || 0,
        'shipping': counts.shippingOrders || 0,
        'delivered': counts.deliveredOrders || 0,
        'cancelled': counts.cancelledOrders || 0
    };
    
    Object.keys(badges).forEach(key => {
        const badge = document.querySelector(`[data-count="${key}"]`);
        if (badge) {
            badge.textContent = badges[key];
            if (badges[key] > 0) {
                badge.classList.add('badge-pulse');
            }
        }
    });
}

function filterOrdersByStatus(status) {
    const url = new URL(window.location.href);
    if (status) {
        url.searchParams.set('status', status);
    } else {
        url.searchParams.delete('status');
    }
    window.location.href = url.toString();
}

// ========================================
// PRODUCT MANAGEMENT
// ========================================

function initProductManagement() {
    // Handle product image preview
    const imageInputs = document.querySelectorAll('input[type="file"].product-image');
    imageInputs.forEach(input => {
        input.addEventListener('change', function(e) {
            previewProductImages(e.target);
        });
    });
    
    // Handle toggle selling status
    const toggleButtons = document.querySelectorAll('.btn-toggle-selling');
    toggleButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const productId = this.dataset.productId;
            const isSelling = this.dataset.isSelling === 'true';
            toggleProductSelling(productId, !isSelling);
        });
    });
    
    // Handle delete product
    const deleteButtons = document.querySelectorAll('.btn-delete-product');
    deleteButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) {
                e.preventDefault();
            }
        });
    });
}

function previewProductImages(input) {
    const preview = document.getElementById('image-preview');
    if (!preview) return;
    
    preview.innerHTML = '';
    
    if (input.files) {
        Array.from(input.files).forEach((file, index) => {
            const reader = new FileReader();
            reader.onload = function(e) {
                const div = document.createElement('div');
                div.className = 'col-md-3 mb-3';
                div.innerHTML = `
                    <div class="card">
                        <img src="${e.target.result}" class="card-img-top" alt="Preview ${index + 1}">
                        <div class="card-body p-2 text-center">
                            <small>Ảnh ${index + 1}</small>
                        </div>
                    </div>
                `;
                preview.appendChild(div);
            };
            reader.readAsDataURL(file);
        });
    }
}

function toggleProductSelling(productId, newStatus) {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `/vendor/products/${productId}/toggle-selling`;
    
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

// ========================================
// NOTIFICATIONS
// ========================================

function initNotifications() {
    // Auto-hide alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });
    
    // Show toast notifications
    const toasts = document.querySelectorAll('.toast');
    toasts.forEach(toast => {
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();
    });
}

// ========================================
// CHARTS & ANALYTICS
// ========================================

function initCharts() {
    // Revenue chart
    const revenueChartCanvas = document.getElementById('revenueChart');
    if (revenueChartCanvas && typeof Chart !== 'undefined') {
        initRevenueChart(revenueChartCanvas);
    }
    
    // Product sales chart
    const salesChartCanvas = document.getElementById('salesChart');
    if (salesChartCanvas && typeof Chart !== 'undefined') {
        initSalesChart(salesChartCanvas);
    }
}

function initRevenueChart(canvas) {
    // Get data from data attributes
    const labels = JSON.parse(canvas.dataset.labels || '[]');
    const data = JSON.parse(canvas.dataset.values || '[]');
    
    new Chart(canvas, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Doanh thu (VNĐ)',
                data: data,
                borderColor: 'rgb(75, 192, 192)',
                backgroundColor: 'rgba(75, 192, 192, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return 'Doanh thu: ' + formatCurrency(context.parsed.y);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return formatCurrency(value);
                        }
                    }
                }
            }
        }
    });
}

function initSalesChart(canvas) {
    const labels = JSON.parse(canvas.dataset.labels || '[]');
    const data = JSON.parse(canvas.dataset.values || '[]');
    
    new Chart(canvas, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Số lượng bán',
                data: data,
                backgroundColor: 'rgba(54, 162, 235, 0.5)',
                borderColor: 'rgb(54, 162, 235)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                }
            }
        }
    });
}

// ========================================
// FILTERS
// ========================================

function initFilters() {
    // Date range filter
    const dateFilters = document.querySelectorAll('.date-filter');
    dateFilters.forEach(filter => {
        filter.addEventListener('change', function() {
            applyFilters();
        });
    });
    
    // Status filter
    const statusFilters = document.querySelectorAll('.status-filter');
    statusFilters.forEach(filter => {
        filter.addEventListener('change', function() {
            applyFilters();
        });
    });
    
    // Search filter with debounce
    const searchInputs = document.querySelectorAll('.search-filter');
    searchInputs.forEach(input => {
        let timeout;
        input.addEventListener('input', function() {
            clearTimeout(timeout);
            timeout = setTimeout(() => {
                applyFilters();
            }, 500);
        });
    });
    
    // Reset filter button
    const resetBtn = document.querySelector('.btn-reset-filter');
    if (resetBtn) {
        resetBtn.addEventListener('click', function(e) {
            e.preventDefault();
            resetFilters();
        });
    }
}

function applyFilters() {
    const form = document.querySelector('.filter-form');
    if (form) {
        form.submit();
    }
}

function resetFilters() {
    const url = new URL(window.location.href);
    url.search = '';
    window.location.href = url.toString();
}

// ========================================
// UTILITY FUNCTIONS
// ========================================

function formatCurrency(value) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(value);
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Export functions for use in other scripts
window.vendorApp = {
    refreshOrderCounts,
    filterOrdersByStatus,
    toggleProductSelling,
    formatCurrency,
    formatDate
};
