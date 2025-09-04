-- ID Generation Migration Script
-- Tax Consultancy Web Portal
-- Created: January 2025

USE tax_consultancy_portal;

-- =====================================================
-- UPDATE LEADS TABLE FOR SEQUENTIAL ID GENERATION
-- =====================================================

-- Update existing leads to have proper format if they don't already
-- This will update any leads that have the old timestamp-based format
UPDATE leads 
SET lead_id = CONCAT('LEAD-', YEAR(created_at), '-', LPAD(id, 3, '0'))
WHERE lead_id LIKE 'LEAD%' 
  AND lead_id NOT LIKE 'LEAD-%-%'
  AND lead_id NOT REGEXP '^LEAD-[0-9]{4}-[0-9]{3}$';

-- =====================================================
-- UPDATE SERVICE_REQUESTS TABLE FOR SEQUENTIAL ID GENERATION
-- =====================================================

-- Update existing service requests to have proper format if they don't already
-- This will update any service requests that have the old timestamp-based format
UPDATE service_requests 
SET request_id = CONCAT('SR-', YEAR(created_at), '-', LPAD(id, 3, '0'))
WHERE request_id LIKE 'SR%' 
  AND request_id NOT LIKE 'SR-%-%'
  AND request_id NOT REGEXP '^SR-[0-9]{4}-[0-9]{3}$';

-- =====================================================
-- CREATE INDEXES FOR BETTER PERFORMANCE
-- =====================================================

-- Add indexes for the new ID format queries
CREATE INDEX IF NOT EXISTS idx_leads_lead_id_year ON leads(lead_id) WHERE lead_id REGEXP '^LEAD-[0-9]{4}-[0-9]{3}$';
CREATE INDEX IF NOT EXISTS idx_service_requests_request_id_year ON service_requests(request_id) WHERE request_id REGEXP '^SR-[0-9]{4}-[0-9]{3}$';

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Verify the format of existing leads
SELECT 
    'LEADS' as table_name,
    COUNT(*) as total_records,
    COUNT(CASE WHEN lead_id REGEXP '^LEAD-[0-9]{4}-[0-9]{3}$' THEN 1 END) as properly_formatted,
    COUNT(CASE WHEN lead_id NOT REGEXP '^LEAD-[0-9]{4}-[0-9]{3}$' THEN 1 END) as needs_fixing
FROM leads;

-- Verify the format of existing service requests
SELECT 
    'SERVICE_REQUESTS' as table_name,
    COUNT(*) as total_records,
    COUNT(CASE WHEN request_id REGEXP '^SR-[0-9]{4}-[0-9]{3}$' THEN 1 END) as properly_formatted,
    COUNT(CASE WHEN request_id NOT REGEXP '^SR-[0-9]{4}-[0-9]{3}$' THEN 1 END) as needs_fixing
FROM service_requests;

-- Show sample of properly formatted IDs
SELECT 'LEADS' as table_name, lead_id, created_at FROM leads WHERE lead_id REGEXP '^LEAD-[0-9]{4}-[0-9]{3}$' ORDER BY created_at DESC LIMIT 5;
SELECT 'SERVICE_REQUESTS' as table_name, request_id, created_at FROM service_requests WHERE request_id REGEXP '^SR-[0-9]{4}-[0-9]{3}$' ORDER BY created_at DESC LIMIT 5;
