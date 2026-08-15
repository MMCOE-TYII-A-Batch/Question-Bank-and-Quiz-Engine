package csvimport;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * CSVQuestionImporter
 * 
 * This script imports descriptive questions from a CSV file into the database.
 * It uses OpenCSV for parsing the CSV file and JDBC for database operations.
 */
public class CSVQuestionImporter {

    // Database connection details - update these accordingly
    private static final String DB_URL = "jdbc:postgresql://localhost:5433/EDI";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        String csvFilePath = "questions.csv"; // Path to your CSV file
        importQuestions(csvFilePath, DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Reads a CSV file and imports descriptive questions into the database.
     * 
     * @param csvFilePath Path to the CSV file
     * @param dbUrl       Database URL
     * @param dbUser      Database username
     * @param dbPassword  Database password
     */
    public static void importQuestions(String csvFilePath, String dbUrl, String dbUser, String dbPassword) {
        String insertSQL = "INSERT INTO question (question, topic_id, author_id, question_type, btl_level, default_marks, create_time, last_update) " +
                           "VALUES (?, ?, ?, 'DESCRIPTIVE', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        int totalRows = 0;
        int importedRows = 0;
        int failedRows = 0;
        int skippedRows = 0;
        java.util.List<String> skippedReasons = new java.util.ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             FileReader fileReader = new FileReader(csvFilePath);
             // Initialize CSV reader and skip the header row
             CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build()) {

            String[] nextRecord;
            
            // Loop through each row in the CSV
            while ((nextRecord = csvReader.readNext()) != null) {
                totalRows++;
                
                try {
                    // Expected CSV Columns: Question, Subject, Topic, Unit, BTL, Author, Marks
                    if (nextRecord.length < 7) {
                        throw new IllegalArgumentException("Invalid row format. Missing columns. Found " + nextRecord.length + " columns.");
                    }

                    String question = nextRecord[0].trim();
                    // String subject = nextRecord[1].trim(); // Subject is in CSV but not mapped in the question table requirements
                    String topic = nextRecord[2].trim();
                    String unit = nextRecord[3].trim();    // Unit is in CSV but not mapped in the question table requirements
                    String btlLevel = nextRecord[4].trim();
                    String author = nextRecord[5].trim();
                    String marksStr = nextRecord[6].trim();

                    // Validate mandatory fields (Author is now optional to allow nullable author_id)
                    if (question.isEmpty() || topic.isEmpty()) {
                        throw new IllegalArgumentException("Mandatory fields (Question, Topic) cannot be empty.");
                    }

                    // Parse marks (defaulting to 0 if not provided or empty)
                    double marks = 0.0;
                    if (!marksStr.isEmpty()) {
                        try {
                            marks = Double.parseDouble(marksStr);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid marks format: " + marksStr);
                        }
                    }

                    // Look up topic_id, create if not exists
                    Integer topicId = createTopicIfNotExists(conn, topic, unit);

                    // Look up author_id based on username/name mapping
                    // The Author column can contain multiple teachers separated by "/"
                    Integer authorId = null;
                    if (!author.isEmpty()) {
                        String[] teachers = author.split("/");
                        for (String teacher : teachers) {
                            String trimmedTeacher = teacher.trim();
                            if (!trimmedTeacher.isEmpty()) {
                                Integer currentAuthorId = getAuthorId(conn, trimmedTeacher);
                                if (currentAuthorId != null) {
                                    authorId = currentAuthorId; // Keeps updating, so it retains the last valid teacher
                                }
                            }
                        }
                    }
                    
                    if (authorId == null) {
                        String skipReason ="No valid teacher found for row. Authors: " + author;
                        System.out.println(skipReason);
                        skippedRows++;
                        skippedReasons.add("Row " + (totalRows + 1) + ": " + skipReason);
                        continue;
                    }

                    // Insert the record into the question table
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                        insertStmt.setString(1, question);
                        insertStmt.setInt(2, topicId);
                        insertStmt.setInt(3, authorId);
                        
                        // Set BTL level (assuming it might be a string like "L1" or integer, setString handles both cleanly)
                        if (btlLevel.isEmpty()) {
                            insertStmt.setNull(4, java.sql.Types.VARCHAR);
                        } else {
                            insertStmt.setString(4, btlLevel);
                        }
                        
                        insertStmt.setDouble(5, marks);

                        int affectedRows = insertStmt.executeUpdate();
                        if (affectedRows > 0) {
                            importedRows++;
                        } else {
                            throw new SQLException("Insert operation failed, no rows affected.");
                        }
                    }

                } catch (Exception e) {
                    failedRows++;
                    // Print error with the corresponding row number (adding 1 because we skipped the header)
                    System.err.println("Error importing row " + (totalRows + 1) + ": " + e.getMessage());
                }
            }

        } catch (SQLException | IOException | CsvValidationException e) {
            System.err.println("Critical error occurred during the import process: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Print final summary
            System.out.println("\n--- Import Summary ---");
            System.out.println("Total rows parsed: " + totalRows);
            System.out.println("Successfully imported: " + importedRows);
            System.out.println("Skipped rows: " + skippedRows);
            System.out.println("Failed rows: " + failedRows);
            
            if (!skippedReasons.isEmpty()) {
                System.out.println("\n--- Skipped Row Reasons ---");
                for (String reason : skippedReasons) {
                    System.out.println(reason);
                }
            }
        }
    }

