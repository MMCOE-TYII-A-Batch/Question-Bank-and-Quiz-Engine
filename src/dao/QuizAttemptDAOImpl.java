package dao;

import model.QuizAttempt;
import model.StudentAnswer;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class QuizAttemptDAOImpl implements QuizAttemptDAO {

    @Override
    public int startQuizAttempt(int quizId, int studentId) {
        int generatedId = -1;
        String sql = """
                INSERT INTO quiz_attempt
                (quiz_id, student_id, start_time, status, create_time, last_update)
                VALUES (?, ?, CURRENT_TIMESTAMP, 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                RETURNING attempt_id
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, quizId);
            ps.setInt(2, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                generatedId = rs.getInt("attempt_id");
            }
            rs.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return generatedId;
    }

    @Override
    public QuizAttempt getAttemptById(int attemptId) {
        String sql = "SELECT * FROM quiz_attempt WHERE attempt_id = ?";
        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, attemptId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                QuizAttempt attempt = new QuizAttempt();
                attempt.setAttemptId(rs.getInt("attempt_id"));
                attempt.setQuizId(rs.getInt("quiz_id"));
                attempt.setStudentId(rs.getInt("student_id"));
                attempt.setStartTime(rs.getTimestamp("start_time"));
                attempt.setSubmitTime(rs.getTimestamp("submit_time"));
                attempt.setScoreObtained(rs.getDouble("score_obtained"));
                attempt.setStatus(rs.getString("status"));
                return attempt;
            }
            rs.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public boolean saveStudentAnswer(StudentAnswer answer) {
        String sql = """
                INSERT INTO student_answer
                (attempt_id, question_id, selected_option_id, text_response, marks_awarded, is_auto_graded, create_time, last_update)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON CONFLICT (attempt_id, question_id) DO UPDATE
                SET selected_option_id = EXCLUDED.selected_option_id,
                    marks_awarded = EXCLUDED.marks_awarded,
                    last_update = CURRENT_TIMESTAMP
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, answer.getAttemptId());
            ps.setInt(2, answer.getQuestionId());
            if (answer.getSelectedOptionId() != null) {
                ps.setInt(3, answer.getSelectedOptionId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setString(4, answer.getTextResponse());
            ps.setDouble(5, answer.getMarksAwarded());
            ps.setBoolean(6, answer.isAutoGraded());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean completeQuizAttempt(int attemptId, double scoreObtained) {
        String sql = """
                UPDATE quiz_attempt
                SET status = 'COMPLETED',
                    submit_time = CURRENT_TIMESTAMP,
                    score_obtained = ?,
                    last_update = CURRENT_TIMESTAMP
                WHERE attempt_id = ?
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setDouble(1, scoreObtained);
            ps.setInt(2, attemptId);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<QuizAttempt> getAttemptsByQuizId(int quizId) {
        List<QuizAttempt> attempts = new ArrayList<>();
        String sql = "SELECT * FROM quiz_attempt WHERE quiz_id = ? ORDER BY score_obtained DESC";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, quizId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                QuizAttempt attempt = new QuizAttempt();
                attempt.setAttemptId(rs.getInt("attempt_id"));
                attempt.setQuizId(rs.getInt("quiz_id"));
                attempt.setStudentId(rs.getInt("student_id"));
                attempt.setStartTime(rs.getTimestamp("start_time"));
                attempt.setSubmitTime(rs.getTimestamp("submit_time"));
                attempt.setScoreObtained(rs.getDouble("score_obtained"));
                attempt.setStatus(rs.getString("status"));
                attempts.add(attempt);
            }
            rs.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return attempts;
    }

    @Override
    public List<QuizAttempt> getStudentAttempts(int studentId) {
        List<QuizAttempt> attempts = new ArrayList<>();
        String sql = "SELECT * FROM quiz_attempt WHERE student_id = ? ORDER BY attempt_id DESC";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                QuizAttempt attempt = new QuizAttempt();
                attempt.setAttemptId(rs.getInt("attempt_id"));
                attempt.setQuizId(rs.getInt("quiz_id"));
                attempt.setStudentId(rs.getInt("student_id"));
                attempt.setStartTime(rs.getTimestamp("start_time"));
                attempt.setSubmitTime(rs.getTimestamp("submit_time"));
                attempt.setScoreObtained(rs.getDouble("score_obtained"));
                attempt.setStatus(rs.getString("status"));
                attempts.add(attempt);
            }
            rs.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return attempts;
    }
}
