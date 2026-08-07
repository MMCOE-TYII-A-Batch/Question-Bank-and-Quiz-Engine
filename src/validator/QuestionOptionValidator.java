package validator;

import model.QuestionOption;
import exception.ValidationException;

import java.util.List;

public class QuestionOptionValidator {

    public void validate(List<QuestionOption> options)
            throws ValidationException {

        if(options.size()!=4){

            throw new ValidationException(
                    "Exactly 4 options required.");

        }

        int correct = 0;

        for(QuestionOption option:options){

            if(option.getOptionText()==null ||
                    option.getOptionText().trim().isEmpty()){

                throw new ValidationException(
                        "Option cannot be empty.");

            }

            if(option.isCorrect())
                correct++;

        }

        if(correct!=1){

            throw new ValidationException(
                    "Exactly one correct option required.");

        }

    }

}