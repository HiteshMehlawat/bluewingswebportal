-- Manual SQL script to update notifications table for lead notifications
-- Run this in your MySQL database

USE tax_consultancy_portal;

-- Update the notification_type enum to include lead-related types
ALTER TABLE notifications 
MODIFY COLUMN notification_type ENUM(
    'TASK_ASSIGNED', 
    'STATUS_UPDATE', 
    'DOCUMENT_UPLOADED', 
    'DOCUMENT_VERIFIED', 
    'DOCUMENT_REJECTED', 
    'DEADLINE_REMINDER', 
    'STAFF_ASSIGNED', 
    'TASK_COMPLETED', 
    'TASK_ACKNOWLEDGED', 
    'SYSTEM', 
    'EMAIL', 
    'MESSAGE_RECEIVED', 
    'LEAD_ASSIGNED', 
    'NEW_LEAD_CREATED',
    'LEAD_CONVERTED'
) NOT NULL;

-- Verify the update
SELECT 'Notifications table updated successfully!' as status;
