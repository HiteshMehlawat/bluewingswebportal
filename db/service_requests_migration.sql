-- Service Requests Table Migration
-- Tax Consultancy Web Portal
-- Created: January 2025

USE tax_consultancy_portal;

-- =====================================================
-- SERVICE_REQUESTS TABLE
-- =====================================================
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

-- =====================================================
-- SAMPLE DATA FOR TESTING (Optional)
-- =====================================================
-- Uncomment the following section to add sample data for testing

/*
INSERT INTO service_requests (
    request_id, 
    service_category_name, 
    service_subcategory_name, 
    service_item_name,
    description, 
    notes, 
    preferred_deadline, 
    priority, 
    status, 
    client_id, 
    created_by
) VALUES 
(
    'SR1704067200001',
    'TAX ADVISORY',
    'GENERAL ADVISORY',
    'Tax Planning Consultation',
    'Need consultation for tax planning for the upcoming financial year',
    'Client is looking for comprehensive tax planning advice',
    '2025-02-15',
    'HIGH',
    'PENDING',
    1, -- Assuming client with ID 1 exists
    1  -- Assuming user with ID 1 exists
),
(
    'SR1704067200002',
    'GST SERVICES',
    'GST FILING',
    'GST Return Filing',
    'Monthly GST return filing for March 2025',
    'Regular monthly filing, all documents ready',
    '2025-04-20',
    'MEDIUM',
    'ASSIGNED',
    1, -- Assuming client with ID 1 exists
    1  -- Assuming user with ID 1 exists
),
(
    'SR1704067200003',
    'COMPANY REGISTRATION',
    'PRIVATE LIMITED',
    'Private Limited Company Registration',
    'New company registration for tech startup',
    'Startup in IT sector, need quick registration',
    '2025-03-30',
    'URGENT',
    'IN_PROGRESS',
    2, -- Assuming client with ID 2 exists
    2  -- Assuming user with ID 2 exists
);
*/

-- =====================================================
-- VIEWS FOR COMMON QUERIES
-- =====================================================

-- View for service requests with client and staff details
CREATE VIEW service_requests_with_details AS
SELECT 
    sr.*,
    CONCAT(cu.first_name, ' ', cu.last_name) as client_name,
    cu.email as client_email,
    cu.phone as client_phone,
    c.company_name,
    CONCAT(su.first_name, ' ', su.last_name) as assigned_staff_name,
    su.email as assigned_staff_email,
    s.employee_id as assigned_staff_employee_id,
    CONCAT(abu.first_name, ' ', abu.last_name) as accepted_by_name,
    CONCAT(asu.first_name, ' ', asu.last_name) as assigned_by_name,
    CONCAT(rbu.first_name, ' ', rbu.last_name) as rejected_by_name,
    CONCAT(cbu.first_name, ' ', cbu.last_name) as created_by_name,
    CONCAT(ubu.first_name, ' ', ubu.last_name) as updated_by_name
FROM service_requests sr
LEFT JOIN clients c ON sr.client_id = c.id
LEFT JOIN users cu ON c.user_id = cu.id
LEFT JOIN staff s ON sr.assigned_staff_id = s.id
LEFT JOIN users su ON s.user_id = su.id
LEFT JOIN users abu ON sr.accepted_by = abu.id
LEFT JOIN users asu ON sr.assigned_by = asu.id
LEFT JOIN users rbu ON sr.rejected_by = rbu.id
LEFT JOIN users cbu ON sr.created_by = cbu.id
LEFT JOIN users ubu ON sr.updated_by = ubu.id;

-- View for overdue service requests
CREATE VIEW overdue_service_requests AS
SELECT 
    sr.*,
    CONCAT(cu.first_name, ' ', cu.last_name) as client_name,
    c.company_name,
    CONCAT(su.first_name, ' ', su.last_name) as assigned_staff_name,
    DATEDIFF(CURDATE(), sr.preferred_deadline) as days_overdue
FROM service_requests sr
LEFT JOIN clients c ON sr.client_id = c.id
LEFT JOIN users cu ON c.user_id = cu.id
LEFT JOIN staff s ON sr.assigned_staff_id = s.id
LEFT JOIN users su ON s.user_id = su.id
WHERE sr.preferred_deadline < CURDATE() 
AND sr.status NOT IN ('COMPLETED', 'CANCELLED', 'REJECTED');

