package validator;

import constant.AppConstants;
import exception.ValidationException;
import model.Question;

public class QuestionValidator {

    public void validate(Question question) throws ValidationException {

        if (question == null) {
            throw new ValidationException("Question object is null.");
        }

        if (question.getQuestion() == null ||
                question.getQuestion().trim().isEmpty()) {

            throw new ValidationException("Question cannot be empty.");
        }

        if (question.getQuestion().length() < 10) {

            throw new ValidationException("Question is too short.");
        }

        if (question.getTopicId() <= 0) {

            throw new ValidationException("Invalid Topic.");
        }

        if (question.getAuthorId() <= 0) {

            throw new ValidationException("Invalid Author.");
        }

        if (question.getDefaultMarks() <= 0) {

            throw new ValidationException("Marks must be greater than zero.");
        }

        String type = question.getQuestionType();
        if (type == null || !(
                type.equalsIgnoreCase(AppConstants.MCQ) ||
                type.equalsIgnoreCase(AppConstants.TRUE_FALSE) ||
                type.equalsIgnoreCase(AppConstants.SHORT_ANSWER) ||
                type.equalsIgnoreCase(AppConstants.ESSAY) ||
                type.equalsIgnoreCase(AppConstants.DESCRIPTIVE)
        )) {
            throw new ValidationException("Invalid Question Type.");
        }

        String status = question.getStatus();
        if (status == null || !(
                status.equalsIgnoreCase(AppConstants.DRAFT) ||
                status.equalsIgnoreCase(AppConstants.SUBMITTED) ||
                status.equalsIgnoreCase(AppConstants.PENDING) ||
                status.equalsIgnoreCase(AppConstants.APPROVED) ||
                status.equalsIgnoreCase(AppConstants.REJECTED) ||
                status.equalsIgnoreCase(AppConstants.ACTIVE) ||
                status.equalsIgnoreCase(AppConstants.LIVE) ||
                status.equalsIgnoreCase(AppConstants.INACTIVE) ||
                status.equalsIgnoreCase(AppConstants.DELETED)
        )) {
            throw new ValidationException("Invalid Status.");
        }
    }
}