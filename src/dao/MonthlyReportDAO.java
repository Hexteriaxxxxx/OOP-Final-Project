package dao;

import models.MonthlyReport;
import main.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MonthlyReportDAO {

    /**
     * Returns a list of monthly report summaries from pass_slip_db.
     * Groups pass slips by month, counts totals, and computes avg duration.
     */
    public List<MonthlyReport> getMonthlyReports() {
        List<MonthlyReport> reports = new ArrayList<>();

        String sql = """
                SELECT
                    DATE_FORMAT(time_out, '%M %Y')          AS month,
                    COUNT(*)                                 AS total_requests,
                    SUM(status = 'Completed')                AS approved,
                    SUM(status = 'Rejected')                 AS rejected,
                    SUM(status = 'Pending')                  AS pending,
                    AVG(
                        TIMESTAMPDIFF(MINUTE, time_out, time_in)
                    )                                        AS avg_minutes
                FROM pass_slip
                WHERE time_out IS NOT NULL
                GROUP BY DATE_FORMAT(time_out, '%Y-%m')
                ORDER BY DATE_FORMAT(time_out, '%Y-%m') DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String month        = rs.getString("month");
                int totalRequests   = rs.getInt("total_requests");
                int approved        = rs.getInt("approved");
                int rejected        = rs.getInt("rejected");
                int pending         = rs.getInt("pending");
                double avgMinutes   = rs.getDouble("avg_minutes");

                String avgDuration  = formatAvgDuration(avgMinutes);

                reports.add(new MonthlyReport(
                        month, totalRequests, approved, rejected, pending, avgDuration
                ));
            }

        } catch (SQLException e) {
            System.err.println("MonthlyReportDAO.getMonthlyReports() error: " + e.getMessage());
        }

        return reports;
    }

    // ─── Helper ───────────────────────────────────────────────────
    private String formatAvgDuration(double totalMinutes) {
        if (totalMinutes <= 0) return "—";
        long mins = Math.round(totalMinutes);
        long hours = mins / 60;
        long remaining = mins % 60;
        return String.format("%dh %02dm", hours, remaining);
    }
}