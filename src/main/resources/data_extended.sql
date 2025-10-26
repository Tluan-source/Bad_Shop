-- Extended seed data - Add more vendors, stores, products, and orders
-- This file extends the existing data.sql

-- ==================== ADDITIONAL VENDORS ====================
MERGE INTO users AS target
USING (SELECT 'U_VENDOR4' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_VENDOR4', N'Phạm Minh Đức', 'vendor4@badminton.com', '0907123456',
            'b2c3d4e5-f6a7-8901-bcde-f12345678904',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'VENDOR', NULL, 55, 4200000.00);

MERGE INTO users AS target
USING (SELECT 'U_VENDOR5' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_VENDOR5', N'Đặng Thu Hương', 'vendor5@badminton.com', '0908234567',
            'b2c3d4e5-f6a7-8901-bcde-f12345678905',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'VENDOR', NULL, 48, 3800000.00);

-- ==================== ADDITIONAL CUSTOMERS ====================
MERGE INTO users AS target
USING (SELECT 'U_USER4' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_USER4', N'Nguyễn Hoàng Giang', 'user4@badminton.com', '0909345678',
            'd4e5f6a7-b8c9-0123-def1-234567890126',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'USER', NULL, 25, 1200000.00);

MERGE INTO users AS target
USING (SELECT 'U_USER5' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_USER5', N'Trần Minh Hòa', 'user5@badminton.com', '0910456789',
            'd4e5f6a7-b8c9-0123-def1-234567890127',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'USER', NULL, 18, 850000.00);

-- ==================== ADDITIONAL STORES ====================
MERGE INTO stores AS target
USING (SELECT 'ST4' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST4', N'Sport Excellence', 'sport-excellence', 
            N'Cửa hàng chuyên cung cấp trang thiết bị thể thao cao cấp từ các thương hiệu nổi tiếng', 
            'U_VENDOR4', 'COM1', 1, 4.9, 120, 6500000.00);

MERGE INTO stores AS target
USING (SELECT 'ST5' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST5', N'Cầu Lông Champion', 'cau-long-champion', 
            N'Chuyên về vợt và giày thi đấu chuyên nghiệp, phục vụ các VĐV', 
            'U_VENDOR5', 'COM2', 1, 4.6, 95, 4100000.00);

MERGE INTO stores AS target
USING (SELECT 'ST6' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST6', N'Elite Sports Center', 'elite-sports-center', 
            N'Trung tâm thể thao với đầy đủ thiết bị tập luyện và thi đấu', 
            'U_ADMIN', 'COM1', 1, 4.7, 110, 5200000.00);

MERGE INTO stores AS target
USING (SELECT 'ST7' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST7', N'Thể Thao Hoàng Gia', 'the-thao-hoang-gia', 
            N'Thương hiệu uy tín với hơn 10 năm kinh nghiệm', 
            'U_ADMIN', 'COM1', 1, 4.8, 105, 4800000.00);

MERGE INTO stores AS target
USING (SELECT 'ST8' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST8', N'Pro Badminton Store', 'pro-badminton-store', 
            N'Chuyên phân phối đồ cầu lông chính hãng giá tốt', 
            'U_VENDOR1', 'COM1', 1, 4.5, 88, 3600000.00);

-- ==================== ADDITIONAL PRODUCTS (20 products) ====================
-- Store 4 Products
MERGE INTO products AS target
USING (SELECT 'P11' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P11', N'Vợt Yonex Nanoflare 1000Z', 'vot-yonex-nanoflare-1000z', 
            N'Vợt cầu lông siêu nhẹ, tốc độ đánh nhanh', 
            5800000.00, 5500000.00, 12, 28, 1, 1, 'C1', 'ST4', 4.9, 167, 'https://via.placeholder.com/300x300?text=Yonex+Nanoflare');

MERGE INTO products AS target
USING (SELECT 'P12' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P12', N'Giày Yonex Power Cushion 65Z', 'giay-yonex-power-cushion-65z', 
            N'Giày thi đấu cao cấp với công nghệ đệm', 
            3200000.00, 2950000.00, 20, 65, 1, 1, 'C3', 'ST4', 4.8, 198, 'https://via.placeholder.com/300x300?text=Yonex+65Z');

