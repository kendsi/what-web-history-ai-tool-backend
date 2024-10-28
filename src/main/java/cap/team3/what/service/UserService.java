package cap.team3.what.service;

import cap.team3.what.model.User;

public interface UserService {
    public String handleGoogleLogin(String email);
    public User getUserByEmail(String email);
    public void deleteUser(String email);
}
