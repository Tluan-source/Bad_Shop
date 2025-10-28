-- Insert sample orders for testing
-- Run this in SQL Server Management Studio or similar tool

USE BadmintonMarketplace;
GO

-- Insert sample orders for user U_USER1
INSERT INTO orders (id, user_id, store_id, address, phone, status, is_paid_before, amount_from_user, amount_from_store, amount_to_store, created_at)
VALUES
('ORD_TEST_001', 'U_USER1', 'ST4', N'123 Đường ABC, Quận 1, TP.HCM', '0901234567', 'DELIVERED', 0, 1500000.00, 1500000.00, 1425000.00, DATEADD(day, -1, GETDATE())),
('ORD_TEST_002', 'U_USER1', 'ST5', N'456 Đường XYZ, Quận 2, TP.HCM', '0901234567', 'SHIPPED', 0, 2500000.00, 2500000.00, 2375000.00, DATEADD(day, -2, GETDATE())),
('ORD_TEST_003', 'U_USER1', 'ST6', N'789 Đường DEF, Quận 3, TP.HCM', '0901234567', 'NOT_PROCESSED', 0, 3200000.00, 3200000.00, 3040000.00, DATEADD(day, -3, GETDATE()));

-- Insert order items for the orders above
INSERT INTO order_items (id, order_id, product_id, style_value_ids, quantity, price, total)
VALUES
-- Order ORD_TEST_001 - 1 product
('OI_TEST_001', 'ORD_TEST_001', 'P_364A79AA', '["SV001","SV002"]', 1, 1500000.00, 1500000.00),

-- Order ORD_TEST_002 - 2 products
('OI_TEST_002', 'ORD_TEST_002', 'P_80EAE588', '["SV003"]', 1, 1200000.00, 1200000.00),
('OI_TEST_003', 'ORD_TEST_002', 'P_A50D3445', '["SV004","SV005"]', 1, 1300000.00, 1300000.00),

-- Order ORD_TEST_003 - 3 products
('OI_TEST_004', 'ORD_TEST_003', 'P_AE966477', '["SV001"]', 2, 800000.00, 1600000.00),
('OI_TEST_005', 'ORD_TEST_003', 'P_364A79AA', '["SV002","SV003"]', 1, 1200000.00, 1200000.00),
('OI_TEST_006', 'ORD_TEST_003', 'P_80EAE588', NULL, 1, 400000.00, 400000.00);

-- Insert payments for the orders
INSERT INTO payments (id, order_id, amount, status, payment_method, created_at)
VALUES
('PAY_TEST_001', 'ORD_TEST_001', 1500000.00, 'PAID', 'COD', DATEADD(day, -1, GETDATE())),
('PAY_TEST_002', 'ORD_TEST_002', 2500000.00, 'PAID', 'VNPAY', DATEADD(day, -2, GETDATE())),
('PAY_TEST_003', 'ORD_TEST_003', 3200000.00, 'PENDING', 'COD', DATEADD(day, -3, GETDATE()));

PRINT 'Sample orders inserted successfully!';
GO