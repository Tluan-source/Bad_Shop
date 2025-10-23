-- Seed data for badminton-marketplace
-- 4 users with password: 123456
-- Salt and hashed_password are pre-computed using BCrypt

-- ADMIN account
MERGE INTO users AS target
USING (SELECT 'U_ADMIN' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_ADMIN', N'Quản Trị Viên', 'admin@badminton.com', '0900000001', 
            'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'ADMIN', NULL, 100, 1000000.00);

-- VENDOR account
MERGE INTO users AS target
USING (SELECT 'U_VENDOR' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_VENDOR', N'Nhà Cung Cấp', 'vendor@badminton.com', '0900000002',
            'b2c3d4e5-f6a7-8901-bcde-f12345678901',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'VENDOR', NULL, 50, 500000.00);

-- SHIPPER account
MERGE INTO users AS target
USING (SELECT 'U_SHIPPER' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_SHIPPER', N'Nhân Viên Giao Hàng', 'shipper@badminton.com', '0900000003',
            'c3d4e5f6-a7b8-9012-cdef-123456789012',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'SHIPPER', NULL, 30, 300000.00);

-- USER account
MERGE INTO users AS target
USING (SELECT 'U_USER' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_USER', N'Người Dùng', 'user@badminton.com', '0900000004',
            'd4e5f6a7-b8c9-0123-def1-234567890123',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'USER', NULL, 10, 100000.00);

-- Categories
MERGE INTO categories AS target
USING (SELECT 'C1' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, is_active)
    VALUES ('C1', N'Vợt Cầu Lông', 'vot-cau-long', N'Các loại vợt cầu lông chuyên nghiệp', 1);

MERGE INTO categories AS target
USING (SELECT 'C2' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, is_active)
    VALUES ('C2', N'Quả Cầu', 'qua-cau', N'Quả cầu lông thi đấu và tập luyện', 1);

MERGE INTO categories AS target
USING (SELECT 'C3' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, is_active)
    VALUES ('C3', N'Giày Cầu Lông', 'giay-cau-long', N'Giày thể thao chuyên dụng', 1);

MERGE INTO categories AS target
USING (SELECT 'C4' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, is_active)
    VALUES ('C4', N'Phụ Kiện', 'phu-kien', N'Phụ kiện và trang thiết bị cầu lông', 1);

-- Commission (hoa hồng cho các store)
MERGE INTO commissions AS target
USING (SELECT 'COM1' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, fee_percent, description)
    VALUES ('COM1', N'Hoa Hồng Chuẩn', 5.00, N'Phí hoa hồng 5% cho mỗi đơn hàng');

MERGE INTO commissions AS target
USING (SELECT 'COM2' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, fee_percent, description)
    VALUES ('COM2', N'Hoa Hồng VIP', 3.00, N'Phí hoa hồng 3% cho đối tác VIP');

-- Shipping Providers (đơn vị vận chuyển)
MERGE INTO shipping_providers AS target
USING (SELECT 'SP1' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, description, shipping_fee, is_active)
    VALUES ('SP1', N'Giao Hàng Nhanh', N'Giao hàng nội thành trong 24h', 30000.00, 1);

MERGE INTO shipping_providers AS target
USING (SELECT 'SP2' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, description, shipping_fee, is_active)
    VALUES ('SP2', N'Giao Hàng Tiết Kiệm', N'Giao hàng toàn quốc 3-5 ngày', 25000.00, 1);

MERGE INTO shipping_providers AS target
USING (SELECT 'SP3' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, description, shipping_fee, is_active)
    VALUES ('SP3', N'J&T Express', N'Dịch vụ chuyển phát nhanh', 28000.00, 1);

MERGE INTO shipping_providers AS target
USING (SELECT 'SP4' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, description, shipping_fee, is_active)
    VALUES ('SP4', N'Viettel Post', N'Dịch vụ bưu chính', 20000.00, 1);

-- Vouchers (mã giảm giá)
MERGE INTO vouchers AS target
USING (SELECT 'V1' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, code, discount_amount, discount_percent, min_order_amount, start_at, end_at, is_active)
    VALUES ('V1', 'WELCOME2024', 50000.00, 0.00, 200000.00, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1);

MERGE INTO vouchers AS target
USING (SELECT 'V2' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, code, discount_amount, discount_percent, min_order_amount, start_at, end_at, is_active)
    VALUES ('V2', 'SALE10', 0.00, 10.00, 500000.00, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1);

MERGE INTO vouchers AS target
USING (SELECT 'V3' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, code, discount_amount, discount_percent, min_order_amount, start_at, end_at, is_active)
    VALUES ('V3', 'FREESHIP', 30000.00, 0.00, 300000.00, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1);

MERGE INTO vouchers AS target
USING (SELECT 'V4' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, code, discount_amount, discount_percent, min_order_amount, start_at, end_at, is_active)
    VALUES ('V4', 'VIP20', 0.00, 20.00, 1000000.00, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1);

-- Stores
MERGE INTO stores AS target
USING (SELECT 'S1' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, bio, slug, owner_id, commission_id, is_active, featured_images, point, rating, e_wallet)
    VALUES ('S1', N'Cửa Hàng Yonex Việt Nam', N'Chuyên phân phối vợt Yonex chính hãng', 'yonex-vn', 'U_VENDOR', 'COM1', 1, NULL, 100, 4.8, 5000000.00);

MERGE INTO stores AS target
USING (SELECT 'S2' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, bio, slug, owner_id, commission_id, is_active, featured_images, point, rating, e_wallet)
    VALUES ('S2', N'Victor Sports Store', N'Đại lý chính thức Victor tại Việt Nam', 'victor-sports', 'U_VENDOR', 'COM2', 1, NULL, 80, 4.5, 3000000.00);

-- Products
MERGE INTO products AS target
USING (SELECT 'P1' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P1', N'Vợt Yonex Astrox 100ZZ', 'yonex-astrox-100zz', N'Vợt cầu lông cao cấp dành cho vận động viên chuyên nghiệp', 5500000.00, 4990000.00, 15, 23, 1, 1, NULL, 'C1', 'S1', NULL, 4.9, 156);

MERGE INTO products AS target
USING (SELECT 'P2' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P2', N'Vợt Victor Brave Sword 12', 'victor-brave-sword-12', N'Vợt tấn công nhanh, nhẹ và linh hoạt', 3800000.00, 3500000.00, 20, 18, 1, 1, NULL, 'C1', 'S2', NULL, 4.7, 124);

MERGE INTO products AS target
USING (SELECT 'P3' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P3', N'Quả Cầu Yonex Aerosensa 50', 'yonex-aerosensa-50', N'Quả cầu lông thi đấu chất lượng cao', 180000.00, 165000.00, 200, 145, 1, 1, NULL, 'C2', 'S1', NULL, 4.6, 89);

MERGE INTO products AS target
USING (SELECT 'P4' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P4', N'Giày Yonex Power Cushion 65Z2', 'yonex-pc-65z2', N'Giày cầu lông chuyên nghiệp với công nghệ đệm', 2200000.00, 1990000.00, 30, 12, 1, 1, NULL, 'C3', 'S1', NULL, 4.8, 67);

MERGE INTO products AS target
USING (SELECT 'P5' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P5', N'Băng Cán Vợt Victor', 'victor-grip-tape', N'Băng quấn cán vợt chống trượt', 45000.00, 0.00, 500, 234, 1, 1, NULL, 'C4', 'S2', NULL, 4.5, 201);

MERGE INTO products AS target
USING (SELECT 'P6' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P6', N'Vợt Yonex Nanoflare 1000Z', 'yonex-nanoflare-1000z', N'Vợt siêu nhẹ cho tốc độ đánh nhanh', 6200000.00, 5890000.00, 10, 8, 1, 1, NULL, 'C1', 'S1', NULL, 5.0, 92);

MERGE INTO products AS target
USING (SELECT 'P7' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P7', N'Quả Cầu Victor Gold Champion', 'victor-gold-champion', N'Quả cầu thi đấu quốc tế', 220000.00, 200000.00, 150, 67, 1, 1, NULL, 'C2', 'S2', NULL, 4.7, 78);

MERGE INTO products AS target
USING (SELECT 'P8' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P8', N'Túi Vợt Yonex BAG92031W', 'yonex-bag-92031w', N'Túi đựng vợt cao cấp', 890000.00, 0.00, 40, 15, 1, 1, NULL, 'C4', 'S1', NULL, 4.4, 45);

-- =====================================================
-- HƯỚNG DẪN ĐĂNG NHẬP:
-- =====================================================
-- ADMIN:   admin@badminton.com    / 123456
-- VENDOR:  vendor@badminton.com   / 123456
-- SHIPPER: shipper@badminton.com  / 123456
-- USER:    user@badminton.com     / 123456
-- 
-- MÃ GIẢM GIÁ CÓ SẴN:
-- - WELCOME2024: Giảm 50k cho đơn từ 200k
-- - SALE10: Giảm 10% cho đơn từ 500k
-- - FREESHIP: Giảm 30k phí ship cho đơn từ 300k
-- - VIP20: Giảm 20% cho đơn từ 1 triệu
-- =====================================================