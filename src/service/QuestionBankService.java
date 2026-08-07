package service;

import exception.ValidationException;
import model.QuestionBank;

import java.util.List;

public interface QuestionBankService {

    int createQuestionBank(QuestionBank questionBank)
            throws ValidationException;

    List<QuestionBank> getQuestionBanksByTeacher(int teacherId);

    QuestionBank getQuestionBankById(int questionBankId);

    void submitQuestionBank(int questionBankId)
            throws ValidationException;

    void approveQuestionBank(int questionBankId)
            throws ValidationException;

    void rejectQuestionBank(int questionBankId)
            throws ValidationException;

    void publishQuestionBank(int questionBankId)
            throws ValidationException;

    List<QuestionBank> getSubmittedQuestionBanks();
}