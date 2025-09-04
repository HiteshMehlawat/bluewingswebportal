-- Test script to verify notifications table works with new types
USE tax_consultancy_portal;

-- Check current notification types
SELECT DISTINCT notification_type FROM notifications;

-- Test inserting a lead assignment notification
INSERT INTO notifications (user_id, title, message, notification_type, is_read, created_at) 
VALUES (1, 'Test Lead Assigned', 'Test notification for lead assignment', 'LEAD_ASSIGNED', FALSE, NOW());

-- Test inserting a new lead notification
INSERT INTO notifications (user_id, title, message, notification_type, is_read, created_at) 
VALUES (1, 'Test New Lead', 'Test notification for new lead', 'NEW_LEAD_CREATED', FALSE, NOW());

-- Verify the insertions
SELECT * FROM notifications WHERE notification_type IN ('LEAD_ASSIGNED', 'NEW_LEAD_CREATED') ORDER BY created_at DESC LIMIT 5;

-- Clean up test data
DELETE FROM notifications WHERE title LIKE 'Test%';
