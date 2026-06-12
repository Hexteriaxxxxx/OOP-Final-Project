package main.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL      = "jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require&socketTimeout=30&connectTimeout=10";
    private static final String USER     = "postgres.axsrbppmxnekniiyfmuh";
    private static final String PASSWORD = "PupPassSlip2026!";

    static {
        try { Class.forName("org.postgresql.Driver"); }
        catch (ClassNotFoundException e) { System.out.println("[DB ERROR] Driver not found: " + e.getMessage()); }
    }

    /**
     * Returns a brand-new connection every call.
     * DAOs use try-with-resources to close their own connection after each
     * query, so connections must NOT be shared/cached across threads —
     * sharing one Connection across concurrent DAO calls causes
     * "I/O error occurred while sending to the backend" when one thread
     * closes the connection while another is still using it.
     */
    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Connected to Supabase!");
            return conn;
        } catch (SQLException e) {
            System.out.println("[DB ERROR] " + e.getMessage());
            return null;
        }
    }
}
