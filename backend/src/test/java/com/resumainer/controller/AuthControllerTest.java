package com.resumainer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumainer.dto.LoginRequest;
import com.resumainer.dto.RegisterRequest;
import com.resumainer.exception.GlobalExceptionHandler;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.User;
import com.resumainer.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

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

    // ============================================================
    // Register tests
    // ============================================================

    @Test
    void register_validInput_returns200() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");

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

    // ============================================================
    // Login tests
    // ============================================================

    @Test
    void login_validInput_returns200() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setRoleId(1L); // USER role
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(user);

        LoginRequest request = new LoginRequest("test@example.com", "CorrectPass1", false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.redirectUrl").value("/home"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        doThrow(new ServiceException("auth.invalidCredentials", "Invalid email or password"))
                .when(authService).authenticate(any(LoginRequest.class));

        LoginRequest request = new LoginRequest("test@example.com", "WrongPass1", false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_lockedAccount_returns423() throws Exception {
        doThrow(new ServiceException("auth.account.locked", "Too many failed attempts. Try again later."))
                .when(authService).authenticate(any(LoginRequest.class));

        LoginRequest request = new LoginRequest("locked@example.com", "WrongPass1", false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Too many failed attempts. Try again later."));
    }

    // ============================================================
    // Logout tests
    // ============================================================

    @Test
    void logout_returns200() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ============================================================
    // Status tests
    // ============================================================

    @Test
    void status_notAuthenticated_returnsFalse() throws Exception {
        // No session attribute set — user is not authenticated
        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.email").value(""))
                .andExpect(jsonPath("$.role").value(""));
    }

    @Test
    void status_authenticated_returnsTrue() throws Exception {
        mockMvc.perform(get("/api/auth/status")
                        .sessionAttr("user", new com.resumainer.dto.UserSession(
                                UUID.randomUUID(), "test@test.com", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.role").value("USER"));
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

    @Test
    void login_rememberMe_setsExtendedSession() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setRoleId(1L);
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(user);

        LoginRequest request = new LoginRequest("test@example.com", "CorrectPass1", true);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.redirectUrl").value("/home"));
    }

    @Test
    void login_adminRole_redirectsToAdmin() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@test.com");
        user.setRoleId(2L);
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(user);

        LoginRequest request = new LoginRequest("admin@test.com", "AdminPass1", false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.redirectUrl").value("/admin"));
    }

    @Test
    void login_accountBlocked_returns403() throws Exception {
        doThrow(new ServiceException("auth.account.blocked", "Account is blocked"))
                .when(authService).authenticate(any(LoginRequest.class));

        LoginRequest request = new LoginRequest("blocked@test.com", "AnyPass1", false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_defaultServiceException_returns500() throws Exception {
        doThrow(new ServiceException("auth.unknown", "Unexpected error"))
                .when(authService).authenticate(any(LoginRequest.class));

        LoginRequest request = new LoginRequest("test@example.com", "AnyPass1", false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
}
