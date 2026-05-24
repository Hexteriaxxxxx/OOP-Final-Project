package dao;

import models.ActivityLog;
import main.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDAO {

    // 1. logAction
    public boolean logAction(int empId, String action, int performedBy) {
        String sql = "INSERT INTO Activity_log (emp_id, action, performed_by, timestamp) " +
                "VALUES (?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, empId);
            stmt.setString(2, action);
            stmt.setInt(3, performedBy);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Log action error: " + e.getMessage());
            return false;
        }
    }

    // 2. getRecentLogs
    public List<ActivityLog> getRecentLogs(int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM Activity_log ORDER BY timestamp DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println("Get recent logs error: " + e.getMessage());
        }
        return logs;
    }

    // 3. getLogsByEmployee
    public List<ActivityLog> getLogsByEmployee(int empId) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM Activity_log WHERE emp_id = ? ORDER BY timestamp DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, empId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println("Get logs by employee error: " + e.getMessage());
        }
        return logs;
    }

    // Helper
    private ActivityLog mapResultSet(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setLogId(rs.getInt("log_id"));
        log.setEmpId(rs.getInt("emp_id"));
        log.setAction(rs.getString("action"));
        log.setPerformedBy(rs.getInt("performed_by"));
        log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        return log;
    }
}