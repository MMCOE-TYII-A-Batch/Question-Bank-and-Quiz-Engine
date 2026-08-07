package service;

import dao.QuizDAO;
import dao.QuizDAOImpl;
import exception.ValidationException;
import model.Question;
import model.Quiz;
import model.QuizCriteria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizServiceImpl implements QuizService {

    private final QuizDAO dao;

    public QuizServiceImpl() {
        this.dao = new QuizDAOImpl();
    }

    public QuizServiceImpl(QuizDAO dao) {
        this.dao = dao;
    }

    @Override
    public int createQuiz(Quiz quiz) throws ValidationException {
        if (quiz == null) {
            throw new ValidationException("Quiz object cannot be null.");
        }
        if (quiz.getSubjectId() <= 0) {
            throw new ValidationException("Invalid Subject ID for Quiz.");
        }
        if (quiz.getCreatorId() <= 0) {
            throw new ValidationException("Invalid Creator ID.");
        }
        if (quiz.getDurationMinutes() <= 0) {
            quiz.setDurationMinutes(60); // Default 60 mins
        }
        if (quiz.getStatus() == null || quiz.getStatus().isBlank()) {
            quiz.setStatus("DRAFT");
        }

        return dao.createQuiz(quiz);
    }

    @Override
    public Quiz getQuizById(int quizId) {
        return dao.getQuizById(quizId);
    }

    @Override
    public List<Question> generateRandomQuiz(int quizId, QuizCriteria criteria) throws ValidationException {
        if (quizId <= 0) {
            throw new ValidationException("Invalid Quiz ID.");
        }
        if (criteria == null) {
            throw new ValidationException("QuizCriteria cannot be null.");
        }
        if (criteria.getSubjectId() <= 0) {
            throw new ValidationException("Invalid Subject ID in Criteria.");
        }

        // Enforce MCQ only rule for Quiz Generation
        criteria.setQuestionType("MCQ");

        Quiz quiz = dao.getQuizById(quizId);
        if (quiz == null) {
            throw new ValidationException("Quiz not found.");
        }

        // Fetch candidate questions matching criteria
        List<Question> candidates = dao.fetchCandidateQuestions(criteria);
        if (candidates.isEmpty()) {
            throw new ValidationException("No active questions available matching the specified criteria.");
        }

        // Randomization Step
        Collections.shuffle(candidates);

        List<Question> selected = new ArrayList<>();
        double currentMarks = 0.0;

        if (criteria.getNumberOfQuestions() > 0) {
            int count = Math.min(criteria.getNumberOfQuestions(), candidates.size());
            selected = candidates.subList(0, count);
        } else if (criteria.getTargetTotalMarks() > 0) {
            for (Question q : candidates) {
                if (currentMarks < criteria.getTargetTotalMarks()) {
                    selected.add(q);
                    currentMarks += q.getDefaultMarks();
                } else {
                    break;
                }
            }
        } else {
            // Default: Pick top 10 randomized
            int count = Math.min(10, candidates.size());
            selected = candidates.subList(0, count);
        }

        // Save generated quiz paper questions into DB
        boolean success = dao.linkQuestionsToQuiz(quizId, selected);
        if (!success) {
            throw new ValidationException("Failed to save randomized questions to quiz.");
        }

        return selected;
    }

    @Override
    public List<Question> getQuizQuestions(int quizId) {
        return dao.getQuizQuestions(quizId);
    }
}
