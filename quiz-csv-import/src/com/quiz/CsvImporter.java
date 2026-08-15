package com.quiz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class CsvImporter {

    private static String btlColName = null;

    public Question.ImportStats importCsv(File file) {
        try (InputStream is = new FileInputStream(file)) {
            return importCsv(is, 1);
        } catch (Exception e) {
            Question.ImportStats stats = new Question.ImportStats();
            stats.addError(0, "Could not open file: " + e.getMessage());
            return stats;
        }
    }

    public Question.ImportStats importCsv(InputStream is, int defaultAuthorId) {
        Question.ImportStats stats = new Question.ImportStats();
        List<String[]> records;

        try {
            records = parseCsvLines(is);
        } catch (Exception e) {
            stats.addError(0, "Error reading CSV: " + e.getMessage());
            return stats;
        }

        if (records == null || records.isEmpty()) {
            stats.addError(0, "CSV file is empty.");
            return stats;
        }

        Map<String, Integer> headers = mapHeaders(records.get(0));
        if (!headers.containsKey("question")) {
            stats.addError(1, "CSV must contain a 'question' header.");
            return stats;
        }

        try (Connection conn = Db.connect()) {
            for (int i = 1; i < records.size(); i++) {
                int lineNum = i + 1;
                String[] row = records.get(i);

                if (row.length == 0 || (row.length == 1 && row[0].trim().isEmpty())) {
                    continue;
                }

                stats.total++;

                try {
                    Question q = parseRow(row, headers, lineNum);

                    conn.setAutoCommit(false);
                    try {
                        int subjectId = getSubject(conn, q.subject, stats);
                        int unitId = getUnit(conn, subjectId, q.unit, stats);
                        int topicId = getTopic(conn, unitId, q.topic, stats);
                        int authorId = getAuthor(conn, q.author, defaultAuthorId);

                        saveQuestion(conn, q, topicId, authorId);

                        conn.commit();
                        stats.success++;
                    } catch (SQLException dbErr) {
                        conn.rollback();
                        stats.addError(lineNum, "DB Error: " + dbErr.getMessage());
                    } finally {
                        conn.setAutoCommit(true);
                    }

                } catch (IllegalArgumentException valErr) {
                    stats.addError(lineNum, valErr.getMessage());
                } catch (Exception ex) {
                    stats.addError(lineNum, "Error: " + ex.getMessage());
                }
            }
        } catch (SQLException e) {
            stats.addError(0, "Database connection failed: " + e.getMessage());
        }

        return stats;
    }

    private Question parseRow(String[] row, Map<String, Integer> headers, int lineNum) {
        Question q = new Question();
        q.lineNum = lineNum;

        String qText = getVal(row, headers, "question", "q_text");
        if (qText == null || qText.trim().isEmpty()) {
            throw new IllegalArgumentException("Question text cannot be blank.");
        }
        q.text = qText.replace("\u200B", "").trim();

        q.subject = getValOrDefault(row, headers, "General", "subject_name", "subject");
        q.unit = getValOrDefault(row, headers, "Unit 1", "unit", "unit_name");
        q.topic = getValOrDefault(row, headers, "General Topic", "topic_name", "topic");
        q.author = getVal(row, headers, "author", "teacher");

        String marksVal = getVal(row, headers, "marks", "default_marks");
        if (marksVal != null && !marksVal.trim().isEmpty()) {
            try {
                double m = Double.parseDouble(marksVal.trim());
                if (m <= 0) throw new IllegalArgumentException("Marks must be positive.");
                q.marks = m;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid marks value: " + marksVal);
            }
        }

        String btlVal = getVal(row, headers, "blt", "btl", "btl_level");
        if (btlVal != null && !btlVal.trim().isEmpty()) {
            q.btl = btlVal.trim();
        }

        String optA = getVal(row, headers, "option_a", "optiona", "option_1");
        String optB = getVal(row, headers, "option_b", "optionb", "option_2");
        String optC = getVal(row, headers, "option_c", "optionc", "option_3");
        String optD = getVal(row, headers, "option_d", "optiond", "option_4");
        String correct = getVal(row, headers, "correct_option", "correct_answer", "answer");

        String[] optTexts = { optA, optB, optC, optD };
        String[] keys = { "A", "B", "C", "D" };

        boolean matchedCorrect = false;
        int optCount = 0;

        for (int i = 0; i < optTexts.length; i++) {
            String val = optTexts[i];
            if (val != null) val = val.replace("\u200B", "").trim();

            if ((val == null || val.isEmpty()) && isMatch(keys[i], i + 1, "", correct)) {
                val = "None of the mentioned";
            }

            if (val != null && !val.isEmpty()) {
                optCount++;
                boolean isRight = isMatch(keys[i], i + 1, val, correct);
                if (isRight) matchedCorrect = true;
                q.options.add(new Question.Option(val, isRight));
            }
        }

        if (optCount < 2) {
            throw new IllegalArgumentException("Question requires at least 2 options.");
        }
        if (!matchedCorrect) {
            throw new IllegalArgumentException("No option matched correct_option: '" + correct + "'.");
        }

        return q;
    }

    private boolean isMatch(String keyLetter, int idx, String text, String correct) {
        if (correct == null || correct.trim().isEmpty()) return false;
        String clean = correct.trim().toUpperCase();
        if (clean.contains(keyLetter)) return true;
        if (clean.contains(String.valueOf(idx))) return true;
        return !text.isEmpty() && text.equalsIgnoreCase(correct.trim());
    }

    private String getBtlColumnName(Connection conn) {
        if (btlColName != null) return btlColName;
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, "question", null)) {
            while (rs.next()) {
                String col = rs.getString("COLUMN_NAME");
                if ("btl_level".equalsIgnoreCase(col) || "btl".equalsIgnoreCase(col)) {
                    btlColName = col;
                    return btlColName;
                }
            }
        } catch (Exception ignored) {}
        btlColName = "btl_level";
        return btlColName;
    }

    private void saveQuestion(Connection conn, Question q, int topicId, int authorId) throws SQLException {
        String colName = getBtlColumnName(conn);
        String sqlQ = "INSERT INTO question (question, topic_id, author_id, mod_id, question_type, " + colName + ", default_marks, status, create_time, last_update) " +
                "VALUES (?, ?, ?, null, ?, ?, ?, ?, ?, ?)";

        int qId;
        Timestamp now = Timestamp.from(Instant.now());

        try (PreparedStatement ps = conn.prepareStatement(sqlQ, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, q.text);
            ps.setInt(2, topicId);
            ps.setInt(3, authorId);
            ps.setString(4, q.type);
            ps.setString(5, q.btl);
            ps.setDouble(6, q.marks);
            ps.setString(7, q.status);
            ps.setTimestamp(8, now);
            ps.setTimestamp(9, now);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    qId = keys.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve question_id.");
                }
            }
        }

        String sqlOpt = "INSERT INTO question_option (question_id, option_text, is_correct, create_time, last_update) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlOpt)) {
            for (Question.Option opt : q.options) {
                ps.setInt(1, qId);
                ps.setString(2, opt.text);
                ps.setBoolean(3, opt.isCorrect);
                ps.setTimestamp(4, now);
                ps.setTimestamp(5, now);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private int getSubject(Connection conn, String name, Question.ImportStats stats) throws SQLException {
        String sName = (name != null && !name.trim().isEmpty()) ? name.trim() : "General";
        String select = "SELECT subject_id FROM subject WHERE LOWER(subject_name) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, sName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("subject_id");
            }
        }

        String insert = "INSERT INTO subject (subject_name, create_time, last_update) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            Timestamp now = Timestamp.from(Instant.now());
            ps.setString(1, sName);
            ps.setTimestamp(2, now);
            ps.setTimestamp(3, now);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    stats.subjectsCreated++;
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Cannot create subject: " + sName);
    }

    private int getUnit(Connection conn, int subjectId, String name, Question.ImportStats stats) throws SQLException {
        String uName = (name != null && !name.trim().isEmpty()) ? name.trim() : "Unit 1";
        if (uName.matches("^\\d+$")) uName = "Unit " + uName;

        String select = "SELECT unit_id FROM unit WHERE subject_id = ? AND LOWER(unit_name) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setInt(1, subjectId);
            ps.setString(2, uName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("unit_id");
            }
        }

        String insert = "INSERT INTO unit (subject_id, unit_name, create_time, last_update) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            Timestamp now = Timestamp.from(Instant.now());
            ps.setInt(1, subjectId);
            ps.setString(2, uName);
            ps.setTimestamp(3, now);
            ps.setTimestamp(4, now);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    stats.unitsCreated++;
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Cannot create unit: " + uName);
    }

    private int getTopic(Connection conn, int unitId, String name, Question.ImportStats stats) throws SQLException {
        String tName = (name != null && !name.trim().isEmpty()) ? name.trim() : "General Topic";
        String select = "SELECT topic_id FROM topic WHERE unit_id = ? AND LOWER(topic_name) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setInt(1, unitId);
            ps.setString(2, tName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("topic_id");
            }
        }

        String insert = "INSERT INTO topic (unit_id, topic_name, create_time, last_update) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            Timestamp now = Timestamp.from(Instant.now());
            ps.setInt(1, unitId);
            ps.setString(2, tName);
            ps.setTimestamp(3, now);
            ps.setTimestamp(4, now);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    stats.topicsCreated++;
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Cannot create topic: " + tName);
    }

    private int getAuthor(Connection conn, String authorName, int defaultId) throws SQLException {
        String name = (authorName != null && !authorName.trim().isEmpty()) ? authorName.trim() : "Teacher System";

        String select = "SELECT user_id FROM \"user\" WHERE LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, name);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id");
            }
        }

        if ("Teacher System".equalsIgnoreCase(name)) {
            String checkDefault = "SELECT user_id FROM \"user\" WHERE LOWER(email) = 'teacher@quiz.system'";
            try (PreparedStatement ps = conn.prepareStatement(checkDefault); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id");
            }
        }

        String email = "Teacher System".equalsIgnoreCase(name) ? "teacher@quiz.system" :
                name.toLowerCase().replaceAll("[^a-z0-9]", ".") + "@quiz.system";

        String checkEmail = "SELECT user_id FROM \"user\" WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(checkEmail)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id");
            }
        }

        String insert = "INSERT INTO \"user\" (username, email, password_hash, status) VALUES (?, ?, 'hash', 'ACTIVE')";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }

        return defaultId;
    }

    private List<String[]> parseCsvLines(InputStream is) throws Exception {
        List<String[]> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            StringBuilder sb = new StringBuilder();
            List<String> fields = new ArrayList<>();
            boolean inQuotes = false;

            while ((line = reader.readLine()) != null) {
                if (lines.isEmpty() && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }

                char[] chars = line.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    char c = chars[i];
                    if (c == '"') {
                        if (inQuotes && i + 1 < chars.length && chars[i + 1] == '"') {
                            sb.append('"');
                            i++;
                        } else {
                            inQuotes = !inQuotes;
                        }
                    } else if (c == ',' && !inQuotes) {
                        fields.add(sb.toString().trim());
                        sb.setLength(0);
                    } else {
                        sb.append(c);
                    }
                }

                if (inQuotes) {
                    sb.append("\n");
                } else {
                    fields.add(sb.toString().trim());
                    sb.setLength(0);
                    if (!fields.isEmpty()) {
                        lines.add(fields.toArray(new String[0]));
                    }
                    fields.clear();
                }
            }
        }
        return lines;
    }

    private Map<String, Integer> mapHeaders(String[] row) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < row.length; i++) {
            String clean = row[i].trim().toLowerCase().replaceAll("[^a-z0-9_]", "");
            map.put(clean, i);
        }
        return map;
    }

    private String getVal(String[] row, Map<String, Integer> map, String... keys) {
        for (String k : keys) {
            Integer idx = map.get(k);
            if (idx != null && idx >= 0 && idx < row.length) {
                return row[idx];
            }
        }
        return null;
    }

    private String getValOrDefault(String[] row, Map<String, Integer> map, String defVal, String... keys) {
        String val = getVal(row, map, keys);
        return (val != null && !val.trim().isEmpty()) ? val.trim() : defVal;
    }
}
