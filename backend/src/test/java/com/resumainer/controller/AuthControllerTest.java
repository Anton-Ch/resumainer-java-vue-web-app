package com.resumainer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumainer.dto.RegisterRequest;
import com.resumainer.exception.GlobalExceptionHandler;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.User;
import com.resumainer.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Tests for {@link AuthController}.
 * <p>
 * Phase 4: Only registration tests remain here. Login/logout are handled by
 * Spring Security and tested in {@code JsonLoginTest}. Status is tested in
 * {@code AuthStatusTest}.
 */
class AuthControllerTest {

    private MockMvc mockMvc;
    private AuthService authService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        objectMapper = new ObjectMapper();

        AuthController controller = new AuthController(authService);

        mockMvc = standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        // Clear SecurityContext after each test to avoid cross-test contamination
        SecurityContextHolder.clearContext();
    }

    // ============================================================
    // Register tests
    // ============================================================

    @Test
    void register_validInput_returns200() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setRoleId(1L); // USER role
        user.setStatusId(1L); // ACTIVE

        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        RegisterRequest request = new RegisterRequest("test@example.com", "StrongPass1", "StrongPass1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.redirectUrl").value("/home"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest request = new RegisterRequest("existing@example.com", "StrongPass1", "StrongPass1");
        doThrow(new ServiceException("auth.email.alreadyRegistered", "Email already registered"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void register_nullUser_returns500() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(null);

        RegisterRequest request = new RegisterRequest("test@example.com", "StrongPass1", "StrongPass1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Registration failed"));
    }

    @Test
    void register_defaultServiceException_returns500() throws Exception {
        doThrow(new ServiceException("auth.unknown.error", "Unexpected error"))
                .when(authService).register(any(RegisterRequest.class));

        RegisterRequest request = new RegisterRequest("test@example.com", "StrongPass1", "StrongPass1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_weakPassword_returns400() throws Exception {
        doThrow(new ServiceException("auth.password.weak", "Password does not meet strength requirements"))
                .when(authService).register(any(RegisterRequest.class));

        RegisterRequest request = new RegisterRequest("test@example.com", "commonpassword", "commonpassword");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_passwordMismatch_returns400() throws Exception {
        doThrow(new ServiceException("auth.password.mismatch", "Passwords do not match"))
                .when(authService).register(any(RegisterRequest.class));

        RegisterRequest request = new RegisterRequest("test@example.com", "StrongPass1", "StrongPass2");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ============================================================
    // Phase 7 — Registration creates Spring Security Authentication
    // ============================================================

    private User createRegisteredUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("newuser@test.com");
        user.setPasswordHash("$2a$12$dummyhashfortestingonly1234567890");
        user.setRoleId(1L); // USER
        user.setStatusId(1L); // ACTIVE
        user.setPermissionId(1L); // ALLOWED
        user.setPrivileged(false);
        return user;
    }

    @Test
    void register_success_createsSpringSecurityAuthentication() throws Exception {
        User user = createRegisteredUser();
        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        RegisterRequest request = new RegisterRequest("newuser@test.com", "StrongPass1", "StrongPass1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify Spring Security Authentication was created in SecurityContext
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Spring Security Authentication must exist after registration");
        assertTrue(auth.isAuthenticated(), "Authentication must be marked as authenticated");
        assertEquals("newuser@test.com", auth.getName());
        assertTrue(auth.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_USER".equals(a.getAuthority())),
                "Must have ROLE_USER authority");
    }

    @Test
    void register_success_createsLegacySessionUserAttribute() throws Exception {
        User user = createRegisteredUser();
        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        RegisterRequest request = new RegisterRequest("newuser@test.com", "StrongPass1", "StrongPass1");

        var result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        var session = result.getRequest().getSession();
        assertNotNull(session, "Session must exist after registration");
        var userAttr = session.getAttribute("user");
        assertNotNull(userAttr, "Legacy session 'user' attribute must be set");
        assertInstanceOf(com.resumainer.dto.UserSession.class, userAttr);
    }
}
