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
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPassword());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database at " + getDbUrl(), e);
        }
    }
}
