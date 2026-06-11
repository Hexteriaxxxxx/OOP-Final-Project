package dao;

import main.utils.DBConnection;
import models.PassSlip;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PassSlipDAO {

    public boolean createPassSlip(PassSlip passSlip) {
        String sql = "INSERT INTO \"Pass_slip\" (emp_id, reason, category, time_out, time_in, issued_by, status) VALUES (?, ?, ?, ?, ?, ?, 'Pending')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, passSlip.getEmpId());
            stmt.setString(2, passSlip.getReason());
            stmt.setString(3, passSlip.getCategory() != null ? passSlip.getCategory() : "Official Business");
            stmt.setTimestamp(4, Timestamp.valueOf(passSlip.getTimeOut()));
            if (passSlip.getTimeIn() != null) stmt.setTimestamp(5, Timestamp.valueOf(passSlip.getTimeIn()));
            else stmt.setNull(5, Types.TIMESTAMP);
            stmt.setInt(6, passSlip.getIssuedBy());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("Create pass slip error: " + e.getMessage()); return false; }
    }

    public boolean issuePassSlip(PassSlip passSlip) {
        String sql = "INSERT INTO \"Pass_slip\" (emp_id, reason, category, time_out, issued_by, status) VALUES (?, ?, ?, ?, ?, 'Pending')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, passSlip.getEmpId());
            stmt.setString(2, passSlip.getReason());
            stmt.setString(3, passSlip.getCategory() != null ? passSlip.getCategory() : "Official Business");
            stmt.setTimestamp(4, Timestamp.valueOf(passSlip.getTimeOut()));
            stmt.setInt(5, passSlip.getIssuedBy());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("Issue pass slip error: " + e.getMessage()); return false; }
    }

    public boolean updatePassSlipStatus(int slipId, String status) {
        String sql = "UPDATE \"Pass_slip\" SET status = ? WHERE slip_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, slipId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("Update status error: " + e.getMessage()); return false; }
    }

    public boolean recordTimeIn(int slipId, LocalDateTime timeIn, String duration) {
        String sql = "UPDATE \"Pass_slip\" SET time_in = ?, duration = ?, status = 'Returned' WHERE slip_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(timeIn));
            stmt.setString(2, duration);
            stmt.setInt(3, slipId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("Record time-in error: " + e.getMessage()); return false; }
    }

    /**
     * Returns Approved slips where the expected time_in has already passed
     * but the employee has not yet returned (status still 'Approved').
     * These are newly overdue — not yet marked as 'Overdue'.
     */
    public List<PassSlip> getNewlyOverdueSlips() {
        List<PassSlip> slips = new ArrayList<>();
        String sql = "SELECT ps.*, e.name AS emp_name, e.department " +
                     "FROM \"Pass_slip\" ps " +
                     "JOIN \"Employee\" e ON ps.emp_id = e.emp_id " +
                     "WHERE ps.status = 'Approved' " +
                     "  AND ps.time_in IS NOT NULL " +
                     "  AND ps.time_in < NOW() " +
                     "ORDER BY ps.time_in ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) slips.add(mapResultSet(rs));
        } catch (SQLException e) { System.out.println("getNewlyOverdueSlips error: " + e.getMessage()); }
        return slips;
    }

    /**
     * Returns all currently overdue slips (for dashboard display).
     */
    public List<PassSlip> getOverdueSlips() {
        List<PassSlip> slips = new ArrayList<>();
        String sql = "SELECT ps.*, e.name AS emp_name, e.department " +
                     "FROM \"Pass_slip\" ps " +
                     "JOIN \"Employee\" e ON ps.emp_id = e.emp_id " +
                     "WHERE ps.status = 'Overdue' " +
                     "ORDER BY ps.time_in ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) slips.add(mapResultSet(rs));
        } catch (SQLException e) { System.out.println("getOverdueSlips error: " + e.getMessage()); }
        return slips;
    }

    public List<PassSlip> getAllPassSlips() {
        List<PassSlip> slips = new ArrayList<>();
        String sql = "SELECT ps.*, e.name AS emp_name, e.department FROM \"Pass_slip\" ps " +
                     "JOIN \"Employee\" e ON ps.emp_id = e.emp_id ORDER BY ps.time_out DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) slips.add(mapResultSet(rs));
        } catch (SQLException e) { System.out.println("Get pass slips error: " + e.getMessage()); }
        return slips;
    }

    public List<PassSlip> getTodayPassSlips() {
        List<PassSlip> slips = new ArrayList<>();
        String sql = "SELECT ps.*, e.name AS emp_name, e.department FROM \"Pass_slip\" ps " +
                     "JOIN \"Employee\" e ON ps.emp_id = e.emp_id " +
                     "WHERE DATE(ps.time_out) = CURRENT_DATE ORDER BY ps.time_out DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) slips.add(mapResultSet(rs));
        } catch (SQLException e) { System.out.println("Get today slips error: " + e.getMessage()); }
        return slips;
    }

    public List<PassSlip> getActivePassSlips() {
        List<PassSlip> slips = new ArrayList<>();
        String sql = "SELECT ps.*, e.name AS emp_name, e.department FROM \"Pass_slip\" ps " +
                     "JOIN \"Employee\" e ON ps.emp_id = e.emp_id " +
                     "WHERE ps.status = 'Approved' ORDER BY ps.time_out DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) slips.add(mapResultSet(rs));
        } catch (SQLException e) { System.out.println("Get active slips error: " + e.getMessage()); }
        return slips;
    }

    public int countTodaySlips() {
        return countQuery("SELECT COUNT(*) FROM \"Pass_slip\" WHERE DATE(time_out) = CURRENT_DATE");
    }

    public int countActiveSlips() {
        return countQuery("SELECT COUNT(*) FROM \"Pass_slip\" WHERE status = 'Approved'");
    }

    public int countOverdueSlips() {
        return countQuery("SELECT COUNT(*) FROM \"Pass_slip\" WHERE status = 'Overdue'");
    }

    private int countQuery(String sql) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.out.println("Count error: " + e.getMessage()); }
        return 0;
    }

    private PassSlip mapResultSet(ResultSet rs) throws SQLException {
        PassSlip slip = new PassSlip();
        slip.setSlipId    (rs.getInt   ("slip_id"));
        slip.setEmpId     (rs.getInt   ("emp_id"));
        slip.setEmpName   (rs.getString("emp_name"));
        slip.setDepartment(rs.getString("department"));
        slip.setReason    (rs.getString("reason"));
        slip.setIssuedBy  (rs.getInt   ("issued_by"));
        slip.setStatus    (rs.getString("status"));
        try {
            String cat = rs.getString("category");
            slip.setCategory(cat != null ? cat : "Official Business");
        } catch (SQLException ignored) {
            slip.setCategory("Official Business");
        }
        Timestamp timeOut = rs.getTimestamp("time_out");
        if (timeOut != null) slip.setTimeOut(timeOut.toLocalDateTime());
        Timestamp timeIn = rs.getTimestamp("time_in");
        if (timeIn != null) slip.setTimeIn(timeIn.toLocalDateTime());
        slip.setDuration(rs.getString("duration"));
        return slip;
    }
}
