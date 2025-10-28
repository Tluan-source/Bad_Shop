-- ==============================================================
-- 🔰 DỮ LIỆU MẪU DÀNH CHO VAI TRÒ SHIPPER - BADMINTON MARKETPLACE
-- ==============================================================

-- 0️⃣ CATEGORIES (Thêm danh mục sản phẩm)
DELETE FROM categories;
INSERT INTO categories (id, name, slug, is_active, created_at)
VALUES
('C001', N'Vợt cầu lông', 'vot-cau-long', 1, GETDATE()),
('C002', N'Giày cầu lông', 'giay-cau-long', 1, GETDATE()),
('C003', N'Quả cầu lông', 'qua-cau-long', 1, GETDATE()),
('C004', N'Túi vợt', 'tui-vot', 1, GETDATE()),
('C005', N'Phụ kiện', 'phu-kien', 1, GETDATE());

-- 1️⃣ USERS
-- Password: 123456 (đã mã hóa BCrypt)
-- BCrypt Hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z9Lbmo8CFL6yKwAKMK1o6txW
DELETE FROM users;
INSERT INTO users (id, full_name, email, phone, salt, hashed_password, status, role, e_wallet, created_at)
VALUES
-- Shippers
('U100', N'Nguyễn Văn Shipper', 'shipper1@badminton.com', '0901000001', NULL, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z9Lbmo8CFL6yKwAKMK1o6txW', 'ACTIVE', 'SHIPPER', 0, GETDATE()),
('U101', N'Lê Văn Giao', 'shipper2@badminton.com', '0901000002', NULL, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z9Lbmo8CFL6yKwAKMK1o6txW', 'ACTIVE', 'SHIPPER', 0, GETDATE()),
('U102', N'Trần Quang Vận', 'shipper3@badminton.com', '0901000003', NULL, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z9Lbmo8CFL6yKwAKMK1o6txW', 'ACTIVE', 'SHIPPER', 0, GETDATE()),
('U103', N'Phạm Minh Tài', 'shipper4@badminton.com', '0901000004', NULL, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z9Lbmo8CFL6yKwAKMK1o6txW', 'ACTIVE', 'SHIPPER', 0, GETDATE()),
('U104', N'Đặng Hoàng Long', 'shipper5@badminton.com', '0901000005', NULL, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z9Lbmo8CFL6yKwAKMK1o6txW', 'ACTIVE', 'SHIPPER', 0, GETDATE()),
-- Vendors (Store Owners)
('V001', N'Nguyễn Văn ProAce', 'vendor1@badminton.com', '0902000001', NULL, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z9Lbmo8CFL6yKwAKMK1o6txW', 'ACTIVE', 'VENDOR', 0, GETDATE()),
('V002', N'Trần Thị Yonex', 'vendor2@badminton.com', '0902000002', NULL, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z9Lbmo8CFL6yKwAKMK1o6txW', 'ACTIVE', 'VENDOR', 0, GETDATE()),
('V003', N'Lê Văn Lining', 'vendor3@badminton.com', '0902000003', NULL, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z9Lbmo8CFL6yKwAKMK1o6txW', 'ACTIVE', 'VENDOR', 0, GETDATE()),
('V004', N'Phạm Thị Victor', 'vendor4@badminton.com', '0902000004', NULL, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z9Lbmo8CFL6yKwAKMK1o6txW', 'ACTIVE', 'VENDOR', 0, GETDATE()),
-- Customers
('U200', N'Trần Thị Mai', 'user1@badminton.com', '0911111111', NULL, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z9Lbmo8CFL6yKwAKMK1o6txW', 'ACTIVE', 'USER', 0, GETDATE()),
('U201', N'Nguyễn Văn Bảo', 'user2@badminton.com', '0911111112', NULL, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z9Lbmo8CFL6yKwAKMK1o6txW', 'ACTIVE', 'USER', 0, GETDATE()),
('U202', N'Lê Thị Hương', 'user3@badminton.com', '0911111113', NULL, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z9Lbmo8CFL6yKwAKMK1o6txW', 'ACTIVE', 'USER', 0, GETDATE()),
('U203', N'Phan Minh Tuấn', 'user4@badminton.com', '0911111114', NULL, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z9Lbmo8CFL6yKwAKMK1o6txW', 'ACTIVE', 'USER', 0, GETDATE()),
('U204', N'Võ Thị Kim Anh', 'user5@badminton.com', '0911111115', NULL, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z9Lbmo8CFL6yKwAKMK1o6txW', 'ACTIVE', 'USER', 0, GETDATE());

-- 2️⃣ USER_ADDRESSES
DELETE FROM user_addresses;
INSERT INTO user_addresses (id, user_id, full_name, phone, address, province, district, ward, is_default, created_at)
VALUES
('A001', 'U200', N'Trần Thị Mai', '0911111111', N'12 Nguyễn Văn Cừ', N'TP.HCM', N'Q.5', N'P.2', 1, GETDATE()),
('A002', 'U201', N'Nguyễn Văn Bảo', '0911111112', N'34 Lê Hồng Phong', N'TP.HCM', N'Q.10', N'P.14', 1, GETDATE()),
('A003', 'U202', N'Lê Thị Hương', '0911111113', N'89 Võ Văn Tần', N'TP.HCM', N'Q.3', N'P.6', 1, GETDATE()),
('A004', 'U203', N'Phan Minh Tuấn', '0911111114', N'120 Nguyễn Trãi', N'TP.HCM', N'Q.1', N'P.Bến Thành', 1, GETDATE()),
('A005', 'U204', N'Võ Thị Kim Anh', '0911111115', N'98 Điện Biên Phủ', N'TP.HCM', N'Q.Bình Thạnh', N'P.25', 1, GETDATE()),
('A006', 'U200', N'Trần Thị Mai', '0911111111', N'11 Nguyễn Thị Minh Khai', N'TP.HCM', N'Q.1', N'P.Đa Kao', 0, GETDATE()),
('A007', 'U201', N'Nguyễn Văn Bảo', '0911111112', N'75 Lý Thường Kiệt', N'TP.HCM', N'Q.Tân Bình', N'P.4', 0, GETDATE()),
('A008', 'U202', N'Lê Thị Hương', '0911111113', N'22 Phan Xích Long', N'TP.HCM', N'Q.Phú Nhuận', N'P.7', 0, GETDATE()),
('A009', 'U203', N'Phan Minh Tuấn', '0911111114', N'88 Nguyễn Huệ', N'TP.HCM', N'Q.1', N'P.Bến Nghé', 0, GETDATE()),
('A010', 'U204', N'Võ Thị Kim Anh', '0911111115', N'43 Trần Hưng Đạo', N'TP.HCM', N'Q.5', N'P.3', 0, GETDATE());

-- 3️⃣ STORES
DELETE FROM stores;
INSERT INTO stores (id, name, bio, slug, owner_id, is_active, rating, e_wallet, created_at)
VALUES
('ST001', N'Cửa hàng cầu lông ProAce', N'Chuyên vợt cao cấp', N'proace-store', 'V001', 1, 4.9, 0, GETDATE()),
('ST002', N'Cửa hàng Yonex', N'Phân phối chính hãng Yonex', N'yonex-store', 'V002', 1, 4.8, 0, GETDATE()),
('ST003', N'Cửa hàng Lining', N'Phụ kiện, giày cầu lông Lining', N'lining-store', 'V003', 1, 4.7, 0, GETDATE()),
('ST004', N'Cửa hàng Victor', N'Vợt, túi, giày Victor chính hãng', N'victor-store', 'V004', 1, 4.6, 0, GETDATE());

-- 3.5️⃣ PRODUCTS (Thêm sản phẩm để liên kết với ORDER_ITEMS)
DELETE FROM products;
INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images, created_at)
VALUES
('P001', N'Vợt Yonex Astrox 99 Pro', 'vot-yonex-astrox-99-pro', N'Vợt cầu lông chuyên nghiệp cao cấp', 4990000, 4500000, 50, 120, 1, 1, 'C001', 'ST001', 4.9, 850, '["https://via.placeholder.com/300x300?text=Yonex+Astrox+99"]', GETDATE()),
('P002', N'Giày Lining AYTQ025-1', 'giay-lining-aytq025-1', N'Giày cầu lông chuyên dụng thoáng khí', 995000, 890000, 80, 250, 1, 1, 'C002', 'ST003', 4.7, 620, '["https://via.placeholder.com/300x300?text=Lining+Shoes"]', GETDATE()),
('P003', N'Quả cầu Yonex Aerosensa 50', 'qua-cau-yonex-aerosensa-50', N'Quả cầu thi đấu chính hãng', 990000, 950000, 200, 500, 1, 1, 'C003', 'ST002', 4.8, 1200, '["https://via.placeholder.com/300x300?text=Shuttlecock"]', GETDATE()),
('P004', N'Vợt Victor Thruster K 9900', 'vot-victor-thruster-k-9900', N'Vợt tấn công mạnh mẽ', 2500000, NULL, 30, 85, 1, 1, 'C001', 'ST004', 4.6, 340, '["https://via.placeholder.com/300x300?text=Victor+Thruster"]', GETDATE()),
('P005', N'Vợt Lining Tectonic 7', 'vot-lining-tectonic-7', N'Vợt công thủ toàn diện', 3890000, 3650000, 25, 95, 1, 1, 'C001', 'ST003', 4.8, 420, '["https://via.placeholder.com/300x300?text=Lining+Tectonic"]', GETDATE()),
('P006', N'Vợt Yonex Voltric Z-Force II', 'vot-yonex-voltric-z-force-2', N'Vợt tấn công đỉnh cao', 4200000, NULL, 15, 60, 1, 1, 'C001', 'ST002', 4.9, 720, '["https://via.placeholder.com/300x300?text=Yonex+Voltric"]', GETDATE()),
('P007', N'Băng cán vợt Yonex AC102', 'bang-can-vot-yonex-ac102', N'Băng quấn cán vợt chống trơn', 510000, 480000, 150, 380, 1, 1, 'C005', 'ST002', 4.5, 290, '["https://via.placeholder.com/300x300?text=Grip+Tape"]', GETDATE()),
('P008', N'Giày Victor P9200', 'giay-victor-p9200', N'Giày cầu lông cao cấp chống lật cổ chân', 1325000, NULL, 45, 180, 1, 1, 'C002', 'ST004', 4.7, 510, '["https://via.placeholder.com/300x300?text=Victor+P9200"]', GETDATE()),
('P009', N'Túi vợt Yonex BAG9831W', 'tui-vot-yonex-bag9831w', N'Túi đựng vợt 6 ngăn chống nước', 3150000, 2950000, 20, 45, 1, 1, 'C004', 'ST002', 4.8, 280, '["https://via.placeholder.com/300x300?text=Yonex+Bag"]', GETDATE()),
('P010', N'Vợt Lining Windstorm 78', 'vot-lining-windstorm-78', N'Vợt đánh nhanh linh hoạt', 1890000, 1750000, 60, 220, 1, 1, 'C001', 'ST003', 4.6, 550, '["https://via.placeholder.com/300x300?text=Lining+Windstorm"]', GETDATE());

-- 4️⃣ ORDERS
DELETE FROM orders;
INSERT INTO orders (id, user_id, store_id, address, phone, shipping_fee, status, is_paid_before, amount_from_user, amount_to_store, discount_amount, created_at, updated_at)
VALUES
('O100', 'U200', 'ST001', N'12 Nguyễn Văn Cừ, Q.5, TP.HCM', '0911111111', 35000, 'PROCESSING', 0, 4990000, 0, 0, GETDATE(), GETDATE()),
('O101', 'U200', 'ST002', N'34 Lê Hồng Phong, Q.10, TP.HCM', '0911111111', 30000, 'SHIPPED', 0, 1990000, 0, 0, GETDATE(), GETDATE()),
('O102', 'U201', 'ST003', N'89 Võ Văn Tần, Q.3, TP.HCM', '0911111112', 25000, 'DELIVERED', 1, 990000, 0, 0, GETDATE(), GETDATE()),
('O103', 'U201', 'ST004', N'120 Nguyễn Trãi, Q.1, TP.HCM', '0911111112', 30000, 'CANCELLED', 0, 2500000, 0, 0, GETDATE(), GETDATE()),
('O104', 'U202', 'ST001', N'98 Điện Biên Phủ, Q.Bình Thạnh', '0911111113', 25000, 'PROCESSING', 0, 3890000, 0, 0, GETDATE(), GETDATE()),
('O105', 'U202', 'ST002', N'11 Nguyễn Thị Minh Khai, Q.1', '0911111113', 35000, 'SHIPPED', 0, 4200000, 0, 0, GETDATE(), GETDATE()),
('O106', 'U203', 'ST003', N'22 Phan Xích Long, Q.Phú Nhuận', '0911111114', 25000, 'SHIPPED', 0, 1550000, 0, 0, GETDATE(), GETDATE()),
('O107', 'U203', 'ST004', N'88 Nguyễn Huệ, Q.1', '0911111114', 30000, 'DELIVERED', 1, 2650000, 0, 0, GETDATE(), GETDATE()),
('O108', 'U204', 'ST001', N'43 Trần Hưng Đạo, Q.5', '0911111115', 35000, 'PROCESSING', 0, 3150000, 0, 0, GETDATE(), GETDATE()),
('O109', 'U204', 'ST002', N'75 Lý Thường Kiệt, Q.Tân Bình', '0911111115', 25000, 'SHIPPED', 0, 1890000, 0, 0, GETDATE(), GETDATE());

-- 5️⃣ ORDER_ITEMS
DELETE FROM order_items;
INSERT INTO order_items (id, order_id, product_id, quantity, price, total, created_at)
VALUES
('OI001', 'O100', 'P001', 1, 4990000, 4990000, GETDATE()),
('OI002', 'O101', 'P002', 2, 995000, 1990000, GETDATE()),
('OI003', 'O102', 'P003', 1, 990000, 990000, GETDATE()),
('OI004', 'O103', 'P004', 1, 2500000, 2500000, GETDATE()),
('OI005', 'O104', 'P005', 1, 3890000, 3890000, GETDATE()),
('OI006', 'O105', 'P006', 1, 4200000, 4200000, GETDATE()),
('OI007', 'O106', 'P007', 3, 510000, 1530000, GETDATE()),
('OI008', 'O107', 'P008', 2, 1325000, 2650000, GETDATE()),
('OI009', 'O108', 'P009', 1, 3150000, 3150000, GETDATE()),
('OI010', 'O109', 'P010', 1, 1890000, 1890000, GETDATE());

-- 6️⃣ SHIPMENTS
DELETE FROM shipments;
INSERT INTO shipments (id, order_id, shipper_id, shipping_provider_id, status, shipping_fee, note, created_at, updated_at)
VALUES
('S100', 'O100', NULL, NULL, 'ACCEPTED', 35000, N'Chờ shipper nhận đơn', GETDATE(), GETDATE()),
('S101', 'O101', 'U100', NULL, 'DELIVERING', 30000, N'Đang giao đến Q.10', GETDATE(), GETDATE()),
('S102', 'O102', 'U101', NULL, 'DELIVERED', 25000, N'Đã giao thành công cho khách', GETDATE(), GETDATE()),
('S103', 'O103', 'U100', NULL, 'FAILED', 30000, N'Khách từ chối nhận hàng', GETDATE(), GETDATE()),
('S104', 'O104', NULL, NULL, 'ACCEPTED', 25000, N'Chờ shipper nhận đơn', GETDATE(), GETDATE()),
('S105', 'O105', 'U102', NULL, 'DELIVERING', 35000, N'Đang giao Q.1', GETDATE(), GETDATE()),
('S106', 'O106', NULL, NULL, 'ACCEPTED', 25000, N'Chờ shipper nhận đơn', GETDATE(), GETDATE()),
('S107', 'O107', 'U103', NULL, 'DELIVERED', 30000, N'Giao thành công', GETDATE(), GETDATE()),
('S108', 'O108', NULL, NULL, 'ACCEPTED', 35000, N'Chờ shipper nhận đơn', GETDATE(), GETDATE()),
('S109', 'O109', 'U104', NULL, 'DELIVERING', 25000, N'Đang trên đường giao', GETDATE(), GETDATE());