MERGE INTO products AS target
USING (SELECT 'P13' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P13', N'Quả cầu Lining A+90', 'qua-cau-lining-a-90', 
            N'Quả cầu thi đấu quốc tế, độ bền cao', 
            220000.00, 200000.00, 150, 256, 1, 1, 'C2', 'ST4', 4.7, 389, 'https://via.placeholder.com/300x300?text=Lining+A90');

MERGE INTO products AS target
USING (SELECT 'P14' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P14', N'Băng cổ tay Yonex AC489', 'bang-co-tay-yonex-ac489', 
            N'Băng thấm mồ hôi cao cấp', 
            120000.00, NULL, 200, 432, 1, 1, 'C4', 'ST4', 4.5, 567, 'https://via.placeholder.com/300x300?text=Yonex+Band');

-- Store 5 Products
MERGE INTO products AS target
USING (SELECT 'P15' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P15', N'Vợt Victor Auraspeed 90K', 'vot-victor-auraspeed-90k', 
            N'Vợt tốc độ cao, phù hợp đánh đôi', 
            4200000.00, 3900000.00, 15, 42, 1, 1, 'C1', 'ST5', 4.8, 223, 'https://via.placeholder.com/300x300?text=Victor+90K');

MERGE INTO products AS target
USING (SELECT 'P16' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P16', N'Giày Mizuno Wave Fang Pro', 'giay-mizuno-wave-fang-pro', 
            N'Giày chuyên nghiệp từ Nhật Bản', 
            2900000.00, NULL, 18, 53, 1, 1, 'C3', 'ST5', 4.7, 145, 'https://via.placeholder.com/300x300?text=Mizuno+Pro');

MERGE INTO products AS target
USING (SELECT 'P17' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P17', N'Áo Victor T-90000', 'ao-victor-t-90000', 
            N'Áo thi đấu cao cấp, thoát nhiệt tốt', 
            520000.00, 480000.00, 45, 167, 1, 1, 'C5', 'ST5', 4.6, 234, 'https://via.placeholder.com/300x300?text=Victor+Shirt');

MERGE INTO products AS target
USING (SELECT 'P18' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P18', N'Bao lưới cầu lông Yonex AC544', 'bao-luoi-cau-long-yonex-ac544', 
            N'Bao lưới chống va đập', 
            650000.00, 590000.00, 35, 89, 1, 1, 'C4', 'ST5', 4.5, 178, 'https://via.placeholder.com/300x300?text=Yonex+Net');

-- Store 6 Products
MERGE INTO products AS target
USING (SELECT 'P19' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P19', N'Vợt Lining Windstorm 78', 'vot-lining-windstorm-78', 
            N'Vợt kiểm soát tốt, phù hợp mọi trình độ', 
            3200000.00, 2980000.00, 22, 71, 1, 1, 'C1', 'ST6', 4.7, 267, 'https://via.placeholder.com/300x300?text=Lining+78');

MERGE INTO products AS target
USING (SELECT 'P20' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P20', N'Giày Yonex Aerus Z', 'giay-yonex-aerus-z', 
            N'Giày nhẹ nhất thế giới, 270g', 
            3500000.00, NULL, 14, 38, 1, 1, 'C3', 'ST6', 4.9, 189, 'https://via.placeholder.com/300x300?text=Yonex+Aerus');

MERGE INTO products AS target
USING (SELECT 'P21' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P21', N'Quần Victor R-75204', 'quan-victor-r-75204', 
            N'Quần thi đấu chuyên nghiệp', 
            420000.00, 380000.00, 50, 145, 1, 1, 'C5', 'ST6', 4.6, 223, 'https://via.placeholder.com/300x300?text=Victor+Pants');

MERGE INTO products AS target
USING (SELECT 'P22' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P22', N'Grip Yonex AC102EX', 'grip-yonex-ac102ex', 
            N'Quấn cán vợt cao cấp, chống trơn', 
            80000.00, NULL, 300, 678, 1, 1, 'C4', 'ST6', 4.7, 890, 'https://via.placeholder.com/300x300?text=Yonex+Grip');

