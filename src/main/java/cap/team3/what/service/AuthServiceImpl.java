package cap.team3.what.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
    private final CategoryService categoryService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public Map<String, String> login(String accessToken) {
        // Google OAuth2 토큰 검증
        String url = "https://www.googleapis.com/oauth2/v3/userinfo";
        log.info(accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        String email = (String) response.getBody().get("email");

        User user;
        try {
            user = userService.getUserByEmail(email);
        } catch (Exception e) {
            user = new User(email);
            userService.registerUser(user);
            categoryService.createDefaultCategories(user);
        }

        // Access Token과 Refresh Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
        jwtTokenProvider.saveRefreshToken(user.getEmail(), refreshToken);

        return Map.of("accessToken", newAccessToken, "refreshToken", refreshToken);
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        if (jwtTokenProvider.validateAccessToken(accessToken)) {
            String email = jwtTokenProvider.getEmailFromAccessToken(accessToken);

            // Access Token 블랙리스트 추가
            jwtTokenProvider.blacklistAccessToken(accessToken);

            // Refresh Token 무효화
            jwtTokenProvider.invalidateRefreshToken(email);
        } else {
            throw new IllegalArgumentException("Invalid access token");
        }
    }

    @Transactional
    public String refresh(String refreshToken) {
        // Refresh Token 검증
        String email = jwtTokenProvider.getEmailFromAccessToken(refreshToken);
        if (!jwtTokenProvider.validateRefreshToken(email, refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // Access Token 재발급
        return jwtTokenProvider.createAccessToken(email);
    }
}
