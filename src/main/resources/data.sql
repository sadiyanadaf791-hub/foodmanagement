-- Sample data for Food Waste Management System
-- Passwords are BCrypt encoded for '1234'

INSERT INTO users (username, password, email, full_name, role, enabled) VALUES
('admin', '$2a$10$8K2GzVtF8Rf6GJ9dO9zOse.5Q8z3z3z3z3z3z3z3z3z3z3z3z3z3z', 'admin@foodbridge.com', 'System Admin', 'ADMIN', 1),
('donor1', '$2a$10$8K2GzVtF8Rf6GJ9dO9zOse.5Q8z3z3z3z3z3z3z3z3z3z3z3z3z3z', 'donor@foodbridge.com', 'John Donor', 'DONOR', 1),
('ngo1', '$2a$10$8K2GzVtF8Rf6GJ9dO9zOse.5Q8z3z3z3z3z3z3z3z3z3z3z3z3z3z', 'ngo@foodbridge.com', 'NGO Representative', 'NGO', 1);