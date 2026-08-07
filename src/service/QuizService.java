package service;

import exception.ValidationException;
import model.Question;
import model.Quiz;
import model.QuizCriteria;

import java.util.List;

public interface QuizService {

    int createQuiz(Quiz quiz) throws ValidationException;

    Quiz getQuizById(int quizId);

    List<Question> generateRandomQuiz(int quizId, QuizCriteria criteria) throws ValidationException;

    List<Question> getQuizQuestions(int quizId);
}
