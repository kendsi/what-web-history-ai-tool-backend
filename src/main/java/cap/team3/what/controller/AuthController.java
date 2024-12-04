package cap.team3.what.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cap.team3.what.service.AuthService;

import java.util.Map;



@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("oauth2/google")
    public ResponseEntity<Map<String, String>> googleLogin(@RequestBody Map<String, String> request) {
        String idToken = request.get("token");
        Map<String, String> tokenMap = authService.login(idToken);

        return new ResponseEntity<>(tokenMap, HttpStatus.OK);
    }

    @PostMapping("refresh")
    public ResponseEntity<String> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

        String newAccessToken = authService.refresh(refreshToken);
        log.info("Access Token refreshed successfully");
        return new ResponseEntity<>(newAccessToken, HttpStatus.OK);
    }

    @PostMapping("logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        
        authService.logout(token);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}