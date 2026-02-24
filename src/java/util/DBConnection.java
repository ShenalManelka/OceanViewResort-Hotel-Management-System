package util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static DBConnection instance;

    private static final String URL = "jdbc:mysql://localhost:3306/oceanvieweresortdb";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // change if you have password

    // Private constructor
    private DBConnection() {
    }

    // Singleton instance
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    // Get connection method
    public Connection getConnection() throws Exception {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new Exception(
                    "MySQL JDBC Driver not found in classpath. Please add the connector JAR to your project libraries.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}