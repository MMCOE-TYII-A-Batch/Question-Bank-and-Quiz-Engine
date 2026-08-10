package dao;

import model.Quiz;
import model.Question;
import model.QuizCriteria;
import java.util.List;

public interface QuizDAO {
    int createQuiz(Quiz quiz);
    Quiz getQuizById(int quizId);
    List<Question> fetchCandidateQuestions(QuizCriteria criteria);
    boolean linkQuestionsToQuiz(int quizId, List<Question> selectedQuestions);
    List<Question> getQuizQuestions(int quizId);
}
