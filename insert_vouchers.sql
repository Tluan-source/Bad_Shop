-- ==================== INSERT VOUCHERS DATA ====================
-- Script to insert sample voucher data into BadmintonMarketplace database
-- Run this script in SQL Server Management Studio

USE BadmintonMarketplace;
GO

-- Check if table exists, if not create it
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'vouchers')
BEGIN
    PRINT 'Creating vouchers table...';
    CREATE TABLE vouchers (
        id VARCHAR(50) PRIMARY KEY,
        code VARCHAR(50) NOT NULL UNIQUE,
        description NVARCHAR(500),
        discount_type VARCHAR(20),
        discount_value DECIMAL(15,2) DEFAULT 0,
        max_discount DECIMAL(15,2) DEFAULT 0,
        min_order_value DECIMAL(15,2) DEFAULT 0,
        quantity INT DEFAULT 0,
        usage_count INT DEFAULT 0,
        start_date DATETIME2,
        end_date DATETIME2,
        is_active BIT DEFAULT 1,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE()
    );
    PRINT 'Vouchers table created successfully!';
END
ELSE
BEGIN
    PRINT 'Vouchers table already exists.';
END
GO

-- Insert voucher data
PRINT 'Inserting voucher data...';

-- Voucher 1: WELCOME2024 (Active, 15% discount)
MERGE INTO vouchers AS target
USING (SELECT 'V1' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, code, description, discount_type, discount_value, max_discount, min_order_value, 
            quantity, usage_count, start_date, end_date, is_active, created_at, updated_at)
    VALUES ('V1', 'WELCOME2024', N'Giảm giá cho khách hàng mới - Áp dụng cho đơn hàng đầu tiên', 
            'PERCENTAGE', 15.00, 200000.00, 500000.00, 
            100, 35, 
            DATEADD(day, -30, GETDATE()), 
            DATEADD(day, 60, GETDATE()), 
            1,
            GETDATE(),
            GETDATE())
WHEN MATCHED THEN
    UPDATE SET 
        code = 'WELCOME2024',
        description = N'Giảm giá cho khách hàng mới - Áp dụng cho đơn hàng đầu tiên',
        discount_type = 'PERCENTAGE',
        discount_value = 15.00,
        max_discount = 200000.00,
        min_order_value = 500000.00,
        quantity = 100,
        usage_count = 35,
        start_date = DATEADD(day, -30, GETDATE()),
        end_date = DATEADD(day, 60, GETDATE()),
        is_active = 1,
        updated_at = GETDATE();

-- Voucher 2: FREESHIP100 (Active, fixed discount)
MERGE INTO vouchers AS target
USING (SELECT 'V2' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, code, description, discount_type, discount_value, max_discount, min_order_value, 
            quantity, usage_count, start_date, end_date, is_active, created_at, updated_at)
    VALUES ('V2', 'FREESHIP100', N'Miễn phí vận chuyển - Giảm 100,000đ phí ship', 
            'FIXED', 100000.00, 100000.00, 300000.00, 
            500, 278, 
            DATEADD(day, -20, GETDATE()), 
            DATEADD(day, 40, GETDATE()), 
            1,
            GETDATE(),
            GETDATE())
WHEN MATCHED THEN
    UPDATE SET 
        code = 'FREESHIP100',
        description = N'Miễn phí vận chuyển - Giảm 100,000đ phí ship',
        discount_type = 'FIXED',
        discount_value = 100000.00,
        max_discount = 100000.00,
        min_order_value = 300000.00,
        quantity = 500,
        usage_count = 278,
        start_date = DATEADD(day, -20, GETDATE()),
        end_date = DATEADD(day, 40, GETDATE()),
        is_active = 1,
        updated_at = GETDATE();

-- Voucher 3: SUMMER2024 (Expired, 20% discount)
MERGE INTO vouchers AS target
USING (SELECT 'V3' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, code, description, discount_type, discount_value, max_discount, min_order_value, 
            quantity, usage_count, start_date, end_date, is_active, created_at, updated_at)
    VALUES ('V3', 'SUMMER2024', N'Khuyến mãi mùa hè 2024 - Đã hết hạn', 
            'PERCENTAGE', 20.00, 500000.00, 1000000.00, 
            200, 200, 
            DATEADD(day, -90, GETDATE()), 
            DATEADD(day, -5, GETDATE()), 
            0,
            GETDATE(),
            GETDATE())
