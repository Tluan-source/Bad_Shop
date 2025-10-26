-- Seed data for badminton-marketplace
-- Password for all accounts: 123456
-- Salt and hashed_password are pre-computed using BCrypt

-- ==================== USERS ====================
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

-- VENDOR accounts
MERGE INTO users AS target
USING (SELECT 'U_VENDOR1' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_VENDOR1', N'Nguyễn Văn A', 'vendor1@badminton.com', '0901234567',
            'b2c3d4e5-f6a7-8901-bcde-f12345678901',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'VENDOR', NULL, 50, 5000000.00);

MERGE INTO users AS target
USING (SELECT 'U_VENDOR2' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_VENDOR2', N'Trần Thị B', 'vendor2@badminton.com', '0902345678',
            'b2c3d4e5-f6a7-8901-bcde-f12345678902',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'VENDOR', NULL, 45, 3500000.00);

MERGE INTO users AS target
USING (SELECT 'U_VENDOR3' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_VENDOR3', N'Lê Văn C', 'vendor3@badminton.com', '0903456789',
            'b2c3d4e5-f6a7-8901-bcde-f12345678903',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'VENDOR', NULL, 40, 2800000.00);

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

-- USER accounts (customers)
MERGE INTO users AS target
USING (SELECT 'U_USER1' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_USER1', N'Phạm Thị D', 'user1@badminton.com', '0904567890',
            'd4e5f6a7-b8c9-0123-def1-234567890123',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'USER', NULL, 15, 500000.00);

MERGE INTO users AS target
USING (SELECT 'U_USER2' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_USER2', N'Võ Văn E', 'user2@badminton.com', '0905678901',
            'd4e5f6a7-b8c9-0123-def1-234567890124',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'USER', NULL, 20, 750000.00);

MERGE INTO users AS target
USING (SELECT 'U_USER3' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_USER3', N'Hoàng Thị F', 'user3@badminton.com', '0906789012',
            'd4e5f6a7-b8c9-0123-def1-234567890125',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'USER', NULL, 10, 300000.00);

-- ==================== CATEGORIES ====================
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

MERGE INTO categories AS target
USING (SELECT 'C5' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, is_active)
    VALUES ('C5', N'Quần Áo Thể Thao', 'quan-ao-the-thao', N'Trang phục thi đấu và tập luyện', 1);

-- ==================== COMMISSIONS ====================
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

-- ==================== STORES ====================
MERGE INTO stores AS target
USING (SELECT 'ST1' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST1', N'Cửa hàng Cầu Lông ABC', 'cua-hang-cau-long-abc', 
            N'Chuyên cung cấp vợt, giày và phụ kiện cầu lông chính hãng', 
            'U_VENDOR1', 'COM1', 1, 4.8, 100, 5000000.00);

MERGE INTO stores AS target
USING (SELECT 'ST2' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST2', N'Shop Thể Thao XYZ', 'shop-the-thao-xyz', 
            N'Đồ thể thao cao cấp, phụ kiện tập luyện đa dạng', 
            'U_VENDOR2', 'COM1', 1, 4.5, 85, 3500000.00);

MERGE INTO stores AS target
USING (SELECT 'ST3' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST3', N'Badminton Pro Shop', 'badminton-pro-shop', 
            N'Cửa hàng chuyên nghiệp, sản phẩm chất lượng cao', 
            'U_VENDOR3', 'COM2', 1, 4.7, 90, 2800000.00);

-- ==================== PRODUCTS ====================
-- Products for Store 1 (ST1)
MERGE INTO products AS target
USING (SELECT 'P1' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P1', N'Vợt Yonex Astrox 88D Pro', 'vot-yonex-astrox-88d-pro', 
            N'Vợt cầu lông chuyên nghiệp dành cho đánh công', 
            4500000.00, 4200000.00, 15, 45, 1, 1, 'C1', 'ST1', 4.9, 234, 'https://via.placeholder.com/300x300?text=Yonex+Astrox+88D');

