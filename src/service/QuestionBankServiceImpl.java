package service;

import dao.QuestionBankDAO;
import dao.QuestionBankDAOImpl;
import exception.ValidationException;
import model.QuestionBank;

import java.util.List;
import model.Question;
public class QuestionBankServiceImpl implements QuestionBankService {

    private QuestionBankDAO dao;

    public QuestionBankServiceImpl() {

        dao = new QuestionBankDAOImpl();

    }

    @Override
    public int createQuestionBank(QuestionBank questionBank)
            throws ValidationException {

        if(questionBank.getQuestionBankName()==null
                || questionBank.getQuestionBankName().isBlank()){

            throw new ValidationException(
                    "Question Bank Name cannot be empty.");

        }

        questionBank.setStatus("DRAFT");

        return dao.createQuestionBank(questionBank);

    }

    @Override
    public List<QuestionBank> getQuestionBanksByTeacher(int teacherId) {

        return dao.getQuestionBanksByTeacher(teacherId);

    }

    @Override
    public QuestionBank getQuestionBankById(int questionBankId) {

        return dao.getQuestionBankById(questionBankId);

    }

@Override
public void submitQuestionBank(int questionBankId)
        throws ValidationException {

    QuestionService questionService =
            new QuestionServiceImpl();

    List<Question> questions =
            questionService.getQuestionsByQuestionBank(questionBankId);

    if (questions.isEmpty()) {

        throw new ValidationException(
                "Cannot submit an empty Question Bank.");

    }

    if (!dao.submitQuestionBank(questionBankId)) {

        throw new ValidationException(
                "Unable to submit Question Bank.");

    }

}

@Override
public void approveQuestionBank(int questionBankId)
        throws ValidationException {

    QuestionService questionService =
            new QuestionServiceImpl();

    List<Question> questions =
            questionService.getQuestionsByQuestionBank(questionBankId);

    if (questions.isEmpty()) {

        throw new ValidationException(
                "Cannot approve an empty Question Bank.");

    }

    if (!dao.approveQuestionBank(questionBankId)) {

        throw new ValidationException(
                "Unable to approve Question Bank.");

    }

}

    @Override
    public void rejectQuestionBank(int questionBankId)
            throws ValidationException {

        if(!dao.rejectQuestionBank(questionBankId)){

            throw new ValidationException(
                    "Unable to reject Question Bank.");

        }

    }

    @Override
    public void publishQuestionBank(int questionBankId)
            throws ValidationException {

        QuestionBank bank = dao.getQuestionBankById(questionBankId);

        if (bank == null) {

            throw new ValidationException(
                    "Question Bank not found.");

        }

        if (!"APPROVED".equalsIgnoreCase(bank.getStatus())) {

            throw new ValidationException(
                    "Only APPROVED Question Banks can be published.");

        }

        if (!dao.publishQuestionBank(questionBankId)) {

            throw new ValidationException(
                    "Unable to publish Question Bank.");

        }

    }
@Override
public List<QuestionBank> getSubmittedQuestionBanks() {

    return dao.getSubmittedQuestionBanks();

}
}