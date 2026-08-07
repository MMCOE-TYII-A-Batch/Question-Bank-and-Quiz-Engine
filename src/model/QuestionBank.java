package model;

import java.sql.Timestamp;

public class QuestionBank {

    private int questionBankId;
    private String questionBankName;

    private int subjectId;
    private int unitId;
    private int topicId;

    private int teacherId;

    // NEW
    private String academicYear;
    private String questionBankType;

    private String status;

    private Timestamp createTime;
    private Timestamp lastUpdate;

    public QuestionBank() {
    }

    // ==========================
    // Question Bank ID
    // ==========================

    public int getQuestionBankId() {
        return questionBankId;
    }

    public void setQuestionBankId(int questionBankId) {
        this.questionBankId = questionBankId;
    }

    // ==========================
    // Question Bank Name
    // ==========================

    public String getQuestionBankName() {
        return questionBankName;
    }

    public void setQuestionBankName(String questionBankName) {
        this.questionBankName = questionBankName;
    }

    // ==========================
    // Subject
    // ==========================

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    // ==========================
    // Unit
    // ==========================

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    // ==========================
    // Topic
    // ==========================

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    // ==========================
    // Teacher
    // ==========================

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    // ==========================
    // Academic Year
    // ==========================

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    // ==========================
    // Question Bank Type
    // ==========================

    public String getQuestionBankType() {
        return questionBankType;
    }

    public void setQuestionBankType(String questionBankType) {
        this.questionBankType = questionBankType;
    }

    // ==========================
    // Status
    // ==========================

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ==========================
    // Create Time
    // ==========================

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    // ==========================
    // Last Update
    // ==========================

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    // ==========================
    // Helper Methods
    // ==========================

    public boolean isMCQ() {
        return "MCQ".equalsIgnoreCase(questionBankType);
    }

    public boolean isDescriptive() {
        return "DESCRIPTIVE".equalsIgnoreCase(questionBankType);
    }

}