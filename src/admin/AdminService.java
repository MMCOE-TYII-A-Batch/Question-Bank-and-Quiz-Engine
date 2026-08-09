package admin;
import exception.ValidationException;
import java.util.List;
public interface AdminService {
    int addUser(UserDTO user, int adminUserId) throws ValidationException;
    boolean updateUserDesignation(int userId, String newRoleName, int adminUserId) throws ValidationException;
    boolean removeUser(int userId, int adminUserId) throws ValidationException;
    UserDTO getUserById(int userId);
    List<UserDTO> getUsersByRole(String roleName);
    List<UserDTO> getAllUsers();
}
