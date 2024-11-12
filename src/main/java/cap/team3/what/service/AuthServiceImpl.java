package cap.team3.what.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import cap.team3.what.exception.UnauthorizedException;
import cap.team3.what.model.User;
import cap.team3.what.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public String login(String accessToken) {
        // Google OAuth2 토큰 검증
        String url = "https://www.googleapis.com/oauth2/v3/userinfo";
        log.info(accessToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            Map.class
        );

        String email = (String) response.getBody().get("email");

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
