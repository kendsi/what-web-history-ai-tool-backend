package cap.team3.what.service;

import cap.team3.what.model.User;

public interface UserService {
    public String login(String email);
    public void logout(String token);
    public User getUserByEmail(String email);
    public void deleteUser(String email);
}
