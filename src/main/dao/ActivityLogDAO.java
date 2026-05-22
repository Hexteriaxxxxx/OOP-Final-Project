package main.dao;

import main.models.ActivityLog;
import main.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDAO {

    public List<ActivityLog> getRecentLogs(int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM Activity_logs ORDER BY timestamp DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ActivityLog log = new ActivityLog();
                log.setLogId(rs.getInt("log_id"));
                log.setEmpId(rs.getInt("emp_id"));
                log.setAction(rs.getString("action"));
                log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                log.setPerformedBy(rs.getString("performed_by"));
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    public boolean logActivity(int empId, String action, String performedBy) {
        String sql = "INSERT INTO Activity_logs (emp_id, action, timestamp, performed_by) VALUES (?, ?, NOW(), ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setString(2, action);
            ps.setString(3, performedBy);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}