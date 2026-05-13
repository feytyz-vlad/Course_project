package ua.com.kisit.course_project.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class MigrationConfig {

    @Bean
    public CommandLineRunner runMigration(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                System.out.println("Checking for geolocation columns...");
                jdbcTemplate.execute("ALTER TABLE cars ADD COLUMN IF NOT EXISTS latitude DECIMAL(10, 8)");
                jdbcTemplate.execute("ALTER TABLE cars ADD COLUMN IF NOT EXISTS longitude DECIMAL(11, 8)");
                System.out.println("Geolocation columns verified/added.");
            } catch (Exception e) {
                // MySQL doesn't support ADD COLUMN IF NOT EXISTS in older versions
                // We handle it by trying and catching
                try {
                    jdbcTemplate.execute("ALTER TABLE cars ADD COLUMN latitude DECIMAL(10, 8)");
                } catch (Exception ignored) {}
                try {
                    jdbcTemplate.execute("ALTER TABLE cars ADD COLUMN longitude DECIMAL(11, 8)");
                } catch (Exception ignored) {}
                System.out.println("Migration attempt finished.");
            }
        };
    }
}
