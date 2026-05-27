package dao;

import models.User;
import main.utils.DBConnection;
import utils.PasswordUtils;
import java.sql.*;

public class UserDAO {

    // Login - check username and password
    public User login(String username, String password, String role) {
        String sql = "SELECT * FROM User WHERE username = ? AND role = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, role);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");

                // I-verify ang password gamit ang PasswordUtils
                if (PasswordUtils.verifyPassword(password, storedHash)) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(storedHash);
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return null;
    }

    // Register new user
    public boolean register(String fullName, String email, String username, String password, String role) {
        String sql = "INSERT INTO User (username, password, role, full_name, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // I-hash ang password bago i-save sa database
            String hashedPassword = PasswordUtils.hashPassword(password);

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword); // ← hashed na!
            stmt.setString(3, role);
            stmt.setString(4, fullName);
            stmt.setString(5, email);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Register error: " + e.getMessage());
            return false;
        }
    }

    // Check if username already exists
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM User WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Check username error: " + e.getMessage());
        }
        return false;
    }
}