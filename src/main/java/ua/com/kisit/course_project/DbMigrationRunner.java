package ua.com.kisit.course_project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbMigrationRunner {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/car_rental_db";
        String user = "root";
        String password = "VladBro2020_";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Starting migration for car_rental_db...");
            
            try {
                System.out.println("Adding column 'latitude'...");
                stmt.execute("ALTER TABLE cars ADD COLUMN latitude DECIMAL(10, 8)");
                System.out.println("Column 'latitude' added.");
            } catch (Exception e) {
                System.out.println("Column 'latitude' might already exist: " + e.getMessage());
            }

            try {
                System.out.println("Adding column 'longitude'...");
                stmt.execute("ALTER TABLE cars ADD COLUMN longitude DECIMAL(11, 8)");
                System.out.println("Column 'longitude' added.");
            } catch (Exception e) {
                System.out.println("Column 'longitude' might already exist: " + e.getMessage());
            }
            
            System.out.println("Migration finished successfully!");
            
        } catch (Exception e) {
            System.err.println("Critical error during migration: " + e.getMessage());
        }
    }
}
