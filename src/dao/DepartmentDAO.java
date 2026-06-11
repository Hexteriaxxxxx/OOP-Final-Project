package dao;

import main.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public List<String> getAllDepartmentNames() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT dept_name FROM \"Department\" ORDER BY dept_name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) names.add(rs.getString("dept_name"));
        } catch (SQLException e) { System.out.println("DepartmentDAO error: " + e.getMessage()); }
        return names;
    }

    public boolean addDepartment(String name) {
        String sql = "INSERT INTO \"Department\" (dept_name) VALUES (?) ON CONFLICT (dept_name) DO NOTHING";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.trim());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) { System.out.println("addDepartment error: " + e.getMessage()); return false; }
    }

    public boolean deleteDepartment(int id) {
        String sql = "DELETE FROM \"Department\" WHERE dept_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("deleteDepartment error: " + e.getMessage()); return false; }
    }
}
