package ua.com.kisit.course_project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbExplorer {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/car_rental_db";
        String user = "root";
        String password = "VladBro2020_";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Checking data in car_rental_db.cars:");
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total, COUNT(latitude) as with_loc FROM cars");
            if (rs.next()) {
                System.out.println("Total cars: " + rs.getInt("total"));
                System.out.println("Cars with location: " + rs.getInt("with_loc"));
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
