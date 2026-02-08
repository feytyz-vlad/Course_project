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
        } catch (Exception e) {
            throw new IllegalStateException("❌ DATABASE CONNECTION FAILED", e);
        }
    }
}