MERGE INTO products AS target
USING (SELECT 'P2' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P2', N'Giày Victor SH-A920', 'giay-victor-sh-a920', 
            N'Giày cầu lông cao cấp, chống trơn trượt', 
            2800000.00, NULL, 25, 78, 1, 1, 'C3', 'ST1', 4.7, 189, 'https://via.placeholder.com/300x300?text=Victor+A920');

MERGE INTO products AS target
USING (SELECT 'P3' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P3', N'Quả cầu Yonex AS-50', 'qua-cau-yonex-as-50', 
            N'Quả cầu lông thi đấu chuyên nghiệp', 
            180000.00, 160000.00, 100, 298, 1, 1, 'C2', 'ST1', 4.6, 445, 'https://via.placeholder.com/300x300?text=Yonex+AS-50');

MERGE INTO products AS target
USING (SELECT 'P4' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P4', N'Túi vợt Yonex BA92012M', 'tui-vot-yonex-ba92012m', 
            N'Túi đựng vợt cao cấp, chống nước', 
            850000.00, NULL, 30, 67, 1, 1, 'C4', 'ST1', 4.5, 156, 'https://via.placeholder.com/300x300?text=Yonex+Bag');

-- Products for Store 2 (ST2)
MERGE INTO products AS target
USING (SELECT 'P5' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P5', N'Vợt Lining Axforce 100', 'vot-lining-axforce-100', 
            N'Vợt tấn công mạnh, thiết kế aerodynamic', 
            3800000.00, 3500000.00, 20, 56, 1, 1, 'C1', 'ST2', 4.8, 201, 'https://via.placeholder.com/300x300?text=Lining+Axforce');

MERGE INTO products AS target
USING (SELECT 'P6' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P6', N'Giày Lining AYZP001', 'giay-lining-ayzp001', 
            N'Giày cầu lông êm ái, bền bỉ', 
            2200000.00, 1980000.00, 18, 89, 1, 1, 'C3', 'ST2', 4.6, 167, 'https://via.placeholder.com/300x300?text=Lining+AYZP001');

MERGE INTO products AS target
USING (SELECT 'P7' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P7', N'Áo Lining Championship', 'ao-lining-championship', 
            N'Áo thi đấu chuyên nghiệp, thoáng mát', 
            450000.00, NULL, 50, 123, 1, 1, 'C5', 'ST2', 4.7, 278, 'https://via.placeholder.com/300x300?text=Lining+Shirt');

