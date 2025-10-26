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
    VALUES ('P1', N'Vợt Yonex Astrox 100ZZ', 'yonex-astrox-100zz', N'Vợt cầu lông cao cấp dành cho vận động viên chuyên nghiệp', 5500000.00, 4990000.00, 15, 23, 1, 1, '["https://via.placeholder.com/500x500/FF0000/FFFFFF?text=Yonex+Astrox+100ZZ"]', 'C1', 'S1', NULL, 4.9, 156);

MERGE INTO products AS target
USING (SELECT 'P2' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P2', N'Vợt Victor Brave Sword 12', 'victor-brave-sword-12', N'Vợt tấn công nhanh, nhẹ và linh hoạt', 3800000.00, 3500000.00, 20, 18, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429166/vot-cau-long-victor-brave-sword-12-pro-chinh-hang_1737312826_bfpid0.webp"]', 'C1', 'S2', NULL, 4.7, 124);

MERGE INTO products AS target
USING (SELECT 'P3' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P3', N'Quả Cầu Yonex Aerosensa 50', 'yonex-aerosensa-50', N'Quả cầu lông thi đấu chất lượng cao', 180000.00, 165000.00, 200, 145, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429176/yonex-a50_uzyh7t.webp"]', 'C2', 'S1', NULL, 4.6, 89);

MERGE INTO products AS target
USING (SELECT 'P4' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P4', N'Giày Yonex Power Cushion 65Z2', 'yonex-pc-65z2', N'Giày cầu lông chuyên nghiệp với công nghệ đệm', 2200000.00, 1990000.00, 30, 12, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429176/yonex_power_cushion_65z2_vc6ta0.webp"]', 'C3', 'S1', NULL, 4.8, 67);

MERGE INTO products AS target
USING (SELECT 'P5' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P5', N'Băng Cán Vợt Victor', 'victor-grip-tape', N'Băng quấn cán vợt chống trượt', 45000.00, 0.00, 500, 234, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429165/quan-can-vot-cau-long-gr200-1_d1hnru.webp"]', 'C4', 'S2', NULL, 4.5, 201);

MERGE INTO products AS target
USING (SELECT 'P6' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P6', N'Vợt Yonex Nanoflare 1000Z', 'yonex-nanoflare-1000z', N'Vợt siêu nhẹ cho tốc độ đánh nhanh', 6200000.00, 5890000.00, 10, 8, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429165/nanoflare_1000z_qcdj5t.png"]', 'C1', 'S1', NULL, 5.0, 92);

MERGE INTO products AS target
USING (SELECT 'P7' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, list_images, category_id, store_id, style_value_ids, rating, view_count)
    VALUES ('P7', N'Quả Cầu Victor Gold Champion', 'victor-gold-champion', N'Quả cầu thi đấu quốc tế', 220000.00, 200000.00, 150, 67, 1, 1, '["https://res.cloudinary.com/duzkugddg/image/upload/v1761429165/victor-champion_rrbcu8.jpg"]', 'C2', 'S2', NULL, 4.7, 78);

MERGE INTO products AS target
USING (SELECT 'P8' AS id) AS source
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