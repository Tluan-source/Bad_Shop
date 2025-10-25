-- Insert 30 products into BadmintonMarketplace database
-- Run this after insert_base_data.sql

USE BadmintonMarketplace;
GO

-- ==================== PRODUCTS ====================
-- Store 1 Products (4 products)
IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P1')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P1', N'Vợt Yonex Astrox 88D Pro', 'vot-yonex-astrox-88d-pro', 
            N'Vợt cầu lông chuyên nghiệp dành cho đánh công', 
            4500000.00, 4200000.00, 15, 45, 1, 1, 'C1', 'ST1', 4.9, 234, 'https://via.placeholder.com/300x300?text=Yonex+Astrox+88D');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P2')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P2', N'Giày Victor SH-A920', 'giay-victor-sh-a920', 
            N'Giày cầu lông cao cấp, chống trơn trượt', 
            2800000.00, NULL, 25, 78, 1, 1, 'C3', 'ST1', 4.7, 189, 'https://via.placeholder.com/300x300?text=Victor+A920');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P3')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P3', N'Quả cầu Yonex AS-50', 'qua-cau-yonex-as-50', 
            N'Quả cầu lông thi đấu chuyên nghiệp', 
            180000.00, 160000.00, 100, 298, 1, 1, 'C2', 'ST1', 4.6, 445, 'https://via.placeholder.com/300x300?text=Yonex+AS-50');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P4')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P4', N'Túi vợt Yonex BA92012M', 'tui-vot-yonex-ba92012m', 
            N'Túi đựng vợt cao cấp, chống nước', 
            850000.00, NULL, 30, 67, 1, 1, 'C4', 'ST1', 4.5, 156, 'https://via.placeholder.com/300x300?text=Yonex+Bag');
END;

-- Store 2 Products (4 products)
IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P5')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P5', N'Vợt Lining Axforce 100', 'vot-lining-axforce-100', 
            N'Vợt tấn công mạnh, thiết kế aerodynamic', 
            3800000.00, 3500000.00, 20, 56, 1, 1, 'C1', 'ST2', 4.8, 201, 'https://via.placeholder.com/300x300?text=Lining+Axforce');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P6')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P6', N'Giày Lining AYZP001', 'giay-lining-ayzp001', 
            N'Giày cầu lông êm ái, bền bỉ', 
            2200000.00, 1980000.00, 18, 89, 1, 1, 'C3', 'ST2', 4.6, 167, 'https://via.placeholder.com/300x300?text=Lining+AYZP001');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P7')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P7', N'Áo Lining Championship', 'ao-lining-championship', 
            N'Áo thi đấu chuyên nghiệp, thoáng mát', 
            450000.00, NULL, 50, 123, 1, 1, 'C5', 'ST2', 4.7, 278, 'https://via.placeholder.com/300x300?text=Lining+Shirt');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P16')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P16', N'Quần Lining R-2024', 'quan-lining-r-2024', 
            N'Quần thi đấu năng động', 
            420000.00, 380000.00, 45, 92, 1, 1, 'C5', 'ST2', 4.5, 198, 'https://via.placeholder.com/300x300?text=Lining+Shorts');
END;

-- Store 3 Products (4 products)
IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P8')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P8', N'Vợt Victor Thruster K9900', 'vot-victor-thruster-k9900', 
            N'Vợt đánh công tốc độ cao', 
            5200000.00, 4900000.00, 10, 34, 1, 1, 'C1', 'ST3', 4.9, 198, 'https://via.placeholder.com/300x300?text=Victor+K9900');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P9')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P9', N'Cước căng vợt Victor VBS-66', 'cuoc-cang-vot-victor-vbs-66', 
            N'Cước cầu lông bền, ổn định', 
            320000.00, 290000.00, 80, 145, 1, 1, 'C4', 'ST3', 4.5, 312, 'https://via.placeholder.com/300x300?text=Victor+String');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P10')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P10', N'Quần cầu lông Victor R-3096', 'quan-cau-long-victor-r-3096', 
            N'Quần thi đấu thoải mái, co giãn tốt', 
            380000.00, NULL, 40, 98, 1, 1, 'C5', 'ST3', 4.6, 189, 'https://via.placeholder.com/300x300?text=Victor+Shorts');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P17')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P17', N'Balo Victor BR290', 'balo-victor-br290', 
            N'Balo đựng đồ thể thao đa năng', 
            680000.00, 620000.00, 35, 76, 1, 1, 'C4', 'ST3', 4.7, 234, 'https://via.placeholder.com/300x300?text=Victor+Backpack');
END;

-- Store 4 Products (4 products)
IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P11')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P11', N'Vợt Yonex Nanoflare 1000Z', 'vot-yonex-nanoflare-1000z', 
            N'Vợt cầu lông siêu nhẹ, tốc độ đánh nhanh', 
            5800000.00, 5500000.00, 12, 28, 1, 1, 'C1', 'ST4', 4.9, 167, 'https://via.placeholder.com/300x300?text=Yonex+Nanoflare');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P12')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P12', N'Giày Yonex Power Cushion 65Z', 'giay-yonex-power-cushion-65z', 
            N'Giày thi đấu cao cấp với công nghệ đệm', 
            3200000.00, 2950000.00, 20, 65, 1, 1, 'C3', 'ST4', 4.8, 198, 'https://via.placeholder.com/300x300?text=Yonex+65Z');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P13')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P13', N'Quả cầu Lining A+90', 'qua-cau-lining-a-90', 
            N'Quả cầu thi đấu quốc tế, độ bền cao', 
            220000.00, 200000.00, 150, 256, 1, 1, 'C2', 'ST4', 4.7, 389, 'https://via.placeholder.com/300x300?text=Lining+A90');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P14')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P14', N'Băng cổ tay Yonex AC489', 'bang-co-tay-yonex-ac489', 
            N'Băng thấm mồ hôi cao cấp', 
            120000.00, NULL, 200, 432, 1, 1, 'C4', 'ST4', 4.5, 567, 'https://via.placeholder.com/300x300?text=Yonex+Band');
