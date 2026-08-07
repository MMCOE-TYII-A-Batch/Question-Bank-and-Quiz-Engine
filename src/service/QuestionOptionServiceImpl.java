package service;

import dao.QuestionOptionDAO;
import dao.QuestionOptionDAOImpl;
import exception.ValidationException;
import model.QuestionOption;
import validator.QuestionOptionValidator;

import java.util.List;

public class QuestionOptionServiceImpl
        implements QuestionOptionService {

    private QuestionOptionDAO dao;
    private QuestionOptionValidator validator;

    public QuestionOptionServiceImpl(){

        dao = new QuestionOptionDAOImpl();
        validator = new QuestionOptionValidator();

    }

    @Override
    public void saveOptions(List<QuestionOption> options)
            throws ValidationException {

        validator.validate(options);

        dao.saveOptions(options);

    }
@Override
public List<QuestionOption> getOptionsByQuestionId(int questionId){

    return dao.getOptionsByQuestionId(questionId);

}

@Override
public void updateOption(QuestionOption option)
        throws ValidationException{

    validator.validate(List.of(option));

    if(!dao.updateOption(option))
        throw new ValidationException("Option not found.");

}

@Override
public void deleteOptions(int questionId)
        throws ValidationException{

    dao.deleteOptions(questionId);

}

@Override
public void updateAllOptions(List<QuestionOption> options)
        throws ValidationException {

    dao.updateAllOptions(options);

}

}