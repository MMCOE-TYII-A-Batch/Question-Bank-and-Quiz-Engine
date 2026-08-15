package model;
import java.sql.Timestamp;
public class Subject {
    private int subjectId;
    private String subjectName;
    private String version;
    private boolean isActive;
    private Timestamp createTime;
    private Timestamp lastUpdate;
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }    
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }    
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }  
    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }   
    public Timestamp getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(Timestamp lastUpdate) { this.lastUpdate = lastUpdate; }
}
