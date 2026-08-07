package model;

import java.sql.Timestamp;

public class StudentAnswer {

    private int answerId;
    private int attemptId;
    private int questionId;
    private Integer selectedOptionId;
    private String textResponse;
    private double marksAwarded;
    private boolean isAutoGraded;
    private Integer evaluatorId;
    private Timestamp createTime;
    private Timestamp lastUpdate;

    public StudentAnswer() {
    }

    public int getAnswerId() {
        return answerId;
    }

    public void setAnswerId(int answerId) {
        this.answerId = answerId;
    }

    public int getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public Integer getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Integer selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public String getTextResponse() {
        return textResponse;
    }

    public void setTextResponse(String textResponse) {
        this.textResponse = textResponse;
    }

    public double getMarksAwarded() {
        return marksAwarded;
    }

    public void setMarksAwarded(double marksAwarded) {
        this.marksAwarded = marksAwarded;
    }

    public boolean isAutoGraded() {
        return isAutoGraded;
    }

    public void setAutoGraded(boolean autoGraded) {
        isAutoGraded = autoGraded;
    }

    public Integer getEvaluatorId() {
        return evaluatorId;
    }

    public void setEvaluatorId(Integer evaluatorId) {
        this.evaluatorId = evaluatorId;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
