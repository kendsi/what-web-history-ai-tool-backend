package cap.team3.what.service;

public interface AuthService {
    public String login(String email);
    public void logout(String token);
}
