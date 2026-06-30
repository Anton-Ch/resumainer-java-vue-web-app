package com.resumainer.service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumainer.dao.UserDao;
import com.resumainer.dto.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;

/**
 * Spring Security {@link AuthenticationSuccessHandler} that returns a JSON
 * response compatible with the existing frontend {@code AuthResponse} contract.
 *
 * <p>Response shape:
 * <pre>
 * { "success": true, "role": "USER", "redirectUrl": "/home" }
 * </pre>
 *
 * <p><b>Temporary legacy bridge:</b> Sets HttpSession attribute "user" with
 * {@link UserSession} so that old {@code AuthInterceptor} and session-based
 * controllers continue to work until Phase 7 removes them.
 * Spring Security Authentication remains the authoritative source of truth.
 *
 * <p>Resets the failed login counter on successful authentication.
 */
public class JsonAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonAuthenticationSuccessHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final UserDao userDao;

    public JsonAuthenticationSuccessHandler(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // Determine role and redirect
        String role;
        String redirectUrl;
        boolean privileged = false;

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            role = "ADMIN";
            redirectUrl = "/admin";
        } else {
            role = "USER";
            redirectUrl = "/home";
        }

        // Extract userId and privileged from CustomUserDetails
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            privileged = userDetails.isPrivileged();
            // Legacy session bridge: set HttpSession "user" attribute
            HttpSession session = request.getSession(true);
            session.setAttribute("user", new UserSession(
                    userDetails.getUserId(),
                    userDetails.getUsername(),
                    role,
                    privileged
            ));

            // Reset failed login counter on successful login
            try {
                userDao.resetLoginAttempts(userDetails.getUserId());
            } catch (Exception e) {
                log.warn("Failed to reset login attempts for userId={}", userDetails.getUserId());
            }
        }

        // Safe logging: email, IP only — no passwords, hashes, tokens, secrets
        String email = authentication.getName();
        String ip = request.getRemoteAddr();
        log.info("Login successful: email={}, ip={}", email, ip);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        objectMapper.writeValue(response.getWriter(), Map.of(
                "success", true,
                "role", role,
                "redirectUrl", redirectUrl
        ));
    }
}
