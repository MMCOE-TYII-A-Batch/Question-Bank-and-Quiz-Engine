package dao;

import model.QuestionBank;
import java.util.List;

public interface QuestionBankDAO {
    int createQuestionBank(QuestionBank questionBank);
    List<QuestionBank> getQuestionBanksByTeacher(int teacherId);
    QuestionBank getQuestionBankById(int questionBankId);
    boolean submitQuestionBank(int questionBankId);
    boolean approveQuestionBank(int questionBankId);
    boolean rejectQuestionBank(int questionBankId);
    boolean publishQuestionBank(int questionBankId);
    List<QuestionBank> getSubmittedQuestionBanks();
    boolean updateQuestionBankStatus(int questionBankId, String status);
}
