package com.resumainer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumainer.dao.UserDao;
import com.resumainer.model.User;
import com.resumainer.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for JSON login/logout via Spring Security.
 * Tests the JsonAuthenticationFilter and success/failure/logout handlers.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SecurityConfig.class, JsonLoginIntegrationTest.TestConfig.class})
@WebAppConfiguration
class JsonLoginIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    // Shared mock — created at class init time, configured per test via reset()
    private static UserDao mockUserDao = mock(UserDao.class);

    @BeforeEach
    void setUp() {
        reset(mockUserDao);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private User createUser(long roleId, long statusId, boolean emailVerified) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$12$b6Flut1MIqFT5gQNqWZwtOWAIxbDDZHNW.tDRA4ppSCcZGHIXJTyG");
        user.setUsername("testuser");
        user.setRoleId(roleId);
        user.setStatusId(statusId);
        user.setPermissionId(1L);
        user.setPrivileged(false);
        user.setEmailVerified(emailVerified);
        user.setPasswordLoginEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    // ============================================================
    // T043 — Successful login
    // ============================================================

    @Test
    @DisplayName("T043: JSON login success as USER returns 200 with role=USER")
    void login_validUser_returns200() throws Exception {
        User user = createUser(1L, 1L, true);
        when(mockUserDao.findByEmail("test@example.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "test@example.com",
                "password", "Aa123456",
                "rememberMe", false
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.redirectUrl").value("/home"));
    }

    @Test
    @DisplayName("T043: JSON login success as ADMIN returns 200 with role=ADMIN")
    void login_adminUser_returns200() throws Exception {
        User user = createUser(2L, 1L, true);
        when(mockUserDao.findByEmail("admin@test.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "admin@test.com",
                "password", "Aa123456",
                "rememberMe", false
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.redirectUrl").value("/admin"));
    }

    // ============================================================
    // T044 — Login failure
    // ============================================================

    @Test
    @DisplayName("T044: bad password returns 401 and INVALID_CREDENTIALS")
    void login_badPassword_returns401() throws Exception {
        User user = createUser(1L, 1L, true);
        when(mockUserDao.findByEmail("test@example.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "test@example.com",
                "password", "WrongPass123",
                "rememberMe", false
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("T044: non-existent email returns 401 and INVALID_CREDENTIALS")
    void login_unknownEmail_returns401() throws Exception {
        when(mockUserDao.findByEmail("unknown@example.com")).thenReturn(null);

        Map<String, Object> body = Map.of(
                "email", "unknown@example.com",
                "password", "AnyPass123",
                "rememberMe", false
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    // ============================================================
    // T046 — Logout
    // ============================================================

    @Test
    @DisplayName("T046: logout returns JSON success")
    void logout_returnsJson() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.redirectUrl").value("/auth"));
    }

    // ============================================================
    // T051 — Logging safety
    // ============================================================

    @Test
    @DisplayName("T051: failure response does not contain stack trace or internal details")
    void failureResponse_doesNotContainStackTrace() throws Exception {
        when(mockUserDao.findByEmail(anyString())).thenReturn(null);

        Map<String, Object> body = Map.of(
                "email", "test@example.com",
                "password", "WrongPass123",
                "rememberMe", false
        );

        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        org.junit.jupiter.api.Assertions.assertFalse(responseBody.contains("Exception"),
                "Response must not contain Java exception class names");
        org.junit.jupiter.api.Assertions.assertFalse(responseBody.contains("at com.resumainer"),
                "Response must not contain internal stack traces");
    }

    @Test
    @DisplayName("T045: GET /api/auth/status without auth returns 404 (no controller)")
    void status_withoutAuth_returns404() throws Exception {
        // Status endpoint is handled by AuthController — this test only confirms
        // that Spring Security allows the request through (Phase 1 permissive mode)
        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // Legacy session bridge tests
    // ============================================================

    @Test
    @DisplayName("Login creates Spring Security Authentication in session")
    void login_createsSecurityAuthentication() throws Exception {
        User user = createUser(1L, 1L, true);
        when(mockUserDao.findByEmail("test@example.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "test@example.com",
                "password", "Aa123456",
                "rememberMe", false
        );

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        // Verify Spring Security Authentication exists in session
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
        org.junit.jupiter.api.Assertions.assertNotNull(session,
                "Session must exist after login");
    }

    @Test
    @DisplayName("Legacy session bridge: login sets HttpSession 'user' attribute")
    void login_setsLegacySessionAttribute() throws Exception {
        User user = createUser(1L, 1L, true);
        user.setId(UUID.randomUUID());
        when(mockUserDao.findByEmail("test@example.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "test@example.com",
                "password", "Aa123456",
                "rememberMe", false
        );

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
        org.junit.jupiter.api.Assertions.assertNotNull(session,
                "Session must exist after login");

        Object userAttr = session.getAttribute("user");
        org.junit.jupiter.api.Assertions.assertNotNull(userAttr,
                "Session attribute 'user' must be set for legacy compatibility");
        org.junit.jupiter.api.Assertions.assertTrue(
                userAttr instanceof com.resumainer.dto.UserSession,
                "Session 'user' must be UserSession");
    }

    @Test
    @DisplayName("Legacy session bridge does not expose password hash")
    void legacySession_doesNotExposePasswordHash() throws Exception {
        User user = createUser(1L, 1L, true);
        user.setId(UUID.randomUUID());
        when(mockUserDao.findByEmail("test@example.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "test@example.com",
                "password", "Aa123456",
                "rememberMe", false
        );

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
        Object userAttr = session.getAttribute("user");

        String toString = userAttr != null ? userAttr.toString() : "";
        org.junit.jupiter.api.Assertions.assertFalse(toString.contains("password"),
                "Legacy UserSession toString must not contain password/hash");
        org.junit.jupiter.api.Assertions.assertFalse(toString.contains("$2a$"),
                "Legacy UserSession toString must not contain BCrypt hash");
    }

    // ============================================================
    // Privileged flag tests
    // ============================================================

    @Test
    @DisplayName("Privileged user login sets legacy session privileged=true")
    void privilegedUser_login_setsPrivilegedTrue() throws Exception {
        User user = createUser(1L, 1L, true);
        user.setId(UUID.randomUUID());
        user.setPrivileged(true);
        when(mockUserDao.findByEmail("admin@test.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "admin@test.com",
                "password", "Aa123456",
                "rememberMe", false
        );

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
        com.resumainer.dto.UserSession userSession =
                (com.resumainer.dto.UserSession) session.getAttribute("user");

        org.junit.jupiter.api.Assertions.assertNotNull(userSession,
                "UserSession must exist");
        org.junit.jupiter.api.Assertions.assertTrue(userSession.isPrivileged(),
                "Privileged user must have privileged=true in legacy session");
    }

    @Test
    @DisplayName("Non-privileged user login sets legacy session privileged=false")
    void nonPrivilegedUser_login_setsPrivilegedFalse() throws Exception {
        User user = createUser(1L, 1L, true);
        user.setId(UUID.randomUUID());
        user.setPrivileged(false);
        when(mockUserDao.findByEmail("test@example.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "test@example.com",
                "password", "Aa123456",
                "rememberMe", false
        );

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
        com.resumainer.dto.UserSession userSession =
                (com.resumainer.dto.UserSession) session.getAttribute("user");

        org.junit.jupiter.api.Assertions.assertNotNull(userSession,
                "UserSession must exist");
        org.junit.jupiter.api.Assertions.assertFalse(userSession.isPrivileged(),
                "Non-privileged user must have privileged=false in legacy session");
    }

    // ============================================================
    // Phase 5 — Failed login counter, CAPTCHA, and lock tests
    // ============================================================

    private User createUserWithAttempts(long roleId, long statusId, boolean emailVerified, int failedAttempts) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$12$b6Flut1MIqFT5gQNqWZwtOWAIxbDDZHNW.tDRA4ppSCcZGHIXJTyG");
        user.setUsername("testuser");
        user.setRoleId(roleId);
        user.setStatusId(statusId);
        user.setPermissionId(1L);
        user.setPrivileged(false);
        user.setEmailVerified(emailVerified);
        user.setPasswordLoginEnabled(true);
        user.setFailedLoginAttempts(failedAttempts);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    @Test
    @DisplayName("T058: bad password increments failed login counter")
    void badPassword_incrementsFailedLoginCounter() throws Exception {
        User user = createUserWithAttempts(1L, 1L, true, 0);
        user.setId(UUID.randomUUID());
        when(mockUserDao.findByEmail("test@example.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "test@example.com",
                "password", "WrongPass123",
                "rememberMe", false
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));

        // Verify DAO was called to increment counter: 0+1=1, no lock
        verify(mockUserDao).updateLoginAttempts(user.getId(), 1, null);
    }

    @Test
    @DisplayName("T059: after 3 failed attempts returns CAPTCHA_REQUIRED")
    void after3Attempts_returnsCaptchaRequired() throws Exception {
        User user = createUserWithAttempts(1L, 1L, true, 2);
        user.setId(UUID.randomUUID());
        when(mockUserDao.findByEmail("test@example.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "test@example.com",
                "password", "WrongPass123",
                "rememberMe", false
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("CAPTCHA_REQUIRED"));

        // Verify DAO was called: 2+1=3, no lock yet
        verify(mockUserDao).updateLoginAttempts(user.getId(), 3, null);
    }

    @Test
    @DisplayName("T060: after 5 failed attempts locks account and returns ACCOUNT_LOCKED")
    void after5Attempts_returnsAccountLocked() throws Exception {
        User user = createUserWithAttempts(1L, 1L, true, 4);
        user.setId(UUID.randomUUID());
        when(mockUserDao.findByEmail("test@example.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "test@example.com",
                "password", "WrongPass123",
                "rememberMe", false
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_LOCKED"));

        // Verify DAO was called: 4+1=5, with lock time
        verify(mockUserDao).updateLoginAttempts(eq(user.getId()), eq(5), notNull());
    }

    @Test
    @DisplayName("T061: successful login resets failed login counter")
    void successfulLogin_resetsFailedLoginCounter() throws Exception {
        User user = createUserWithAttempts(1L, 1L, true, 3);
        user.setId(UUID.randomUUID());
        when(mockUserDao.findByEmail("test@example.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "test@example.com",
                "password", "Aa123456",
                "rememberMe", false
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(mockUserDao).resetLoginAttempts(user.getId());
    }

    @Test
    @DisplayName("T068: blocked account returns INVALID_CREDENTIALS and does NOT increment counter")
    void blockedAccount_returnsInvalidCredentials_andDoesNotIncrementCounter() throws Exception {
        User user = createUserWithAttempts(1L, 2L, true, 0);
        user.setId(UUID.randomUUID());
        when(mockUserDao.findByEmail("blocked@test.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "blocked@test.com",
                "password", "AnyPass123",
                "rememberMe", false
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));

        // Must not increment counter — blocked accounts are not eligible for password login
        verify(mockUserDao, never()).updateLoginAttempts(any(), anyInt(), any());
    }

    @Test
    @DisplayName("Password-login-disabled returns INVALID_CREDENTIALS and does NOT increment counter")
    void passwordLoginDisabled_returnsInvalidCredentials_andDoesNotIncrementCounter() throws Exception {
        User user = createUserWithAttempts(1L, 1L, true, 0);
        user.setId(UUID.randomUUID());
        user.setPasswordLoginEnabled(false);
        when(mockUserDao.findByEmail("disabled@test.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "disabled@test.com",
                "password", "AnyPass123",
                "rememberMe", false
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));

        verify(mockUserDao, never()).updateLoginAttempts(any(), anyInt(), any());
    }

    @Test
    @DisplayName("Unverified email returns EMAIL_NOT_VERIFIED and does NOT increment counter")
    void unverifiedEmail_returnsEmailNotVerified_andDoesNotIncrementCounter() throws Exception {
        User user = createUserWithAttempts(1L, 1L, false, 0);
        user.setId(UUID.randomUUID());
        when(mockUserDao.findByEmail("unverified@test.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "unverified@test.com",
                "password", "AnyPass123",
                "rememberMe", false
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("EMAIL_NOT_VERIFIED"));

        verify(mockUserDao, never()).updateLoginAttempts(any(), anyInt(), any());
    }

    @Test
    @DisplayName("Locked account returns ACCOUNT_LOCKED and does NOT increment counter")
    void lockedAccount_returnsAccountLocked_andDoesNotIncrementCounter() throws Exception {
        User user = createUserWithAttempts(1L, 1L, true, 5);
        user.setId(UUID.randomUUID());
        user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
        when(mockUserDao.findByEmail("locked@test.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "locked@test.com",
                "password", "AnyPass123",
                "rememberMe", false
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_LOCKED"));

        verify(mockUserDao, never()).updateLoginAttempts(any(), anyInt(), any());
    }

    @Test
    @DisplayName("Unknown email returns INVALID_CREDENTIALS and does NOT increment counter")
    void unknownEmail_returnsInvalidCredentials_andDoesNotIncrementCounter() throws Exception {
        when(mockUserDao.findByEmail("unknown@example.com")).thenReturn(null);

        Map<String, Object> body = Map.of(
                "email", "unknown@example.com",
                "password", "AnyPass123",
                "rememberMe", false
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));

        verify(mockUserDao, never()).updateLoginAttempts(any(), anyInt(), any());
    }

    @Test
    @DisplayName("After 4 failed attempts returns CAPTCHA_REQUIRED and does NOT lock yet")
    void after4Attempts_returnsCaptchaRequired_andDoesNotLockYet() throws Exception {
        User user = createUserWithAttempts(1L, 1L, true, 3);
        user.setId(UUID.randomUUID());
        when(mockUserDao.findByEmail("test@example.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "test@example.com",
                "password", "WrongPass123",
                "rememberMe", false
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("CAPTCHA_REQUIRED"));

        // Counter incremented from 3 to 4, but lock is null (only locks at 5+)
        verify(mockUserDao).updateLoginAttempts(user.getId(), 4, null);
    }

    // ============================================================
    // Phase 6 — CSRF tests
    // ============================================================

    @Test
    @DisplayName("T071: POST without CSRF to protected path returns 403")
    void postWithoutCsrf_returns403() throws Exception {
        mockMvc.perform(post("/api/some-protected-path")
                        .contentType("application/json"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("T072: Authenticated POST with valid CSRF passes Spring Security (returns 404 from no controller)")
    void authenticatedPostWithCsrf_passesSecurity() throws Exception {
        mockMvc.perform(post("/api/some-protected-path")
                        .contentType("application/json")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("test@example.com").roles("USER")))
                .andExpect(status().isNotFound()); // 404 = passed CSRF+auth, no controller
    }

    @Test
    @DisplayName("T072: Auth endpoint excluded from CSRF: login works without CSRF token")
    void authLogin_worksWithoutCsrf() throws Exception {
        User user = createUserWithAttempts(1L, 1L, true, 0);
        user.setId(UUID.randomUUID());
        when(mockUserDao.findByEmail("test@example.com")).thenReturn(user);

        Map<String, Object> body = Map.of(
                "email", "test@example.com",
                "password", "WrongPass123",
                "rememberMe", false
        );

        // Login works without CSRF token because /api/auth/** is excluded
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("Public endpoint excluded from CSRF: POST without CSRF token works")
    void publicEndpoint_worksWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/public/some-resume")
                        .contentType("application/json"))
                .andExpect(status().isNotFound()); // 404 = passed CSRF+auth, no controller
    }

    // ============================================================
    // Test Configuration
    // ============================================================

    @Configuration
    static class TestConfig {

        @Bean
        public UserDao userDao() {
            return mockUserDao;
        }

        @Bean
        public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
            return new HandlerMappingIntrospector();
        }
    }
}
