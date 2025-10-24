/**
 * Main JavaScript for Bad_Shop
 * Handles dropdown initialization and other UI interactions
 */

// Initialize all Bootstrap dropdowns when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Initialize Bootstrap dropdowns
    const dropdownElementList = document.querySelectorAll('.dropdown-toggle');
    const dropdownList = [...dropdownElementList].map(dropdownToggleEl => new bootstrap.Dropdown(dropdownToggleEl));
    
    console.log('Dropdowns initialized:', dropdownList.length);
});

// Additional dropdown toggle for manual trigger (fallback)
document.addEventListener('DOMContentLoaded', function() {
    const dropdownToggles = document.querySelectorAll('[data-bs-toggle="dropdown"]');
    
    dropdownToggles.forEach(toggle => {
        toggle.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            const dropdownMenu = this.nextElementSibling;
            
            // Close other dropdowns
            document.querySelectorAll('.dropdown-menu.show').forEach(menu => {
                if (menu !== dropdownMenu) {
                    menu.classList.remove('show');
                }
            });
            
            // Toggle current dropdown
            dropdownMenu.classList.toggle('show');
        });
    });
    
    // Close dropdown when clicking outside
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.dropdown')) {
            document.querySelectorAll('.dropdown-menu.show').forEach(menu => {
                menu.classList.remove('show');
            });
        }
    });
});
