-- Lead Management System - Database Schema
-- MySQL Database Design for Tax Consultancy Web Portal
-- Created: August 2025

USE tax_consultancy_portal;

-- =====================================================
-- LEADS TABLE (Lead management and tracking)
-- =====================================================
CREATE TABLE leads (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lead_id VARCHAR(100) UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    company_name VARCHAR(255),
    service_required ENUM('ITR_FILING', 'GST_REGISTRATION', 'GST_FILING', 'COMPANY_REGISTRATION', 'TDS_FILING', 'AUDIT', 'BOOK_KEEPING', 'OTHER') NOT NULL,
    service_description TEXT,
    source ENUM('WEBSITE', 'SOCIAL_MEDIA', 'REFERRAL', 'COLD_CALL', 'ADVERTISING', 'OTHER') DEFAULT 'WEBSITE',
    status ENUM('NEW', 'CONTACTED', 'IN_DISCUSSION', 'PROPOSAL_SENT', 'CONVERTED', 'LOST') DEFAULT 'NEW',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    assigned_staff_id BIGINT NULL,
    estimated_value DECIMAL(10,2),
    notes TEXT,
    next_follow_up_date DATE,
    last_contact_date TIMESTAMP NULL,
    converted_date TIMESTAMP NULL,
    lost_reason TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    FOREIGN KEY (assigned_staff_id) REFERENCES staff(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_status (status),
    INDEX idx_service_required (service_required),
    INDEX idx_assigned_staff (assigned_staff_id),
    INDEX idx_created_at (created_at),
    INDEX idx_next_follow_up (next_follow_up_date)
);

-- =====================================================
-- LEAD_COMMENTS TABLE (Lead communication tracking)
-- =====================================================
CREATE TABLE lead_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lead_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    comment TEXT NOT NULL,
    comment_type ENUM('NOTE', 'CALL', 'EMAIL', 'MEETING', 'PROPOSAL', 'FOLLOW_UP') DEFAULT 'NOTE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lead_id) REFERENCES leads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_lead_id (lead_id),
    INDEX idx_created_at (created_at)
);

-- =====================================================
-- LEAD_ACTIVITIES TABLE (Lead activity tracking)
-- =====================================================
CREATE TABLE lead_activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lead_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    activity_type ENUM('LEAD_CREATED', 'STATUS_CHANGED', 'ASSIGNED', 'CONTACTED', 'FOLLOW_UP', 'PROPOSAL_SENT', 'CONVERTED', 'LOST') NOT NULL,
    description TEXT,
    old_value VARCHAR(255),
    new_value VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lead_id) REFERENCES leads(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_lead_id (lead_id),
    INDEX idx_activity_type (activity_type),
    INDEX idx_created_at (created_at)
);

-- =====================================================
-- LEAD_SOURCES TABLE (Lead source tracking)
-- =====================================================
CREATE TABLE lead_sources (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default lead sources
INSERT INTO lead_sources (source_name, description) VALUES
('WEBSITE', 'Lead captured from company website'),
('SOCIAL_MEDIA', 'Lead from social media platforms'),
('REFERRAL', 'Lead from existing client referral'),
('COLD_CALL', 'Lead from cold calling campaigns'),
('ADVERTISING', 'Lead from paid advertising'),
('OTHER', 'Other lead sources');

-- =====================================================
-- LEAD_SERVICES TABLE (Available services for leads)
-- =====================================================
CREATE TABLE lead_services (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_name VARCHAR(100) NOT NULL UNIQUE,
    service_code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    estimated_duration VARCHAR(100),
    base_price DECIMAL(10,2),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default lead services
INSERT INTO lead_services (service_name, service_code, description, estimated_duration, base_price) VALUES
('ITR Filing', 'ITR_FILING', 'Income Tax Return Filing Services', '1-2 weeks', 1500.00),
('GST Registration', 'GST_REGISTRATION', 'GST Registration Services', '3-5 days', 2000.00),
('GST Filing', 'GST_FILING', 'GST Return Filing Services', '1-2 days', 500.00),
('Company Registration', 'COMPANY_REGISTRATION', 'Private Limited Company Registration', '15-20 days', 15000.00),
('TDS Filing', 'TDS_FILING', 'TDS Return Filing Services', '1-2 days', 800.00),
('Audit Services', 'AUDIT', 'Statutory and Tax Audit Services', '1-2 months', 5000.00),
('Book Keeping', 'BOOK_KEEPING', 'Monthly Book Keeping Services', 'Monthly', 2000.00),
('Other Services', 'OTHER', 'Other tax and compliance services', 'Varies', 1000.00);

-- =====================================================
-- COMMENTS AND DOCUMENTATION
-- =====================================================

/*
Lead Management System Database Schema

This schema supports:
1. Lead capture and storage
2. Lead status tracking (New → Contacted → In Discussion → Proposal Sent → Converted/Lost)
3. Lead assignment to staff
4. Lead communication tracking
5. Lead activity logging
6. Lead source and service management
7. Follow-up scheduling
8. Conversion tracking

Key Features:
- Unique lead ID generation
- Service requirement tracking
- Source attribution
- Priority management
- Staff assignment
- Value estimation
- Follow-up scheduling
- Activity audit trail

Business Flow:
1. Lead captured from public form
2. Admin/staff reviews and assigns
3. Communication tracked via comments
4. Status updated as lead progresses
5. Converted leads become clients
6. Lost leads tracked for analytics
*/
