package model;
import java.sql.Timestamp;
public class Unit {
    private int unitId;
    private int subjectId;
    private String unitName;
    private int questionQuota;
    private Timestamp createTime;
    private Timestamp lastUpdate;
    public int getUnitId() { return unitId; }
    public void setUnitId(int unitId) { this.unitId = unitId; }
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public int getQuestionQuota() { return questionQuota; }
    public void setQuestionQuota(int questionQuota) { this.questionQuota = questionQuota; }
    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
    public Timestamp getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(Timestamp lastUpdate) { this.lastUpdate = lastUpdate; }
}
