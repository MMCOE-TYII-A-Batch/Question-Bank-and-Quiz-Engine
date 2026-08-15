package com.quiz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {

    private static final String URL = "jdbc:postgresql://localhost:5432/EDI";
    private static final String USER = "postgres";
    private static final String PASS = "postgres";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found!");
        }
    }

    public static Connection connect() throws SQLException {
        String url = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : URL;
        String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : USER;
        String pass = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : PASS;
        return DriverManager.getConnection(url, user, pass);
    }
}
