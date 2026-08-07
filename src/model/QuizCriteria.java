package model;

public class QuizCriteria {

    private int subjectId;
    private Integer topicId;
    private String questionType;
    private String btlLevel;
    private int numberOfQuestions;
    private double targetTotalMarks;

    public QuizCriteria() {
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getTopicId() {
        return topicId;
    }

    public void setTopicId(Integer topicId) {
        this.topicId = topicId;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getBtlLevel() {
        return btlLevel;
    }

    public void setBtlLevel(String btlLevel) {
        this.btlLevel = btlLevel;
    }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(int numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public double getTargetTotalMarks() {
        return targetTotalMarks;
    }

    public void setTargetTotalMarks(double targetTotalMarks) {
        this.targetTotalMarks = targetTotalMarks;
    }
}
