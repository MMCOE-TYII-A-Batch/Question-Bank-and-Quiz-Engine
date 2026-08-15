package model;
import java.sql.Timestamp;
public class Topic {
    private int topicId;
    private int unitId;
    private String topicName;
    private int questionQuota;
    private Timestamp createTime;
    private Timestamp lastUpdate;
    public int getTopicId() { return topicId; }
    public void setTopicId(int topicId) { this.topicId = topicId; }
    public int getUnitId() { return unitId; }
    public void setUnitId(int unitId) { this.unitId = unitId; }
    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }
    public int getQuestionQuota() { return questionQuota; }
    public void setQuestionQuota(int questionQuota) { this.questionQuota = questionQuota; }
    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
    public Timestamp getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(Timestamp lastUpdate) { this.lastUpdate = lastUpdate; }
}
