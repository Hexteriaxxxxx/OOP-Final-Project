package dao;

import models.PassSlip;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ReportDAO {

    // ─────────────────────────────────────────────
    // GET DAILY REPORT
    // Returns all pass slips for a specific date
    // ─────────────────────────────────────────────
    public List<PassSlip> getDailyReport(LocalDate date) {
        List<PassSlip> list = new ArrayList<>();

        String sql = """
                SELECT ps.slip_id, ps.emp_id, e.name AS emp_name, e.department,
                       ps.reason, ps.time_out, ps.time_in, ps.duration,
                       ps.issued_by, ps.status
                FROM pass_slip ps
                JOIN employee e ON ps.emp_id = e.emp_id
                WHERE DATE(ps.time_out) = ?
                ORDER BY ps.time_out ASC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PassSlip slip = mapResultSet(rs);
                list.add(slip);
            }

        } catch (SQLException e) {
            System.err.println("getDailyReport error: " + e.getMessage());
        }

        return list;
    }

    // ─────────────────────────────────────────────
    // GET MONTHLY REPORT
    // Returns all pass slips for a specific month and year
    // ─────────────────────────────────────────────
    public List<PassSlip> getMonthlyReport(int month, int year) {
        List<PassSlip> list = new ArrayList<>();

        String sql = """
                SELECT ps.slip_id, ps.emp_id, e.name AS emp_name, e.department,
                       ps.reason, ps.time_out, ps.time_in, ps.duration,
                       ps.issued_by, ps.status
                FROM pass_slip ps
                JOIN employee e ON ps.emp_id = e.emp_id
                WHERE MONTH(ps.time_out) = ? AND YEAR(ps.time_out) = ?
                ORDER BY ps.time_out ASC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, month);
            stmt.setInt(2, year);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PassSlip slip = mapResultSet(rs);
                list.add(slip);
            }

        } catch (SQLException e) {
            System.err.println("getMonthlyReport error: " + e.getMessage());
        }

        return list;
    }

    // ─────────────────────────────────────────────
    // GET TOTAL BY DEPARTMENT
    // Returns count of pass slips per department
    // Key = department name, Value = count
    // ─────────────────────────────────────────────
    public Map<String, Integer> getTotalByDepartment() {
        Map<String, Integer> map = new LinkedHashMap<>();

        String sql = """
                SELECT e.department, COUNT(ps.slip_id) AS total
                FROM pass_slip ps
                JOIN employee e ON ps.emp_id = e.emp_id
                GROUP BY e.department
                ORDER BY total DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String department = rs.getString("department");
                int total = rs.getInt("total");
                map.put(department, total);
            }

        } catch (SQLException e) {
            System.err.println("getTotalByDepartment error: " + e.getMessage());
        }

        return map;
    }

    // ─────────────────────────────────────────────
    // GET AVERAGE DURATION
    // Returns average time outside in minutes (completed slips only)
    // ─────────────────────────────────────────────
    public double getAverageDuration() {
        String sql = """
                SELECT AVG(TIMESTAMPDIFF(MINUTE, time_out, time_in)) AS avg_duration
                FROM pass_slip
                WHERE time_in IS NOT NULL AND status = 'Completed'
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("avg_duration");
            }

        } catch (SQLException e) {
            System.err.println("getAverageDuration error: " + e.getMessage());
        }

        return 0.0;
    }

    // ─────────────────────────────────────────────
    // HELPER — Map ResultSet to PassSlip object
    // ─────────────────────────────────────────────
    private PassSlip mapResultSet(ResultSet rs) throws SQLException {
        PassSlip slip = new PassSlip();

        slip.setSlipId(rs.getInt("slip_id"));
        slip.setEmpId(rs.getInt("emp_id"));
        slip.setEmpName(rs.getString("emp_name"));
        slip.setDepartment(rs.getString("department"));
        slip.setReason(rs.getString("reason"));
        slip.setIssuedBy(rs.getString("issued_by"));
        slip.setStatus(rs.getString("status"));
        slip.setDuration(rs.getString("duration"));

        Timestamp timeOut = rs.getTimestamp("time_out");
        if (timeOut != null) slip.setTimeOut(timeOut.toLocalDateTime());

        Timestamp timeIn = rs.getTimestamp("time_in");
        if (timeIn != null) slip.setTimeIn(timeIn.toLocalDateTime());

        return slip;
    }
}
