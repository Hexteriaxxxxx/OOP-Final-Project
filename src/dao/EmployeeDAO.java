package dao;

import models.Employee;
import main.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    // Get all employees
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM Employee ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Employee emp = new Employee();
                emp.setEmpId(rs.getInt("emp_id"));
                emp.setName(rs.getString("name"));
                emp.setDepartment(rs.getString("department"));
                emp.setPosition(rs.getString("position"));
                employees.add(emp);
            }
        } catch (SQLException e) {
            System.out.println("Get employees error: " + e.getMessage());
        }
        return employees;
    }

    // Get employee by ID
    public Employee getEmployeeById(int empId) {
        String sql = "SELECT * FROM Employee WHERE emp_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, empId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Employee emp = new Employee();
                emp.setEmpId(rs.getInt("emp_id"));
                emp.setName(rs.getString("name"));
                emp.setDepartment(rs.getString("department"));
                emp.setPosition(rs.getString("position"));
                return emp;
            }
        } catch (SQLException e) {
            System.out.println("Get employee error: " + e.getMessage());
        }
        return null;
    }

    // Add new employee
    public boolean addEmployee(Employee employee) {
        String sql = "INSERT INTO Employee (name, department, position) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employee.getName());
            stmt.setString(2, employee.getDepartment());
            stmt.setString(3, employee.getPosition());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Add employee error: " + e.getMessage());
            return false;
        }
    }

    // Update employee
    public boolean updateEmployee(Employee employee) {
        String sql = "UPDATE Employee SET name = ?, department = ?, position = ? WHERE emp_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employee.getName());
            stmt.setString(2, employee.getDepartment());
            stmt.setString(3, employee.getPosition());
            stmt.setInt(4, employee.getEmpId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Update employee error: " + e.getMessage());
            return false;
        }
    }

    // Delete employee
    public boolean deleteEmployee(int empId) {
        String sql = "DELETE FROM Employee WHERE emp_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, empId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Delete employee error: " + e.getMessage());
            return false;
        }
    }

    // Search employees
    public List<Employee> searchEmployees(String keyword) {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM Employee WHERE name LIKE ? OR department LIKE ? OR position LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String search = "%" + keyword + "%";
            stmt.setString(1, search);
            stmt.setString(2, search);
            stmt.setString(3, search);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Employee emp = new Employee();
                emp.setEmpId(rs.getInt("emp_id"));
                emp.setName(rs.getString("name"));
                emp.setDepartment(rs.getString("department"));
                emp.setPosition(rs.getString("position"));
                employees.add(emp);
            }
        } catch (SQLException e) {
            System.out.println("Search employee error: " + e.getMessage());
        }
        return employees;
    }
}
