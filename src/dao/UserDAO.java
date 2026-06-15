package dao;

import models.User;
import main.utils.DBConnection;
import main.utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // ── Existing: Login ──────────────────────────────────────────
    public User login(String username, String password, String role) {
        String sql = "SELECT * FROM \"User\" WHERE username = ? AND LOWER(role) = LOWER(?) AND LOWER(status) = 'active'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, role);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                boolean valid = false;
                try { valid = PasswordUtils.verifyPassword(password, storedHash); } catch (Exception e) {}
                if (!valid) valid = password.equals(storedHash);
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

    // ── Existing: Register ───────────────────────────────────────
    public boolean register(String fullName, String email, String username, String password, String role) {
        String sql = "INSERT INTO \"User\" (username, password, role, full_name, email, status) VALUES (?, ?, ?, ?, ?, 'PENDING')";
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

    // ── Existing: Username Check ─────────────────────────────────
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

    // ── NEW: Email Exists (for Forgot Password) ──────────────────
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM \"User\" WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { System.out.println("Email check error: " + e.getMessage()); }
        return false;
    }

    // ── NEW: Update Password by Email (for Forgot Password) ──────
    public boolean updatePasswordByEmail(String email, String newPassword) {
        String sql = "UPDATE \"User\" SET password = ? WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String hashed = PasswordUtils.hashPassword(newPassword);
            stmt.setString(1, hashed);
            stmt.setString(2, email.trim());
            int rows = stmt.executeUpdate();
            System.out.println("[UserDAO] Updated password for email: " + email + " (" + rows + " rows)");
            return rows > 0;
        } catch (Exception e) {
            System.out.println("Update password error: " + e.getMessage());
            return false;
        }
    }

    // ── UPDATED: Get ALL users (PENDING, ACTIVE, REJECTED) for history ──
    public List<User> getAllUsersForApproval() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, username, role, status FROM \"User\" ORDER BY user_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setUserId  (rs.getInt   ("user_id"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail   (rs.getString("email"));
                u.setUsername(rs.getString("username"));
                u.setRole    (rs.getString("role"));
                u.setStatus  (rs.getString("status"));
                list.add(u);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── KEPT for backward compat ─────────────────────────────────
    public List<User> getPendingUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, username, role, status FROM \"User\" WHERE status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setUserId  (rs.getInt   ("user_id"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail   (rs.getString("email"));
                u.setUsername(rs.getString("username"));
                u.setRole    (rs.getString("role"));
                u.setStatus  (rs.getString("status"));
                list.add(u);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── NEW: Approve user ────────────────────────────────────────
    public boolean approveUser(int userId) {
        String sql = "UPDATE \"User\" SET status = 'ACTIVE' WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── NEW: Reject user ─────────────────────────────────────────
    public boolean rejectUser(int userId) {
        String sql = "UPDATE \"User\" SET status = 'REJECTED' WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}
