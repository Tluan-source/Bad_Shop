-- Script to load all data into BadmintonMarketplace database
-- Run this script in SQL Server Management Studio after the application has created the tables
-- Make sure you're connected to BadmintonMarketplace database

USE BadmintonMarketplace;
GO

-- ==================== USERS ====================
-- Delete existing data if needed (optional - comment out if you want to keep existing data)
-- DELETE FROM order_items;
-- DELETE FROM orders;
-- DELETE FROM products;
-- DELETE FROM stores;
-- DELETE FROM users WHERE id LIKE 'U_%';

-- ADMIN account
IF NOT EXISTS (SELECT 1 FROM users WHERE id = 'U_ADMIN')
BEGIN
    INSERT INTO users (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_ADMIN', N'Quản Trị Viên', 'admin@badminton.com', '0900000001', 
            'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'ADMIN', NULL, 100, 1000000.00);
END;

-- VENDOR accounts
IF NOT EXISTS (SELECT 1 FROM users WHERE id = 'U_VENDOR1')
BEGIN
    INSERT INTO users (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_VENDOR1', N'Nguyễn Văn A', 'vendor1@badminton.com', '0901234567',
            'b2c3d4e5-f6a7-8901-bcde-f12345678901',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'VENDOR', NULL, 50, 5000000.00);
END;

IF NOT EXISTS (SELECT 1 FROM users WHERE id = 'U_VENDOR2')
BEGIN
    INSERT INTO users (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_VENDOR2', N'Trần Thị B', 'vendor2@badminton.com', '0902345678',
            'b2c3d4e5-f6a7-8901-bcde-f12345678902',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'VENDOR', NULL, 45, 3500000.00);
END;

IF NOT EXISTS (SELECT 1 FROM users WHERE id = 'U_VENDOR3')
BEGIN
    INSERT INTO users (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_VENDOR3', N'Lê Văn C', 'vendor3@badminton.com', '0903456789',
            'b2c3d4e5-f6a7-8901-bcde-f12345678903',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'VENDOR', NULL, 40, 2800000.00);
END;

IF NOT EXISTS (SELECT 1 FROM users WHERE id = 'U_VENDOR4')
BEGIN
    INSERT INTO users (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_VENDOR4', N'Phạm Minh Đức', 'vendor4@badminton.com', '0907123456',
            'b2c3d4e5-f6a7-8901-bcde-f12345678904',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'VENDOR', NULL, 55, 4200000.00);
END;

IF NOT EXISTS (SELECT 1 FROM users WHERE id = 'U_VENDOR5')
BEGIN
    INSERT INTO users (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_VENDOR5', N'Đặng Thu Hương', 'vendor5@badminton.com', '0908234567',
            'b2c3d4e5-f6a7-8901-bcde-f12345678905',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'VENDOR', NULL, 48, 3800000.00);
END;

-- SHIPPER account
IF NOT EXISTS (SELECT 1 FROM users WHERE id = 'U_SHIPPER')
BEGIN
    INSERT INTO users (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_SHIPPER', N'Nhân Viên Giao Hàng', 'shipper@badminton.com', '0900000003',
            'c3d4e5f6-a7b8-9012-cdef-123456789012',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'SHIPPER', NULL, 30, 300000.00);
END;

-- USER accounts (customers)
IF NOT EXISTS (SELECT 1 FROM users WHERE id = 'U_USER1')
BEGIN
    INSERT INTO users (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_USER1', N'Phạm Thị D', 'user1@badminton.com', '0904567890',
            'd4e5f6a7-b8c9-0123-def1-234567890123',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'USER', NULL, 15, 500000.00);
END;

IF NOT EXISTS (SELECT 1 FROM users WHERE id = 'U_USER2')
BEGIN
    INSERT INTO users (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_USER2', N'Võ Văn E', 'user2@badminton.com', '0905678901',
            'd4e5f6a7-b8c9-0123-def1-234567890124',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'USER', NULL, 20, 750000.00);
END;

IF NOT EXISTS (SELECT 1 FROM users WHERE id = 'U_USER3')
BEGIN
    INSERT INTO users (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_USER3', N'Hoàng Thị F', 'user3@badminton.com', '0906789012',
            'd4e5f6a7-b8c9-0123-def1-234567890125',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'USER', NULL, 10, 300000.00);
END;

IF NOT EXISTS (SELECT 1 FROM users WHERE id = 'U_USER4')
BEGIN
    INSERT INTO users (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_USER4', N'Nguyễn Hoàng Giang', 'user4@badminton.com', '0909345678',
            'd4e5f6a7-b8c9-0123-def1-234567890126',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'USER', NULL, 25, 1200000.00);
END;

IF NOT EXISTS (SELECT 1 FROM users WHERE id = 'U_USER5')
BEGIN
    INSERT INTO users (id, full_name, email, phone, salt, hashed_password, status, role, addresses, point, e_wallet)
    VALUES ('U_USER5', N'Trần Minh Hòa', 'user5@badminton.com', '0910456789',
            'd4e5f6a7-b8c9-0123-def1-234567890127',
            '$2a$10$pF3J/Xvng74sVF6AnfsbTOrJUlCmEkpWniuM0Eysfo5LGs8O7aPzG',
            'ACTIVE', 'USER', NULL, 18, 850000.00);
END;

PRINT 'Users inserted successfully';
GO

-- ==================== CATEGORIES ====================
IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 'C1')
BEGIN
    INSERT INTO categories (id, name, slug, description, is_active)
    VALUES ('C1', N'Vợt Cầu Lông', 'vot-cau-long', N'Các loại vợt cầu lông chuyên nghiệp', 1);
