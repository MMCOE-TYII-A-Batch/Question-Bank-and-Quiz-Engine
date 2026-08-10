package dao;

import model.Question;
import model.Quiz;
import model.QuizCriteria;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class QuizDAOImpl implements QuizDAO {

    @Override
    public int createQuiz(Quiz quiz) {
        int generatedId = -1;
        String sql = """
                INSERT INTO quiz
                (creator_id, subject_id, is_randomized, duration_minutes, status, create_time, last_update)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                RETURNING quiz_id
                """;
        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, quiz.getCreatorId());
            ps.setInt(2, quiz.getSubjectId());
            ps.setBoolean(3, quiz.isRandomized());
            ps.setInt(4, quiz.getDurationMinutes());
            ps.setString(5, quiz.getStatus() != null ? quiz.getStatus() : "DRAFT");

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                generatedId = rs.getInt("quiz_id");
            }
            rs.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return generatedId;
    }

    @Override
    public Quiz getQuizById(int quizId) {
        String sql = "SELECT * FROM quiz WHERE quiz_id = ?";
        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, quizId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Quiz quiz = new Quiz();
                quiz.setQuizId(rs.getInt("quiz_id"));
                quiz.setCreatorId(rs.getInt("creator_id"));
                quiz.setSubjectId(rs.getInt("subject_id"));
                quiz.setRandomized(rs.getBoolean("is_randomized"));
                quiz.setDurationMinutes(rs.getInt("duration_minutes"));
                quiz.setStatus(rs.getString("status"));
                return quiz;
            }
            rs.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Question> fetchCandidateQuestions(QuizCriteria criteria) {
        List<Question> questions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT q.* FROM question q
                JOIN question_bank qb ON q.question_bank_id = qb.question_bank_id
                WHERE qb.subject_id = ?
                  AND UPPER(q.question_type) = 'MCQ'
                  AND qb.status IN ('LIVE', 'APPROVED', 'DRAFT')
                  AND q.status != 'DELETED'
                """);

        if (criteria.getTopicId() != null && criteria.getTopicId() > 0) {
            sql.append(" AND q.topic_id = ").append(criteria.getTopicId());
        }
        if (criteria.getBtlLevel() != null && !criteria.getBtlLevel().isBlank()) {
            sql.append(" AND UPPER(q.btl_level) = '").append(criteria.getBtlLevel().toUpperCase()).append("'");
        }

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())
        ) {
            ps.setInt(1, criteria.getSubjectId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Question q = new Question();
                q.setQuestionId(rs.getInt("question_id"));
                q.setQuestionBankId(rs.getInt("question_bank_id"));
                q.setQuestion(rs.getString("question"));
                q.setTopicId(rs.getInt("topic_id"));
                q.setAuthorId(rs.getInt("author_id"));
                q.setQuestionType(rs.getString("question_type"));
                q.setBtlLevel(rs.getString("btl_level"));
                q.setDefaultMarks(rs.getDouble("default_marks"));
                q.setStatus(rs.getString("status"));
                questions.add(q);
            }
            rs.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return questions;
    }

    @Override
    public boolean linkQuestionsToQuiz(int quizId, List<Question> selectedQuestions) {
        String sql = """
                INSERT INTO quiz_question
                (quiz_id, question_id, marks_assigned, question_order)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (quiz_id, question_id) DO UPDATE
                SET marks_assigned = EXCLUDED.marks_assigned,
                    question_order = EXCLUDED.question_order
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            int order = 1;
            for (Question q : selectedQuestions) {
                ps.setInt(1, quizId);
                ps.setInt(2, q.getQuestionId());
                ps.setDouble(3, q.getDefaultMarks());
                ps.setInt(4, order++);
                ps.addBatch();
            }
            ps.executeBatch();
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Question> getQuizQuestions(int quizId) {
        List<Question> questions = new ArrayList<>();
        String sql = """
                SELECT q.*, qq.marks_assigned, qq.question_order
                FROM quiz_question qq
                JOIN question q ON qq.question_id = q.question_id
                WHERE qq.quiz_id = ?
                ORDER BY qq.question_order ASC
                """;

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, quizId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Question q = new Question();
                q.setQuestionId(rs.getInt("question_id"));
                q.setQuestionBankId(rs.getInt("question_bank_id"));
                q.setQuestion(rs.getString("question"));
                q.setTopicId(rs.getInt("topic_id"));
                q.setAuthorId(rs.getInt("author_id"));
                q.setQuestionType(rs.getString("question_type"));
                q.setBtlLevel(rs.getString("btl_level"));
                q.setDefaultMarks(rs.getDouble("marks_assigned"));
                q.setStatus(rs.getString("status"));
                questions.add(q);
            }
            rs.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return questions;
    }
}
