-- Fix Service Requests Table
-- Run this in MySQL to fix the table structure

USE tax_consultancy_portal;

-- Drop the problematic triggers first
DROP TRIGGER IF EXISTS service_request_after_insert;
DROP TRIGGER IF EXISTS service_request_after_update;

-- Drop the problematic views
DROP VIEW IF EXISTS service_requests_with_details;
DROP VIEW IF EXISTS overdue_service_requests;
DROP VIEW IF EXISTS service_requests_due_soon;

-- Drop the problematic procedures
DROP PROCEDURE IF EXISTS GetServiceRequestStatistics;
DROP PROCEDURE IF EXISTS GetClientServiceRequestStatistics;
DROP PROCEDURE IF EXISTS GetStaffServiceRequestStatistics;

-- Drop the existing table if it exists
DROP TABLE IF EXISTS service_requests;

-- Create the service_requests table with correct structure
CREATE TABLE service_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id VARCHAR(100) NOT NULL UNIQUE,
    service_category_name VARCHAR(255),
    service_subcategory_name VARCHAR(255),
    service_item_id BIGINT,
    service_item_name VARCHAR(255),
    description TEXT,
    notes TEXT,
    preferred_deadline DATE,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    status ENUM('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'REJECTED') DEFAULT 'PENDING',
    rejection_reason TEXT,
    admin_notes TEXT,
    staff_notes TEXT,
    estimated_price DECIMAL(10,2),
    final_price DECIMAL(10,2),
    assigned_date DATETIME(6),
    completed_date DATETIME(6),
    rejected_date DATETIME(6),
    client_id BIGINT NOT NULL,
    assigned_staff_id BIGINT,
    accepted_by BIGINT,
    assigned_by BIGINT,
    rejected_by BIGINT,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    -- Foreign Key Constraints
    FOREIGN KEY (service_item_id) REFERENCES service_items(id) ON DELETE SET NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_staff_id) REFERENCES staff(id) ON DELETE SET NULL,
    FOREIGN KEY (accepted_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (rejected_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    
    -- Indexes for better performance
    INDEX idx_request_id (request_id),
    INDEX idx_client_id (client_id),
    INDEX idx_assigned_staff_id (assigned_staff_id),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_preferred_deadline (preferred_deadline),
    INDEX idx_created_at (created_at),
    INDEX idx_service_item_id (service_item_id),
    INDEX idx_created_by (created_by),
    INDEX idx_assigned_date (assigned_date),
    INDEX idx_completed_date (completed_date),
    INDEX idx_rejected_date (rejected_date)
);

-- Verify the table was created successfully
SELECT 'Service Requests table created successfully!' as status;
DESCRIBE service_requests;
