package com.resumainer.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

import com.resumainer.dto.UserSession;

/**
 * HandlerInterceptor that checks for an authenticated session.
 * <p>
 * Excludes {@code /api/auth/*} (login, register, status — no session needed).
 * All other paths require a valid {@code user} attribute in the session.
 */
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Allow preflight CORS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            UserSession user = (UserSession) session.getAttribute("user");

            // For /api/admin/** paths, require ADMIN role
            String path = request.getServletPath();
            if (path.startsWith("/api/admin/") && !"ADMIN".equals(user.getRole())) {
                log.warn("Forbidden admin access attempt: {} {} by userId={} role={}",
                        request.getMethod(), request.getRequestURI(), user.getUserId(), user.getRole());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getWriter(), Map.of(
                        "error", "Forbidden",
                        "message", "Admin access required"
                ));
                return false;
            }

            return true;
        }

        log.warn("Unauthorized access attempt: {} {}", request.getMethod(), request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getWriter(), Map.of(
                "error", "Unauthorized",
                "message", "Authentication required"
        ));
        return false;
    }
}
