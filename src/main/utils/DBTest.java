package main.utils;

import main.dao.ActivityLogDAO;
import main.dao.EmployeeDAO;
import main.dao.PassSlipDAO;
import main.dao.UserDAO;
import main.models.ActivityLog;
import main.models.Employee;
import main.models.PassSlip;
import main.models.User;

import java.sql.Connection;
import java.util.List;

public class DBTest {
    public static void main(String[] args) {

        System.out.println("=============================");
        System.out.println("   DATABASE CONNECTION TEST  ");
        System.out.println("=============================");

        // ✅ Test Connection
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            System.out.println("✅ Database connected successfully!\n");
        } else {
            System.out.println("❌ Connection failed! Stopping tests.");
            return;
        }

        // =============================
        // TEST 1 — UserDAO
        // =============================
        System.out.println("--- UserDAO Tests ---");
        UserDAO userDAO = new UserDAO();

        boolean exists = userDAO.usernameExists("admin");
        System.out.println("usernameExists('admin'): " + exists);

        User user = userDAO.login("admin", "admin123", "Admin");
        System.out.println("login(): " + (user != null ? "✅ Found: " + user.getUsername() : "❌ Not found"));

        // =============================
        // TEST 2 — EmployeeDAO
        // =============================
        System.out.println("\n--- EmployeeDAO Tests ---");
        EmployeeDAO empDAO = new EmployeeDAO();

        List<Employee> employees = empDAO.getAllEmployees();
        System.out.println("getAllEmployees(): " + employees.size() + " records");

        Employee emp = empDAO.getEmployeeById(1);
        System.out.println("getEmployeeById(1): " + (emp != null ? "✅ Found: " + emp.getName() : "❌ Not found"));

        List<Employee> searched = empDAO.searchEmployees("a");
        System.out.println("searchEmployees('a'): " + searched.size() + " records");

        // =============================
        // TEST 3 — PassSlipDAO
        // =============================
        System.out.println("\n--- PassSlipDAO Tests ---");
        PassSlipDAO passDAO = new PassSlipDAO();

        List<PassSlip> allSlips = passDAO.getAllPassSlips();
        System.out.println("getAllPassSlips(): " + allSlips.size() + " records");

        List<PassSlip> todaySlips = passDAO.getTodayPassSlips();
        System.out.println("getTodayPassSlips(): " + todaySlips.size() + " records");

        List<PassSlip> activeSlips = passDAO.getActivePassSlips();
        System.out.println("getActivePassSlips(): " + activeSlips.size() + " records");

        System.out.println("countTodaySlips(): " + passDAO.countTodaySlips());
        System.out.println("countActiveSlips(): " + passDAO.countActiveSlips());

        // =============================
        // TEST 4 — ActivityLogDAO
        // =============================
        System.out.println("\n--- ActivityLogDAO Tests ---");
        ActivityLogDAO logDAO = new ActivityLogDAO();

        boolean logged = logDAO.logActivity(1, "TEST_ACTION", "admin");
        System.out.println("logActivity(): " + (logged ? "✅ Saved!" : "❌ Failed!"));

        List<ActivityLog> recentLogs = logDAO.getRecentLogs(5);
        System.out.println("getRecentLogs(5): " + recentLogs.size() + " records");

        System.out.println("\n=============================");
        System.out.println("      ALL TESTS DONE!        ");
        System.out.println("=============================");
    }
}