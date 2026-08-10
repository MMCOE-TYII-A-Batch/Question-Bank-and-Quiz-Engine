package dao;
import util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;
import model.QuestionOption;
public class QuestionOptionDAOImpl implements QuestionOptionDAO {
    @Override
    public void saveOptions(List<QuestionOption> options) {
        String sql = """
                INSERT INTO question_option
                (question_id,
                option_order,
                option_text,
                is_correct,
                create_time,
                last_update)
                VALUES
                (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;
        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            for (QuestionOption option : options) {
                ps.setInt(1, option.getQuestionId());
                ps.setInt(2, option.getOptionOrder());
                ps.setString(3, option.getOptionText());
                ps.setBoolean(4, option.isCorrect());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
public List<QuestionOption> getOptionsByQuestionId(int questionId) {
    List<QuestionOption> options = new ArrayList<>();
    String sql = """
        SELECT *
        FROM question_option
        WHERE question_id=?
        ORDER BY option_order
    """;
    try (
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
    ) {
        ps.setInt(1, questionId);
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            QuestionOption option = new QuestionOption();
            option.setOptionId(rs.getInt("option_id"));
            option.setQuestionId(rs.getInt("question_id"));
            option.setOptionOrder(rs.getInt("option_order"));
            option.setOptionText(rs.getString("option_text"));
            option.setCorrect(rs.getBoolean("is_correct"));
            options.add(option);
        }
    } catch(Exception e){
        throw new RuntimeException(e);
    }
    return options;
}
@Override
public boolean updateOption(QuestionOption option) {
    String sql = """
        UPDATE question_option
        SET option_text=?,
            is_correct=?,
            last_update=CURRENT_TIMESTAMP
        WHERE option_id=?
    """;
    try (
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
    ){
        ps.setString(1, option.getOptionText());
        ps.setBoolean(2, option.isCorrect());
        ps.setInt(3, option.getOptionId());
        return ps.executeUpdate() > 0;
    } catch(Exception e){
        throw new RuntimeException(e);
    }
}
@Override
public boolean deleteOptions(int questionId){
    String sql="""
        DELETE FROM question_option
        WHERE question_id=?
    """;
    try(
        Connection con=DBConnection.getConnection();
        PreparedStatement ps=con.prepareStatement(sql);
    ){
        ps.setInt(1,questionId);
        ps.executeUpdate();
        return true;
    }catch(Exception e){
        throw new RuntimeException(e);
    }
}
@Override
public boolean updateAllOptions(List<QuestionOption> options) {
    String sql = """
            UPDATE question_option
            SET option_text=?,
                is_correct=?,
                last_update=CURRENT_TIMESTAMP
            WHERE option_id=?
            """;
    try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
    ) {
        for (QuestionOption option : options) {
            ps.setString(1, option.getOptionText());
            ps.setBoolean(2, option.isCorrect());
            ps.setInt(3, option.getOptionId());
            ps.addBatch();
        }
        ps.executeBatch();
        return true;
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
}