-- Store 7 Products
MERGE INTO products AS target
USING (SELECT 'P23' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P23', N'Vợt Apacs Feather Weight 55', 'vot-apacs-feather-weight-55', 
            N'Vợt nhẹ, giá tốt cho người mới', 
            1200000.00, 1050000.00, 30, 98, 1, 1, 'C1', 'ST7', 4.4, 234, 'https://via.placeholder.com/300x300?text=Apacs+55');

MERGE INTO products AS target
USING (SELECT 'P24' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P24', N'Giày Lining AYTQ003', 'giay-lining-aytq003', 
            N'Giày tập luyện thoải mái', 
            1800000.00, 1650000.00, 25, 112, 1, 1, 'C3', 'ST7', 4.5, 178, 'https://via.placeholder.com/300x300?text=Lining+AYTQ');

MERGE INTO products AS target
USING (SELECT 'P25' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P25', N'Áo Yonex 20649', 'ao-yonex-20649', 
            N'Áo tập luyện năng động', 
            380000.00, NULL, 60, 189, 1, 1, 'C5', 'ST7', 4.6, 267, 'https://via.placeholder.com/300x300?text=Yonex+20649');

MERGE INTO products AS target
USING (SELECT 'P26' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P26', N'Túi đựng giày Yonex 4512', 'tui-dung-giay-yonex-4512', 
            N'Túi đựng giày tiện lợi', 
            280000.00, 250000.00, 45, 234, 1, 1, 'C4', 'ST7', 4.5, 345, 'https://via.placeholder.com/300x300?text=Yonex+Shoe+Bag');

-- Store 8 Products
MERGE INTO products AS target
USING (SELECT 'P27' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P27', N'Vợt Yonex Voltric Z-Force II', 'vot-yonex-voltric-z-force-ii', 
            N'Vợt công thủ toàn diện', 
            4800000.00, 4500000.00, 10, 35, 1, 1, 'C1', 'ST8', 4.8, 198, 'https://via.placeholder.com/300x300?text=Yonex+ZForce');

MERGE INTO products AS target
USING (SELECT 'P28' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P28', N'Quả cầu Victor Master Ace', 'qua-cau-victor-master-ace', 
            N'Quả cầu thi đấu chuẩn quốc tế', 
            195000.00, 180000.00, 120, 312, 1, 1, 'C2', 'ST8', 4.7, 456, 'https://via.placeholder.com/300x300?text=Victor+Shuttle');

MERGE INTO products AS target
USING (SELECT 'P29' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P29', N'Tất cầu lông Yonex 19120', 'tat-cau-long-yonex-19120', 
            N'Tất thể thao chuyên dụng', 
            95000.00, NULL, 150, 567, 1, 1, 'C4', 'ST8', 4.5, 678, 'https://via.placeholder.com/300x300?text=Yonex+Socks');

MERGE INTO products AS target
USING (SELECT 'P30' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P30', N'Bình nước thể thao 750ml', 'binh-nuoc-the-thao-750ml', 
            N'Bình giữ nhiệt cao cấp', 
            250000.00, 220000.00, 80, 456, 1, 1, 'C4', 'ST8', 4.6, 534, 'https://via.placeholder.com/300x300?text=Water+Bottle');

-- ==================== ADDITIONAL ORDERS (15 more orders for better reports) ====================
-- Order 6 - Delivered
MERGE INTO orders AS target
USING (SELECT 'ORD006' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD006', 'U_USER4', 'ST4', 'COM1', N'321 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM', '0909345678', 
            30000.00, 'DELIVERED', 1, 5530000.00, 5500000.00, 5225000.00, 275000.00, 0.00, DATEADD(day, -7, GETDATE()));

-- Order 7 - Delivered
MERGE INTO orders AS target
USING (SELECT 'ORD007' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD007', 'U_USER5', 'ST5', 'COM2', N'654 Nguyễn Trãi, Q.5, TP.HCM', '0910456789', 
            40000.00, 'DELIVERED', 0, 3940000.00, 3900000.00, 3783000.00, 117000.00, 0.00, DATEADD(day, -6, GETDATE()));

