-- Tạo bảng activity_logs để lưu trữ lịch sử hoạt động của người dùng (SQL Server)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'activity_logs')
BEGIN
    CREATE TABLE activity_logs (
        id VARCHAR(255) PRIMARY KEY,
        user_id VARCHAR(255) NOT NULL,
        activity_type VARCHAR(50) NOT NULL,
        description NVARCHAR(500) NOT NULL,
        ip_address VARCHAR(50),
        user_agent VARCHAR(500),
        target_entity VARCHAR(100),
        target_id VARCHAR(255),
        created_at DATETIME NOT NULL,
        CONSTRAINT FK_activity_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );
    
    -- Tạo các index
    CREATE INDEX idx_user_created ON activity_logs(user_id, created_at DESC);
    CREATE INDEX idx_activity_type ON activity_logs(activity_type);
    CREATE INDEX idx_created_at ON activity_logs(created_at DESC);
END
GO

-- Thêm một số dữ liệu mẫu cho admin
DECLARE @admin_id VARCHAR(255);
SELECT TOP 1 @admin_id = id FROM users WHERE role = 'ADMIN';

IF @admin_id IS NOT NULL
BEGIN
    -- Xóa dữ liệu cũ nếu có
    DELETE FROM activity_logs WHERE user_id = @admin_id;
    
    -- Thêm dữ liệu mẫu
    INSERT INTO activity_logs (id, user_id, activity_type, description, ip_address, created_at)
    VALUES 
        (CONCAT('AL_', CAST(DATEDIFF(SECOND, '1970-01-01', GETDATE()) AS VARCHAR), '_', CAST(FLOOR(RAND() * 1000) AS VARCHAR)),
         @admin_id,
         'LOGIN',
         N'Đăng nhập vào hệ thống',
         '192.168.1.1',
         DATEADD(HOUR, -2, GETDATE()));
    
    INSERT INTO activity_logs (id, user_id, activity_type, description, ip_address, created_at)
    VALUES 
        (CONCAT('AL_', CAST(DATEDIFF(SECOND, '1970-01-01', GETDATE()) + 1 AS VARCHAR), '_', CAST(FLOOR(RAND() * 1000) AS VARCHAR)),
         @admin_id,
         'VIEW_DASHBOARD',
         N'Xem trang dashboard',
         '192.168.1.1',
         DATEADD(HOUR, -1, GETDATE()));
    
    INSERT INTO activity_logs (id, user_id, activity_type, description, ip_address, created_at)
    VALUES 
        (CONCAT('AL_', CAST(DATEDIFF(SECOND, '1970-01-01', GETDATE()) + 2 AS VARCHAR), '_', CAST(FLOOR(RAND() * 1000) AS VARCHAR)),
         @admin_id,
         'VIEW_REPORT',
         N'Xem báo cáo thống kê',
         '192.168.1.1',
         DATEADD(MINUTE, -30, GETDATE()));
    
    INSERT INTO activity_logs (id, user_id, activity_type, description, ip_address, created_at)
    VALUES 
        (CONCAT('AL_', CAST(DATEDIFF(SECOND, '1970-01-01', GETDATE()) + 3 AS VARCHAR), '_', CAST(FLOOR(RAND() * 1000) AS VARCHAR)),
         @admin_id,
         'UPDATE_PROFILE',
         N'Cập nhật thông tin cá nhân',
         '192.168.1.1',
         DATEADD(MINUTE, -15, GETDATE()));
    
    INSERT INTO activity_logs (id, user_id, activity_type, description, ip_address, created_at)
    VALUES 
        (CONCAT('AL_', CAST(DATEDIFF(SECOND, '1970-01-01', GETDATE()) + 4 AS VARCHAR), '_', CAST(FLOOR(RAND() * 1000) AS VARCHAR)),
         @admin_id,
         'VIEW_DASHBOARD',
         N'Xem trang thông tin cá nhân',
         '192.168.1.1',
         DATEADD(MINUTE, -5, GETDATE()));
END
GO

PRINT N'Bảng activity_logs đã được tạo và thêm dữ liệu mẫu thành công!';