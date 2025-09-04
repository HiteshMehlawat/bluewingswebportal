# Tax Consultancy Web Portal - Database Schema Documentation

## Overview

This document describes the MySQL database schema for the Tax Consultancy Web Portal, designed to support a secure, scalable platform for document management, task tracking, and client-staff communication.

## Database Structure

### Core Tables

#### 1. `users` - Base User Management

- **Purpose**: Central user authentication and basic profile information
- **Key Features**:
  - Supports three roles: ADMIN, STAFF, CLIENT
  - Email-based authentication
  - Password hashing (BCrypt)
  - Account status tracking
  - Last login tracking

#### 2. `clients` - Client Information

- **Purpose**: Extended client profile and business details
- **Key Features**:
  - PAN and GST number storage
  - Business address and contact information
  - Client type classification (Individual, Company, Partnership, LLP)
  - Registration date tracking

#### 3. `staff` - Staff Information

- **Purpose**: Employee details and organizational structure
- **Key Features**:
  - Employee ID and position tracking
  - Department assignment
  - Supervisor hierarchy
  - Availability status

#### 4. `tasks` - Work Assignment and Tracking

- **Purpose**: Core workflow management
- **Key Features**:
  - Task assignment to staff
  - Status tracking (Pending → In Progress → Completed)
  - Priority levels and due dates
  - Time tracking (estimated vs actual hours)
  - Multiple task types (ITR, GST, Company Registration, etc.)

### Document Management

#### 5. `documents` - File Management

- **Purpose**: Secure document upload and tracking
- **Key Features**:
  - File metadata storage
  - Document type classification
  - Verification workflow
  - Audit trail for uploads

### Communication & Notifications

#### 6. `task_comments` - Internal Communication

- **Purpose**: Task-specific communication between staff and clients
- **Key Features**:
  - Internal vs external comment distinction
  - Threaded communication

#### 7. `notifications` - System Notifications

- **Purpose**: Real-time notification system
- **Key Features**:
  - Multiple notification types
  - Read/unread status
  - Related entity linking

### Security & Audit

#### 8. `audit_logs` - Security Audit Trail

- **Purpose**: Comprehensive activity logging
- **Key Features**:
  - JSON-based change tracking
  - IP address and user agent logging
  - Entity-specific audit trails

### System Configuration

#### 9. `settings` - System Configuration

- **Purpose**: Configurable system parameters
- **Key Features**:
  - Key-value configuration storage
  - Public vs private settings

#### 10. `email_templates` - Notification Templates

- **Purpose**: Email notification templates
- **Key Features**:
  - Variable substitution support
  - Template versioning

## Key Features

### 1. Role-Based Access Control

- **ADMIN**: Full system access, user management, reporting
- **STAFF**: Assigned task management, client communication
- **CLIENT**: Document upload, status tracking, communication

### 2. Document Security

- Secure file upload with metadata tracking
- Document verification workflow
- Audit trail for all document operations

### 3. Task Management

- Comprehensive task lifecycle tracking
- Priority and deadline management
- Time tracking and performance metrics

### 4. Communication System

- Internal staff communication
- Client-staff messaging
- Email notification system

### 5. Audit and Compliance

- Complete audit trail for all operations
- Security event logging
- Compliance-ready data structure

## Database Views

### 1. `admin_dashboard_summary`

Provides real-time statistics for admin dashboard:

- Total clients
- Active cases
- Pending tasks
- Monthly completions
- Staff count

### 2. `staff_dashboard`

Shows assigned tasks with client information for staff members.

### 3. `client_dashboard`

Displays task status and assigned staff for clients.

## Stored Procedures

### 1. `GetTaskStatistics(client_id)`

Returns task statistics for a specific client:

- Total tasks
- Pending tasks
- In-progress tasks
- Completed tasks

### 2. `GetStaffPerformance(staff_id, start_date, end_date)`

Provides performance metrics for staff members:

- Total assigned tasks
- Completion rate
- Time tracking
- Average hours per task

## Triggers

### 1. `task_status_audit_trigger`

Automatically logs task status changes for audit purposes.

### 2. `document_upload_audit_trigger`

Logs all document uploads with metadata for security tracking.

## Performance Optimization

### Indexes

- **Primary Keys**: All tables have auto-incrementing primary keys
- **Foreign Keys**: Properly indexed for join performance
- **Composite Indexes**: Status + due date, client + upload date
- **Search Indexes**: Email, PAN, GST, employee ID

### Query Optimization

- Views for common dashboard queries
- Stored procedures for complex operations
- Proper indexing strategy for high-traffic queries

## Security Features

### 1. Data Protection

- Password hashing using BCrypt
- Encrypted sensitive data storage
- Audit trail for all operations

### 2. Access Control

- Role-based permissions
- Session management
- IP address logging

### 3. Compliance

- GDPR-ready data structure
- Audit logging for regulatory compliance
- Data retention policies

## Sample Data

The schema includes sample data for:

- Default admin user (admin@taxportal.com / admin123)
- Sample staff members
- Sample client
- Sample task assignment
- Default system settings
- Email notification templates

## Installation Instructions

### 1. Database Setup

```sql
-- Run the schema files in order:
-- 1. database_schema_part1.sql
-- 2. database_schema_part2.sql
-- 3. database_schema_part3.sql
```

### 2. Configuration

Update the following settings in the `settings` table:

- `company_name`: Your company name
- `max_file_size`: Maximum file upload size
- `allowed_file_types`: Allowed file extensions
- `email_notifications`: Enable/disable email notifications

### 3. Default Credentials

- **Admin**: admin@taxportal.com / admin123
- **Staff**: staff1@taxportal.com / admin123
- **Client**: client1@example.com / admin123

## Maintenance

### Regular Tasks

1. **Backup**: Daily database backups
2. **Audit Log Cleanup**: Monthly cleanup of old audit logs
3. **Performance Monitoring**: Monitor query performance
4. **Security Updates**: Regular security patches

### Monitoring Queries

```sql
-- Check active sessions
SELECT COUNT(*) FROM users WHERE last_login > DATE_SUB(NOW(), INTERVAL 1 HOUR);

-- Monitor task completion rates
SELECT status, COUNT(*) FROM tasks GROUP BY status;

-- Check document upload activity
SELECT DATE(upload_date), COUNT(*) FROM documents GROUP BY DATE(upload_date);
```

## Scalability Considerations

### 1. Partitioning

- Consider partitioning large tables (audit_logs, documents) by date
- Archive old data to separate tables

### 2. Caching

- Implement application-level caching for frequently accessed data
- Use Redis for session management

### 3. Backup Strategy

- Daily automated backups
- Point-in-time recovery capability
- Off-site backup storage

## Support and Documentation

For technical support or questions about the database schema:

1. Review the audit logs for troubleshooting
2. Check the settings table for configuration issues
3. Monitor performance using the provided views and procedures

---

**Version**: 1.0  
**Last Updated**: July 2025  
**Compatibility**: MySQL 8.0+  
**Security Level**: Enterprise-grade with audit trails
