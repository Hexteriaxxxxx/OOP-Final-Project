-- ============================================
-- Employee Pass Slip Request, Issuance and Monitoring System
-- Database Schema - Final Version
-- ============================================

CREATE DATABASE IF NOT EXISTS pass_slip_db;
USE pass_slip_db;

-- ============================================
-- TABLE: User
-- ============================================
CREATE TABLE IF NOT EXISTS User (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100),
    username    VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        ENUM('admin', 'staff') NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TABLE: Employee
-- ============================================
CREATE TABLE IF NOT EXISTS Employee (
    emp_id      INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    department  VARCHAR(100) NOT NULL,
    position    VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TABLE: Pass_slip
-- ============================================
CREATE TABLE IF NOT EXISTS Pass_slip (
    slip_id     INT AUTO_INCREMENT PRIMARY KEY,
    emp_id      INT NOT NULL,
    reason      VARCHAR(255) NOT NULL,
    time_out    DATETIME NOT NULL,
    time_in     DATETIME,
    duration    VARCHAR(50),
    issued_by   INT NOT NULL,
    status      ENUM('active', 'returned') DEFAULT 'active',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (emp_id) REFERENCES Employee(emp_id),
    FOREIGN KEY (issued_by) REFERENCES User(user_id)
);

-- ============================================
-- TABLE: Activity_logs
-- ============================================
CREATE TABLE IF NOT EXISTS Activity_logs (
    log_id       INT AUTO_INCREMENT PRIMARY KEY,
    emp_id       INT,
    action       VARCHAR(255) NOT NULL,
    timestamp    DATETIME DEFAULT CURRENT_TIMESTAMP,
    performed_by INT,
    FOREIGN KEY (emp_id) REFERENCES Employee(emp_id),
    FOREIGN KEY (performed_by) REFERENCES User(user_id)
);

-- ============================================
-- Default Admin Account
-- Username: admin | Password: admin123
-- ============================================
INSERT INTO User (full_name, email, username, password, role)
VALUES ('Administrator', 'admin@passlip.com', 'admin', 'admin123', 'admin');

-- ============================================
-- Sample Employees for Testing
-- ============================================
INSERT INTO Employee (name, department, position) VALUES
('Kevin Brian', 'IT Department', 'IT Staff'),
('Justin Gian', 'HR Department', 'HR Officer'),
('Nico Ancheta', 'Finance', 'Finance Staff'),
('Josiah David', 'Marketing', 'Marketing Staff'),
('LJ Catindig', 'Operations', 'Operations Staff'),
('Emil Fernandez', 'IT Department', 'IT Staff'),
('Ryken Gabriel', 'Admin', 'Administrative Staff');
