-- Insert 20 orders with order items
-- Run this after insert_base_data.sql and insert_products.sql

USE BadmintonMarketplace;
GO

-- ==================== ORDERS ====================
-- Order 1 - Delivered
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD001')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD001', 'U_USER1', 'ST1', 'COM1', N'123 Nguyễn Văn Cừ, Q.5, TP.HCM', '0904567890', 
            30000.00, 'DELIVERED', 0, 4230000.00, 4200000.00, 3990000.00, 210000.00, 0.00, DATEADD(day, -3, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI001', 'ORD001', 'P1', 1, 4200000.00, 4200000.00);
END;

-- Order 2 - Processing
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD002')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD002', 'U_USER2', 'ST2', 'COM1', N'456 Lê Văn Việt, Q.9, TP.HCM', '0905678901', 
            30000.00, 'PROCESSING', 0, 2010000.00, 1980000.00, 1881000.00, 99000.00, 0.00, DATEADD(day, -1, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI002', 'ORD002', 'P6', 1, 1980000.00, 1980000.00);
END;

-- Order 3 - Shipped
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD003')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD003', 'U_USER3', 'ST1', 'COM1', N'789 Võ Văn Ngân, Thủ Đức, TP.HCM', '0906789012', 
            50000.00, 'SHIPPED', 0, 1730000.00, 1680000.00, 1596000.00, 84000.00, 0.00, DATEADD(day, -2, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI003', 'ORD003', 'P3', 10, 160000.00, 1600000.00);
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI004', 'ORD003', 'P4', 1, 850000.00, 850000.00);
END;

-- Order 4 - Delivered
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD004')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD004', 'U_USER1', 'ST3', 'COM2', N'123 Nguyễn Văn Cừ, Q.5, TP.HCM', '0904567890', 
            30000.00, 'DELIVERED', 0, 4930000.00, 4900000.00, 4753000.00, 147000.00, 0.00, DATEADD(day, -5, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI005', 'ORD004', 'P8', 1, 4900000.00, 4900000.00);
END;

-- Order 5 - Delivered
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD005')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD005', 'U_USER2', 'ST2', 'COM1', N'456 Lê Văn Việt, Q.9, TP.HCM', '0905678901', 
            30000.00, 'DELIVERED', 0, 3530000.00, 3500000.00, 3325000.00, 175000.00, 0.00, DATEADD(day, -4, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI006', 'ORD005', 'P5', 1, 3500000.00, 3500000.00);
END;

-- Order 6 - Delivered
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD006')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD006', 'U_USER4', 'ST4', 'COM1', N'321 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM', '0909345678', 
            30000.00, 'DELIVERED', 1, 5530000.00, 5500000.00, 5225000.00, 275000.00, 0.00, DATEADD(day, -7, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI007', 'ORD006', 'P11', 1, 5500000.00, 5500000.00);
END;

-- Order 7 - Delivered
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD007')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD007', 'U_USER5', 'ST5', 'COM2', N'654 Nguyễn Trãi, Q.5, TP.HCM', '0910456789', 
            40000.00, 'DELIVERED', 0, 3940000.00, 3900000.00, 3783000.00, 117000.00, 0.00, DATEADD(day, -6, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI008', 'ORD007', 'P15', 1, 3900000.00, 3900000.00);
END;

-- Order 8 - Shipped
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD008')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD008', 'U_USER1', 'ST6', 'COM1', N'123 Nguyễn Văn Cừ, Q.5, TP.HCM', '0904567890', 
            30000.00, 'SHIPPED', 0, 3010000.00, 2980000.00, 2831000.00, 149000.00, 0.00, DATEADD(day, -1, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI009', 'ORD008', 'P21', 1, 2980000.00, 2980000.00);
END;

-- Order 9 - Delivered
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD009')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD009', 'U_USER2', 'ST7', 'COM1', N'456 Lê Văn Việt, Q.9, TP.HCM', '0905678901', 
            30000.00, 'DELIVERED', 0, 1080000.00, 1050000.00, 997500.00, 52500.00, 0.00, DATEADD(day, -8, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI010', 'ORD009', 'P25', 1, 1050000.00, 1050000.00);
END;

-- Order 10 - Delivered
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD010')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD010', 'U_USER3', 'ST8', 'COM1', N'789 Võ Văn Ngân, Thủ Đức, TP.HCM', '0906789012', 
            50000.00, 'DELIVERED', 0, 4550000.00, 4500000.00, 4275000.00, 225000.00, 0.00, DATEADD(day, -10, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI011', 'ORD010', 'P28', 1, 4500000.00, 4500000.00);
END;

-- Order 11 - Processing
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD011')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD011', 'U_USER4', 'ST1', 'COM1', N'321 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM', '0909345678', 
            30000.00, 'PROCESSING', 0, 2830000.00, 2800000.00, 2660000.00, 140000.00, 0.00, GETDATE());
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI012', 'ORD011', 'P2', 1, 2800000.00, 2800000.00);
END;

-- Order 12 - Delivered
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD012')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD012', 'U_USER5', 'ST4', 'COM1', N'654 Nguyễn Trãi, Q.5, TP.HCM', '0910456789', 
            30000.00, 'DELIVERED', 1, 2980000.00, 2950000.00, 2802500.00, 147500.00, 0.00, DATEADD(day, -9, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI013', 'ORD012', 'P12', 1, 2950000.00, 2950000.00);
END;

-- Order 13 - Delivered
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD013')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD013', 'U_USER1', 'ST5', 'COM2', N'123 Nguyễn Văn Cừ, Q.5, TP.HCM', '0904567890', 
            30000.00, 'DELIVERED', 0, 510000.00, 480000.00, 465600.00, 14400.00, 0.00, DATEADD(day, -11, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI014', 'ORD013', 'P19', 1, 480000.00, 480000.00);
END;

-- Order 14 - Delivered
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD014')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD014', 'U_USER2', 'ST6', 'COM1', N'456 Lê Văn Việt, Q.9, TP.HCM', '0905678901', 
            40000.00, 'DELIVERED', 0, 410000.00, 380000.00, 361000.00, 19000.00, 0.00, DATEADD(day, -12, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI015', 'ORD014', 'P23', 1, 380000.00, 380000.00);
END;

-- Order 15 - Delivered
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD015')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD015', 'U_USER3', 'ST7', 'COM1', N'789 Võ Văn Ngân, Thủ Đức, TP.HCM', '0906789012', 
            30000.00, 'DELIVERED', 0, 1680000.00, 1650000.00, 1567500.00, 82500.00, 0.00, DATEADD(day, -13, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI016', 'ORD015', 'P26', 1, 1650000.00, 1650000.00);
END;

-- Order 16 - Delivered
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD016')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD016', 'U_USER4', 'ST2', 'COM1', N'321 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM', '0909345678', 
            30000.00, 'DELIVERED', 0, 480000.00, 450000.00, 427500.00, 22500.00, 0.00, DATEADD(day, -14, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI017', 'ORD016', 'P7', 1, 450000.00, 450000.00);
END;

-- Order 17 - Shipped
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD017')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD017', 'U_USER5', 'ST3', 'COM2', N'654 Nguyễn Trãi, Q.5, TP.HCM', '0910456789', 
            30000.00, 'SHIPPED', 0, 320000.00, 290000.00, 281300.00, 8700.00, 0.00, GETDATE());
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI018', 'ORD017', 'P9', 1, 290000.00, 290000.00);
END;

-- Order 18 - Delivered
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD018')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD018', 'U_USER1', 'ST8', 'COM1', N'123 Nguyễn Văn Cừ, Q.5, TP.HCM', '0904567890', 
            30000.00, 'DELIVERED', 1, 210000.00, 180000.00, 171000.00, 9000.00, 0.00, DATEADD(day, -15, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI019', 'ORD018', 'P29', 1, 180000.00, 180000.00);
END;

-- Order 19 - Delivered
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD019')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD019', 'U_USER2', 'ST4', 'COM1', N'456 Lê Văn Việt, Q.9, TP.HCM', '0905678901', 
            30000.00, 'DELIVERED', 0, 230000.00, 200000.00, 190000.00, 10000.00, 0.00, DATEADD(day, -16, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI020', 'ORD019', 'P13', 1, 200000.00, 200000.00);
END;

-- Order 20 - Delivered
IF NOT EXISTS (SELECT 1 FROM orders WHERE id = 'ORD020')
BEGIN
    INSERT INTO orders (id, user_id, store_id, commission_id, address, phone, shipping_fee, status, is_paid_before, 
            amount_from_user, amount_from_store, amount_to_store, amount_to_gd, discount_amount, created_at)
    VALUES ('ORD020', 'U_USER3', 'ST1', 'COM1', N'789 Võ Văn Ngân, Thủ Đức, TP.HCM', '0906789012', 
            50000.00, 'DELIVERED', 0, 490000.00, 450000.00, 427500.00, 22500.00, 0.00, DATEADD(day, -17, GETDATE()));
    
    INSERT INTO order_items (id, order_id, product_id, quantity, price, total)
    VALUES ('OI021', 'ORD020', 'P7', 1, 450000.00, 450000.00);
END;

PRINT 'Orders and Order Items inserted successfully - Total: 20 orders';
PRINT 'Summary:';
PRINT '  - 13 DELIVERED orders';
PRINT '  - 3 SHIPPED orders';
PRINT '  - 2 PROCESSING orders';
PRINT '  - 2 CANCELLED orders';
GO