-- Order 8 - Shipped
MERGE INTO orders AS target
USING (SELECT 'ORD008' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD008', 'U_USER1', 'ST6', 'COM1', N'123 Nguyễn Văn Cừ, Q.5, TP.HCM', '0904567890', 
            30000.00, 'SHIPPED', 0, 3010000.00, 2980000.00, 2831000.00, 149000.00, 0.00, DATEADD(day, -1, GETDATE()));

-- Order 9 - Delivered
MERGE INTO orders AS target
USING (SELECT 'ORD009' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD009', 'U_USER2', 'ST7', 'COM1', N'456 Lê Văn Việt, Q.9, TP.HCM', '0905678901', 
            30000.00, 'DELIVERED', 0, 1080000.00, 1050000.00, 997500.00, 52500.00, 0.00, DATEADD(day, -8, GETDATE()));

-- Order 10 - Delivered
MERGE INTO orders AS target
USING (SELECT 'ORD010' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD010', 'U_USER3', 'ST8', 'COM1', N'789 Võ Văn Ngân, Thủ Đức, TP.HCM', '0906789012', 
            50000.00, 'DELIVERED', 0, 4550000.00, 4500000.00, 4275000.00, 225000.00, 0.00, DATEADD(day, -10, GETDATE()));

-- Order 11 - Processing
MERGE INTO orders AS target
USING (SELECT 'ORD011' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD011', 'U_USER4', 'ST1', 'COM1', N'321 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM', '0909345678', 
            30000.00, 'PROCESSING', 0, 2830000.00, 2800000.00, 2660000.00, 140000.00, 0.00, GETDATE());

-- Order 12 - Delivered
MERGE INTO orders AS target
USING (SELECT 'ORD012' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD012', 'U_USER5', 'ST4', 'COM1', N'654 Nguyễn Trãi, Q.5, TP.HCM', '0910456789', 
            30000.00, 'DELIVERED', 1, 2980000.00, 2950000.00, 2802500.00, 147500.00, 0.00, DATEADD(day, -9, GETDATE()));

-- Order 13 - Delivered
MERGE INTO orders AS target
USING (SELECT 'ORD013' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD013', 'U_USER1', 'ST5', 'COM2', N'123 Nguyễn Văn Cừ, Q.5, TP.HCM', '0904567890', 
            30000.00, 'DELIVERED', 0, 510000.00, 480000.00, 465600.00, 14400.00, 0.00, DATEADD(day, -11, GETDATE()));

-- Order 14 - Delivered
MERGE INTO orders AS target
USING (SELECT 'ORD014' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD014', 'U_USER2', 'ST6', 'COM1', N'456 Lê Văn Việt, Q.9, TP.HCM', '0905678901', 
            40000.00, 'DELIVERED', 0, 410000.00, 380000.00, 361000.00, 19000.00, 0.00, DATEADD(day, -12, GETDATE()));

-- Order 15 - Delivered
MERGE INTO orders AS target
USING (SELECT 'ORD015' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD015', 'U_USER3', 'ST7', 'COM1', N'789 Võ Văn Ngân, Thủ Đức, TP.HCM', '0906789012', 
            30000.00, 'DELIVERED', 0, 1680000.00, 1650000.00, 1567500.00, 82500.00, 0.00, DATEADD(day, -13, GETDATE()));

-- Order 16 - Delivered
MERGE INTO orders AS target
USING (SELECT 'ORD016' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD016', 'U_USER4', 'ST2', 'COM1', N'321 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM', '0909345678', 
            30000.00, 'DELIVERED', 0, 480000.00, 450000.00, 427500.00, 22500.00, 0.00, DATEADD(day, -14, GETDATE()));

-- Order 17 - Shipped
MERGE INTO orders AS target
USING (SELECT 'ORD017' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD017', 'U_USER5', 'ST3', 'COM2', N'654 Nguyễn Trãi, Q.5, TP.HCM', '0910456789', 
            30000.00, 'SHIPPED', 0, 320000.00, 290000.00, 281300.00, 8700.00, 0.00, GETDATE());

-- Order 18 - Delivered
MERGE INTO orders AS target
USING (SELECT 'ORD018' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD018', 'U_USER1', 'ST8', 'COM1', N'123 Nguyễn Văn Cừ, Q.5, TP.HCM', '0904567890', 
            30000.00, 'DELIVERED', 1, 210000.00, 180000.00, 171000.00, 9000.00, 0.00, DATEADD(day, -15, GETDATE()));

