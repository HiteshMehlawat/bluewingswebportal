-- Tax Consultancy Web Portal Database Schema
-- MySQL Database Design
-- Created: July 2025

-- Drop database if exists (for development)
-- DROP DATABASE IF EXISTS tax_consultancy_portal;

-- Create database
CREATE DATABASE IF NOT EXISTS tax_consultancy_portal
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE tax_consultancy_portal;

-- =====================================================
-- 1. USERS TABLE (Base table for all user types)
-- =====================================================
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'STAFF', 'CLIENT') NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    last_login TIMESTAMP NULL,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_active (is_active)
);

-- =====================================================
-- 2. CLIENTS TABLE (Extended client information)
-- =====================================================
CREATE TABLE clients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    client_id VARCHAR(100) UNIQUE,
    company_name VARCHAR(255),
    company_type VARCHAR(100),
    pan_number VARCHAR(20),
    gst_number VARCHAR(20),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    country VARCHAR(100),
    business_type VARCHAR(100),
    industry VARCHAR(100),
    website VARCHAR(255),
    contact_person VARCHAR(100),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255),
    emergency_contact VARCHAR(20),
    client_type ENUM('INDIVIDUAL', 'COMPANY', 'PARTNERSHIP', 'LLP') DEFAULT 'INDIVIDUAL',
    registration_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    assigned_staff_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_staff_id) REFERENCES staff(id) ON DELETE SET NULL,
    INDEX idx_pan (pan_number),
    INDEX idx_gst (gst_number),
    INDEX idx_client_type (client_type),
    INDEX idx_is_active (is_active),
    INDEX idx_assigned_staff (assigned_staff_id)
);

-- =====================================================
-- 3. STAFF TABLE (Extended staff information)
-- =====================================================
CREATE TABLE staff (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    employee_id VARCHAR(20) UNIQUE,
    position VARCHAR(100),
    department VARCHAR(100),
    joining_date DATE,
    salary DECIMAL(10,2),
    supervisor_id BIGINT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (supervisor_id) REFERENCES staff(id) ON DELETE SET NULL,
    INDEX idx_employee_id (employee_id),
    INDEX idx_position (position),
    INDEX idx_available (is_available)
);

-- =====================================================
-- 4. TASKS TABLE (Work assignments and tracking)
-- =====================================================
CREATE TABLE tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    client_id BIGINT NOT NULL,
    assigned_staff_id BIGINT,
    task_type ENUM('ITR_FILING', 'GST_FILING', 'COMPANY_REGISTRATION', 'TDS_FILING', 'AUDIT', 'OTHER') NOT NULL,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'ON_HOLD', 'CANCELLED') DEFAULT 'PENDING',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    due_date DATE,
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_date TIMESTAMP NULL,
    completed_date TIMESTAMP NULL,
    estimated_hours DECIMAL(5,2),
    actual_hours DECIMAL(5,2),
    created_by BIGINT NOT NULL,
    updated_by BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_staff_id) REFERENCES staff(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_client_id (client_id),
    INDEX idx_staff_id (assigned_staff_id),
    INDEX idx_status (status),
    INDEX idx_task_type (task_type),
    INDEX idx_due_date (due_date)
);

-- =====================================================
-- 5. DOCUMENTS TABLE (File management)
-- =====================================================
CREATE TABLE documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT,
    client_id BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100),
    document_type ENUM('PAN_CARD', 'AADHAR', 'BANK_STATEMENT', 'INVOICE', 'FORM_16', 'GST_RETURN', 'COMPANY_DOCS', 'OTHER') NOT NULL,
    status ENUM('PENDING', 'VERIFIED', 'REJECTED') DEFAULT 'PENDING',
    verified_by BIGINT NULL,
    verified_at TIMESTAMP NULL,
    rejected_by BIGINT NULL,
    rejected_at TIMESTAMP NULL,
    rejection_reason TEXT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id),
    FOREIGN KEY (verified_by) REFERENCES users(id),
    FOREIGN KEY (rejected_by) REFERENCES users(id),
    INDEX idx_task_id (task_id),
    INDEX idx_client_id (client_id),
    INDEX idx_document_type (document_type),
    INDEX idx_upload_date (upload_date)
);


-- =====================================================
-- 6. TASK_COMMENTS TABLE (Communication and updates)
-- =====================================================
CREATE TABLE task_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    comment TEXT NOT NULL,
    is_internal BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_task_id (task_id),
    INDEX idx_created_at (created_at)
);

