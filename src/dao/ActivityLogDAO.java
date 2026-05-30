package dao;

import models.ActivityLog;
import main.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDAO {

    // ===== LOG ACTION =====
    public boolean logAction(
            int empId,
            String action,
            int performedBy
    ) {

        String sql =
                "INSERT INTO Activity_logs " +
                        "(emp_id, action, performed_by, timestamp) " +
                        "VALUES (?, ?, ?, NOW())";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, empId);

            stmt.setString(2, action);

            stmt.setInt(3, performedBy);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Log action error: " +
                            e.getMessage()
            );

            return false;
        }
    }

    // ===== LOG ACTIVITY =====
    public boolean logActivity(
            int empId,
            String action,
            String performedBy
    ) {

        String sql =
                "INSERT INTO Activity_logs " +
                        "(emp_id, action, performed_by, timestamp) " +
                        "VALUES (?, ?, ?, NOW())";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, empId);

            stmt.setString(2, action);

            stmt.setString(3, performedBy);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Log activity error: " +
                            e.getMessage()
            );

            return false;
        }
    }

    // ===== GET RECENT LOGS =====
    public List<ActivityLog> getRecentLogs(int limit) {

        List<ActivityLog> logs =
                new ArrayList<>();

        String sql =
                "SELECT * FROM Activity_logs " +
                        "ORDER BY timestamp DESC " +
                        "LIMIT ?";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, limit);

            ResultSet rs =
                    stmt.executeQuery();

            while (rs.next()) {

                logs.add(
                        mapResultSet(rs)
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "Get recent logs error: " +
                            e.getMessage()
            );
        }

        return logs;
    }

    // ===== GET LOGS BY EMPLOYEE =====
    public List<ActivityLog> getLogsByEmployee(
            int empId
    ) {

        List<ActivityLog> logs =
                new ArrayList<>();

        String sql =
                "SELECT * FROM Activity_logs " +
                        "WHERE emp_id = ? " +
                        "ORDER BY timestamp DESC";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, empId);

            ResultSet rs =
                    stmt.executeQuery();

            while (rs.next()) {

                logs.add(
                        mapResultSet(rs)
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "Get logs by employee error: " +
                            e.getMessage()
            );
        }

        return logs;
    }

    // ===== MAP RESULT SET =====
    private ActivityLog mapResultSet(ResultSet rs)
            throws SQLException {

        ActivityLog log =
                new ActivityLog();

        log.setLogId(
                rs.getInt("log_id")
        );

        log.setEmpId(
                rs.getInt("emp_id")
        );

        log.setAction(
                rs.getString("action")
        );

        // ===== PERFORMED BY =====
        try {

            log.setPerformedBy(
                    rs.getString("performed_by")
            );

        } catch (Exception e) {

            System.out.println(
                    "Performed by mapping error: " +
                            e.getMessage()
            );
        }

        // ===== TIMESTAMP =====
        Timestamp ts =
                rs.getTimestamp("timestamp");

        if (ts != null) {

            log.setTimestamp(
                    ts.toLocalDateTime()
            );
        }

        return log;
    }
}