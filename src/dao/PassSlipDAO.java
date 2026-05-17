package dao;

import models.PassSlip;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PassSlipDAO {

    // Issue new pass slip
    public boolean issuePassSlip(PassSlip passSlip) {
        String sql = "INSERT INTO Pass_slip (emp_id, reason, time_out, issued_by, status) VALUES (?, ?, ?, ?, 'active')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, passSlip.getEmpId());
            stmt.setString(2, passSlip.getReason());
            stmt.setTimestamp(3, Timestamp.valueOf(passSlip.getTimeOut()));
            stmt.setInt(4, passSlip.getIssuedBy());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Issue pass slip error: " + e.getMessage());
            return false;
        }
    }

    // Record time-in
    public boolean recordTimeIn(int slipId, LocalDateTime timeIn, String duration) {
        String sql = "UPDATE Pass_slip SET time_in = ?, duration = ?, status = 'returned' WHERE slip_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(timeIn));
            stmt.setString(2, duration);
            stmt.setInt(3, slipId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Record time-in error: " + e.getMessage());
            return false;
        }
    }

    // Get all pass slips with employee info
    public List<PassSlip> getAllPassSlips() {
        List<PassSlip> slips = new ArrayList<>();
        String sql = "SELECT ps.*, e.name as emp_name, e.department " +
                     "FROM Pass_slip ps " +
                     "JOIN Employee e ON ps.emp_id = e.emp_id " +
                     "ORDER BY ps.time_out DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PassSlip slip = mapResultSet(rs);
                slips.add(slip);
            }
        } catch (SQLException e) {
            System.out.println("Get pass slips error: " + e.getMessage());
        }
        return slips;
    }

    // Get today's pass slips
    public List<PassSlip> getTodayPassSlips() {
        List<PassSlip> slips = new ArrayList<>();
        String sql = "SELECT ps.*, e.name as emp_name, e.department " +
                     "FROM Pass_slip ps " +
                     "JOIN Employee e ON ps.emp_id = e.emp_id " +
                     "WHERE DATE(ps.time_out) = CURDATE() " +
                     "ORDER BY ps.time_out DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PassSlip slip = mapResultSet(rs);
                slips.add(slip);
            }
        } catch (SQLException e) {
            System.out.println("Get today slips error: " + e.getMessage());
        }
        return slips;
    }

    // Get active pass slips (not yet returned)
    public List<PassSlip> getActivePassSlips() {
        List<PassSlip> slips = new ArrayList<>();
        String sql = "SELECT ps.*, e.name as emp_name, e.department " +
                     "FROM Pass_slip ps " +
                     "JOIN Employee e ON ps.emp_id = e.emp_id " +
                     "WHERE ps.status = 'active' " +
                     "ORDER BY ps.time_out DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PassSlip slip = mapResultSet(rs);
                slips.add(slip);
            }
        } catch (SQLException e) {
            System.out.println("Get active slips error: " + e.getMessage());
        }
        return slips;
    }

    // Count today's stats
    public int countTodaySlips() {
        String sql = "SELECT COUNT(*) FROM Pass_slip WHERE DATE(time_out) = CURDATE()";
        return countQuery(sql);
    }

    public int countActiveSlips() {
        String sql = "SELECT COUNT(*) FROM Pass_slip WHERE status = 'active'";
        return countQuery(sql);
    }

    private int countQuery(String sql) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Count error: " + e.getMessage());
        }
        return 0;
    }

    // Map ResultSet to PassSlip
    private PassSlip mapResultSet(ResultSet rs) throws SQLException {
        PassSlip slip = new PassSlip();
        slip.setSlipId(rs.getInt("slip_id"));
        slip.setEmpId(rs.getInt("emp_id"));
        slip.setEmpName(rs.getString("emp_name"));
        slip.setDepartment(rs.getString("department"));
        slip.setReason(rs.getString("reason"));
        slip.setTimeOut(rs.getTimestamp("time_out").toLocalDateTime());
        Timestamp timeIn = rs.getTimestamp("time_in");
        if (timeIn != null) slip.setTimeIn(timeIn.toLocalDateTime());
        slip.setDuration(rs.getString("duration"));
        slip.setIssuedBy(rs.getInt("issued_by"));
        slip.setStatus(rs.getString("status"));
        return slip;
    }
}
