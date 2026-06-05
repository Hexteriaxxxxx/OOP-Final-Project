package main.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL      = "jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require&socketTimeout=30&connectTimeout=10";
    private static final String USER     = "postgres.axsrbppmxnekniiyfmuh";
    private static final String PASSWORD = "PupPassSlip2026!";

    private static Connection sharedConnection = null;

    public static synchronized Connection getConnection() {
        try {
            // Reuse existing connection if still valid
            if (sharedConnection != null && !sharedConnection.isClosed() && sharedConnection.isValid(3)) {
                return sharedConnection;
            }
            // Create new connection
            Class.forName("org.postgresql.Driver");
            sharedConnection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Connected to Supabase!");
            return sharedConnection;
        } catch (ClassNotFoundException e) {
            System.out.println("[DB ERROR] Driver not found: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.out.println("[DB ERROR] " + e.getMessage());
            return null;
        }
    }
}
