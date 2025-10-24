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
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P8', N'Vợt Victor Thruster K9900', 'vot-victor-thruster-k9900', 
            N'Vợt đánh công tốc độ cao', 
            5200000.00, 4900000.00, 10, 34, 1, 1, 'C1', 'ST3', 4.9, 198, 'https://via.placeholder.com/300x300?text=Victor+K9900');

MERGE INTO products AS target
USING (SELECT 'P9' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P9', N'Cước căng vợt Victor VBS-66', 'cuoc-cang-vot-victor-vbs-66', 
            N'Cước cầu lông bền, ổn định', 
            320000.00, 290000.00, 80, 145, 1, 1, 'C4', 'ST3', 4.5, 312, 'https://via.placeholder.com/300x300?text=Victor+String');

MERGE INTO products AS target
USING (SELECT 'P10' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P10', N'Quần cầu lông Victor R-3096', 'quan-cau-long-victor-r-3096', 
            N'Quần thi đấu thoải mái, co giãn tốt', 
            380000.00, NULL, 40, 98, 1, 1, 'C5', 'ST3', 4.6, 189, 'https://via.placeholder.com/300x300?text=Victor+Shorts');

-- ==================== ORDERS ====================
-- Order 1 - Completed
MERGE INTO orders AS target
USING (SELECT 'ORD001' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD001', 'U_USER1', 'ST1', 'COM1', N'123 Nguyễn Văn Cừ, Q.5, TP.HCM', '0904567890', 
            30000.00, 'DELIVERED', 0, 4230000.00, 4200000.00, 3990000.00, 210000.00, 0.00, DATEADD(day, -3, GETDATE()));

-- Order 2 - Processing
MERGE INTO orders AS target
USING (SELECT 'ORD002' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD002', 'U_USER2', 'ST2', 'COM1', N'456 Lê Văn Việt, Q.9, TP.HCM', '0905678901', 
            30000.00, 'PROCESSING', 0, 2010000.00, 1980000.00, 1881000.00, 99000.00, 0.00, DATEADD(day, -1, GETDATE()));

-- Order 3 - Shipped
MERGE INTO orders AS target
USING (SELECT 'ORD003' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD003', 'U_USER3', 'ST1', 'COM1', N'789 Võ Văn Ngân, Thủ Đức, TP.HCM', '0906789012', 
            50000.00, 'SHIPPED', 0, 1730000.00, 1680000.00, 1596000.00, 84000.00, 0.00, DATEADD(day, -2, GETDATE()));

-- Order 4 - Delivered
MERGE INTO orders AS target
USING (SELECT 'ORD004' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD004', 'U_USER1', 'ST3', 'COM2', N'123 Nguyễn Văn Cừ, Q.5, TP.HCM', '0904567890', 
            30000.00, 'DELIVERED', 0, 4930000.00, 4900000.00, 4753000.00, 147000.00, 0.00, DATEADD(day, -5, GETDATE()));

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
    INSERT (id, code, description, discount_type, discount_value, max_discount, min_order_value, 
            quantity, usage_count, start_date, end_date, is_active)
    VALUES ('V3', 'SUMMER2024', N'Khuyến mãi mùa hè', 'PERCENTAGE', 20.00, 500000.00, 1000000.00, 
            200, 200, DATEADD(day, -90, GETDATE()), DATEADD(day, -5, GETDATE()), 0);