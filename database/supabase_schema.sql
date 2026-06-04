-- ============================================
-- PUP Pass Slip System - Supabase PostgreSQL Schema
-- ============================================

-- TABLE: User
CREATE TABLE IF NOT EXISTS "User" (
    user_id     SERIAL PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100),
    username    VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(10) NOT NULL CHECK (role IN ('admin', 'staff')),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- TABLE: Employee
CREATE TABLE IF NOT EXISTS "Employee" (
    emp_id      SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    department  VARCHAR(100) NOT NULL,
    position    VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- TABLE: Visitor
CREATE TABLE IF NOT EXISTS "Visitor" (
    visitor_id    SERIAL PRIMARY KEY,
    visitor_name  VARCHAR(100) NOT NULL,
    company       VARCHAR(100),
    purpose       VARCHAR(200),
    time_out      TIMESTAMP,
    time_in       TIMESTAMP,
    host_employee VARCHAR(100),
    email         VARCHAR(100),
    contact       VARCHAR(20),
    status        VARCHAR(20) DEFAULT 'Pending'
);

-- TABLE: Pass_slip
CREATE TABLE IF NOT EXISTS "Pass_slip" (
    slip_id     SERIAL PRIMARY KEY,
    emp_id      INT NOT NULL,
    reason      VARCHAR(255) NOT NULL,
    time_out    TIMESTAMP NOT NULL,
    time_in     TIMESTAMP,
    duration    VARCHAR(50),
    issued_by   INT NOT NULL,
    status      VARCHAR(20) DEFAULT 'Pending' CHECK (status IN ('Pending','Approved','Rejected','Returned')),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (emp_id) REFERENCES "Employee"(emp_id),
    FOREIGN KEY (issued_by) REFERENCES "User"(user_id)
);

-- TABLE: Activity_logs
CREATE TABLE IF NOT EXISTS "Activity_logs" (
    log_id       SERIAL PRIMARY KEY,
    emp_id       INT,
    action       VARCHAR(255) NOT NULL,
    timestamp    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    performed_by VARCHAR(100),
    FOREIGN KEY (emp_id) REFERENCES "Employee"(emp_id)
);

-- Default Admin Account (password: admin123)
INSERT INTO "User" (full_name, email, username, password, role)
VALUES ('Administrator', 'admin@passlip.com', 'admin', 'admin123', 'admin')
ON CONFLICT (username) DO NOTHING;

-- Sample Employees
INSERT INTO "Employee" (name, department, position) VALUES
('Kevin Brian',    'IT Department', 'IT Staff'),
('Justin Gian',    'HR Department', 'HR Officer'),
('Nico Ancheta',   'Finance',       'Finance Staff'),
('Josiah David',   'Marketing',     'Marketing Staff'),
('LJ Catindig',    'Operations',    'Operations Staff'),
('Emil Fernandez', 'IT Department', 'IT Staff'),
('Ryken Gabriel',  'Admin',         'Administrative Staff')
ON CONFLICT DO NOTHING;
