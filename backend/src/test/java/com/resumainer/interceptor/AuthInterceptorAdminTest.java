package com.resumainer.interceptor;

import com.resumainer.dto.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests for ADMIN role check in AuthInterceptor for /api/admin/** paths.
 */
class AuthInterceptorAdminTest {

    private AuthInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        interceptor = new AuthInterceptor();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Default: allow OPTIONS preflight
        when(request.getMethod()).thenReturn("GET");
        // Default: use getServletPath() for context-path-safe path
        when(request.getServletPath()).thenReturn("/api/admin/dashboard");
    }

    @Test
    void preHandle_adminPath_noSession_returns401() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, new Object());

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("Unauthorized"));
    }

    @Test
    void preHandle_adminPath_nonAdminSession_returns403() throws Exception {
        UserSession user = new UserSession(UUID.randomUUID(), "user@test.com", "USER");
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getSession(false)).thenReturn(session);

        boolean result = interceptor.preHandle(request, response, new Object());

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(responseWriter.toString().contains("Forbidden"));
    }

    @Test
    void preHandle_adminPath_adminSession_returnsTrue() throws Exception {
        UserSession user = new UserSession(UUID.randomUUID(), "admin@test.com", "ADMIN");
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getSession(false)).thenReturn(session);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    void preHandle_nonAdminPath_nonAdminSession_returnsTrue() throws Exception {
        when(request.getServletPath()).thenReturn("/api/resumes");
        UserSession user = new UserSession(UUID.randomUUID(), "user@test.com", "USER");
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getSession(false)).thenReturn(session);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    void preHandle_adminPath_privilegedUserWithUserRole_returns403() throws Exception {
        UserSession user = new UserSession(UUID.randomUUID(), "user@test.com", "USER", true);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getSession(false)).thenReturn(session);

        boolean result = interceptor.preHandle(request, response, new Object());

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(responseWriter.toString().contains("Forbidden"));
    }
}
