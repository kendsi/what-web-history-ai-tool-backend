package cap.team3.what.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Access Token 생성
    public String createAccessToken(String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(validity)
            .signWith(this.getSigningKey())
            .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String email) {
        Date now = new Date();

        return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .signWith(this.getSigningKey())
            .compact();
    }

    public void saveRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set("refresh:" + email, refreshToken);
    }

    public boolean validateRefreshToken(String email, String refreshToken) {
        String storedToken = (String) redisTemplate.opsForValue().get("refresh:" + email);
        return storedToken != null && storedToken.equals(refreshToken);
    }
    
    public void invalidateRefreshToken(String email) {
        redisTemplate.delete("refresh:" + email);
    }

    public void blacklistAccessToken(String token) {
        long expiration = parseClaims(token).getExpiration().getTime();
        long remainingTime = expiration - System.currentTimeMillis();
        if (remainingTime > 0) {
            redisTemplate.opsForValue().set(token, "blacklisted", remainingTime, TimeUnit.MILLISECONDS);
        }
    }

    public boolean validateAccessToken(String token) {
        try {
            if (redisTemplate.hasKey(token)) {
                log.error("Blacklisted JWT token");
                return false;
            }
            parseClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Token validation error: " + token + "\n", e);
            return false;
        }
    }

    public String getEmailFromAccessToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }
    
    public Claims parseClaims (String token) {
        return Jwts.parser()
                .verifyWith(this.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}