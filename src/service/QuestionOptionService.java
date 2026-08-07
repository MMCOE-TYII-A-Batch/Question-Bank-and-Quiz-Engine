package service;

import java.util.List;

import exception.ValidationException;
import model.QuestionOption;

public interface QuestionOptionService {

    void saveOptions(List<QuestionOption> options)
            throws ValidationException;

    List<QuestionOption> getOptionsByQuestionId(int questionId);

    void updateOption(QuestionOption option)
            throws ValidationException;

    void deleteOptions(int questionId)
            throws ValidationException;

    void updateAllOptions(List<QuestionOption> options)
            throws ValidationException;

}