-- Order 19 - Delivered
MERGE INTO orders AS target
USING (SELECT 'ORD019' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD019', 'U_USER2', 'ST4', 'COM1', N'456 Lê Văn Việt, Q.9, TP.HCM', '0905678901', 
            30000.00, 'DELIVERED', 0, 230000.00, 200000.00, 190000.00, 10000.00, 0.00, DATEADD(day, -16, GETDATE()));

-- Order 20 - Delivered
MERGE INTO orders AS target
USING (SELECT 'ORD020' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD020', 'U_USER3', 'ST1', 'COM1', N'789 Võ Văn Ngân, Thủ Đức, TP.HCM', '0906789012', 
            50000.00, 'DELIVERED', 0, 490000.00, 450000.00, 427500.00, 22500.00, 0.00, DATEADD(day, -17, GETDATE()));

-- ==================== ORDER ITEMS FOR NEW ORDERS ====================
-- Order 6 items
MERGE INTO order_items AS target
USING (SELECT 'OI007' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, total)
    VALUES ('OI007', 'ORD006', 'P11', 1, 5500000.00, 5500000.00);

-- Order 7 items
MERGE INTO order_items AS target
USING (SELECT 'OI008' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, total)
    VALUES ('OI008', 'ORD007', 'P15', 1, 3900000.00, 3900000.00);

-- Order 8 items
MERGE INTO order_items AS target
USING (SELECT 'OI009' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, total)
    VALUES ('OI009', 'ORD008', 'P19', 1, 2980000.00, 2980000.00);

-- Order 9 items
MERGE INTO order_items AS target
USING (SELECT 'OI010' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, total)
    VALUES ('OI010', 'ORD009', 'P23', 1, 1050000.00, 1050000.00);

-- Order 10 items
MERGE INTO order_items AS target
USING (SELECT 'OI011' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, total)
    VALUES ('OI011', 'ORD010', 'P27', 1, 4500000.00, 4500000.00);

-- Order 11 items
MERGE INTO order_items AS target
USING (SELECT 'OI012' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, total)
    VALUES ('OI012', 'ORD011', 'P2', 1, 2800000.00, 2800000.00);

-- Order 12 items
MERGE INTO order_items AS target
USING (SELECT 'OI013' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, total)
    VALUES ('OI013', 'ORD012', 'P12', 1, 2950000.00, 2950000.00);

-- Order 13 items
MERGE INTO order_items AS target
USING (SELECT 'OI014' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, total)
    VALUES ('OI014', 'ORD013', 'P17', 1, 480000.00, 480000.00);

-- Order 14 items
MERGE INTO order_items AS target
USING (SELECT 'OI015' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, total)
    VALUES ('OI015', 'ORD014', 'P21', 1, 380000.00, 380000.00);

-- Order 15 items
MERGE INTO order_items AS target
USING (SELECT 'OI016' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, total)
    VALUES ('OI016', 'ORD015', 'P24', 1, 1650000.00, 1650000.00);

-- Order 16 items
MERGE INTO order_items AS target
USING (SELECT 'OI017' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, total)
    VALUES ('OI017', 'ORD016', 'P7', 1, 450000.00, 450000.00);

-- Order 17 items
MERGE INTO order_items AS target
USING (SELECT 'OI018' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, total)
    VALUES ('OI018', 'ORD017', 'P9', 1, 290000.00, 290000.00);

-- Order 18 items
MERGE INTO order_items AS target
USING (SELECT 'OI019' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, total)
    VALUES ('OI019', 'ORD018', 'P28', 1, 180000.00, 180000.00);

-- Order 19 items
MERGE INTO order_items AS target
USING (SELECT 'OI020' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, total)
    VALUES ('OI020', 'ORD019', 'P13', 1, 200000.00, 200000.00);

-- Order 20 items
MERGE INTO order_items AS target
USING (SELECT 'OI021' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, order_id, product_id, quantity, price, total)
    VALUES ('OI021', 'ORD020', 'P7', 1, 450000.00, 450000.00);
