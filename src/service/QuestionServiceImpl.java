package service;
import java.util.List;
import dao.QuestionDAO;
import dao.QuestionDAOImpl;
import exception.ValidationException;
import model.Question;
import validator.QuestionValidator;
import dao.QuestionBankDAO;
import dao.QuestionBankDAOImpl;
import model.QuestionBank;

public class QuestionServiceImpl implements QuestionService {

    private final QuestionValidator validator;
    private final QuestionDAO dao;

    public QuestionServiceImpl() {

        validator = new QuestionValidator();
        dao = new QuestionDAOImpl();

    }

@Override
public int createQuestion(Question question)
        throws ValidationException {

    validateQuestionBankEditable(question.getQuestionBankId());

    QuestionBankDAO bankDAO = new QuestionBankDAOImpl();

    QuestionBank bank =
            bankDAO.getQuestionBankById(
                    question.getQuestionBankId());

    if (bank == null) {

        throw new ValidationException(
                "Question Bank not found.");

    }

    if (!bank.getQuestionBankType()
            .equalsIgnoreCase(question.getQuestionType())) {

        throw new ValidationException(
                "Question type does not match Question Bank type.");

    }

    validator.validate(question);

    if (dao.isDuplicate(question.getQuestion())) {

        throw new ValidationException(
                "Question already exists.");

    }

    return dao.saveQuestion(question);

}
@Override
public void approveQuestion(int questionId, int moderatorId)
        throws ValidationException {

    if(questionId <= 0)
        throw new ValidationException("Invalid Question ID.");

    if(moderatorId <= 0)
        throw new ValidationException("Invalid Moderator ID.");

    boolean success =
            dao.approveQuestion(questionId, moderatorId);

    if(!success)
        throw new ValidationException(
                "Question not found.");

}
@Override
public void rejectQuestion(int questionId, int moderatorId)
        throws ValidationException {

    if(questionId <= 0)
        throw new ValidationException("Invalid Question ID.");

    if(moderatorId <= 0)
        throw new ValidationException("Invalid Moderator ID.");

    boolean success =
            dao.rejectQuestion(questionId, moderatorId);

    if(!success)
        throw new ValidationException(
                "Question not found.");

}
@Override
public void updateQuestion(Question question)
        throws ValidationException {

    // Check if Question Bank can still be edited
    validateQuestionBankEditable(question.getQuestionBankId());

    // Existing validation
    validator.validate(question);

    boolean success = dao.updateQuestion(question);

    if (!success) {

        throw new ValidationException(
                "Question not found.");

    }

}

@Override
public void deleteQuestion(int questionId)
        throws ValidationException {

    if(questionId <= 0)
        throw new ValidationException(
                "Invalid Question ID.");

    Question question = dao.getQuestionById(questionId);

    if(question == null){

        throw new ValidationException(
                "Question not found.");

    }

    validateQuestionBankEditable(question.getQuestionBankId());

    boolean success =
            dao.deleteQuestion(questionId);

    if(!success)
        throw new ValidationException(
                "Question not found.");

}
@Override
public Question getQuestionById(int questionId) {

    return dao.getQuestionById(questionId);

}
@Override
public List<Question> getQuestionsByQuestionBank(int questionBankId){

    return dao.getQuestionsByQuestionBank(questionBankId);

}
private void validateQuestionBankEditable(int questionBankId)
        throws ValidationException {

    QuestionBankDAO bankDAO = new QuestionBankDAOImpl();

    QuestionBank bank = bankDAO.getQuestionBankById(questionBankId);

    if (bank == null) {

        throw new ValidationException("Question Bank not found.");

    }

    String status = bank.getStatus();

    if (status.equals("APPROVED")) {

        throw new ValidationException(
                "Approved Question Bank cannot be edited.");

    }

}
@Override
public List<Question> getPendingQuestions() {

    return dao.getPendingQuestions();

}
}