WHEN MATCHED THEN
    UPDATE SET 
        code = 'SUMMER2024',
        description = N'Khuyến mãi mùa hè 2024 - Đã hết hạn',
        discount_type = 'PERCENTAGE',
        discount_value = 20.00,
        max_discount = 500000.00,
        min_order_value = 1000000.00,
        quantity = 200,
        usage_count = 200,
        start_date = DATEADD(day, -90, GETDATE()),
        end_date = DATEADD(day, -5, GETDATE()),
        is_active = 0,
        updated_at = GETDATE();

-- Voucher 4: NEWYEAR2025 (Active, upcoming)
MERGE INTO vouchers AS target
USING (SELECT 'V4' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, code, description, discount_type, discount_value, max_discount, min_order_value, 
            quantity, usage_count, start_date, end_date, is_active, created_at, updated_at)
    VALUES ('V4', 'NEWYEAR2025', N'Chúc mừng năm mới 2025 - Giảm đặc biệt', 
            'PERCENTAGE', 25.00, 1000000.00, 2000000.00, 
            150, 0, 
            DATEADD(day, 5, GETDATE()), 
            DATEADD(day, 90, GETDATE()), 
            1,
            GETDATE(),
            GETDATE())
WHEN MATCHED THEN
    UPDATE SET 
        code = 'NEWYEAR2025',
        description = N'Chúc mừng năm mới 2025 - Giảm đặc biệt',
        discount_type = 'PERCENTAGE',
        discount_value = 25.00,
        max_discount = 1000000.00,
        min_order_value = 2000000.00,
        quantity = 150,
        usage_count = 0,
        start_date = DATEADD(day, 5, GETDATE()),
        end_date = DATEADD(day, 90, GETDATE()),
        is_active = 1,
        updated_at = GETDATE();

-- Voucher 5: FLASH50 (Active, limited quantity)
MERGE INTO vouchers AS target
USING (SELECT 'V5' AS id) AS source
ON target.id = source.id
WHEN NOT MATCHED THEN
    INSERT (id, code, description, discount_type, discount_value, max_discount, min_order_value, 
            quantity, usage_count, start_date, end_date, is_active, created_at, updated_at)
    VALUES ('V5', 'FLASH50', N'Flash Sale - Giảm 50,000đ cho đơn hàng nhỏ', 
            'FIXED', 50000.00, 50000.00, 200000.00, 
            50, 45, 
            DATEADD(day, -2, GETDATE()), 
            DATEADD(day, 3, GETDATE()), 
            1,
            GETDATE(),
            GETDATE())
WHEN MATCHED THEN
    UPDATE SET 
        code = 'FLASH50',
        description = N'Flash Sale - Giảm 50,000đ cho đơn hàng nhỏ',
        discount_type = 'FIXED',
        discount_value = 50000.00,
        max_discount = 50000.00,
        min_order_value = 200000.00,
        quantity = 50,
        usage_count = 45,
        start_date = DATEADD(day, -2, GETDATE()),
        end_date = DATEADD(day, 3, GETDATE()),
        is_active = 1,
        updated_at = GETDATE();

PRINT 'Voucher data inserted/updated successfully!';
GO

-- Verify data
PRINT 'Verifying inserted data...';
SELECT 
    id,
    code,
    description,
    discount_type,
    discount_value,
    max_discount,
    min_order_value,
    quantity,
    usage_count,
    CONVERT(VARCHAR, start_date, 103) AS start_date,
    CONVERT(VARCHAR, end_date, 103) AS end_date,
    is_active,
    CASE 
        WHEN end_date < GETDATE() THEN 'EXPIRED'
        WHEN start_date > GETDATE() THEN 'UPCOMING'
        WHEN is_active = 1 THEN 'ACTIVE'
        ELSE 'INACTIVE'
    END AS status
FROM vouchers
ORDER BY created_at DESC;

PRINT 'Script completed successfully!';
GO