-- =====================================================
-- 7. NOTIFICATIONS TABLE (System notifications)
-- =====================================================
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    notification_type ENUM('TASK_ASSIGNED', 'STATUS_UPDATE', 'DOCUMENT_UPLOADED', 'DOCUMENT_VERIFIED', 'DOCUMENT_REJECTED', 'DEADLINE_REMINDER', 'STAFF_ASSIGNED', 'TASK_COMPLETED', 'TASK_ACKNOWLEDGED', 'SYSTEM', 'EMAIL', 'MESSAGE_RECEIVED') NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    related_task_id BIGINT NULL,
    related_document_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (related_task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    FOREIGN KEY (related_document_id) REFERENCES documents(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
);

-- =====================================================
-- 8. AUDIT_LOGS TABLE (Security and tracking)
-- =====================================================
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    old_values JSON,
    new_values JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_created_at (created_at)
);

-- =====================================================
-- 9. SETTINGS TABLE (System configuration)
-- =====================================================
CREATE TABLE settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_setting_key (setting_key)
);

-- =====================================================
-- 10. EMAIL_TEMPLATES TABLE (Notification templates)
-- =====================================================
CREATE TABLE email_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_name VARCHAR(100) UNIQUE NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    variables JSON,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_template_name (template_name),
    INDEX idx_active (is_active)
);

-- =====================================================
-- SAMPLE DATA INSERTION
-- =====================================================

-- Insert default admin user (password: admin123)
INSERT INTO users (email, password_hash, role, first_name, last_name, phone, is_active, email_verified) VALUES
('admin@taxportal.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN', 'System', 'Administrator', '+91-9876543210', TRUE, TRUE);

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
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Additional indexes for better performance
CREATE INDEX idx_tasks_status_due_date ON tasks(status, due_date);
CREATE INDEX idx_documents_client_upload_date ON documents(client_id, upload_date);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
CREATE INDEX idx_audit_logs_user_action ON audit_logs(user_id, action);

-- =====================================================
-- COMMENTS AND DOCUMENTATION
-- =====================================================

/*
Database Schema for Tax Consultancy Web Portal

This schema supports:
1. Multi-role user management (Admin, Staff, Client)
2. Task assignment and tracking
3. Document upload and management
4. Communication and notifications
5. Audit logging for security
6. Performance optimization with indexes
7. Dashboard views for different roles
8. Stored procedures for common operations

Key Features:
- Role-based access control
- File upload tracking
- Task status management
- Email notification system
- Audit trail for compliance
- Scalable design for growth

Security Considerations:
- Password hashing (BCrypt)
- Audit logging for sensitive operations
- Role-based permissions
- Input validation at application level
*/ 

-- Staff Activities and Logs Table
CREATE TABLE staff_activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    staff_id BIGINT NOT NULL,
    activity_type ENUM('LOGIN', 'LOGOUT', 'TASK_STARTED', 'TASK_COMPLETED', 'TASK_DELAYED', 'CLIENT_ASSIGNED', 'DOCUMENT_UPLOADED', 'CLIENT_CONTACT', 'BREAK_START', 'BREAK_END') NOT NULL,
    task_description TEXT,
    work_status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'DELAYED', 'CANCELLED') DEFAULT 'PENDING',
    log_date DATE NOT NULL,
    login_time TIME,
    logout_time TIME,
    duration_minutes INT DEFAULT 0,
    client_id BIGINT,
    task_id BIGINT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE SET NULL,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_staff_date (staff_id, log_date),
    INDEX idx_activity_type (activity_type),
    INDEX idx_work_status (work_status),
    INDEX idx_log_date (log_date)
);

-- Staff Workload Summary Table (for performance optimization)
CREATE TABLE staff_workload_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    staff_id BIGINT NOT NULL,
    summary_date DATE NOT NULL,
    total_tasks_assigned INT DEFAULT 0,
    completed_tasks INT DEFAULT 0,
    pending_tasks INT DEFAULT 0,
    delayed_tasks INT DEFAULT 0,
    total_clients_assigned INT DEFAULT 0,
    total_work_hours DECIMAL(5,2) DEFAULT 0.00,
    login_count INT DEFAULT 0,
    last_activity TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE,
    UNIQUE KEY unique_staff_date (staff_id, summary_date),
    INDEX idx_summary_date (summary_date)
); 