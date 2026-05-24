package main.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static String HOST;
    private static String PORT;
    private static String DATABASE;
    private static String USERNAME;
    private static String PASSWORD;

    static {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(".env"));
            HOST = props.getProperty("DB_HOST");
            PORT = props.getProperty("DB_PORT");
            DATABASE = props.getProperty("DB_NAME");
            USERNAME = props.getProperty("DB_USER");
            PASSWORD = props.getProperty("DB_PASSWORD");
        } catch (IOException e) {
            System.out.println("Error loading .env file: " + e.getMessage());
        }
    }

    private static String URL() {
        return "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
                + "?useSSL=false&serverTimezone=Asia/Manila&allowPublicKeyRetrieval=true";
    }

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL(), USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
        return null;
    }

    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}