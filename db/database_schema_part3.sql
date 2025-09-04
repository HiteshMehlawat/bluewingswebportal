-- Tax Consultancy Web Portal Database Schema - Part 3
-- MySQL Database Design - Sample Data, Views, Procedures & Triggers
-- Created: July 2025

USE tax_consultancy_portal;

-- =====================================================
-- SAMPLE DATA INSERTION
-- =====================================================

-- Insert default admin user (password: admin123)
INSERT INTO users (email, password_hash, role, first_name, last_name, phone, is_active, email_verified, created_by, updated_by) VALUES
('admin@taxportal.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN', 'System', 'Administrator', '+91-9876543210', TRUE, TRUE, NULL, NULL);

-- Insert sample staff
INSERT INTO users (email, password_hash, role, first_name, last_name, phone, is_active, email_verified) VALUES
('staff1@taxportal.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'STAFF', 'Rahul', 'Sharma', '+91-9876543211', TRUE, TRUE),
('staff2@taxportal.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'STAFF', 'Priya', 'Patel', '+91-9876543212', TRUE, TRUE);

-- Insert staff details
INSERT INTO staff (user_id, employee_id, position, department, joining_date, salary) VALUES
((SELECT id FROM users WHERE email = 'staff1@taxportal.com'), 'EMP001', 'Senior Tax Consultant', 'Tax Filing', '2024-01-15', 45000.00),
((SELECT id FROM users WHERE email = 'staff2@taxportal.com'), 'EMP002', 'GST Specialist', 'GST Department', '2024-02-01', 40000.00);

-- Insert sample client
INSERT INTO users (email, password_hash, role, first_name, last_name, phone, is_active, email_verified) VALUES
('client1@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'CLIENT', 'Amit', 'Kumar', '+91-9876543213', TRUE, TRUE);

-- Insert client details
INSERT INTO clients (user_id, company_name, pan_number, gst_number, address, city, state, pincode, client_type) VALUES
((SELECT id FROM users WHERE email = 'client1@example.com'), 'Amit Kumar & Co.', 'ABCDE1234F', '22AAAAA0000A1Z5', '123 Business Street, Mumbai', 'Mumbai', 'Maharashtra', '400001', 'INDIVIDUAL');

-- Insert sample task
INSERT INTO tasks (title, description, client_id, assigned_staff_id, task_type, status, priority, due_date, created_by) VALUES
('ITR Filing FY 2024-25', 'Income Tax Return filing for financial year 2024-25', 
 (SELECT id FROM clients WHERE user_id = (SELECT id FROM users WHERE email = 'client1@example.com')),
 (SELECT id FROM staff WHERE user_id = (SELECT id FROM users WHERE email = 'staff1@taxportal.com')),
 'ITR_FILING', 'IN_PROGRESS', 'HIGH', '2025-07-31',
 (SELECT id FROM users WHERE email = 'admin@taxportal.com'));

-- Insert default settings
INSERT INTO settings (setting_key, setting_value, description, is_public) VALUES
('company_name', 'Tax Consultancy Portal', 'Company name for the portal', TRUE),
('max_file_size', '10485760', 'Maximum file upload size in bytes (10MB)', FALSE),
('allowed_file_types', 'pdf,doc,docx,jpg,jpeg,png', 'Allowed file extensions', FALSE),
('email_notifications', 'true', 'Enable email notifications', FALSE),
('session_timeout', '3600', 'Session timeout in seconds', FALSE);

-- Insert email templates
INSERT INTO email_templates (template_name, subject, body, variables) VALUES
('task_assigned', 'New Task Assigned - {task_title}', 
 'Dear {staff_name},\n\nA new task "{task_title}" has been assigned to you.\nDue Date: {due_date}\nPriority: {priority}\n\nPlease login to the portal to view details.\n\nBest regards,\nTax Portal Team',
 '["staff_name", "task_title", "due_date", "priority"]'),
('document_uploaded', 'Document Uploaded - {document_name}', 
 'Dear {client_name},\n\nYour document "{document_name}" has been successfully uploaded.\nUpload Date: {upload_date}\n\nWe will review and process it shortly.\n\nBest regards,\nTax Portal Team',
 '["client_name", "document_name", "upload_date"]'),
('status_update', 'Task Status Updated - {task_title}', 
 'Dear {client_name},\n\nThe status of your task "{task_title}" has been updated to {new_status}.\nUpdated by: {updated_by}\nDate: {update_date}\n\nBest regards,\nTax Portal Team',
 '["client_name", "task_title", "new_status", "updated_by", "update_date"]');

-- =====================================================
-- VIEWS FOR DASHBOARDS
-- =====================================================

-- Admin Dashboard Summary View
CREATE VIEW admin_dashboard_summary AS
SELECT 
    (SELECT COUNT(*) FROM users WHERE role = 'CLIENT' AND is_active = TRUE) as total_clients,
    (SELECT COUNT(*) FROM tasks WHERE status IN ('PENDING', 'IN_PROGRESS')) as active_cases,
    (SELECT COUNT(*) FROM tasks WHERE status = 'PENDING') as pending_tasks,
    (SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED' AND completed_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)) as completed_this_month,
    (SELECT COUNT(*) FROM users WHERE role = 'STAFF' AND is_active = TRUE) as total_staff;

-- Staff Dashboard View (no parameter)
CREATE VIEW staff_dashboard AS
SELECT 
    t.id,
    t.title,
    t.status,
    t.priority,
    t.due_date,
    c.company_name,
    CONCAT(u.first_name, ' ', u.last_name) as client_name,
    u.phone as client_phone,
    t.assigned_staff_id
FROM tasks t
JOIN clients c ON t.client_id = c.id
JOIN users u ON c.user_id = u.id;
-- To filter: SELECT * FROM staff_dashboard WHERE assigned_staff_id = (SELECT id FROM staff WHERE user_id = 123);

-- Client Dashboard View (no parameter)
CREATE VIEW client_dashboard AS
SELECT 
    t.id,
    t.title,
    t.status,
    t.priority,
    t.due_date,
    CONCAT(s.first_name, ' ', s.last_name) as assigned_staff,
    s.phone as staff_phone,
    t.client_id
FROM tasks t
LEFT JOIN staff st ON t.assigned_staff_id = st.id
LEFT JOIN users s ON st.user_id = s.id;
-- To filter: SELECT * FROM client_dashboard WHERE client_id = 123;

-- =====================================================
-- STORED PROCEDURES
-- =====================================================

DELIMITER //

-- Procedure to get task statistics
CREATE PROCEDURE GetTaskStatistics(IN client_id_param BIGINT)
BEGIN
    SELECT 
        COUNT(*) as total_tasks,
        SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending_tasks,
        SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress_tasks,
        SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_tasks
    FROM tasks 
    WHERE client_id = client_id_param;
END //

-- Procedure to get staff performance
CREATE PROCEDURE GetStaffPerformance(IN staff_id_param BIGINT, IN start_date DATE, IN end_date DATE)
BEGIN
    SELECT 
        COUNT(*) as total_tasks,
        SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_tasks,
        SUM(CASE WHEN status = 'COMPLETED' THEN actual_hours ELSE 0 END) as total_hours,
        AVG(CASE WHEN status = 'COMPLETED' THEN actual_hours ELSE NULL END) as avg_hours_per_task
    FROM tasks 
    WHERE assigned_staff_id = staff_id_param 
    AND assigned_date BETWEEN start_date AND end_date;
END //

DELIMITER ;

-- =====================================================
-- TRIGGERS FOR AUDIT LOGGING
-- =====================================================

DELIMITER //

-- Trigger to log task status changes
CREATE TRIGGER task_status_audit_trigger
AFTER UPDATE ON tasks
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO audit_logs (user_id, action, entity_type, entity_id, old_values, new_values)
        VALUES (
            NEW.assigned_staff_id,
            'TASK_STATUS_CHANGE',
            'TASKS',
            NEW.id,
            JSON_OBJECT('status', OLD.status),
            JSON_OBJECT('status', NEW.status)
        );
    END IF;
END //

-- Trigger to log document uploads
CREATE TRIGGER document_upload_audit_trigger
AFTER INSERT ON documents
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (user_id, action, entity_type, entity_id, new_values)
    VALUES (
        NEW.uploaded_by,
        'DOCUMENT_UPLOADED',
        'DOCUMENTS',
        NEW.id,
        JSON_OBJECT('file_name', NEW.file_name, 'document_type', NEW.document_type)
    );
END //

DELIMITER ;

-- =====================================================
-- ADDITIONAL INDEXES FOR PERFORMANCE
-- =====================================================

-- Additional indexes for better performance
CREATE INDEX idx_tasks_status_due_date ON tasks(status, due_date);
CREATE INDEX idx_documents_client_upload_date ON documents(client_id, upload_date);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
CREATE INDEX idx_audit_logs_user_action ON audit_logs(user_id, action); 