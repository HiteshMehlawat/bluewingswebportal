-- Dummy data for dashboard testing (safe for existing data)

-- 10 staff users
INSERT INTO users (email, password_hash, role, first_name, last_name, phone, is_active, email_verified)
VALUES
('staff3@taxportal.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'STAFF', 'Amit', 'Kumar', '+91-9000000001', TRUE, TRUE),
('staff4@taxportal.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'STAFF', 'Pooja', 'Sharma', '+91-9000000002', TRUE, TRUE),
('staff5@taxportal.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'STAFF', 'Rajesh', 'Gupta', '+91-9000000003', TRUE, TRUE),
('staff6@taxportal.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'STAFF', 'Anjali', 'Patel', '+91-9000000004', TRUE, TRUE),
('staff7@taxportal.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'STAFF', 'Vikas', 'Singh', '+91-9000000005', TRUE, TRUE),
('staff8@taxportal.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'STAFF', 'Neha', 'Jain', '+91-9000000006', TRUE, TRUE),
('staff9@taxportal.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'STAFF', 'Suresh', 'Mehta', '+91-9000000007', TRUE, TRUE),
('staff10@taxportal.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'STAFF', 'Priya', 'Verma', '+91-9000000008', TRUE, TRUE),
('staff11@taxportal.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'STAFF', 'Manoj', 'Yadav', '+91-9000000009', TRUE, TRUE),
('staff12@taxportal.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'STAFF', 'Kavita', 'Rao', '+91-9000000010', TRUE, TRUE);

-- 2 client users
INSERT INTO users (email, password_hash, role, first_name, last_name, phone, is_active, email_verified)
VALUES
('client2@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'CLIENT', 'Sunil', 'Agarwal', '+91-9000000011', TRUE, TRUE),
('client3@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'CLIENT', 'Meena', 'Joshi', '+91-9000000012', TRUE, TRUE);

-- 10 staff
INSERT INTO staff (user_id, employee_id, position, department, joining_date, salary)
VALUES
((SELECT id FROM users WHERE email = 'staff3@taxportal.com'), 'EMP003', 'Tax Consultant', 'Tax', '2024-03-01', 42000.00),
((SELECT id FROM users WHERE email = 'staff4@taxportal.com'), 'EMP004', 'Accountant', 'Finance', '2024-03-02', 41000.00),
((SELECT id FROM users WHERE email = 'staff5@taxportal.com'), 'EMP005', 'Tax Consultant', 'Tax', '2024-03-03', 43000.00),
((SELECT id FROM users WHERE email = 'staff6@taxportal.com'), 'EMP006', 'Client Manager', 'Client Services', '2024-03-04', 44000.00),
((SELECT id FROM users WHERE email = 'staff7@taxportal.com'), 'EMP007', 'Auditor', 'Audit', '2024-03-05', 40000.00),
((SELECT id FROM users WHERE email = 'staff8@taxportal.com'), 'EMP008', 'GST Specialist', 'GST', '2024-03-06', 40500.00),
((SELECT id FROM users WHERE email = 'staff9@taxportal.com'), 'EMP009', 'TDS Expert', 'TDS', '2024-03-07', 41500.00),
((SELECT id FROM users WHERE email = 'staff10@taxportal.com'), 'EMP010', 'Legal Advisor', 'Legal', '2024-03-08', 45000.00),
((SELECT id FROM users WHERE email = 'staff11@taxportal.com'), 'EMP011', 'Payroll Manager', 'Payroll', '2024-03-09', 42000.00),
((SELECT id FROM users WHERE email = 'staff12@taxportal.com'), 'EMP012', 'HR Executive', 'HR', '2024-03-10', 40000.00);

-- 2 clients
INSERT INTO clients (user_id, company_name, pan_number, gst_number, address, city, state, pincode, client_type)
VALUES
((SELECT id FROM users WHERE email = 'client2@example.com'), 'Sunil Agarwal & Co.', 'ABCDE1234G', '22AAAAA0000A1Z6', '456 Market Road, Delhi', 'Delhi', 'Delhi', '110001', 'INDIVIDUAL'),
((SELECT id FROM users WHERE email = 'client3@example.com'), 'Meena Joshi & Associates', 'ABCDE1234H', '22AAAAA0000A1Z7', '789 Main Street, Pune', 'Pune', 'Maharashtra', '411001', 'INDIVIDUAL');

-- 10 tasks
INSERT INTO tasks (title, description, client_id, assigned_staff_id, task_type, status, priority, due_date, created_by)
VALUES
('ITR Filing Q1', 'Quarterly ITR Filing', (SELECT id FROM clients WHERE company_name = 'Sunil Agarwal & Co.'), (SELECT id FROM staff WHERE employee_id = 'EMP003'), 'ITR_FILING', 'COMPLETED', 'HIGH', '2024-04-10', 1),
('GST Return Q1', 'Quarterly GST Return', (SELECT id FROM clients WHERE company_name = 'Meena Joshi & Associates'), (SELECT id FROM staff WHERE employee_id = 'EMP004'), 'GST_FILING', 'PENDING', 'MEDIUM', '2024-07-10', 1),
('Company Registration', 'New company registration', (SELECT id FROM clients WHERE company_name = 'Sunil Agarwal & Co.'), (SELECT id FROM staff WHERE employee_id = 'EMP005'), 'COMPANY_REGISTRATION', 'IN_PROGRESS', 'HIGH', '2024-06-15', 1),
('TDS Filing', 'TDS Filing for Q1', (SELECT id FROM clients WHERE company_name = 'Meena Joshi & Associates'), (SELECT id FROM staff WHERE employee_id = 'EMP006'), 'TDS_FILING', 'COMPLETED', 'LOW', '2024-05-20', 1),
('Audit FY 2023-24', 'Annual audit', (SELECT id FROM clients WHERE company_name = 'Sunil Agarwal & Co.'), (SELECT id FROM staff WHERE employee_id = 'EMP007'), 'AUDIT', 'PENDING', 'URGENT', '2024-07-01', 1),
('Legal Compliance', 'Legal compliance check', (SELECT id FROM clients WHERE company_name = 'Meena Joshi & Associates'), (SELECT id FROM staff WHERE employee_id = 'EMP008'), 'OTHER', 'COMPLETED', 'MEDIUM', '2024-03-30', 1),
('Payroll Setup', 'Setup payroll system', (SELECT id FROM clients WHERE company_name = 'Sunil Agarwal & Co.'), (SELECT id FROM staff WHERE employee_id = 'EMP009'), 'OTHER', 'IN_PROGRESS', 'HIGH', '2024-07-15', 1),
('HR Policy Update', 'Update HR policies', (SELECT id FROM clients WHERE company_name = 'Meena Joshi & Associates'), (SELECT id FROM staff WHERE employee_id = 'EMP010'), 'OTHER', 'PENDING', 'LOW', '2024-07-20', 1),
('Client Onboarding', 'Onboard new client', (SELECT id FROM clients WHERE company_name = 'Sunil Agarwal & Co.'), (SELECT id FROM staff WHERE employee_id = 'EMP011'), 'OTHER', 'COMPLETED', 'MEDIUM', '2024-06-10', 1),
('Tax Planning', 'Tax planning for FY 2024-25', (SELECT id FROM clients WHERE company_name = 'Meena Joshi & Associates'), (SELECT id FROM staff WHERE employee_id = 'EMP012'), 'ITR_FILING', 'PENDING', 'HIGH', '2024-07-25', 1);

-- 10 documents
INSERT INTO documents (task_id, client_id, uploaded_by, file_name, original_file_name, file_path, file_size, file_type, document_type, upload_date)
VALUES
(1, (SELECT id FROM clients WHERE company_name = 'Sunil Agarwal & Co.'), 1, 'doc1.pdf', 'doc1.pdf', '/files/doc1.pdf', 102400, 'pdf', 'PAN_CARD', NOW()),
(2, (SELECT id FROM clients WHERE company_name = 'Meena Joshi & Associates'), 1, 'doc2.pdf', 'doc2.pdf', '/files/doc2.pdf', 204800, 'pdf', 'AADHAR', NOW()),
(3, (SELECT id FROM clients WHERE company_name = 'Sunil Agarwal & Co.'), 1, 'doc3.pdf', 'doc3.pdf', '/files/doc3.pdf', 307200, 'pdf', 'BANK_STATEMENT', NOW()),
(4, (SELECT id FROM clients WHERE company_name = 'Meena Joshi & Associates'), 1, 'doc4.pdf', 'doc4.pdf', '/files/doc4.pdf', 409600, 'pdf', 'INVOICE', NOW()),
(5, (SELECT id FROM clients WHERE company_name = 'Sunil Agarwal & Co.'), 1, 'doc5.pdf', 'doc5.pdf', '/files/doc5.pdf', 512000, 'pdf', 'FORM_16', NOW()),
(6, (SELECT id FROM clients WHERE company_name = 'Meena Joshi & Associates'), 1, 'doc6.pdf', 'doc6.pdf', '/files/doc6.pdf', 614400, 'pdf', 'GST_RETURN', NOW()),
(7, (SELECT id FROM clients WHERE company_name = 'Sunil Agarwal & Co.'), 1, 'doc7.pdf', 'doc7.pdf', '/files/doc7.pdf', 716800, 'pdf', 'COMPANY_DOCS', NOW()),
(8, (SELECT id FROM clients WHERE company_name = 'Meena Joshi & Associates'), 1, 'doc8.pdf', 'doc8.pdf', '/files/doc8.pdf', 819200, 'pdf', 'OTHER', NOW()),
(9, (SELECT id FROM clients WHERE company_name = 'Sunil Agarwal & Co.'), 1, 'doc9.pdf', 'doc9.pdf', '/files/doc9.pdf', 921600, 'pdf', 'PAN_CARD', NOW()),
(10, (SELECT id FROM clients WHERE company_name = 'Meena Joshi & Associates'), 1, 'doc10.pdf', 'doc10.pdf', '/files/doc10.pdf', 1024000, 'pdf', 'AADHAR', NOW()); 