END;

-- Store 5 Products (4 products)
IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P15')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P15', N'Vợt Victor Auraspeed 90K', 'vot-victor-auraspeed-90k', 
            N'Vợt tốc độ cao, phù hợp đánh đôi', 
            4200000.00, 3900000.00, 15, 42, 1, 1, 'C1', 'ST5', 4.8, 223, 'https://via.placeholder.com/300x300?text=Victor+90K');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P18')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P18', N'Giày Mizuno Wave Fang Pro', 'giay-mizuno-wave-fang-pro', 
            N'Giày chuyên nghiệp từ Nhật Bản', 
            2900000.00, NULL, 18, 53, 1, 1, 'C3', 'ST5', 4.7, 145, 'https://via.placeholder.com/300x300?text=Mizuno+Pro');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P19')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P19', N'Áo Victor T-90000', 'ao-victor-t-90000', 
            N'Áo thi đấu cao cấp, thoát nhiệt tốt', 
            520000.00, 480000.00, 45, 167, 1, 1, 'C5', 'ST5', 4.6, 234, 'https://via.placeholder.com/300x300?text=Victor+Shirt');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P20')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P20', N'Bao lưới cầu lông Yonex AC544', 'bao-luoi-cau-long-yonex-ac544', 
            N'Bao lưới chống va đập', 
            650000.00, 590000.00, 35, 89, 1, 1, 'C4', 'ST5', 4.5, 178, 'https://via.placeholder.com/300x300?text=Yonex+Net');
END;

-- Store 6 Products (4 products)
IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P21')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P21', N'Vợt Lining Windstorm 78', 'vot-lining-windstorm-78', 
            N'Vợt kiểm soát tốt, phù hợp mọi trình độ', 
            3200000.00, 2980000.00, 22, 71, 1, 1, 'C1', 'ST6', 4.7, 267, 'https://via.placeholder.com/300x300?text=Lining+78');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P22')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P22', N'Giày Yonex Aerus Z', 'giay-yonex-aerus-z', 
            N'Giày nhẹ nhất thế giới, 270g', 
            3500000.00, NULL, 14, 38, 1, 1, 'C3', 'ST6', 4.9, 189, 'https://via.placeholder.com/300x300?text=Yonex+Aerus');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P23')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P23', N'Quần Victor R-75204', 'quan-victor-r-75204', 
            N'Quần thi đấu chuyên nghiệp', 
            420000.00, 380000.00, 50, 145, 1, 1, 'C5', 'ST6', 4.6, 223, 'https://via.placeholder.com/300x300?text=Victor+Pants');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P24')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P24', N'Grip Yonex AC102EX', 'grip-yonex-ac102ex', 
            N'Quấn cán vợt cao cấp, chống trơn', 
            80000.00, NULL, 300, 678, 1, 1, 'C4', 'ST6', 4.7, 890, 'https://via.placeholder.com/300x300?text=Yonex+Grip');
END;

-- Store 7 Products (3 products)
IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P25')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P25', N'Vợt Apacs Feather Weight 55', 'vot-apacs-feather-weight-55', 
            N'Vợt nhẹ, giá tốt cho người mới', 
            1200000.00, 1050000.00, 30, 98, 1, 1, 'C1', 'ST7', 4.4, 234, 'https://via.placeholder.com/300x300?text=Apacs+55');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P26')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P26', N'Giày Lining AYTQ003', 'giay-lining-aytq003', 
            N'Giày tập luyện thoải mái', 
            1800000.00, 1650000.00, 25, 112, 1, 1, 'C3', 'ST7', 4.5, 178, 'https://via.placeholder.com/300x300?text=Lining+AYTQ');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P27')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P27', N'Áo Yonex 20649', 'ao-yonex-20649', 
            N'Áo tập luyện năng động', 
            380000.00, NULL, 60, 189, 1, 1, 'C5', 'ST7', 4.6, 267, 'https://via.placeholder.com/300x300?text=Yonex+20649');
END;

-- Store 8 Products (3 products)
IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P28')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P28', N'Vợt Yonex Voltric Z-Force II', 'vot-yonex-voltric-z-force-ii', 
            N'Vợt công thủ toàn diện', 
            4800000.00, 4500000.00, 10, 35, 1, 1, 'C1', 'ST8', 4.8, 198, 'https://via.placeholder.com/300x300?text=Yonex+ZForce');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P29')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P29', N'Quả cầu Victor Master Ace', 'qua-cau-victor-master-ace', 
            N'Quả cầu thi đấu chuẩn quốc tế', 
            195000.00, 180000.00, 120, 312, 1, 1, 'C2', 'ST8', 4.7, 456, 'https://via.placeholder.com/300x300?text=Victor+Shuttle');
END;

IF NOT EXISTS (SELECT 1 FROM products WHERE id = 'P30')
BEGIN
    INSERT INTO products (id, name, slug, description, price, promotional_price, quantity, sold, is_active, is_selling, category_id, store_id, rating, view_count, list_images)
    VALUES ('P30', N'Tất cầu lông Yonex 19120', 'tat-cau-long-yonex-19120', 
            N'Tất thể thao chuyên dụng', 
            95000.00, NULL, 150, 567, 1, 1, 'C4', 'ST8', 4.5, 678, 'https://via.placeholder.com/300x300?text=Yonex+Socks');
END;

PRINT 'Products inserted successfully - Total: 30 products';
GO