END;

IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 'C2')
BEGIN
    INSERT INTO categories (id, name, slug, description, is_active)
    VALUES ('C2', N'Quả Cầu', 'qua-cau', N'Quả cầu lông thi đấu và tập luyện', 1);
END;

IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 'C3')
BEGIN
    INSERT INTO categories (id, name, slug, description, is_active)
    VALUES ('C3', N'Giày Cầu Lông', 'giay-cau-long', N'Giày thể thao chuyên dụng', 1);
END;

IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 'C4')
BEGIN
    INSERT INTO categories (id, name, slug, description, is_active)
    VALUES ('C4', N'Phụ Kiện', 'phu-kien', N'Phụ kiện và trang thiết bị cầu lông', 1);
END;

IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 'C5')
BEGIN
    INSERT INTO categories (id, name, slug, description, is_active)
    VALUES ('C5', N'Quần Áo Thể Thao', 'quan-ao-the-thao', N'Trang phục thi đấu và tập luyện', 1);
END;

PRINT 'Categories inserted successfully';
GO

-- ==================== COMMISSIONS ====================
IF NOT EXISTS (SELECT 1 FROM commissions WHERE id = 'COM1')
BEGIN
    INSERT INTO commissions (id, name, fee_percent, description)
    VALUES ('COM1', N'Hoa Hồng Chuẩn', 5.00, N'Phí hoa hồng 5% cho mỗi đơn hàng');
END;

IF NOT EXISTS (SELECT 1 FROM commissions WHERE id = 'COM2')
BEGIN
    INSERT INTO commissions (id, name, fee_percent, description)
    VALUES ('COM2', N'Hoa Hồng VIP', 3.00, N'Phí hoa hồng 3% cho đối tác VIP');
END;

PRINT 'Commissions inserted successfully';
GO

-- ==================== STORES ====================
IF NOT EXISTS (SELECT 1 FROM stores WHERE id = 'ST1')
BEGIN
    INSERT INTO stores (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST1', N'Cửa hàng Cầu Lông ABC', 'cua-hang-cau-long-abc', 
            N'Chuyên cung cấp vợt, giày và phụ kiện cầu lông chính hãng', 
            'U_VENDOR1', 'COM1', 1, 4.8, 100, 5000000.00);
END;

IF NOT EXISTS (SELECT 1 FROM stores WHERE id = 'ST2')
BEGIN
    INSERT INTO stores (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST2', N'Shop Thể Thao XYZ', 'shop-the-thao-xyz', 
            N'Đồ thể thao cao cấp, phụ kiện tập luyện đa dạng', 
            'U_VENDOR2', 'COM1', 1, 4.5, 85, 3500000.00);
END;

IF NOT EXISTS (SELECT 1 FROM stores WHERE id = 'ST3')
BEGIN
    INSERT INTO stores (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST3', N'Badminton Pro Shop', 'badminton-pro-shop', 
            N'Cửa hàng chuyên nghiệp, sản phẩm chất lượng cao', 
            'U_VENDOR3', 'COM2', 1, 4.7, 90, 2800000.00);
END;

IF NOT EXISTS (SELECT 1 FROM stores WHERE id = 'ST4')
BEGIN
    INSERT INTO stores (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST4', N'Sport Excellence', 'sport-excellence', 
            N'Cửa hàng chuyên cung cấp trang thiết bị thể thao cao cấp từ các thương hiệu nổi tiếng', 
            'U_VENDOR4', 'COM1', 1, 4.9, 120, 6500000.00);
END;

IF NOT EXISTS (SELECT 1 FROM stores WHERE id = 'ST5')
BEGIN
    INSERT INTO stores (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST5', N'Cầu Lông Champion', 'cau-long-champion', 
            N'Chuyên về vợt và giày thi đấu chuyên nghiệp, phục vụ các VĐV', 
            'U_VENDOR5', 'COM2', 1, 4.6, 95, 4100000.00);
END;

IF NOT EXISTS (SELECT 1 FROM stores WHERE id = 'ST6')
BEGIN
    INSERT INTO stores (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST6', N'Elite Sports Center', 'elite-sports-center', 
            N'Trung tâm thể thao với đầy đủ thiết bị tập luyện và thi đấu', 
            'U_ADMIN', 'COM1', 1, 4.7, 110, 5200000.00);
END;

IF NOT EXISTS (SELECT 1 FROM stores WHERE id = 'ST7')
BEGIN
    INSERT INTO stores (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST7', N'Thể Thao Hoàng Gia', 'the-thao-hoang-gia', 
            N'Thương hiệu uy tín với hơn 10 năm kinh nghiệm', 
            'U_ADMIN', 'COM1', 1, 4.8, 105, 4800000.00);
END;

IF NOT EXISTS (SELECT 1 FROM stores WHERE id = 'ST8')
BEGIN
    INSERT INTO stores (id, name, slug, bio, owner_id, commission_id, is_active, rating, point, e_wallet)
    VALUES ('ST8', N'Pro Badminton Store', 'pro-badminton-store', 
            N'Chuyên phân phối đồ cầu lông chính hãng giá tốt', 
            'U_VENDOR1', 'COM1', 1, 4.5, 88, 3600000.00);
END;

PRINT 'Stores inserted successfully - Total: 8 stores';
GO

-- Note: Run insert_products.sql next to add 30 products
-- Then run insert_orders.sql to add 20 orders with order items
PRINT 'Phase 1 complete: Users, Categories, Commissions, and Stores created';
PRINT 'Next: Run insert_products.sql and insert_orders.sql';
