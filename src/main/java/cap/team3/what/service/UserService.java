package cap.team3.what.service;

import cap.team3.what.model.User;

public interface UserService {
    public User getUserByEmail(String email);
    public User registerUser(User user);
    public void deleteUser(String email);
}
