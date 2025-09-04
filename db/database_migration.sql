-- Database Migration Script for Document Status Update
-- This script updates the documents table to use a status enum instead of is_verified boolean

USE tax_consultancy_portal;

-- Step 1: Add new columns
ALTER TABLE documents 
ADD COLUMN status ENUM('PENDING', 'VERIFIED', 'REJECTED') DEFAULT 'PENDING' AFTER document_type,
ADD COLUMN rejected_by BIGINT NULL AFTER verified_at,
ADD COLUMN rejected_at TIMESTAMP NULL AFTER rejected_by,
ADD COLUMN rejection_reason TEXT NULL AFTER rejected_at;

-- Step 2: Add foreign key for rejected_by
ALTER TABLE documents 
ADD CONSTRAINT fk_documents_rejected_by 
FOREIGN KEY (rejected_by) REFERENCES users(id);

-- Step 3: Migrate existing data from is_verified to status
UPDATE documents 
SET status = CASE 
    WHEN is_verified = 1 THEN 'VERIFIED'
    ELSE 'PENDING'
END;

-- Step 4: Drop the old is_verified column
ALTER TABLE documents DROP COLUMN is_verified;

-- Step 5: Update indexes if needed
-- The existing indexes should still work with the new structure

-- Verify the migration
SELECT 
    COUNT(*) as total_documents,
    SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending_documents,
    SUM(CASE WHEN status = 'VERIFIED' THEN 1 ELSE 0 END) as verified_documents,
    SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejected_documents
FROM documents;
