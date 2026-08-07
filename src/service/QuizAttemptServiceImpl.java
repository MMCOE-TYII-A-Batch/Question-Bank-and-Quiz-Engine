package service;

import dao.QuestionDAO;
import dao.QuestionDAOImpl;
import dao.QuestionOptionDAO;
import dao.QuestionOptionDAOImpl;
import dao.QuizAttemptDAO;
import dao.QuizAttemptDAOImpl;
import dao.QuizDAO;
import dao.QuizDAOImpl;
import exception.ValidationException;
import model.Question;
import model.QuestionOption;
import model.Quiz;
import model.QuizAttempt;
import model.StudentAnswer;

import java.util.List;
import java.util.Map;

public class QuizAttemptServiceImpl implements QuizAttemptService {

    private final QuizAttemptDAO attemptDAO;
    private final QuizDAO quizDAO;
    private final QuestionDAO questionDAO;
    private final QuestionOptionDAO optionDAO;

    public QuizAttemptServiceImpl() {
        this.attemptDAO = new QuizAttemptDAOImpl();
        this.quizDAO = new QuizDAOImpl();
        this.questionDAO = new QuestionDAOImpl();
        this.optionDAO = new QuestionOptionDAOImpl();
    }

    @Override
    public int startQuizAttempt(int quizId, int studentId) throws ValidationException {
        if (quizId <= 0) {
            throw new ValidationException("Invalid Quiz ID.");
        }
        if (studentId <= 0) {
            throw new ValidationException("Invalid Student ID.");
        }

        Quiz quiz = quizDAO.getQuizById(quizId);
        if (quiz == null) {
            throw new ValidationException("Quiz not found.");
        }

        return attemptDAO.startQuizAttempt(quizId, studentId);
    }

    @Override
    public QuizAttempt submitAndAutoGradeQuiz(int attemptId, Map<Integer, Integer> studentSelectedOptions) throws ValidationException {
        if (attemptId <= 0) {
            throw new ValidationException("Invalid Attempt ID.");
        }

        QuizAttempt attempt = attemptDAO.getAttemptById(attemptId);
        if (attempt == null) {
            throw new ValidationException("Quiz Attempt not found.");
        }

        if ("COMPLETED".equalsIgnoreCase(attempt.getStatus())) {
            throw new ValidationException("Quiz Attempt has already been submitted and completed.");
        }

        double totalScore = 0.0;

        if (studentSelectedOptions != null && !studentSelectedOptions.isEmpty()) {
            for (Map.Entry<Integer, Integer> entry : studentSelectedOptions.entrySet()) {
                int questionId = entry.getKey();
                int selectedOptionId = entry.getValue();

                Question question = questionDAO.getQuestionById(questionId);
                if (question == null) continue;

                List<QuestionOption> options = optionDAO.getOptionsByQuestionId(questionId);
                boolean isCorrect = false;

                for (QuestionOption opt : options) {
                    if (opt.getOptionId() == selectedOptionId && opt.isCorrect()) {
                        isCorrect = true;
                        break;
                    }
                }

                double marksAwarded = isCorrect ? question.getDefaultMarks() : 0.0;
                totalScore += marksAwarded;

                StudentAnswer answer = new StudentAnswer();
                answer.setAttemptId(attemptId);
                answer.setQuestionId(questionId);
                answer.setSelectedOptionId(selectedOptionId);
                answer.setMarksAwarded(marksAwarded);
                answer.setAutoGraded(true);

                attemptDAO.saveStudentAnswer(answer);
            }
        }

        attemptDAO.completeQuizAttempt(attemptId, totalScore);
        return attemptDAO.getAttemptById(attemptId);
    }

    @Override
    public QuizAttempt getAttemptById(int attemptId) {
        return attemptDAO.getAttemptById(attemptId);
    }

    @Override
    public List<QuizAttempt> getQuizResultsForTeacher(int quizId) {
        return attemptDAO.getAttemptsByQuizId(quizId);
    }

    @Override
    public List<QuizAttempt> getStudentAttempts(int studentId) {
        return attemptDAO.getStudentAttempts(studentId);
    }
}
