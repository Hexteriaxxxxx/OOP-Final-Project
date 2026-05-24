package dao;

import models.Visitor;
import main.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VisitorDAO {

    // ── Add new visitor request ────────────────────────────────────
    public boolean addVisitor(Visitor visitor) {
        String sql = "INSERT INTO Visitor (visitor_name, company, purpose, " +
                     "time_out, host_employee, status) VALUES (?, ?, ?, ?, ?, 'Pending')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, visitor.getVisitorName());
            stmt.setString(2, visitor.getCompany());
            stmt.setString(3, visitor.getPurpose());
            stmt.setTimestamp(4, Timestamp.valueOf(visitor.getTimeOut()));
            stmt.setString(5, visitor.getHostEmployee());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("addVisitor error: " + e.getMessage());
            return false;
        }
    }

    // ── Get all visitors ───────────────────────────────────────────
    public List<Visitor> getAllVisitors() {
        List<Visitor> list = new ArrayList<>();
        String sql = "SELECT * FROM Visitor ORDER BY time_out DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("getAllVisitors error: " + e.getMessage());
        }
        return list;
    }

    // ── Get today's visitors ───────────────────────────────────────
    public List<Visitor> getTodayVisitors() {
        List<Visitor> list = new ArrayList<>();
        String sql = "SELECT * FROM Visitor WHERE DATE(time_out) = CURDATE() " +
                     "ORDER BY time_out DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("getTodayVisitors error: " + e.getMessage());
        }
        return list;
    }

    // ── Update visitor status ──────────────────────────────────────
    public boolean updateStatus(int visitorId, String status) {
        String sql = "UPDATE Visitor SET status = ? WHERE visitor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, visitorId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("updateStatus error: " + e.getMessage());
            return false;
        }
    }

    // ── Record time-in ─────────────────────────────────────────────
    public boolean recordTimeIn(int visitorId) {
        String sql = "UPDATE Visitor SET time_in = NOW(), status = 'Approved' " +
                     "WHERE visitor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, visitorId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("recordTimeIn error: " + e.getMessage());
            return false;
        }
    }

    // ── Count stats ────────────────────────────────────────────────
    public int countPending()  {
        return countByStatus("Pending");
    }

    public int countApproved() {
        return countByStatus("Approved");
    }

    public int countRejected() {
        return countByStatus("Rejected");
    }

    public int countActiveToday() {
        String sql = "SELECT COUNT(*) FROM Visitor " +
                     "WHERE status = 'Approved' AND DATE(time_out) = CURDATE()";
        return countQuery(sql);
    }

    private int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Visitor WHERE status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("countByStatus error: " + e.getMessage());
        }
        return 0;
    }

    private int countQuery(String sql) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("countQuery error: " + e.getMessage());
        }
        return 0;
    }

    // ── Map ResultSet to Visitor ───────────────────────────────────
    private Visitor mapResultSet(ResultSet rs) throws SQLException {
        Visitor v = new Visitor();
        v.setVisitorId(rs.getInt("visitor_id"));
        v.setVisitorName(rs.getString("visitor_name"));
        v.setCompany(rs.getString("company"));
        v.setPurpose(rs.getString("purpose"));
        v.setHostEmployee(rs.getString("host_employee"));
        v.setStatus(rs.getString("status"));

        Timestamp timeOut = rs.getTimestamp("time_out");
        if (timeOut != null) v.setTimeOut(timeOut.toLocalDateTime());

        Timestamp timeIn = rs.getTimestamp("time_in");
        if (timeIn != null) v.setTimeIn(timeIn.toLocalDateTime());

        return v;
    }
}
