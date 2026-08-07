package service;

import exception.ValidationException;
import model.QuizAttempt;

import java.util.List;
import java.util.Map;

public interface QuizAttemptService {

    int startQuizAttempt(int quizId, int studentId) throws ValidationException;

    QuizAttempt submitAndAutoGradeQuiz(int attemptId, Map<Integer, Integer> studentSelectedOptions) throws ValidationException;

    QuizAttempt getAttemptById(int attemptId);

    List<QuizAttempt> getQuizResultsForTeacher(int quizId);

    List<QuizAttempt> getStudentAttempts(int studentId);
}
