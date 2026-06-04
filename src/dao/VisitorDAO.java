package dao;

import models.Visitor;
import main.utils.DBConnection;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VisitorDAO {

    private static String getServerUrl() {
        File envFile = new File(".env");
        if (!envFile.exists()) return "http://localhost:5055";
        try (BufferedReader br = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("VISITOR_SERVER_URL=")) return line.split("=", 2)[1].trim();
            }
        } catch (IOException e) { System.out.println("Could not read .env: " + e.getMessage()); }
        return "http://localhost:5055";
    }

    public boolean addVisitor(Visitor visitor) {
        String sql = "INSERT INTO \"Visitor\" (visitor_name, company, purpose, time_out, host_employee, email, contact, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, visitor.getVisitorName());
            stmt.setString(2, visitor.getCompany());
            stmt.setString(3, visitor.getPurpose());
            stmt.setTimestamp(4, Timestamp.valueOf(visitor.getTimeOut()));
            stmt.setString(5, visitor.getHostEmployee());
            stmt.setString(6, visitor.getEmail()   != null ? visitor.getEmail()   : "");
            stmt.setString(7, visitor.getContact() != null ? visitor.getContact() : "");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("addVisitor error: " + e.getMessage()); return false; }
    }

    public int addVisitorGetId(Visitor visitor) {
        String sql = "INSERT INTO \"Visitor\" (visitor_name, company, purpose, time_out, host_employee, email, contact, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending') RETURNING visitor_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, visitor.getVisitorName());
            stmt.setString(2, visitor.getCompany());
            stmt.setString(3, visitor.getPurpose());
            stmt.setTimestamp(4, Timestamp.valueOf(visitor.getTimeOut()));
            stmt.setString(5, visitor.getHostEmployee());
            stmt.setString(6, visitor.getEmail()   != null ? visitor.getEmail()   : "");
            stmt.setString(7, visitor.getContact() != null ? visitor.getContact() : "");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.out.println("addVisitorGetId error: " + e.getMessage()); }
        return -1;
    }

    public List<Visitor> getAllVisitors() {
        List<Visitor> list = new ArrayList<>();
        String sql = "SELECT * FROM \"Visitor\" ORDER BY visitor_id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) { System.out.println("getAllVisitors error: " + e.getMessage()); }
        return list;
    }

    public Visitor getById(int id) {
        String sql = "SELECT * FROM \"Visitor\" WHERE visitor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) { System.out.println("getById error: " + e.getMessage()); }
        return null;
    }

    public boolean updateStatus(int visitorId, String status) {
        String sql = "UPDATE \"Visitor\" SET status = ? WHERE visitor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, visitorId);
            boolean ok = stmt.executeUpdate() > 0;
            if (ok && "Approved".equalsIgnoreCase(status)) {
                Visitor v = getById(visitorId);
                if (v != null && v.getEmail() != null && !v.getEmail().isBlank())
                    sendApprovalEmailViaPython(v);
            }
            return ok;
        } catch (SQLException e) { System.out.println("updateStatus error: " + e.getMessage()); return false; }
    }

    private void sendApprovalEmailViaPython(Visitor v) {
        new Thread(() -> {
            try {
                String serverUrl = getServerUrl() + "/send-approval-email";
                URL url = new URL(serverUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                String json = String.format(
                    "{\"visitor_id\":%d,\"visitor_name\":\"%s\",\"email\":\"%s\"," +
                    "\"company\":\"%s\",\"purpose\":\"%s\",\"visit_date\":\"%s\"," +
                    "\"host_employee\":\"%s\",\"request_id\":\"%s\"}",
                    v.getVisitorId(), escape(v.getVisitorName()), escape(v.getEmail()),
                    escape(v.getCompany()), escape(v.getPurpose()), escape(v.getFormattedTimeOut()),
                    escape(v.getHostEmployee()), escape(v.getRequestId()));
                try (OutputStream os = conn.getOutputStream()) { os.write(json.getBytes()); }
                int code = conn.getResponseCode();
                System.out.println(code == 200 ? "[OK] Approval email sent to: " + v.getEmail() : "[WARN] Email server returned: " + code);
                conn.disconnect();
            } catch (Exception e) { System.out.println("[WARN] Could not send approval email: " + e.getMessage()); }
        }).start();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n");
    }

    public int countPending()  { return countByStatus("Pending");  }
    public int countApproved() { return countByStatus("Approved"); }
    public int countRejected() { return countByStatus("Rejected"); }
    public int countActiveToday() {
        return countQuery("SELECT COUNT(*) FROM \"Visitor\" WHERE status='Approved' AND DATE(time_out)=CURRENT_DATE");
    }

    private int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM \"Visitor\" WHERE status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.out.println("countByStatus error: " + e.getMessage()); }
        return 0;
    }

    private int countQuery(String sql) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.out.println("countQuery error: " + e.getMessage()); }
        return 0;
    }

    private Visitor mapResultSet(ResultSet rs) throws SQLException {
        Visitor v = new Visitor();
        v.setVisitorId   (rs.getInt   ("visitor_id"));
        v.setVisitorName (rs.getString("visitor_name"));
        v.setCompany     (rs.getString("company"));
        v.setPurpose     (rs.getString("purpose"));
        v.setHostEmployee(rs.getString("host_employee"));
        v.setStatus      (rs.getString("status"));
        try { v.setEmail  (rs.getString("email"));   } catch (SQLException ignored) {}
        try { v.setContact(rs.getString("contact")); } catch (SQLException ignored) {}
        Timestamp to = rs.getTimestamp("time_out");
        if (to != null) v.setTimeOut(to.toLocalDateTime());
        Timestamp ti = rs.getTimestamp("time_in");
        if (ti != null) v.setTimeIn(ti.toLocalDateTime());
        return v;
    }
}
