package dao;

import java.util.List;
import model.QuestionOption;

public interface QuestionOptionDAO {
void saveOptions(List<QuestionOption> options);
List<QuestionOption> getOptionsByQuestionId(int questionId);
boolean updateOption(QuestionOption option);
boolean deleteOptions(int questionId);
boolean updateAllOptions(List<QuestionOption> options);
}
