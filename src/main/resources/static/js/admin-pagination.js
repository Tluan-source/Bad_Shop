/**
 * Admin Pagination - Reusable pagination script for admin tables
 * Usage: Add this script to any admin page with a table that needs pagination
 */

class AdminPagination {
    constructor(tableSelector = '.admin-table tbody', itemsPerPage = 10) {
        this.tableSelector = tableSelector;
        this.itemsPerPage = itemsPerPage;
        this.currentPage = 1;
        this.searchValue = '';
    }

    init() {
        this.paginateTable();
    }

    getAllRows() {
        const tbody = document.querySelector(this.tableSelector);
        if (!tbody) return [];
        
        return Array.from(tbody.querySelectorAll('tr')).filter(row => {
            // Skip empty state rows (those with colspan)
            const hasColspan = row.querySelector('td[colspan]');
            // Skip hidden rows (from search)
            const isHidden = row.style.display === 'none';
            return !hasColspan && !isHidden;
        });
    }

    paginateTable() {
        const tableRows = this.getAllRows();
        
        if (tableRows.length === 0) {
            this.hidePagination();
            return;
        }

        const totalPages = Math.ceil(tableRows.length / this.itemsPerPage);
        
        if (totalPages <= 1) {
            this.hidePagination();
            // Show all rows
            tableRows.forEach(row => row.classList.remove('d-none'));
            return;
        }

        this.showPagination();

        // Hide all rows first
        tableRows.forEach(row => row.classList.add('d-none'));

        // Show only current page rows
        const start = (this.currentPage - 1) * this.itemsPerPage;
        const end = start + this.itemsPerPage;
        tableRows.slice(start, end).forEach(row => row.classList.remove('d-none'));

        // Update pagination controls
        this.updatePaginationControls(totalPages);
    }

    updatePaginationControls(totalPages) {
        const paginationContainer = document.querySelector('.pagination');
        if (!paginationContainer) return;

        let html = '';

        // Previous button
        html += `<li class="page-item ${this.currentPage === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${this.currentPage - 1}">Trước</a>
        </li>`;

        // Page numbers with smart ellipsis
        for (let i = 1; i <= totalPages; i++) {
            if (i === 1 || i === totalPages || (i >= this.currentPage - 2 && i <= this.currentPage + 2)) {
                html += `<li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <a class="page-link" href="#" data-page="${i}">${i}</a>
                </li>`;
            } else if (i === this.currentPage - 3 || i === this.currentPage + 3) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        // Next button
        html += `<li class="page-item ${this.currentPage === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${this.currentPage + 1}">Sau</a>
        </li>`;

        paginationContainer.innerHTML = html;

        // Add click event listeners
        paginationContainer.querySelectorAll('a.page-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const page = parseInt(link.getAttribute('data-page'));
                this.changePage(page);
            });
        });
    }

    changePage(page) {
        const tableRows = this.getAllRows();
        const totalPages = Math.ceil(tableRows.length / this.itemsPerPage);

        if (page < 1 || page > totalPages) return;

        this.currentPage = page;
        this.paginateTable();

        // Scroll to top of table
        const table = document.querySelector('.admin-table');
        if (table) {
            table.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }

    hidePagination() {
        const paginationNav = document.querySelector('nav:has(.pagination)');
        if (paginationNav) {
            paginationNav.style.display = 'none';
        }
    }

    showPagination() {
        const paginationNav = document.querySelector('nav:has(.pagination)');
        if (paginationNav) {
            paginationNav.style.display = 'block';
        }
    }

    refresh() {
        this.currentPage = 1;
        this.paginateTable();
    }
}

// Initialize pagination when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // Check if we're on an admin page with a table
    const adminTable = document.querySelector('.admin-table tbody');
    if (adminTable) {
        window.adminPagination = new AdminPagination('.admin-table tbody', 10);
        window.adminPagination.init();
    }
});

// Search functionality with pagination
function initAdminSearch(searchInputId) {
    const searchInput = document.getElementById(searchInputId);
    if (!searchInput) return;

    searchInput.addEventListener('keyup', function() {
        const searchValue = this.value.toLowerCase();
        const tableRows = document.querySelectorAll('.admin-table tbody tr');
        
        tableRows.forEach(row => {
            // Skip empty state rows
            if (row.querySelector('td[colspan]')) {
                return;
            }

            const text = row.textContent.toLowerCase();
            if (text.includes(searchValue)) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });

        // Refresh pagination after search
        if (window.adminPagination) {
            window.adminPagination.refresh();
        }
    });
}
