-- Update lead_activities table to allow null user_id for public lead activities
USE tax_consultancy_portal;

-- First, let's see the current table structure and constraint names
SHOW CREATE TABLE lead_activities;

-- Find the foreign key constraint name for user_id
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE 
    TABLE_SCHEMA = 'tax_consultancy_portal' 
    AND TABLE_NAME = 'lead_activities' 
    AND REFERENCED_TABLE_NAME IS NOT NULL;

-- Alternative approach: Drop and recreate the constraint without specifying the name
-- This is safer as it doesn't rely on specific constraint names

-- Step 1: Drop the foreign key constraint (MySQL will find it automatically)
ALTER TABLE lead_activities DROP FOREIGN KEY (
    SELECT CONSTRAINT_NAME 
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = 'tax_consultancy_portal' 
    AND TABLE_NAME = 'lead_activities' 
    AND COLUMN_NAME = 'user_id' 
    AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);

-- Step 2: Modify the user_id column to allow NULL values
ALTER TABLE lead_activities MODIFY COLUMN user_id BIGINT NULL;

-- Step 3: Add the foreign key constraint back with ON DELETE SET NULL
ALTER TABLE lead_activities 
ADD CONSTRAINT fk_lead_activities_user 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
