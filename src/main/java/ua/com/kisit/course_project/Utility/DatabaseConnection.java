package ua.com.kisit.course_project.Utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections
 * Handles connection creation and configuration
 */
public class DatabaseConnection {

    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/car_rental_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Change this to your MySQL password

    // Connection parameters
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final String CONNECTION_PARAMS =
            "?useSSL=false" +
                    "&serverTimezone=UTC" +
                    "&allowPublicKeyRetrieval=true" +
                    "&useUnicode=true" +
                    "&characterEncoding=UTF-8";

    private static Connection connection;

    /**
     * Private constructor to prevent instantiation
     */
    private DatabaseConnection() {
    }

    /**
     * Get database connection (singleton pattern)
     * @return database connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load MySQL JDBC Driver
                Class.forName(DRIVER_CLASS);

                // Create connection
                connection = DriverManager.getConnection(
                        DB_URL + CONNECTION_PARAMS,
                        DB_USER,
                        DB_PASSWORD
                );

                System.out.println("Database connection established successfully");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found", e);
            }
        }

        return connection;
    }

    /**
     * Create new database connection
     * @return new database connection
     * @throws SQLException if connection fails
     */
    public static Connection createNewConnection() throws SQLException {
        try {
            Class.forName(DRIVER_CLASS);
            return DriverManager.getConnection(
                    DB_URL + CONNECTION_PARAMS,
                    DB_USER,
                    DB_PASSWORD
            );
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    /**
     * Close database connection
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    /**
     * Test database connection
     * @return true if connection is successful
     */
    public static boolean testConnection() {
        try {
            Connection testConnection = getConnection();
            return testConnection != null && !testConnection.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get database URL
     * @return database URL
     */
    public static String getDatabaseUrl() {
        return DB_URL;
    }

    /**
     * Get database user
     * @return database user
     */
    public static String getDatabaseUser() {
        return DB_USER;
    }
}