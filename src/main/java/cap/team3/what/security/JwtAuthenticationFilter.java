package cap.team3.what.security;

import org.springframework.web.filter.GenericFilterBean;

import cap.team3.what.exception.JwtValidationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;

    private final List<AntPathRequestMatcher> excludePaths = Arrays.asList(
        new AntPathRequestMatcher("/api/auth/oauth2/google", "POST"),
        new AntPathRequestMatcher("/favicon.ico", "GET"),
        new AntPathRequestMatcher("/swagger-resources/**"),
        new AntPathRequestMatcher("/swagger-ui/**"),
        new AntPathRequestMatcher("/v3/api-docs/**"),
        new AntPathRequestMatcher("/webjars/**"),
        new AntPathRequestMatcher("/api/test/**")
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // 제외 경로 확인
            if (excludePaths.stream().anyMatch(matcher -> matcher.matches(httpRequest))) {
                chain.doFilter(request, response);  // 예외 경로는 필터를 건너뜀
                return;
            }
        
            String token = resolveToken(httpRequest);
            if (token != null) {
                validateAndSetAuthentication(token);
            }
            chain.doFilter(request, response);
        } catch (JwtValidationException e) {
            log.error("JWT validation error: {}", e.getMessage());
            setUnauthorizedResponse(httpResponse, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in JwtAuthenticationFilter: ", e);
            setUnauthorizedResponse(httpResponse, "Unexpected authentication error");
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void validateAndSetAuthentication(String token) {
        if (!jwtTokenProvider.validateAccessToken(token)) {
            throw new JwtValidationException("Invalid or blacklisted JWT token");
        }

        String email = jwtTokenProvider.getEmailFromAccessToken(token);
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void setUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }
}