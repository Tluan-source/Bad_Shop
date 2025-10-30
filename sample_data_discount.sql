-- ============================================
-- DỮ LIỆU MẪU CHO HỆ THỐNG GIẢM GIÁ
-- Vouchers (Admin) & Promotions (Vendor)
-- ============================================

USE BadmintonMarketplace;
GO

-- ============================================
-- PHẦN 1: VOUCHERS (Mã giảm giá toàn sàn)
-- ============================================

PRINT 'Inserting sample vouchers...';

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



PRINT 'Vouchers inserted successfully!';
PRINT '---';

-- ============================================
-- PHẦN 2: PROMOTIONS (Khuyến mãi của Shop)
-- ============================================

PRINT 'Inserting sample promotions...';

INSERT INTO promotions (id, store_id, name, description, discount_type, discount_value, max_discount, min_order_amount, quantity, usage_count, start_date, end_date, is_active, applies_to, created_at, updated_at)
VALUES
-- ===== PROMOTIONS CHO ST001 - Cửa hàng ProAce (Chuyên vợt cao cấp) =====
-- Promotion 1: Giảm 10% vợt cao cấp
('PROMO_ST001_001', 'ST001', N'Sale vợt ProAce cao cấp', N'Giảm 10% cho tất cả vợt ProAce - Chuyên nghiệp hàng đầu', 'PERCENTAGE', 10.00, 500000.00, 2000000.00, 500, 0,
 DATEADD(day, -15, GETDATE()), DATEADD(day, 30, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion 2: Flash sale cuối tuần
('PROMO_ST001_002', 'ST001', N'Flash Sale cuối tuần', N'Giảm 15% cho đơn hàng từ 3 triệu - Áp dụng tất cả sản phẩm', 'PERCENTAGE', 15.00, 600000.00, 3000000.00, 200, 0,
 DATEADD(day, -2, GETDATE()), DATEADD(day, 5, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion 3: Giảm cố định cho đơn lớn
('PROMO_ST001_003', 'ST001', N'Mua vợt ProAce giảm ngay 300k', N'Giảm 300k cho đơn hàng từ 4 triệu', 'FIXED_AMOUNT', 300000.00, 300000.00, 4000000.00, 300, 0,
 DATEADD(day, -10, GETDATE()), DATEADD(day, 25, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion 4: Ưu đãi khách VIP
('PROMO_ST001_004', 'ST001', N'Ưu đãi khách hàng VIP', N'Giảm 12% cho đơn từ 5 triệu - Dành cho khách thân thiết', 'PERCENTAGE', 12.00, 700000.00, 5000000.00, 100, 0,
 DATEADD(day, -20, GETDATE()), DATEADD(day, 40, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- ===== PROMOTIONS CHO ST002 - Cửa hàng Yonex (Phân phối chính hãng Yonex) =====
-- Promotion 1: Sale Yonex chính hãng
('PROMO_ST002_001', 'ST002', N'Sale Yonex chính hãng 10%', N'Giảm 10% tất cả sản phẩm Yonex - Chính hãng 100%', 'PERCENTAGE', 10.00, 400000.00, 1500000.00, 800, 0,
 DATEADD(day, -18, GETDATE()), DATEADD(day, 35, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion 2: Combo vợt + phụ kiện
('PROMO_ST002_002', 'ST002', N'Combo Yonex - Giảm 200k', N'Mua vợt + phụ kiện Yonex giảm ngay 200k', 'FIXED_AMOUNT', 200000.00, 200000.00, 2500000.00, 400, 0,
 DATEADD(day, -12, GETDATE()), DATEADD(day, 28, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion 3: Flash sale Yonex
('PROMO_ST002_003', 'ST002', N'Flash Sale Yonex 15%', N'Giảm 15% cho đơn từ 3 triệu - Chỉ 3 ngày', 'PERCENTAGE', 15.00, 500000.00, 3000000.00, 150, 0,
 DATEADD(day, -1, GETDATE()), DATEADD(day, 2, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion 4: Miễn phí ship
('PROMO_ST002_004', 'ST002', N'Miễn phí vận chuyển', N'Free ship toàn quốc cho đơn từ 500k', 'FREE_SHIPPING', 35000.00, 35000.00, 500000.00, 999999, 0,
 DATEADD(day, -25, GETDATE()), DATEADD(day, 60, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion 5: Sale túi vợt Yonex
('PROMO_ST002_005', 'ST002', N'Sale túi vợt Yonex 20%', N'Giảm 20% cho tất cả túi vợt Yonex cao cấp', 'PERCENTAGE', 20.00, 600000.00, 2000000.00, 250, 0,
 DATEADD(day, -8, GETDATE()), DATEADD(day, 22, GETDATE()), 1, 'SPECIFIC_PRODUCTS', GETDATE(), GETDATE()),

-- ===== PROMOTIONS CHO ST003 - Cửa hàng Lining (Phụ kiện, giày cầu lông) =====
-- Promotion 1: Sale giày Lining
('PROMO_ST003_001', 'ST003', N'Sale giày Lining 15%', N'Giảm 15% cho tất cả giày cầu lông Lining chính hãng', 'PERCENTAGE', 15.00, 250000.00, 800000.00, 600, 0,
 DATEADD(day, -14, GETDATE()), DATEADD(day, 30, GETDATE()), 1, 'SPECIFIC_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion 2: Ưu đãi vợt Lining
('PROMO_ST003_002', 'ST003', N'Mua vợt Lining giảm ngay 150k', N'Giảm 150k cho đơn vợt Lining từ 2 triệu', 'FIXED_AMOUNT', 150000.00, 150000.00, 2000000.00, 350, 0,
 DATEADD(day, -10, GETDATE()), DATEADD(day, 25, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion 3: Flash sale cuối tuần
('PROMO_ST003_003', 'ST003', N'Flash Sale cuối tuần 18%', N'Giảm 18% cho tất cả sản phẩm - Chỉ 2 ngày', 'PERCENTAGE', 18.00, 350000.00, 1200000.00, 100, 0,
 DATEADD(day, -2, GETDATE()), DATEADD(day, 1, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion 4: Mua nhiều giảm nhiều
('PROMO_ST003_004', 'ST003', N'Mua nhiều giảm nhiều', N'Giảm 20% cho đơn từ 3 triệu trở lên', 'PERCENTAGE', 20.00, 600000.00, 3000000.00, 450, 0,
 DATEADD(day, -20, GETDATE()), DATEADD(day, 40, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion 5: Free ship
('PROMO_ST003_005', 'ST003', N'Miễn phí giao hàng', N'Freeship cho đơn từ 300k', 'FREE_SHIPPING', 25000.00, 25000.00, 300000.00, 999999, 0,
 DATEADD(day, -15, GETDATE()), DATEADD(day, 50, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- ===== PROMOTIONS CHO ST004 - Cửa hàng Victor (Vợt, túi, giày Victor chính hãng) =====
-- Promotion 1: Sale Victor chính hãng
('PROMO_ST004_001', 'ST004', N'Sale Victor chính hãng 12%', N'Giảm 12% cho tất cả sản phẩm Victor - Nhập khẩu chính hãng', 'PERCENTAGE', 12.00, 350000.00, 1500000.00, 700, 0,
 DATEADD(day, -16, GETDATE()), DATEADD(day, 32, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion 2: Combo vợt Victor
('PROMO_ST004_002', 'ST004', N'Combo vợt Victor giảm 250k', N'Mua vợt Victor kèm phụ kiện giảm 250k', 'FIXED_AMOUNT', 250000.00, 250000.00, 2800000.00, 300, 0,
 DATEADD(day, -12, GETDATE()), DATEADD(day, 28, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion 3: Sale giày Victor
('PROMO_ST004_003', 'ST004', N'Sale giày Victor 20%', N'Giảm 20% cho tất cả giày Victor chuyên dụng', 'PERCENTAGE', 20.00, 300000.00, 1000000.00, 400, 0,
 DATEADD(day, -8, GETDATE()), DATEADD(day, 20, GETDATE()), 1, 'SPECIFIC_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion 4: Ưu đãi đặc biệt
('PROMO_ST004_004', 'ST004', N'Ưu đãi đặc biệt 15%', N'Giảm 15% cho đơn từ 3.5 triệu', 'PERCENTAGE', 15.00, 550000.00, 3500000.00, 200, 0,
 DATEADD(day, -18, GETDATE()), DATEADD(day, 35, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion 5: Flash sale Victor
('PROMO_ST004_005', 'ST004', N'Flash Sale Victor 25%', N'Sale sốc giảm 25% - Chỉ 48h', 'PERCENTAGE', 25.00, 700000.00, 4000000.00, 80, 0,
 DATEADD(day, -3, GETDATE()), DATEADD(day, 1, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- ===== PROMOTIONS ĐỂ TEST (Các trường hợp đặc biệt) =====
-- Promotion đã hết hạn
('PROMO_ST001_EXPIRED', 'ST001', N'Khuyến mãi đã kết thúc', N'Promotion đã hết hạn - Để test validation', 'PERCENTAGE', 10.00, 100000.00, 500000.00, 100, 0,
 DATEADD(day, -60, GETDATE()), DATEADD(day, -10, GETDATE()), 0, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion chưa bắt đầu
('PROMO_ST002_FUTURE', 'ST002', N'Khuyến mãi sắp diễn ra', N'Sẽ bắt đầu vào tuần sau - Để test validation', 'PERCENTAGE', 15.00, 200000.00, 800000.00, 200, 0,
 DATEADD(day, 7, GETDATE()), DATEADD(day, 30, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion không hoạt động
('PROMO_ST003_INACTIVE', 'ST003', N'Khuyến mãi tạm dừng', N'Đã tạm dừng hoạt động - Để test validation', 'PERCENTAGE', 20.00, 300000.00, 1000000.00, 150, 0,
 DATEADD(day, -10, GETDATE()), DATEADD(day, 20, GETDATE()), 0, 'ALL_PRODUCTS', GETDATE(), GETDATE()),

-- Promotion đã hết lượt
('PROMO_ST004_SOLDOUT', 'ST004', N'Khuyến mãi đã hết lượt', N'Đã hết lượt sử dụng - Để test validation', 'PERCENTAGE', 25.00, 400000.00, 1500000.00, 50, 50,
 DATEADD(day, -5, GETDATE()), DATEADD(day, 15, GETDATE()), 1, 'ALL_PRODUCTS', GETDATE(), GETDATE());

PRINT 'Promotions inserted successfully!';
PRINT '---';

-- ============================================
-- PHẦN 3: THỐNG KÊ DỮ LIỆU
-- ============================================

PRINT '';
PRINT '=== THỐNG KÊ DỮ LIỆU ===';
PRINT '';

-- Đếm số voucher
DECLARE @TotalVouchers INT;
DECLARE @ActiveVouchers INT;
DECLARE @AvailableVouchers INT;

SELECT @TotalVouchers = COUNT(*) FROM vouchers;
SELECT @ActiveVouchers = COUNT(*) FROM vouchers WHERE is_active = 1;
SELECT @AvailableVouchers = COUNT(*) FROM vouchers 
WHERE is_active = 1 AND end_date > GETDATE() AND start_date <= GETDATE() AND usage_count < quantity;

PRINT 'VOUCHERS:';
PRINT '  - Tổng số vouchers: ' + CAST(@TotalVouchers AS VARCHAR);
PRINT '  - Vouchers đang active: ' + CAST(@ActiveVouchers AS VARCHAR);
PRINT '  - Vouchers khả dụng: ' + CAST(@AvailableVouchers AS VARCHAR);
PRINT '';

-- Đếm số promotion
DECLARE @TotalPromotions INT;
DECLARE @ActivePromotions INT;
DECLARE @AvailablePromotions INT;

SELECT @TotalPromotions = COUNT(*) FROM promotions;
SELECT @ActivePromotions = COUNT(*) FROM promotions WHERE is_active = 1;
SELECT @AvailablePromotions = COUNT(*) FROM promotions 
WHERE is_active = 1 AND end_date > GETDATE() AND start_date <= GETDATE();

PRINT 'PROMOTIONS:';
PRINT '  - Tổng số promotions: ' + CAST(@TotalPromotions AS VARCHAR);
PRINT '  - Promotions đang active: ' + CAST(@ActivePromotions AS VARCHAR);
PRINT '  - Promotions khả dụng: ' + CAST(@AvailablePromotions AS VARCHAR);
PRINT '';

-- Hiển thị vouchers khả dụng
PRINT '=== VOUCHERS KHẢ DỤNG ===';
SELECT 
    code AS 'Mã Voucher',
    discount_type AS 'Loại',
    discount_value AS 'Giá trị',
    max_discount AS 'Giảm tối đa',
    min_order_value AS 'Đơn tối thiểu',
    (quantity - usage_count) AS 'Còn lại',
    CONVERT(VARCHAR, end_date, 103) AS 'Hết hạn'
FROM vouchers
WHERE is_active = 1 AND end_date > GETDATE() AND start_date <= GETDATE() AND usage_count < quantity
ORDER BY discount_value DESC;

PRINT '';
PRINT '=== PROMOTIONS THEO SHOP ===';
SELECT 
    store_id AS 'Shop',
    name AS 'Tên khuyến mãi',
    discount_type AS 'Loại',
    discount_value AS 'Giá trị',
    min_order_amount AS 'Đơn tối thiểu',
    CONVERT(VARCHAR, end_date, 103) AS 'Hết hạn'
FROM promotions
WHERE is_active = 1 AND end_date > GETDATE() AND start_date <= GETDATE()
ORDER BY store_id, discount_value DESC;

PRINT '';
PRINT '=== DỮ LIỆU MẪU ĐÃ ĐƯỢC TẠO THÀNH CÔNG ===';
PRINT 'Bạn có thể test với các mã sau:';
PRINT '';
PRINT 'VOUCHERS HỢP LỆ:';
PRINT '  - WELCOME10  : Giảm 10% (max 100k, đơn từ 200k)';
PRINT '  - SUMMER15   : Giảm 15% (max 200k, đơn từ 500k)';
PRINT '  - MEGA20     : Giảm 20% (max 300k, đơn từ 1tr)';
PRINT '  - FREESHIP50 : Giảm 50k (đơn từ 150k)';
PRINT '  - SAVE100K   : Giảm 100k (đơn từ 800k)';
PRINT '  - FLASH25    : Giảm 25% flash sale (max 500k, đơn từ 1.5tr)';
PRINT '';
PRINT 'VOUCHERS ĐỂ TEST LỖI:';
PRINT '  - EXPIRED10  : Đã hết hạn';
PRINT '  - FUTURE15   : Chưa bắt đầu';
PRINT '  - SOLDOUT20  : Đã hết lượt sử dụng';
PRINT '';

GO
