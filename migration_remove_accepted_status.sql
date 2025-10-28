-- ==============================================================
-- MIGRATION: BỎ TRẠNG THÁI ACCEPTED - THAY BẰNG PENDING
-- ==============================================================
-- Lý do: Shipper sẽ nhận trực tiếp các đơn PROCESSING từ Order
--        Không cần trạng thái ACCEPTED nữa
-- ==============================================================

USE BadmintonMarketplace;
GO

-- Bước 1: Cập nhật tất cả các shipment có status = 'ACCEPTED' thành 'PENDING'
UPDATE shipments
SET status = 'PENDING'
WHERE status = 'ACCEPTED';
GO

PRINT 'Đã cập nhật tất cả shipments ACCEPTED -> PENDING';

-- Bước 2: Cập nhật tất cả các shipment có status = 'ASSIGNED' thành 'PENDING'
UPDATE shipments
SET status = 'PENDING'
WHERE status = 'ASSIGNED';
GO

PRINT 'Đã cập nhật tất cả shipments ASSIGNED -> PENDING';

-- Bước 3: Xóa cột accepted_at (không còn dùng nữa)
-- Lưu ý: Chỉ chạy khi chắc chắn không cần dữ liệu này
-- ALTER TABLE shipments
-- DROP COLUMN accepted_at;
-- GO
-- 
-- PRINT 'Đã xóa cột accepted_at';

-- Bước 4: Kiểm tra kết quả
SELECT status, COUNT(*) as count
FROM shipments
GROUP BY status
ORDER BY status;
GO

PRINT 'Migration hoàn tất! Các trạng thái Shipment hiện tại:';
PRINT 'PENDING - Chờ shipper nhận';
PRINT 'DELIVERING - Đang giao hàng';
PRINT 'DELIVERED - Đã giao thành công';
PRINT 'FAILED - Giao hàng thất bại';
