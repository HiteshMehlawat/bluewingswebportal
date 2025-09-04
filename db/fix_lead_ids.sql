-- Fix existing lead IDs to use the new format
-- Run this in MySQL to fix the existing data

USE tax_consultancy_portal;

-- Update existing leads to have proper format if they don't already
UPDATE leads 
SET lead_id = CONCAT('LEAD-', YEAR(created_at), '-', LPAD(id, 3, '0'))
WHERE lead_id LIKE 'LEAD%' 
  AND lead_id NOT LIKE 'LEAD-%-%'
  AND lead_id NOT REGEXP '^LEAD-[0-9]{4}-[0-9]{3}$';

-- Update existing service requests to have proper format if they don't already
UPDATE service_requests 
SET request_id = CONCAT('SR-', YEAR(created_at), '-', LPAD(id, 3, '0'))
WHERE request_id LIKE 'SR%' 
  AND request_id NOT LIKE 'SR-%-%'
  AND request_id NOT REGEXP '^SR-[0-9]{4}-[0-9]{3}$';

-- Show the results
SELECT 'LEADS' as table_name, COUNT(*) as total_records FROM leads;
SELECT 'SERVICE_REQUESTS' as table_name, COUNT(*) as total_records FROM service_requests;
