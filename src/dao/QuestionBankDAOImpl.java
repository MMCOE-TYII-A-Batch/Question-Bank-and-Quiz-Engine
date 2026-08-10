package dao;

import model.QuestionBank;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class QuestionBankDAOImpl implements QuestionBankDAO {
    @Override
    public int createQuestionBank(QuestionBank questionBank) {
        int generatedId = -1;
        String sql = """
                INSERT INTO question_bank
                (question_bank_name,
                subject_id,
                unit_id,
                topic_id,
                teacher_id,
                academic_year,
                question_bank_type,
                status,
                create_time,
                last_update)
                VALUES
                (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                RETURNING question_bank_id
                """;
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, questionBank.getQuestionBankName());
            ps.setInt(2, questionBank.getSubjectId());
            ps.setInt(3, questionBank.getUnitId());
            ps.setInt(4, questionBank.getTopicId());
            ps.setInt(5, questionBank.getTeacherId());
            ps.setString(6, questionBank.getAcademicYear());
            ps.setString(7, questionBank.getQuestionBankType());
            ps.setString(8, questionBank.getStatus());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                generatedId = rs.getInt("question_bank_id");
            }
            rs.close();
            ps.close();
            con.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return generatedId;
    }
    @Override
    public List<QuestionBank> getQuestionBanksByTeacher(int teacherId) {
        List<QuestionBank> banks = new ArrayList<>();
        String sql = """
                SELECT *
                FROM question_bank
                WHERE teacher_id=?
                ORDER BY question_bank_id DESC
                """;
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, teacherId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                QuestionBank bank = new QuestionBank();
                bank.setQuestionBankId(rs.getInt("question_bank_id"));
                bank.setQuestionBankName(rs.getString("question_bank_name"));
                bank.setSubjectId(rs.getInt("subject_id"));
                bank.setUnitId(rs.getInt("unit_id"));
                bank.setTopicId(rs.getInt("topic_id"));
                bank.setTeacherId(rs.getInt("teacher_id"));
                bank.setAcademicYear(rs.getString("academic_year"));
                bank.setQuestionBankType(rs.getString("question_bank_type"));
                bank.setStatus(rs.getString("status"));
                banks.add(bank);
            }
            rs.close();
            ps.close();
            con.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return banks;
    }
    @Override
    public QuestionBank getQuestionBankById(int questionBankId) {
        String sql = """
                SELECT *
                FROM question_bank
                WHERE question_bank_id=?
                """;
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, questionBankId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                QuestionBank bank = new QuestionBank();
                bank.setQuestionBankId(rs.getInt("question_bank_id"));
                bank.setQuestionBankName(rs.getString("question_bank_name"));
                bank.setSubjectId(rs.getInt("subject_id"));
                bank.setUnitId(rs.getInt("unit_id"));
                bank.setTopicId(rs.getInt("topic_id"));
                bank.setTeacherId(rs.getInt("teacher_id"));
                bank.setAcademicYear(rs.getString("academic_year"));
                bank.setQuestionBankType(rs.getString("question_bank_type"));
                bank.setStatus(rs.getString("status"));
                return bank;
            }
            rs.close();
            ps.close();
            con.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
@Override
public boolean submitQuestionBank(int questionBankId) {
    return updateQuestionBankStatus(questionBankId, "SUBMITTED");
}
@Override
public boolean approveQuestionBank(int questionBankId) {
    return updateQuestionBankStatus(questionBankId, "APPROVED");
}
@Override
public boolean rejectQuestionBank(int questionBankId) {
    return updateQuestionBankStatus(questionBankId, "REJECTED");
}
@Override
public boolean publishQuestionBank(int questionBankId) {
    return updateQuestionBankStatus(questionBankId, "LIVE");
}
   @Override
public boolean updateQuestionBankStatus(int questionBankId, String status) {
    String sql = """
            UPDATE question_bank
            SET status=?,
                last_update=CURRENT_TIMESTAMP
            WHERE question_bank_id=?
            """;
    try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
    ) {
        ps.setString(1, status);
        ps.setInt(2, questionBankId);
        return ps.executeUpdate() > 0;
    }
    catch (Exception e) {
        throw new RuntimeException(e);
    }
}
@Override
public List<QuestionBank> getSubmittedQuestionBanks() {
    List<QuestionBank> banks = new ArrayList<>();
    String sql = """
            SELECT *
            FROM question_bank
            WHERE status='SUBMITTED'
            ORDER BY question_bank_id
            """;
    try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
    ) {
        while (rs.next()) {
            QuestionBank bank = new QuestionBank();
            bank.setQuestionBankId(rs.getInt("question_bank_id"));
            bank.setQuestionBankName(rs.getString("question_bank_name"));
            bank.setSubjectId(rs.getInt("subject_id"));
            bank.setUnitId(rs.getInt("unit_id"));
            bank.setTopicId(rs.getInt("topic_id"));
            bank.setTeacherId(rs.getInt("teacher_id"));
            bank.setAcademicYear(rs.getString("academic_year"));
            bank.setQuestionBankType(rs.getString("question_bank_type"));
            bank.setStatus(rs.getString("status"));
            banks.add(bank);
        }
    }
    catch (Exception e) {
        throw new RuntimeException(e);
    }
    return banks;
}
}
