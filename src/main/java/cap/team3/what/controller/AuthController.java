package cap.team3.what.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("oauth2/google")
    public ResponseEntity<String> googleLogin(@RequestBody Map<String, String> request) {
        String idToken = request.get("token");
        String jwtToken = authService.login(idToken);

        return new ResponseEntity<>(jwtToken, HttpStatus.OK);
    }

    @PostMapping("logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        
        authService.logout(token);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}