// Admin shipping providers JS
document.addEventListener('DOMContentLoaded', function () {
    // Open edit modal and populate fields
    window.openEditModal = function(btn) {
        var id = btn.getAttribute('data-id') || btn.dataset.id;
        var name = btn.getAttribute('data-name') || btn.dataset.name;
        var fee = btn.getAttribute('data-fee') || btn.dataset.fee;
        var active = btn.getAttribute('data-active') || btn.dataset.active;

        document.getElementById('edit-id').value = id || '';
        document.getElementById('edit-name').value = name || '';
        document.getElementById('edit-fee').value = fee != null ? fee : 0;
        var chk = document.getElementById('edit-active');
        if (active === 'false' || active === '0' || active === 'null' ) {
            chk.checked = false;
        } else {
            chk.checked = true;
        }

        var editModal = new bootstrap.Modal(document.getElementById('editShippingModal'));
        editModal.show();
    }

    // Confirm delete action
    window.confirmDeleteShipping = function(id, name) {
        if (!id) return;
        var ok = confirm('Bạn có chắc muốn xóa nhà vận chuyển "' + (name || '') + '" ?');
        if (!ok) return;

        // Try to get CSRF token from page (rendered hidden inputs)
        var csrfInput = document.querySelector('input[name="_csrf"]');
        var csrfToken = csrfInput ? csrfInput.value : null;

        var headers = {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'XMLHttpRequest'
        };
        if (csrfToken) {
            headers['X-CSRF-TOKEN'] = csrfToken;
        }

        fetch('/admin/shipping/delete', {
            method: 'POST',
            headers: headers,
            body: 'id=' + encodeURIComponent(id)
        }).then(function(resp) {
            if (resp.ok) {
                location.reload();
            } else {
                resp.text().then(function(t) { alert('Xóa thất bại: ' + t); });
            }
        }).catch(function(err){
            alert('Error: ' + err);
        });
    }

    // Simple card pagination (reuse itemsPerPage if needed)
    var currentPage = 1;
    var itemsPerPage = 6;

    function paginate() {
        var cards = Array.from(document.querySelectorAll('.row.g-4 > .col-md-4'));
        var visible = cards.filter(c => !c.classList.contains('d-none'));
        var total = visible.length;
        var totalPages = Math.max(1, Math.ceil(total / itemsPerPage));
        if (totalPages <= 1) {
            document.querySelector('nav.mt-4')?.style.setProperty('display', 'none');
            return;
        }
        document.querySelector('nav.mt-4')?.style.removeProperty('display');
        cards.forEach(c => c.classList.add('d-none'));
        var start = (currentPage - 1) * itemsPerPage;
        var end = start + itemsPerPage;
        visible.slice(start, end).forEach(c => c.classList.remove('d-none'));
        renderPagination(totalPages);
    }

    function renderPagination(totalPages) {
        var container = document.querySelector('.pagination');
        if (!container) return;
        var html = '';
        html += `<li class="page-item ${currentPage===1? 'disabled':''}"><a class="page-link" href="#" onclick="changePage(${currentPage-1});return false;">Trước</a></li>`;
        for (var i=1;i<=totalPages;i++){
            html += `<li class="page-item ${i===currentPage? 'active':''}"><a class="page-link" href="#" onclick="changePage(${i});return false;">${i}</a></li>`;
        }
        html += `<li class="page-item ${currentPage===totalPages? 'disabled':''}"><a class="page-link" href="#" onclick="changePage(${currentPage+1});return false;">Sau</a></li>`;
        container.innerHTML = html;
    }

    window.changePage = function(p) {
        var cards = Array.from(document.querySelectorAll('.row.g-4 > .col-md-4'));
        var visible = cards.filter(c => !c.querySelector('.text-center.text-muted'));
        var totalPages = Math.max(1, Math.ceil(visible.length / itemsPerPage));
        if (p < 1 || p > totalPages) return;
        currentPage = p;
        paginate();
        window.scrollTo({top:0, behavior:'smooth'});
    }

    // Initialize pagination on DOM ready
    paginate();
});