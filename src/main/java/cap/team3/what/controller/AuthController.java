package cap.team3.what.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cap.team3.what.dto.AuthResponse;
import cap.team3.what.service.UserService;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @GetMapping("oauth2/google")
    public ResponseEntity<AuthResponse> googleLogin(OAuth2AuthenticationToken authentication) {
        Map<String, Object> userAttributes = authentication.getPrincipal().getAttributes();
        String email = (String) userAttributes.get("email");

        String token = userService.login(email);

        return new ResponseEntity<>(new AuthResponse(token), HttpStatus.OK);
    }

    @PostMapping("logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        
        userService.logout(token);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}