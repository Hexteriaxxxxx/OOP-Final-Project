package dao;

import models.User;
import main.utils.DBConnection;
import main.utils.PasswordUtils;
import java.sql.*;

public class UserDAO {

    public User login(String username, String password, String role) {
        String sql = "SELECT * FROM \"User\" WHERE username = ? AND role = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, role);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                // Try hashed password first, then plain text fallback
                boolean valid = false;
                try { valid = PasswordUtils.verifyPassword(password, storedHash); } catch (Exception e) {}
                if (!valid) valid = password.equals(storedHash); // plain text fallback
                if (valid) {
                    User user = new User();
                    user.setUserId  (rs.getInt   ("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(storedHash);
                    user.setRole    (rs.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) { System.out.println("Login error: " + e.getMessage()); }
        return null;
    }

    public boolean register(String fullName, String email, String username, String password, String role) {
        String sql = "INSERT INTO \"User\" (username, password, role, full_name, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String hashedPassword = PasswordUtils.hashPassword(password);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);
            stmt.setString(4, fullName);
            stmt.setString(5, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("Register error: " + e.getMessage()); return false; }
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM \"User\" WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { System.out.println("Check username error: " + e.getMessage()); }
        return false;
    }
}
