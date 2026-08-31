package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static String getDbUrl() {
        return System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/question_bank");
    }
    private static String getDbUser() {
        return System.getenv().getOrDefault("DB_USER", "postgres");
    }
    private static String getDbPassword() {
        return System.getenv().getOrDefault("DB_PASSWORD", "postgres");
    }
    public static Connection getConnection() {
        try {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                System.err.println("Warning: org.postgresql.Driver class not explicitly loaded via Class.forName.");
            }
            return DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPassword());
        } catch (SQLException e) {
            throw new RuntimeException("PostgreSQL Connection Error: " + e.getMessage() +
                    " [Target URL: " + getDbUrl() + ", User: " + getDbUser() + "]. " +
                    "You can override connection settings by passing environment variables: DB_URL, DB_USER, DB_PASSWORD.", e);
        }
    }

    public static void initSchema() {
        try (Connection con = getConnection();
             java.sql.Statement stmt = con.createStatement()) {
            stmt.executeUpdate("ALTER TABLE \"user\" ADD COLUMN IF NOT EXISTS subject VARCHAR(150)");
            stmt.executeUpdate("ALTER TABLE \"user\" ADD COLUMN IF NOT EXISTS force_password_change BOOLEAN DEFAULT TRUE");
            stmt.executeUpdate("ALTER TABLE question ADD COLUMN IF NOT EXISTS difficulty VARCHAR(20)");
            stmt.executeUpdate("ALTER TABLE question ADD COLUMN IF NOT EXISTS chapter VARCHAR(150)");
            stmt.executeUpdate("ALTER TABLE question ADD COLUMN IF NOT EXISTS explanation TEXT");
            stmt.executeUpdate("ALTER TABLE question ADD COLUMN IF NOT EXISTS reference_text TEXT");
            stmt.executeUpdate("ALTER TABLE question ADD COLUMN IF NOT EXISTS syllabus_version VARCHAR(30) DEFAULT 'v2026.1'");
            stmt.executeUpdate("ALTER TABLE question ADD COLUMN IF NOT EXISTS syllabus_section VARCHAR(200)");

            stmt.executeUpdate("ALTER TABLE question_bank ADD COLUMN IF NOT EXISTS assigned_moderator VARCHAR(150)");
            stmt.executeUpdate("ALTER TABLE question_bank ADD COLUMN IF NOT EXISTS syllabus_version VARCHAR(30) DEFAULT 'v2026.1'");
            stmt.executeUpdate("ALTER TABLE question_bank ADD COLUMN IF NOT EXISTS syllabus_section VARCHAR(200)");

            stmt.executeUpdate("ALTER TABLE quiz ADD COLUMN IF NOT EXISTS start_time TIMESTAMP");
            stmt.executeUpdate("ALTER TABLE quiz ADD COLUMN IF NOT EXISTS due_date DATE");

            // MODULE 3: Subject & Syllabus Tables
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS subject ("
                + "subject_id SERIAL PRIMARY KEY, "
                + "subject_code VARCHAR(50) UNIQUE NOT NULL, "
                + "subject_name VARCHAR(150) NOT NULL, "
                + "department VARCHAR(100), "
                + "syllabus_version VARCHAR(30) DEFAULT 'v2026.1', "
                + "description TEXT, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS syllabus_chapter ("
                + "chapter_id SERIAL PRIMARY KEY, "
                + "subject_code VARCHAR(50) REFERENCES subject(subject_code) ON DELETE CASCADE, "
                + "chapter_name VARCHAR(200) NOT NULL, "
                + "target_quota INT DEFAULT 10, "
                + "topics TEXT, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")");

            // Insert default subjects into DB if table is empty
            stmt.executeUpdate("INSERT INTO subject (subject_code, subject_name, department, syllabus_version, description) VALUES "
                + "('CS301', 'Data Science and Machine Learning', 'Computer Engineering', 'v2026.1', 'Core course covering Data Preprocessing, Regression, Classification, Clustering, and Neural Networks.'), "
                + "('CS302', 'Database Management Systems', 'Computer Engineering', 'v2026.1', 'Relational Database Concepts, SQL Queries, Normalization, and Transaction Processing.'), "
                + "('CS303', 'Data Structures & Algorithms', 'Computer Engineering', 'v2026.1', 'Fundamental Data Structures, Trees, Graphs, Sorting, and Algorithm Complexity.') "
                + "ON CONFLICT (subject_code) DO NOTHING");

            System.out.println("[DBConnection] PostgreSQL schema auto-migrations for Module 3 applied successfully.");
        } catch (Exception e) {
            System.err.println("[DBConnection] Schema init notice (DB may be offline or already up to date): " + e.getMessage());
        }
    }
}
