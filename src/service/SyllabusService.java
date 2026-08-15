package service;
import exception.ValidationException;
import model.Subject;
import model.Topic;
import model.Unit;
import java.util.List;
public interface SyllabusService {
    Subject createSubject(Subject subject) throws ValidationException;
    List<Subject> getAllSubjects();
    boolean updateSubject(Subject subject) throws ValidationException;
    boolean setSubjectActive(int subjectId, boolean isActive) throws ValidationException;
    int cloneSubjectVersion(int oldSubjectId, String newVersion) throws ValidationException
    Unit createUnit(Unit unit) throws ValidationException;
    List<Unit> getUnitsBySubjectId(int subjectId) throws ValidationException;
    boolean updateUnit(Unit unit) throws ValidationException;
    Topic createTopic(Topic topic) throws ValidationException;
    List<Topic> getTopicsByUnitId(int unitId) throws ValidationException;
    boolean updateTopic(Topic topic) throws ValidationException;
}
