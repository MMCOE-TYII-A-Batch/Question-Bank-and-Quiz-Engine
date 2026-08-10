package dao;

import java.util.List;
import model.Question;

public interface QuestionDAO {

    int saveQuestion(Question question);
    boolean isDuplicate(String question);
    List<Question> getPendingQuestions();
    boolean approveQuestion(int questionId, int moderatorId);
    boolean rejectQuestion(int questionId, int moderatorId);
    boolean updateQuestion(Question question);
    boolean deleteQuestion(int questionId);
    Question getQuestionById(int questionId);
    List<Question> getQuestionsByQuestionBank(int questionBankId);
}
