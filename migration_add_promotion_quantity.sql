-- ============================================
-- MIGRATION: Thêm quantity và usage_count vào bảng promotions
-- ============================================

USE BadmintonMarketplace;
GO

-- Thêm cột quantity và usage_count vào bảng promotions
ALTER TABLE promotions
ADD quantity INT NULL,
    usage_count INT NULL;
GO

-- Set giá trị mặc định cho các promotion hiện có
UPDATE promotions
SET quantity = 999999,  -- Số lượng lớn để không giới hạn
    usage_count = 0
WHERE quantity IS NULL;
GO

-- Thay đổi cột thành NOT NULL với giá trị mặc định
ALTER TABLE promotions
ALTER COLUMN quantity INT NOT NULL;
GO

ALTER TABLE promotions
ALTER COLUMN usage_count INT NOT NULL;
GO

-- Thêm constraint để đảm bảo usage_count <= quantity
ALTER TABLE promotions
ADD CONSTRAINT CHK_Promotion_UsageCount CHECK (usage_count <= quantity);
GO

PRINT 'Migration completed successfully!';
PRINT 'Added quantity and usage_count columns to promotions table';

-- Hiển thị cấu trúc bảng sau khi update
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'promotions'
ORDER BY ORDINAL_POSITION;
GO
