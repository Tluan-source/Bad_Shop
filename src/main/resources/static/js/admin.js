// Admin Dashboard JavaScript

// Chart initialization (placeholder - needs Chart.js library)
function initCharts() {
    console.log('Charts initialized');
    // Add Chart.js implementation here
}

// Search functionality
function initSearch() {
    const searchInputs = document.querySelectorAll('.admin-search');
    searchInputs.forEach(input => {
        input.addEventListener('input', function(e) {
            const searchTerm = e.target.value.toLowerCase();
            console.log('Searching for:', searchTerm);
            // Implement search logic here
        });
    });
}

// Table sorting
function initTableSort() {
    const tables = document.querySelectorAll('.admin-table');
    tables.forEach(table => {
        const headers = table.querySelectorAll('thead th');
        headers.forEach((header, index) => {
            header.style.cursor = 'pointer';
            header.addEventListener('click', () => {
                sortTable(table, index);
            });
        });
    });
}

function sortTable(table, columnIndex) {
    const tbody = table.querySelector('tbody');
    const rows = Array.from(tbody.querySelectorAll('tr'));
    
    rows.sort((a, b) => {
        const aValue = a.querySelectorAll('td')[columnIndex].textContent;
        const bValue = b.querySelectorAll('td')[columnIndex].textContent;
        return aValue.localeCompare(bValue);
    });
    
    rows.forEach(row => tbody.appendChild(row));
}

// Notification system
function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
    notification.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.remove();
    }, 5000);
}

// Confirm delete action
function confirmDelete(itemName) {
    return confirm(`Bạn có chắc chắn muốn xóa "${itemName}"?`);
}

// Export data functionality
function exportData(format = 'csv') {
    showNotification('Đang xuất dữ liệu...', 'info');
    // Implement export logic
    setTimeout(() => {
        showNotification('Xuất dữ liệu thành công!', 'success');
    }, 1500);
}

// Real-time statistics update (simulated)
function updateStatistics() {
    const statCards = document.querySelectorAll('.admin-stat-card h2');
    statCards.forEach(card => {
        // Add animation when updating
        card.classList.add('animate__animated', 'animate__pulse');
        setTimeout(() => {
            card.classList.remove('animate__animated', 'animate__pulse');
        }, 1000);
    });
}

// Initialize everything when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('Admin Dashboard loaded');
    initSearch();
    initTableSort();
    
    // Auto-update statistics every 30 seconds
    setInterval(updateStatistics, 30000);
    
    // Add click handlers for action buttons
    document.querySelectorAll('[data-action="delete"]').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const itemName = this.dataset.itemName || 'mục này';
            if (confirmDelete(itemName)) {
                showNotification('Đã xóa thành công!', 'success');
            }
        });
    });
    
    // Add click handlers for export buttons
    document.querySelectorAll('[data-action="export"]').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            exportData();
        });
    });
});

// Smooth scroll to top
function scrollToTop() {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
}

// Add scroll to top button
window.addEventListener('scroll', function() {
    const scrollBtn = document.getElementById('scrollTopBtn');
    if (scrollBtn) {
        if (window.pageYOffset > 300) {
            scrollBtn.style.display = 'block';
        } else {
            scrollBtn.style.display = 'none';
        }
    }
});

// Form validation
function validateForm(formId) {
    const form = document.getElementById(formId);
    if (!form) return false;
    
    const inputs = form.querySelectorAll('input[required], textarea[required], select[required]');
    let isValid = true;
    
    inputs.forEach(input => {
        if (!input.value.trim()) {
            input.classList.add('is-invalid');
            isValid = false;
        } else {
            input.classList.remove('is-invalid');
        }
    });
    
    return isValid;
}
