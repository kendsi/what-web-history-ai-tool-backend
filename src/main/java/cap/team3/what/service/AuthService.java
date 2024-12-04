package cap.team3.what.service;

import java.util.Map;

public interface AuthService {
    public Map<String, String> login(String email);
    public void logout(String token);
    public String refresh(String token);
}
