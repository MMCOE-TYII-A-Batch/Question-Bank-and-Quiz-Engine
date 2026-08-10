package dao;

import model.QuizAttempt;
import model.StudentAnswer;
import java.util.List;

public interface QuizAttemptDAO {
    int startQuizAttempt(int quizId, int studentId);
    QuizAttempt getAttemptById(int attemptId);
    boolean saveStudentAnswer(StudentAnswer answer);
    boolean completeQuizAttempt(int attemptId, double scoreObtained);
    List<QuizAttempt> getAttemptsByQuizId(int quizId);
    List<QuizAttempt> getStudentAttempts(int studentId);
}
