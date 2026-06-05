package dao;

import main.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MonthlyReportDAO {

    public static class MonthlyRow {
        public final String month;
        public final int totalRequests, approved, rejected, pending, totalVisitors;
        public final String avgDuration;

        public MonthlyRow(String month, int totalRequests, int approved, int rejected,
                          int pending, int totalVisitors, String avgDuration) {
            this.month         = month;
            this.totalRequests = totalRequests;
            this.approved      = approved;
            this.rejected      = rejected;
            this.pending       = pending;
            this.totalVisitors = totalVisitors;
            this.avgDuration   = avgDuration;
        }
    }

    public List<MonthlyRow> getMonthlyReports() {
        List<MonthlyRow> reports = new ArrayList<>();

        String sql = """
                SELECT
                    TO_CHAR(time_out, 'Month YYYY')                                    AS month,
                    TO_CHAR(time_out, 'YYYY-MM')                                       AS sort_key,
                    COUNT(*)                                                            AS total_requests,
                    SUM(CASE WHEN status = 'Approved'  THEN 1 ELSE 0 END)              AS approved,
                    SUM(CASE WHEN status = 'Rejected'  THEN 1 ELSE 0 END)              AS rejected,
                    SUM(CASE WHEN status = 'Pending'   THEN 1 ELSE 0 END)              AS pending,
                    AVG(CASE WHEN time_in IS NOT NULL
                        THEN EXTRACT(EPOCH FROM (time_in - time_out))/60
                        ELSE NULL END)                                                  AS avg_minutes
                FROM "Pass_slip"
                WHERE time_out IS NOT NULL
                GROUP BY TO_CHAR(time_out, 'Month YYYY'), TO_CHAR(time_out, 'YYYY-MM')
                ORDER BY TO_CHAR(time_out, 'YYYY-MM') DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String month      = rs.getString("month").trim();
                int totalRequests = rs.getInt("total_requests");
                int approved      = rs.getInt("approved");
                int rejected      = rs.getInt("rejected");
                int pending       = rs.getInt("pending");
                double avgMins    = rs.getDouble("avg_minutes");
                String avgDur     = formatAvgDuration(avgMins);
                int visitors      = countVisitorsByMonth(conn, rs.getString("sort_key"));

                reports.add(new MonthlyRow(month, totalRequests, approved, rejected, pending, visitors, avgDur));
            }

        } catch (SQLException e) {
            System.out.println("MonthlyReportDAO error: " + e.getMessage());
        }

        return reports;
    }

    private int countVisitorsByMonth(Connection conn, String sortKey) {
        String sql = "SELECT COUNT(*) FROM \"Visitor\" WHERE TO_CHAR(time_out,'YYYY-MM') = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sortKey);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { /* ignore */ }
        return 0;
    }

    private String formatAvgDuration(double totalMinutes) {
        if (totalMinutes <= 0) return "—";
        long mins      = Math.round(totalMinutes);
        long hours     = mins / 60;
        long remaining = mins % 60;
        return String.format("%dh %02dm", hours, remaining);
    }
}
