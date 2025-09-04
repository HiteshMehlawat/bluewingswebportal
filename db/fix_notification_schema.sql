-- Fix notification_type column to be wide enough for enum values
ALTER TABLE notifications MODIFY COLUMN notification_type VARCHAR(50) NOT NULL;