-- View for service requests due soon (within 7 days)
CREATE VIEW service_requests_due_soon AS
SELECT 
    sr.*,
    CONCAT(cu.first_name, ' ', cu.last_name) as client_name,
    c.company_name,
    CONCAT(su.first_name, ' ', su.last_name) as assigned_staff_name,
    DATEDIFF(sr.preferred_deadline, CURDATE()) as days_remaining
FROM service_requests sr
LEFT JOIN clients c ON sr.client_id = c.id
LEFT JOIN users cu ON c.user_id = cu.id
LEFT JOIN staff s ON sr.assigned_staff_id = s.id
LEFT JOIN users su ON s.user_id = su.id
WHERE sr.preferred_deadline BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
AND sr.status NOT IN ('COMPLETED', 'CANCELLED', 'REJECTED');

-- =====================================================
-- STORED PROCEDURES FOR COMMON OPERATIONS
-- =====================================================

DELIMITER //

-- Procedure to get service request statistics
CREATE PROCEDURE GetServiceRequestStatistics()
BEGIN
    SELECT 
        COUNT(*) as total_requests,
        SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending_requests,
        SUM(CASE WHEN status = 'ASSIGNED' THEN 1 ELSE 0 END) as assigned_requests,
        SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress_requests,
        SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_requests,
        SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejected_requests,
        SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled_requests,
        SUM(CASE WHEN preferred_deadline < CURDATE() AND status NOT IN ('COMPLETED', 'CANCELLED', 'REJECTED') THEN 1 ELSE 0 END) as overdue_requests
    FROM service_requests;
END //

-- Procedure to get client service request statistics
CREATE PROCEDURE GetClientServiceRequestStatistics(IN client_id_param BIGINT)
BEGIN
    SELECT 
        COUNT(*) as total_requests,
        SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending_requests,
        SUM(CASE WHEN status = 'ASSIGNED' THEN 1 ELSE 0 END) as assigned_requests,
        SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress_requests,
        SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_requests,
        SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejected_requests,
        SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled_requests
    FROM service_requests 
    WHERE client_id = client_id_param;
END //

-- Procedure to get staff service request statistics
CREATE PROCEDURE GetStaffServiceRequestStatistics(IN staff_id_param BIGINT)
BEGIN
    SELECT 
        COUNT(*) as total_assigned_requests,
        SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending_requests,
        SUM(CASE WHEN status = 'ASSIGNED' THEN 1 ELSE 0 END) as assigned_requests,
        SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress_requests,
        SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_requests
    FROM service_requests 
    WHERE assigned_staff_id = staff_id_param;
END //

DELIMITER ;

-- =====================================================
-- TRIGGERS FOR AUDIT LOGGING
-- =====================================================

DELIMITER //

-- Trigger to log service request creation
CREATE TRIGGER service_request_after_insert
AFTER INSERT ON service_requests
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (
        table_name, 
        record_id, 
        action, 
        old_values, 
        new_values, 
        user_id, 
        created_at
    ) VALUES (
        'service_requests',
        NEW.id,
        'INSERT',
        NULL,
        JSON_OBJECT(
            'request_id', NEW.request_id,
            'service_category_name', NEW.service_category_name,
            'service_subcategory_name', NEW.service_subcategory_name,
            'service_item_name', NEW.service_item_name,
            'description', NEW.description,
            'priority', NEW.priority,
            'status', NEW.status,
            'client_id', NEW.client_id
        ),
        NEW.created_by,
        NOW()
    );
END //

-- Trigger to log service request updates
CREATE TRIGGER service_request_after_update
AFTER UPDATE ON service_requests
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (
        table_name, 
        record_id, 
        action, 
        old_values, 
        new_values, 
        user_id, 
        created_at
    ) VALUES (
        'service_requests',
        NEW.id,
        'UPDATE',
        JSON_OBJECT(
            'request_id', OLD.request_id,
            'status', OLD.status,
            'priority', OLD.priority,
            'assigned_staff_id', OLD.assigned_staff_id,
            'description', OLD.description,
            'notes', OLD.notes
        ),
        JSON_OBJECT(
            'request_id', NEW.request_id,
            'status', NEW.status,
            'priority', NEW.priority,
            'assigned_staff_id', NEW.assigned_staff_id,
            'description', NEW.description,
            'notes', NEW.notes
        ),
        COALESCE(NEW.updated_by, NEW.created_by),
        NOW()
    );
