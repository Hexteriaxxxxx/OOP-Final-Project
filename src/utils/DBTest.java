package utils;

import dao.ActivityLogDAO;
import dao.EmployeeDAO;
import dao.PassSlipDAO;
import dao.UserDAO;
import models.ActivityLog;
import models.Employee;
import models.PassSlip;
import models.User;
import main.utils.DBConnection;
import java.sql.Connection;
import java.util.List;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

        boolean logged = logDAO.logAction(1, "TEST_ACTION", 1);
        System.out.println("logAction(): " + (logged ? "✅ Saved!" : "❌ Failed!"));

        List<ActivityLog> recentLogs = logDAO.getRecentLogs(5);
        System.out.println("getRecentLogs(5): " + recentLogs.size() + " records");

        List<ActivityLog> empLogs = logDAO.getLogsByEmployee(1);
        System.out.println("getLogsByEmployee(1): " + empLogs.size() + " records");
        // =============================
        // HASH EXISTING PASSWORDS
        // =============================
        System.out.println("\n--- Hashing Existing Passwords ---");
        String selectSQL = "SELECT user_id, password FROM User";
        String updateSQL = "UPDATE User SET password = ? WHERE user_id = ?";

        try (Statement hashStmt = conn.createStatement();
             ResultSet hashRs = hashStmt.executeQuery(selectSQL);
             PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {

            while (hashRs.next()) {
                int userId = hashRs.getInt("user_id");
                String plainPassword = hashRs.getString("password");

                if (plainPassword.length() > 50) {
                    System.out.println("Skipping user_id " + userId + " - already hashed.");
                    continue;
                }

                String hashed = utils.PasswordUtils.hashPassword(plainPassword);
                updateStmt.setString(1, hashed);
                updateStmt.setInt(2, userId);
                updateStmt.executeUpdate();
                System.out.println("✅ Hashed user_id: " + userId);
            }
            System.out.println("✅ Done! Lahat ng passwords na-hash na!");

        } catch (SQLException e) {
            System.out.println("Hash error: " + e.getMessage());
        }

        System.out.println("\n=============================");
        System.out.println("      ALL TESTS DONE!        ");
        System.out.println("=============================");
    }
}