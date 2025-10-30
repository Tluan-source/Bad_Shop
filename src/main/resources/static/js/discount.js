/**
 * Discount Helper - Xử lý logic giảm giá (Voucher & Promotion)
 */

const DiscountHelper = {
    /**
     * Validate và tính toán giảm giá
     */
    async validateDiscount(orderAmount, storeId, voucherCode = null, promotionId = null) {
        try {
            const response = await fetch('/api/discount/validate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    orderAmount: orderAmount,
                    storeId: storeId,
                    voucherCode: voucherCode,
                    promotionId: promotionId
                })
            });

            if (!response.ok) {
                throw new Error('Không thể validate giảm giá');
            }

            return await response.json();
        } catch (error) {
            console.error('Error validating discount:', error);
            return null;
        }
    },

    /**
     * Lấy danh sách voucher khả dụng
     */
    async getAvailableVouchers() {
        try {
            const response = await fetch('/api/discount/vouchers/available');
            if (!response.ok) {
                throw new Error('Không thể lấy danh sách voucher');
            }
            return await response.json();
        } catch (error) {
            console.error('Error getting vouchers:', error);
            return [];
        }
    },

    /**
     * Lấy danh sách promotion của shop
     */
    async getStorePromotions(storeId) {
        try {
            const response = await fetch(`/api/discount/promotions/store/${storeId}`);
            if (!response.ok) {
                throw new Error('Không thể lấy danh sách khuyến mãi');
            }
            return await response.json();
        } catch (error) {
            console.error('Error getting promotions:', error);
            return [];
        }
    },

    /**
     * Kiểm tra mã voucher
     */
    async checkVoucher(code, orderAmount = null) {
        try {
            let url = `/api/discount/voucher/check/${encodeURIComponent(code)}`;
            if (orderAmount) {
                url += `?orderAmount=${orderAmount}`;
            }

            const response = await fetch(url);
            const data = await response.json();

            if (!response.ok) {
                return {
                    valid: false,
                    message: data.message || 'Mã voucher không hợp lệ'
                };
            }

            return {
                valid: true,
                voucher: data
            };
        } catch (error) {
            console.error('Error checking voucher:', error);
            return {
                valid: false,
                message: 'Có lỗi xảy ra khi kiểm tra mã voucher'
            };
        }
    },

    /**
     * Format số tiền
     */
    formatMoney(amount) {
        if (!amount) return '0đ';
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    },

    /**
     * Hiển thị thông tin giảm giá
     */
    displayDiscountInfo(result, containerSelector) {
        const container = document.querySelector(containerSelector);
        if (!container) return;

        let html = '';

        // Hiển thị promotion discount
        if (result.promotionValid && result.promotionDiscount > 0) {
            html += `
                <div class="discount-item promotion">
                    <span class="discount-label">Giảm giá shop (${result.promotionName}):</span>
                    <span class="discount-value">-${this.formatMoney(result.promotionDiscount)}</span>
                </div>
            `;
        }

        // Hiển thị voucher discount
        if (result.voucherValid && result.voucherDiscount > 0) {
            html += `
                <div class="discount-item voucher">
                    <span class="discount-label">Mã giảm giá (${result.voucherCode}):</span>
                    <span class="discount-value">-${this.formatMoney(result.voucherDiscount)}</span>
                </div>
            `;
        }

        // Hiển thị tổng giảm giá
        if (result.totalDiscount > 0) {
            html += `
                <div class="discount-item total">
                    <span class="discount-label"><strong>Tổng giảm giá:</strong></span>
                    <span class="discount-value"><strong>-${this.formatMoney(result.totalDiscount)}</strong></span>
                </div>
            `;
        }

        container.innerHTML = html;
    },

    /**
     * Hiển thị lỗi
     */
    showError(message, containerSelector) {
        const container = document.querySelector(containerSelector);
        if (!container) return;

        container.innerHTML = `
            <div class="alert alert-danger">
                ${message}
            </div>
        `;
    },

    /**
     * Render danh sách voucher
     */
    renderVoucherList(vouchers, containerSelector, onSelectCallback) {
        const container = document.querySelector(containerSelector);
        if (!container) return;

        if (!vouchers || vouchers.length === 0) {
            container.innerHTML = '<p class="text-muted">Không có voucher khả dụng</p>';
            return;
        }

        let html = '<div class="voucher-list">';
        vouchers.forEach(voucher => {
            const discount = voucher.discountType === 'PERCENTAGE' 
                ? `${voucher.discountValue}%` 
                : this.formatMoney(voucher.discountValue);
            
            const minOrder = voucher.minOrderValue > 0 
                ? `<small>Đơn tối thiểu: ${this.formatMoney(voucher.minOrderValue)}</small>` 
                : '';

            html += `
                <div class="voucher-card" data-code="${voucher.code}">
                    <div class="voucher-header">
                        <span class="voucher-code">${voucher.code}</span>
                        <span class="voucher-discount">${discount}</span>
                    </div>
                    <div class="voucher-body">
                        <p class="voucher-description">${voucher.description || ''}</p>
                        ${minOrder}
                    </div>
                    <div class="voucher-footer">
                        <small>Còn ${voucher.quantity - voucher.usageCount} lượt</small>
                        <button class="btn btn-sm btn-primary apply-voucher">Áp dụng</button>
                    </div>
                </div>
            `;
        });
        html += '</div>';

        container.innerHTML = html;

        // Attach event listeners
        container.querySelectorAll('.apply-voucher').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const card = e.target.closest('.voucher-card');
                const code = card.dataset.code;
                if (onSelectCallback) {
                    onSelectCallback(code);
                }
            });
        });
    },

    /**
     * Render danh sách promotion
     */
    renderPromotionList(promotions, containerSelector, onSelectCallback) {
        const container = document.querySelector(containerSelector);
        if (!container) return;

        if (!promotions || promotions.length === 0) {
            container.innerHTML = '<p class="text-muted">Không có khuyến mãi khả dụng</p>';
            return;
        }

        let html = '<div class="promotion-list">';
        promotions.forEach(promotion => {
            const discount = promotion.discountType === 'PERCENTAGE' 
                ? `${promotion.discountValue}%` 
                : this.formatMoney(promotion.discountValue);
            
            const minOrder = promotion.minOrderAmount > 0 
                ? `<small>Đơn tối thiểu: ${this.formatMoney(promotion.minOrderAmount)}</small>` 
                : '';

            html += `
                <div class="promotion-card" data-id="${promotion.id}">
                    <div class="promotion-header">
                        <span class="promotion-name">${promotion.name}</span>
                        <span class="promotion-discount">${discount}</span>
                    </div>
                    <div class="promotion-body">
                        <p class="promotion-description">${promotion.description || ''}</p>
                        ${minOrder}
                    </div>
                    <div class="promotion-footer">
                        <button class="btn btn-sm btn-success apply-promotion">Áp dụng</button>
                    </div>
                </div>
            `;
        });
        html += '</div>';

        container.innerHTML = html;

        // Attach event listeners
        container.querySelectorAll('.apply-promotion').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const card = e.target.closest('.promotion-card');
                const id = card.dataset.id;
                if (onSelectCallback) {
                    onSelectCallback(id);
                }
            });
        });
    }
};

// Export nếu dùng module
if (typeof module !== 'undefined' && module.exports) {
    module.exports = DiscountHelper;
}
