USE pass_slip_db;

ALTER TABLE Visitor
    ADD COLUMN email   VARCHAR(100) AFTER host_employee,
    ADD COLUMN contact VARCHAR(20)  AFTER email;
