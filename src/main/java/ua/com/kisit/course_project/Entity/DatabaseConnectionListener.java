package ua.com.kisit.course_project.Entity;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;


@Component
public class DatabaseConnectionListener {

    private final DataSource dataSource;

    public DatabaseConnectionListener(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("✅ DATABASE CONNECTED SUCCESSFULLY");
            System.out.println("🔗 URL: " + connection.getMetaData().getURL());
            // Database migration for new column rnokpp and making old columns nullable
            try (java.sql.Statement stmt = connection.createStatement()) {
                stmt.execute("ALTER TABLE clients ADD COLUMN rnokpp VARCHAR(10)");
                System.out.println("✅ ADDED rnokpp COLUMN TO clients TABLE");
            } catch (Exception ex) {
                System.out.println("ℹ️ Column rnokpp check: already exists or could not be added.");
            }
            
            try (java.sql.Statement stmt = connection.createStatement()) {
                stmt.execute("ALTER TABLE clients MODIFY COLUMN passport_series VARCHAR(255) NULL");
                stmt.execute("ALTER TABLE clients MODIFY COLUMN passport_number VARCHAR(255) NULL");
                stmt.execute("ALTER TABLE clients MODIFY COLUMN passport_issued_by VARCHAR(255) NULL");
                stmt.execute("ALTER TABLE clients MODIFY COLUMN passport_issue_date DATE NULL");
                stmt.execute("ALTER TABLE clients MODIFY COLUMN address VARCHAR(255) NULL");
                stmt.execute("ALTER TABLE clients MODIFY COLUMN date_of_birth DATE NULL");
            } catch (Exception ex) {
                System.out.println("ℹ️ Make columns nullable check: already nullable or could not be modified.");
            }
            
        } catch (Exception e) {
            throw new IllegalStateException("❌ DATABASE CONNECTION FAILED", e);
        }
    }
}
