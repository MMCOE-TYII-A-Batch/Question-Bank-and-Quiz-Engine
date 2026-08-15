package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static String getDbUrl() {
        return System.getenv().getOrDefault(
                "DB_URL",
                "jdbc:postgresql://localhost:5432/question_bank"
        );
    }

    private static String getDbUser() {
        return System.getenv().getOrDefault("DB_USER", "postgres");
    }

    private static String getDbPassword() {
        return System.getenv().getOrDefault("DB_PASSWORD", "postgres");
    }

    public static Connection getConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(
                    getDbUrl(),
                    getDbUser(),
                    getDbPassword()
            );
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("PostgreSQL connection failed", e);
        }
    }

    public static void initSchema() {
        String[] migrations = {
            "ALTER TABLE subject ADD COLUMN IF NOT EXISTS version VARCHAR(50) DEFAULT '1.0'",
            "ALTER TABLE subject ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE",
            "ALTER TABLE unit ADD COLUMN IF NOT EXISTS question_quota INT DEFAULT 0",
            "ALTER TABLE topic ADD COLUMN IF NOT EXISTS question_quota INT DEFAULT 0"
        };

        try (Connection con = getConnection();
             java.sql.Statement stmt = con.createStatement()) {

            int applied = 0;

            for (String sql : migrations) {
                try {
                    stmt.executeUpdate(sql);
                    applied++;
                } catch (Exception e) {
                    System.err.println(
                        "[DBConnection] Migration skipped: " + e.getMessage()
                    );
                }
            }

            System.out.println(
                "[DBConnection] Syllabus schema migrations applied: "
                + applied + "/" + migrations.length
            );

        } catch (Exception e) {
            System.err.println(
                "[DBConnection] Schema init notice: " + e.getMessage()
            );
        }
    }
}