    /**
     * Helper method to find topic_id using the topic_name.
     * 
     * @param conn      Active database connection
     * @param topicName Name of the topic
     * @return topic_id if found, null otherwise
     * @throws SQLException If a database access error occurs
     */
    private static Integer getUnitId(Connection conn, String unitName) throws SQLException {
        String query = "SELECT unit_id FROM unit WHERE unit_name = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, unitName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("unit_id");
                }
            }
        }

        return null;
    }
    private static Integer getTopicId(Connection conn, String topicName) throws SQLException {
        String query = "SELECT topic_id FROM topic WHERE topic_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, topicName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("topic_id");
                }
            }
        }
        return null;
    }

    /**
     * Helper method to find a topic by name. If it doesn't exist, it inserts it and returns the new topic_id.
     * 
     * @param conn      Active database connection
     * @param topicName Name of the topic
     * @return topic_id (either existing or newly generated)
     * @throws SQLException If a database access error occurs
     */
    private static Integer createTopicIfNotExists(Connection conn,String topicName,String unitName) throws SQLException {

        Integer topicId = getTopicId(conn, topicName);

        if (topicId != null) {
            return topicId;
        }

        Integer unitId = getUnitId(conn, unitName);

        if (unitId == null) {
            throw new SQLException("Unit not found: " + unitName);
        }

        String insertTopicSQL =
                "INSERT INTO topic (topic_name, unit_id) VALUES (?, ?)";

        try (PreparedStatement insertStmt =
                    conn.prepareStatement(insertTopicSQL,
                            java.sql.Statement.RETURN_GENERATED_KEYS)) {

            insertStmt.setString(1, topicName);
            insertStmt.setInt(2, unitId);

            int affectedRows = insertStmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }

            throw new SQLException("Creating topic failed.");
        }
    }

    /**
     * Helper method to find author_id using a more robust search.
     * It strips common titles and attempts a fuzzy match if an exact match fails.
     * 
     * @param conn       Active database connection
     * @param authorName The name of the author from CSV
     * @return user_id if found, null otherwise
     * @throws SQLException If a database access error occurs
     */
    private static Integer getAuthorId(Connection conn, String username) throws SQLException {
    String query = "SELECT user_id FROM \"user\" WHERE username = ?";

    try (PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, username.trim());

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        }
    }

    return null;
    }
}
