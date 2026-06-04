package main.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL      = "jdbc:postgresql://db.axsrbppmxnekniiyfmuh.supabase.co:5432/postgres";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "PupPassSlip2026!";

    public static Connection getConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("[DB ERROR] PostgreSQL driver not found: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.out.println("[DB ERROR] Connection failed: " + e.getMessage());
            return null;
        }
    }
}
