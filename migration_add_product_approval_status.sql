-- Migration: Add approval status fields to products table
-- Date: 2025-11-30
-- Purpose: Add product moderation system

-- Step 1: Add new columns (if not exists - Hibernate may have created them)
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'products' AND COLUMN_NAME = 'approval_status')
BEGIN
    ALTER TABLE products ADD approval_status INT NULL;
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'products' AND COLUMN_NAME = 'rejection_reason')
BEGIN
    ALTER TABLE products ADD rejection_reason NVARCHAR(1000) NULL;
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'products' AND COLUMN_NAME = 'moderated_by')
BEGIN
    ALTER TABLE products ADD moderated_by VARCHAR(255) NULL;
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'products' AND COLUMN_NAME = 'moderated_at')
BEGIN
    ALTER TABLE products ADD moderated_at DATETIME2 NULL;
END

-- Step 2: Update existing products
-- Set approval_status for existing products based on is_active status
-- PENDING = 0, APPROVED = 1, REJECTED = 2

-- Products that are active -> set as APPROVED (1)
UPDATE products 
SET approval_status = 1, 
    moderated_by = 'system', 
    moderated_at = GETDATE()
WHERE is_active = 1 AND approval_status IS NULL;

-- Products that are not active -> set as PENDING (0) 
UPDATE products 
SET approval_status = 0
WHERE is_active = 0 AND approval_status IS NULL;

-- Step 3: Set NOT NULL constraint with default value for new products
ALTER TABLE products ALTER COLUMN approval_status INT NOT NULL;

-- Add default constraint for new records
IF NOT EXISTS (SELECT * FROM sys.default_constraints WHERE name = 'DF_products_approval_status')
BEGIN
    ALTER TABLE products ADD CONSTRAINT DF_products_approval_status DEFAULT 0 FOR approval_status;
END

-- Verify the migration
SELECT 
    COUNT(*) as total_products,
    SUM(CASE WHEN approval_status = 0 THEN 1 ELSE 0 END) as pending,
    SUM(CASE WHEN approval_status = 1 THEN 1 ELSE 0 END) as approved,
    SUM(CASE WHEN approval_status = 2 THEN 1 ELSE 0 END) as rejected
FROM products;
