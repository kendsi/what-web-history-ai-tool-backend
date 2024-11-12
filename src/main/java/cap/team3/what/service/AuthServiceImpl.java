package cap.team3.what.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import cap.team3.what.exception.UnauthorizedException;
import cap.team3.what.model.User;
import cap.team3.what.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public String login(String idToken) {
        // Google OAuth2 토큰 검증
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, String>>() {});

        Map<String, String> tokenInfo = response.getBody();
        
        if (tokenInfo == null || !tokenInfo.containsKey("email")) {
            throw new UnauthorizedException("Invalid Google token");
        }

        String email = tokenInfo.get("email");

        User user = userService.getUserByEmail(email);

        if (user == null) {
            User newUser = new User(email);
            userService.registerUser(newUser);
            return jwtTokenProvider.createToken(email);
        }
        
        return jwtTokenProvider.createToken(user.getEmail());
    }

    @Override
    @Transactional
    public void logout(String token) {

        if (jwtTokenProvider.validateToken(token)) {
            jwtTokenProvider.blacklistToken(token);
        } else {
            throw new IllegalArgumentException("Invalid token");
        }
    }
}
