package utils;

import main.utils.DBConnection;
import java.sql.*;

public class HashExistingPasswords {
    public static void main(String[] args) {
        String selectSQL = "SELECT user_id, password FROM User";
        String updateSQL = "UPDATE User SET password = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL);
             PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String plainPassword = rs.getString("password");

                // I-skip kung hashed na (base64 encoded, mahaba)
                if (plainPassword.length() > 50) {
                    System.out.println("Skipping user_id " + userId + " - already hashed.");
                    continue;
                }

                String hashed = PasswordUtils.hashPassword(plainPassword);
                updateStmt.setString(1, hashed);
                updateStmt.setInt(2, userId);
                updateStmt.executeUpdate();
                System.out.println("✅ Hashed password for user_id: " + userId);
            }

            System.out.println("✅ Done! Lahat ng passwords ay na-hash na.");

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}