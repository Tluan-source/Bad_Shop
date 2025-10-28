-- ==============================================================
-- SỬA CONSTRAINT CHO BẢNG SHIPMENTS - CHO PHÉP SHIPPER_ID NULL
-- ==============================================================

USE BadmintonMarketplace;
GO

-- Bước 1: Xóa constraint NOT NULL cho cột shipper_id
ALTER TABLE shipments
ALTER COLUMN shipper_id VARCHAR(255) NULL;
GO

PRINT 'Đã sửa constraint cho shipper_id - bây giờ có thể NULL';
