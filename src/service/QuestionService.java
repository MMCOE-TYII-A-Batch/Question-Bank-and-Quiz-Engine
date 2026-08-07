package service;

import java.util.List;

import exception.ValidationException;
import model.Question;

public interface QuestionService {

    // Existing Methods
    int createQuestion(Question question) throws ValidationException;

    // Sprint 3 Methods

    List<Question> getPendingQuestions();

    void approveQuestion(int questionId, int moderatorId)
            throws ValidationException;

    void rejectQuestion(int questionId, int moderatorId)
            throws ValidationException;

    void updateQuestion(Question question)
            throws ValidationException;

    void deleteQuestion(int questionId)
            throws ValidationException;
     Question getQuestionById(int questionId);   
     List<Question> getQuestionsByQuestionBank(int questionBankId);
}       