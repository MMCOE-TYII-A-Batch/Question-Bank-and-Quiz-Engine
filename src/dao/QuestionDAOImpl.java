package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.Question;
import util.DBConnection;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAOImpl implements QuestionDAO {
    @Override
    public int saveQuestion(Question question) {
        int generatedId = -1;
        String sql = """
                INSERT INTO question
                (question_bank_id,
                question,
                 topic_id,
                 author_id,
                 mod_id,
                 question_type,
                 btl_level,
                 default_marks,
                 status)
                VALUES (?,?,?,?,?,?,?,?,?)
                RETURNING question_id;
                """;
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, question.getQuestionBankId());
            ps.setString(2, question.getQuestion());
            ps.setInt(3, question.getTopicId());
            ps.setInt(4, question.getAuthorId());
if(question.getModeratorId()==null)
    ps.setNull(5, java.sql.Types.INTEGER);
else
    ps.setInt(5, question.getModeratorId());
    ps.setString(6, question.getQuestionType());
    ps.setString(7, question.getBtlLevel());
    ps.setDouble(8, question.getDefaultMarks());
    ps.setString(9, question.getStatus());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                generatedId = rs.getInt("question_id");
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return generatedId;
    }
    @Override
    public boolean isDuplicate(String questionText) {
        String sql =
                "SELECT COUNT(*) FROM question WHERE LOWER(question)=LOWER(?)";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, questionText);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            rs.close();
            ps.close();
            con.close();
        } catch(SQLException e){
    throw new RuntimeException("Database Error: " + e.getMessage());
}
        return false;
    }
    @Override
public List<Question> getPendingQuestions() {
    List<Question> questions = new ArrayList<>();
    String sql = """
            SELECT *
            FROM question
            WHERE status='PENDING'
            ORDER BY question_id
            """;
    try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
    ) {
        while (rs.next()) {
            Question question = new Question();
            question.setQuestionId(rs.getInt("question_id"));
            question.setTopicId(rs.getInt("topic_id"));
            question.setAuthorId(rs.getInt("author_id"));
            int mod_id = rs.getInt("mod_id");
            if (!rs.wasNull())
                question.setModeratorId(mod_id);
            question.setQuestion(rs.getString("question"));
            question.setQuestionType(rs.getString("question_type"));
            question.setBtlLevel(rs.getString("btl_level"));
            question.setDefaultMarks(rs.getDouble("default_marks"));
            question.setStatus(rs.getString("status"));
            questions.add(question);
        }
    }
    catch (Exception e) {
        throw new RuntimeException(e);
    }
    return questions;
}
@Override
public boolean approveQuestion(int questionId, int moderatorId) {
    String sql = """
            UPDATE question
            SET status='ACTIVE',
                mod_id=?,
                last_update=CURRENT_TIMESTAMP
            WHERE question_id=?
            """;
    try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
    ) {
        ps.setInt(1, moderatorId);
        ps.setInt(2, questionId);
        return ps.executeUpdate() > 0;
    }
    catch (Exception e) {
        throw new RuntimeException(e);
    }
}
@Override
public boolean rejectQuestion(int questionId, int moderatorId) {
    String sql = """
            UPDATE question
            SET status='REJECTED',
                mod_id=?,
                last_update=CURRENT_TIMESTAMP
            WHERE question_id=?
            """;
    try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
    ) {
        ps.setInt(1, moderatorId);
        ps.setInt(2, questionId);
        return ps.executeUpdate() > 0;
    }
    catch (Exception e) {
        throw new RuntimeException(e);
    }
}
@Override
public boolean updateQuestion(Question question) {
    String sql = """
            UPDATE question
            SET question=?,
                btl_level=?,
                default_marks=?,
                last_update=CURRENT_TIMESTAMP
            WHERE question_id=?
            """;
    try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
    ) {
        ps.setString(1, question.getQuestion());
        ps.setString(2, question.getBtlLevel());
        ps.setDouble(3, question.getDefaultMarks());
        ps.setInt(4, question.getQuestionId());
        return ps.executeUpdate() > 0;
    }
    catch (Exception e) {
        throw new RuntimeException(e);
    }
}
@Override
public boolean deleteQuestion(int questionId) {
    String sql = """
            UPDATE question
            SET status='DELETED',
                last_update=CURRENT_TIMESTAMP
            WHERE question_id=?
            """;
    try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
    ) {
        ps.setInt(1, questionId);
        return ps.executeUpdate() > 0;
    }
    catch (Exception e) {
        throw new RuntimeException(e);
    }
}
@Override
public Question getQuestionById(int questionId){
    String sql="""
        SELECT *
        FROM question
        WHERE question_id=?
    """;
    try(
            Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql);
    ){
        ps.setInt(1,questionId);
        ResultSet rs=ps.executeQuery();
        if(rs.next()){
            Question question=new Question();
            question.setQuestionId(rs.getInt("question_id"));
            question.setQuestionBankId(
                    rs.getInt("question_bank_id"));
            question.setQuestion(
                    rs.getString("question"));
            question.setTopicId(
                    rs.getInt("topic_id"));
            question.setAuthorId(
                    rs.getInt("author_id"));
            int modId=rs.getInt("mod_id");

            if(!rs.wasNull())
                question.setModeratorId(modId);
                question.setQuestionType(
                    rs.getString("question_type"));
                question.setBtlLevel(
                    rs.getString("btl_level"));
                question.setDefaultMarks(
                    rs.getDouble("default_marks"));
                question.setStatus(
                    rs.getString("status"));
            return question;
        }
    }
    catch(Exception e){
        throw new RuntimeException(e);
    }
    return null;
}
@Override
public List<Question> getQuestionsByQuestionBank(int questionBankId) {
    List<Question> questions = new ArrayList<>();
    String sql = """
            SELECT *
            FROM question
            WHERE question_bank_id=?
            ORDER BY question_id
            """;
    try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
    ) {
        ps.setInt(1, questionBankId);
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            Question question = new Question();
            question.setQuestionId(rs.getInt("question_id"));
            question.setQuestionBankId(rs.getInt("question_bank_id"));
            question.setQuestion(rs.getString("question"));
            question.setQuestionType(rs.getString("question_type"));
            question.setBtlLevel(rs.getString("btl_level"));
            question.setDefaultMarks(rs.getDouble("default_marks"));
            question.setStatus(rs.getString("status"));
            questions.add(question);
        }
    }
    catch(Exception e){
        throw new RuntimeException(e);
    }
    return questions;
}
}