-- Products for Store 3 (ST3)
MERGE INTO products AS target
USING (SELECT 'P8' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P1', N'Vợt Yonex Astrox 100ZZ', 'yonex-astrox-100zz', N'Vợt cầu lông cao cấp dành cho vận động viên chuyên nghiệp', 5500000.00, 4990000.00, 15, 23, 1, 1, '["https://via.placeholder.com/500x500/FF0000/FFFFFF?text=Yonex+Astrox+100ZZ"]', 'C1', 'S1', NULL, 4.9, 156);

MERGE INTO products AS target
USING (SELECT 'P9' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P2', N'Vợt Victor Brave Sword 12', 'victor-brave-sword-12', N'Vợt tấn công nhanh, nhẹ và linh hoạt', 3800000.00, 3500000.00, 20, 18, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429166/vot-cau-long-victor-brave-sword-12-pro-chinh-hang_1737312826_bfpid0.webp"]', 'C1', 'S2', NULL, 4.7, 124);

MERGE INTO products AS target
USING (SELECT 'P10' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P3', N'Quả Cầu Yonex Aerosensa 50', 'yonex-aerosensa-50', N'Quả cầu lông thi đấu chất lượng cao', 180000.00, 165000.00, 200, 145, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429176/yonex-a50_uzyh7t.webp"]', 'C2', 'S1', NULL, 4.6, 89);

-- ==================== ORDERS ====================
-- Order 1 - Completed
MERGE INTO orders AS target
USING (SELECT 'ORD001' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P4', N'Giày Yonex Power Cushion 65Z2', 'yonex-pc-65z2', N'Giày cầu lông chuyên nghiệp với công nghệ đệm', 2200000.00, 1990000.00, 30, 12, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429176/yonex_power_cushion_65z2_vc6ta0.webp"]', 'C3', 'S1', NULL, 4.8, 67);

-- Order 2 - Processing
MERGE INTO orders AS target
USING (SELECT 'ORD002' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P5', N'Băng Cán Vợt Victor', 'victor-grip-tape', N'Băng quấn cán vợt chống trượt', 45000.00, 0.00, 500, 234, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429165/quan-can-vot-cau-long-gr200-1_d1hnru.webp"]', 'C4', 'S2', NULL, 4.5, 201);

-- Order 3 - Shipped
MERGE INTO orders AS target
USING (SELECT 'ORD003' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P6', N'Vợt Yonex Nanoflare 1000Z', 'yonex-nanoflare-1000z', N'Vợt siêu nhẹ cho tốc độ đánh nhanh', 6200000.00, 5890000.00, 10, 8, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429165/nanoflare_1000z_qcdj5t.png"]', 'C1', 'S1', NULL, 5.0, 92);

-- Order 4 - Delivered
MERGE INTO orders AS target
USING (SELECT 'ORD004' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P7', N'Quả Cầu Victor Gold Champion', 'victor-gold-champion', N'Quả cầu thi đấu quốc tế', 220000.00, 200000.00, 150, 67, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429165/victor-champion_rrbcu8.jpg"]', 'C2', 'S2', NULL, 4.7, 78);

-- Order 5 - Delivered
MERGE INTO orders AS target
USING (SELECT 'ORD005' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD005', 'U_USER2', 'ST2', 'COM1', N'456 Lê Văn Việt, Q.9, TP.HCM', '0905678901', 
            30000.00, 'DELIVERED', 0, 3530000.00, 3500000.00, 3325000.00, 175000.00, 0.00, DATEADD(day, -4, GETDATE()));

-- ==================== ORDER ITEMS ====================
-- Items for Order 1
MERGE INTO order_items AS target
USING (SELECT 'OI001' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, discount_amount)
    VALUES ('OI001', 'ORD001', 'P1', 1, 4200000.00, 0.00);

-- Items for Order 2
MERGE INTO order_items AS target
USING (SELECT 'OI002' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, discount_amount)
    VALUES ('OI002', 'ORD002', 'P6', 1, 1980000.00, 0.00);

-- Items for Order 3
MERGE INTO order_items AS target
USING (SELECT 'OI003' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, discount_amount)
    VALUES ('OI003', 'ORD003', 'P3', 10, 160000.00, 0.00);

MERGE INTO order_items AS target
USING (SELECT 'OI004' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, discount_amount)
    VALUES ('OI004', 'ORD003', 'P4', 1, 850000.00, 0.00);

-- Items for Order 4
MERGE INTO order_items AS target
USING (SELECT 'OI005' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, discount_amount)
    VALUES ('OI005', 'ORD004', 'P8', 1, 4900000.00, 0.00);

-- Items for Order 5
MERGE INTO order_items AS target
USING (SELECT 'OI006' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, discount_amount)
    VALUES ('OI006', 'ORD005', 'P5', 1, 3500000.00, 0.00);

-- ==================== VOUCHERS ====================
MERGE INTO vouchers AS target
USING (SELECT 'V1' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, code, description, discount_type, discount_value, max_discount, min_order_value, 
            quantity, usage_count, start_date, end_date, is_active)
    VALUES ('V1', 'WELCOME2024', N'Giảm giá cho khách hàng mới', 'PERCENTAGE', 15.00, 200000.00, 500000.00, 
            100, 65, DATEADD(day, -30, GETDATE()), DATEADD(day, 30, GETDATE()), 1);

MERGE INTO vouchers AS target
USING (SELECT 'V2' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, code, description, discount_type, discount_value, max_discount, min_order_value, 
            quantity, usage_count, start_date, end_date, is_active)
    VALUES ('V2', 'FREESHIP100', N'Miễn phí vận chuyển', 'FIXED', 100000.00, 100000.00, 300000.00, 
            500, 425, DATEADD(day, -20, GETDATE()), DATEADD(day, 40, GETDATE()), 1);

MERGE INTO vouchers AS target
USING (SELECT 'V3' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P8', N'Túi Vợt Yonex BAG92031W', 'yonex-bag-92031w', N'Túi đựng vợt cao cấp', 890000.00, 0.00, 40, 15, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429165/tui-cau-long-yonex-ba92031wex-trang-xanh-la-gia-cong_1703203791_hffuev.webp"]', 'C4', 'S1', NULL, 4.4, 45);

MERGE INTO products AS target
USING (SELECT 'P9' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P9', N'Vợt Li-Ning Turbo Charging 75D', 'lining-tc-75d', N'Vợt tấn công mạnh mẽ với công nghệ Dynamic-Optimum Frame', 4200000.00, 3890000.00, 18, 14, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429164/charing-75D_k0eqse.jpg"]', 'C1', 'S1', NULL, 4.6, 82);

MERGE INTO products AS target
USING (SELECT 'P10' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P10', N'Giày Victor A922', 'victor-a922', N'Giày cầu lông chuyên nghiệp với đế chống trượt', 1800000.00, 1650000.00, 25, 19, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429165/Victor-A922_cgv6ld.webp"]', 'C3', 'S2', NULL, 4.5, 73);

MERGE INTO products AS target
USING (SELECT 'P11' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P11', N'Vợt Mizuno Fortius Tour F', 'mizuno-fortius-tour-f', N'Vợt linh hoạt cho người chơi trung bình', 2800000.00, 0.00, 22, 9, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429165/Mizuno-fortius_idjg2t.webp"]', 'C1', 'S2', NULL, 4.3, 56);

MERGE INTO products AS target
USING (SELECT 'P12' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P12', N'Áo Cầu Lông Yonex 10274EX', 'yonex-shirt-10274ex', N'Áo thi đấu chuyên nghiệp, vải thoáng mát', 450000.00, 399000.00, 100, 54, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429163/ao-yonex-10274ex_myfi5r.webp"]', 'C4', 'S1', NULL, 4.7, 112);

MERGE INTO products AS target
USING (SELECT 'P13' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P13', N'Quần Cầu Lông Victor R-3096', 'victor-shorts-r3096', N'Quần thi đấu thoải mái, co giãn tốt', 380000.00, 340000.00, 80, 42, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429165/quan-victor-R-3096_mkgw68.webp"]', 'C4', 'S2', NULL, 4.5, 87);

MERGE INTO products AS target
USING (SELECT 'P14' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P14', N'Vợt Yonex Duora 10', 'yonex-duora-10', N'Vợt 2 mặt khác nhau cho phong cách linh hoạt', 4800000.00, 4490000.00, 12, 7, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429163/yonex-duora-10_dkrrkg.webp"]', 'C1', 'S1', NULL, 4.8, 64);

MERGE INTO products AS target
USING (SELECT 'P15' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P15', N'Dây Cước Yonex BG80', 'yonex-string-bg80', N'Dây vợt chuyên nghiệp độ bền cao', 280000.00, 0.00, 150, 89, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429164/day-cuoc-cang-vot-yonex-bg80-1_ffzqz0.webp"]', 'C4', 'S1', NULL, 4.6, 134);

MERGE INTO products AS target
USING (SELECT 'P16' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P16', N'Giày Li-Ning Saga Lite 3', 'lining-saga-lite-3', N'Giày nhẹ, êm ái cho chân', 2100000.00, 1890000.00, 28, 16, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429164/li-ning-saga-lite-3_ebrz6s.jpg"]', 'C3', 'S1', NULL, 4.7, 71);

MERGE INTO products AS target
USING (SELECT 'P17' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P17', N'Balo Cầu Lông Victor BR9008', 'victor-backpack-br9008', N'Balo đựng vợt và đồ tập luyện tiện lợi', 780000.00, 690000.00, 35, 21, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429164/balo-br900_gczyrg.webp"]', 'C4', 'S2', NULL, 4.4, 58);

MERGE INTO products AS target
USING (SELECT 'P18' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P18', N'Vợt Victor Thruster K 9900', 'victor-tk-9900', N'Vợt đánh nhanh cho người chơi tấn công', 5200000.00, 4890000.00, 8, 5, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429166/vot-cau-long-victor-tk-9900-1_oqqfek.webp"]', 'C1', 'S2', NULL, 4.9, 48);

MERGE INTO products AS target
USING (SELECT 'P19' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P19', N'Quả Cầu Li-Ning A+90', 'lining-a90', N'Quả cầu thi đấu chuyên nghiệp', 195000.00, 180000.00, 180, 98, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429164/cau-li-ning-A_90_dimxay.jpg]', 'C2', 'S1', NULL, 4.6, 105);

MERGE INTO products AS target
USING (SELECT 'P20' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P20', N'Găng Tay Cầu Lông Victor SP199', 'victor-glove-sp199', N'Găng tay chống trượt khi chơi', 250000.00, 0.00, 120, 67, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429164/gang_tay_cau_long_sp199_tkd4f2.avif"]', 'C4', 'S2', NULL, 4.3, 91);

MERGE INTO products AS target
USING (SELECT 'P21' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P21', N'Vợt Yonex Arcsaber 11 Pro', 'yonex-arcsaber-11-pro', N'Vợt cân bằng tốt giữa công thủ', 5800000.00, 5490000.00, 10, 6, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429175/yonex_arcsaber_11_pro_y924rr.png"]', 'C1', 'S1', NULL, 4.9, 55);

MERGE INTO products AS target
USING (SELECT 'P22' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P22', N'Giày Yonex Comfort Z2', 'yonex-comfort-z2', N'Giày êm ái cho người chơi nghiệp dư', 1600000.00, 1450000.00, 32, 18, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429165/giay-cau-long-yonex-shb-comfort-z2-vang-den-1_cdyo3g.webp"]', 'C3', 'S1', NULL, 4.5, 69);

MERGE INTO products AS target
USING (SELECT 'P23' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P23', N'Vợt Victor Jetspeed S 12', 'victor-js-s12', N'Vợt siêu nhẹ cho đòn đánh nhanh', 3600000.00, 3290000.00, 15, 11, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429166/vot-cau-long-victor-jetspeed-s12-m-chinh-hang-1_zneu1f.webp"]', 'C1', 'S2', NULL, 4.7, 62);

MERGE INTO products AS target
USING (SELECT 'P24' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P24', N'Tất Cầu Lông Yonex 1955', 'yonex-socks-1955', N'Tất chuyên dụng chống phồng rộp', 95000.00, 85000.00, 200, 134, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429164/li-ning-saga-lite-3_ebrz6s.jpg"]', 'C4', 'S1', NULL, 4.4, 167);

MERGE INTO products AS target
USING (SELECT 'P25' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P25', N'Vợt Li-Ning Windstorm 72', 'lining-windstorm-72', N'Vợt tốc độ cao, phù hợp đánh đôi', 3400000.00, 0.00, 20, 8, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429166/vot-cau-long-lining-windstom-72-or-4_iigr3j.webp"]', 'C1', 'S1', NULL, 4.5, 47);

-- =============================================
-- STYLES (Loại thuộc tính sản phẩm)
-- =============================================
MERGE INTO styles AS target
USING (SELECT 'ST001' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, category_ids, is_deleted)
    VALUES ('ST001', N'Màu sắc', '["C1","C3","C4"]', 0);

MERGE INTO styles AS target
USING (SELECT 'ST002' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, category_ids, is_deleted)
    VALUES ('ST002', N'Kích thước giày', '["C3"]', 0);

MERGE INTO styles AS target
USING (SELECT 'ST003' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, category_ids, is_deleted)
    VALUES ('ST003', N'Trọng lượng vợt', '["C1"]', 0);

MERGE INTO styles AS target
USING (SELECT 'ST004' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, category_ids, is_deleted)
    VALUES ('ST004', N'Độ căng dây', '["C1"]', 0);

-- =============================================
-- STYLE VALUES (Giá trị cụ thể)
-- =============================================
-- Màu sắc (ST001)
MERGE INTO style_values AS target
USING (SELECT 'SV001' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV001', N'Đỏ', 'ST001', 0);

MERGE INTO style_values AS target
USING (SELECT 'SV002' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV002', N'Xanh dương', 'ST001', 0);

MERGE INTO style_values AS target
USING (SELECT 'SV003' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV003', N'Vàng', 'ST001', 0);

MERGE INTO style_values AS target
USING (SELECT 'SV004' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV004', N'Đen', 'ST001', 0);

MERGE INTO style_values AS target
USING (SELECT 'SV005' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV005', N'Trắng', 'ST001', 0);

MERGE INTO style_values AS target
USING (SELECT 'SV006' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV006', N'Xanh lá', 'ST001', 0);

-- Kích thước giày (ST002)
MERGE INTO style_values AS target
USING (SELECT 'SV007' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV007', N'39', 'ST002', 0);

MERGE INTO style_values AS target
USING (SELECT 'SV008' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV008', N'40', 'ST002', 0);

MERGE INTO style_values AS target
USING (SELECT 'SV009' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV009', N'41', 'ST002', 0);

MERGE INTO style_values AS target
USING (SELECT 'SV010' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV010', N'42', 'ST002', 0);

MERGE INTO style_values AS target
USING (SELECT 'SV011' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV011', N'43', 'ST002', 0);

-- Trọng lượng vợt (ST003)
MERGE INTO style_values AS target
USING (SELECT 'SV012' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV012', N'5U (75-79g)', 'ST003', 0);

MERGE INTO style_values AS target
USING (SELECT 'SV013' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV013', N'4U (80-84g)', 'ST003', 0);

MERGE INTO style_values AS target
USING (SELECT 'SV014' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV014', N'3U (85-89g)', 'ST003', 0);

MERGE INTO style_values AS target
USING (SELECT 'SV015' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV015', N'2U (90-94g)', 'ST003', 0);

-- Độ căng dây (ST004)
MERGE INTO style_values AS target
USING (SELECT 'SV016' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV016', N'24-26 lbs', 'ST004', 0);

MERGE INTO style_values AS target
USING (SELECT 'SV017' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV017', N'27-29 lbs', 'ST004', 0);

MERGE INTO style_values AS target
USING (SELECT 'SV018' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, style_id, is_deleted)
    VALUES ('SV018', N'30-32 lbs', 'ST004', 0);

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
-- 
-- TỔNG SẢN PHẨM: 25 sản phẩm
-- - Vợt cầu lông: 10 sản phẩm (P1, P2, P6, P9, P11, P14, P18, P21, P23, P25)
-- - Quả cầu: 3 sản phẩm (P3, P7, P19)
-- - Giày: 4 sản phẩm (P4, P10, P16, P22)
-- - Phụ kiện: 8 sản phẩm (P5, P8, P12, P13, P15, P17, P20, P24)
-- =====================================================
