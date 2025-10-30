-- =============================================
-- DỮ LIỆU MẪU VOUCHERS - CHỈ PERCENTAGE
-- Logic mới: Voucher chỉ hỗ trợ giảm theo phần trăm
-- Kiểm tra theo TỔNG TẤT CẢ SHOP, áp dụng % cho TỪNG SHOP riêng biệt
-- =============================================

USE BadmintonMarketplace;
GO

-- Xóa dữ liệu cũ
DELETE FROM vouchers;

-- Vouchers hợp lệ - CHỈ PERCENTAGE VỚI MAXDISCOUNT
INSERT INTO vouchers (id, code, description, discount_type, discount_value, max_discount, min_order_value, quantity, usage_count, start_date, end_date, is_active, created_at, updated_at)
VALUES
-- 1. WELCOME10 - Giảm 10%, max 100k, đơn từ 200k
('VOUC001', 'WELCOME10', 'Khuyến mãi chào mừng - Giảm 10% tối đa 100k', 'PERCENTAGE', 10, 100000, 200000, 1000, 0, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1, GETDATE(), GETDATE()),

-- 2. SUMMER15 - Giảm 15%, max 200k, đơn từ 500k
('VOUC002', 'SUMMER15', 'Khuyến mãi mùa hè - Giảm 15% tối đa 200k', 'PERCENTAGE', 15, 200000, 500000, 500, 0, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1, GETDATE(), GETDATE()),

-- 3. MEGA20 - Giảm 20%, max 300k, đơn từ 1tr
('VOUC003', 'MEGA20', 'Mega Sale - Giảm 20% tối đa 300k', 'PERCENTAGE', 20, 300000, 1000000, 300, 0, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1, GETDATE(), GETDATE()),

-- 4. FLASH25 - Flash sale giảm 25%, max 500k, đơn từ 1.5tr
('VOUC004', 'FLASH25', 'Flash Sale - Giảm 25% tối đa 500k', 'PERCENTAGE', 25, 500000, 1500000, 200, 0, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1, GETDATE(), GETDATE()),

-- 5. VIP30 - Giảm 30%, max 1tr, đơn từ 3tr
('VOUC005', 'VIP30', 'Ưu đãi VIP - Giảm 30% tối đa 1tr', 'PERCENTAGE', 30, 1000000, 3000000, 100, 0, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1, GETDATE(), GETDATE()),

-- 6. SPRING12 - Giảm 12%, max 150k, đơn từ 300k
('VOUC006', 'SPRING12', 'Xuân mới - Giảm 12% tối đa 150k', 'PERCENTAGE', 12, 150000, 300000, 800, 0, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1, GETDATE(), GETDATE()),

-- 7. SUPER18 - Giảm 18%, max 250k, đơn từ 800k
('VOUC007', 'SUPER18', 'Siêu Sale - Giảm 18% tối đa 250k', 'PERCENTAGE', 18, 250000, 800000, 400, 0, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1, GETDATE(), GETDATE()),

-- 8. HAPPY8 - Giảm 8%, max 80k, không điều kiện
('VOUC008', 'HAPPY8', 'Happy Day - Giảm 8% tối đa 80k', 'PERCENTAGE', 8, 80000, 0, 1500, 0, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1, GETDATE(), GETDATE()),

-- 9. LUCKY7 - Giảm 7%, max 70k, đơn từ 150k
('VOUC009', 'LUCKY7', 'May mắn - Giảm 7% tối đa 70k', 'PERCENTAGE', 7, 70000, 150000, 2000, 0, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1, GETDATE(), GETDATE()),

-- 10. BIGDAY35 - Giảm 35%, max 2tr, đơn từ 5tr (VIP++)
('VOUC010', 'BIGDAY35', 'Ngày lớn - Giảm 35% tối đa 2tr', 'PERCENTAGE', 35, 2000000, 5000000, 50, 0, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1, GETDATE(), GETDATE()),

-- Vouchers để test lỗi
-- 11. EXPIRED10 - Đã hết hạn
('VOUC011', 'EXPIRED10', 'Voucher hết hạn', 'PERCENTAGE', 10, NULL, 100000, 100, 0, '2025-01-01 00:00:00', '2025-01-31 23:59:59', 1, GETDATE(), GETDATE()),

-- 12. FUTURE15 - Chưa bắt đầu
('VOUC012', 'FUTURE15', 'Voucher chưa bắt đầu', 'PERCENTAGE', 15, NULL, 200000, 100, 0, '2025-12-01 00:00:00', '2025-12-31 23:59:59', 1, GETDATE(), GETDATE()),

-- 13. SOLDOUT20 - Đã hết lượt
('VOUC013', 'SOLDOUT20', 'Voucher hết lượt', 'PERCENTAGE', 20, NULL, 300000, 10, 10, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1, GETDATE(), GETDATE());