END //

DELIMITER ;

-- =====================================================
-- COMMENTS AND DOCUMENTATION
-- =====================================================

-- Add comments to table and columns for documentation
ALTER TABLE service_requests COMMENT = 'Service requests submitted by clients for various tax and business services';

-- Add column comments
ALTER TABLE service_requests 
MODIFY COLUMN request_id VARCHAR(100) NOT NULL UNIQUE COMMENT 'Unique request identifier (format: SR + timestamp)',
MODIFY COLUMN service_category_name VARCHAR(255) COMMENT 'Service category (e.g., TAX ADVISORY, GST SERVICES)',
MODIFY COLUMN service_subcategory_name VARCHAR(255) COMMENT 'Service subcategory (e.g., GENERAL ADVISORY, GST FILING)',
MODIFY COLUMN service_item_id BIGINT COMMENT 'Reference to service_items table',
MODIFY COLUMN service_item_name VARCHAR(255) COMMENT 'Name of the specific service item',
MODIFY COLUMN description TEXT COMMENT 'Detailed description of the service request',
MODIFY COLUMN notes TEXT COMMENT 'Additional notes from client',
MODIFY COLUMN preferred_deadline DATE COMMENT 'Client preferred completion date',
MODIFY COLUMN priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM' COMMENT 'Request priority level',
MODIFY COLUMN status ENUM('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'REJECTED') DEFAULT 'PENDING' COMMENT 'Current status of the request',
MODIFY COLUMN rejection_reason TEXT COMMENT 'Reason for rejection (if applicable)',
MODIFY COLUMN admin_notes TEXT COMMENT 'Notes from admin during processing',
MODIFY COLUMN staff_notes TEXT COMMENT 'Notes from assigned staff',
MODIFY COLUMN estimated_price DECIMAL(10,2) COMMENT 'Estimated cost for the service',
MODIFY COLUMN final_price DECIMAL(10,2) COMMENT 'Final cost for the service',
MODIFY COLUMN assigned_date DATETIME(6) COMMENT 'Date when request was assigned to staff',
MODIFY COLUMN completed_date DATETIME(6) COMMENT 'Date when request was completed',
MODIFY COLUMN rejected_date DATETIME(6) COMMENT 'Date when request was rejected',
MODIFY COLUMN client_id BIGINT NOT NULL COMMENT 'Reference to clients table',
MODIFY COLUMN assigned_staff_id BIGINT COMMENT 'Reference to staff table (assigned staff member)',
MODIFY COLUMN accepted_by BIGINT COMMENT 'Reference to users table (staff who accepted)',
MODIFY COLUMN assigned_by BIGINT COMMENT 'Reference to users table (admin who assigned)',
MODIFY COLUMN rejected_by BIGINT COMMENT 'Reference to users table (admin who rejected)',
MODIFY COLUMN created_by BIGINT NOT NULL COMMENT 'Reference to users table (client who created)',
MODIFY COLUMN updated_by BIGINT COMMENT 'Reference to users table (user who last updated)';

-- =====================================================
-- MIGRATION COMPLETION
-- =====================================================

-- Verify the table was created successfully
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME,
    UPDATE_TIME
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'tax_consultancy_portal' 
AND TABLE_NAME = 'service_requests';

-- Show table structure
DESCRIBE service_requests;

-- Show indexes
SHOW INDEX FROM service_requests;

-- Show views
SHOW FULL TABLES WHERE Table_type = 'VIEW';

-- Show procedures
SHOW PROCEDURE STATUS WHERE Db = 'tax_consultancy_portal';

-- Show triggers
SHOW TRIGGERS WHERE `Table` = 'service_requests';

-- Migration completed successfully!
SELECT 'Service Requests migration completed successfully!' as status;
