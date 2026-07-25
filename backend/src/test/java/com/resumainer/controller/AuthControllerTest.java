package com.resumainer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumainer.dto.RegisterRequest;
import com.resumainer.exception.GlobalExceptionHandler;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.User;
import com.resumainer.service.AuthService;
import com.resumainer.service.VerificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthControllerTest {

    private MockMvc mockMvc;
    private AuthService authService;
    private VerificationService verificationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        authService = mock(AuthService.class);
        verificationService = mock(VerificationService.class);
        objectMapper = new ObjectMapper();

        AuthController controller = new AuthController(authService, verificationService, "http://localhost:5173");

        mockMvc = standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ============================================================
    // Registration — success
    // ============================================================

    @Test
    void register_validInput_returns201WithPendingVerification() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("newuser@test.com");
        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        var result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("newuser@test.com", "StrongPass1", "StrongPass1", "valid-captcha"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("REGISTRATION_PENDING_EMAIL_VERIFICATION"))
                .andExpect(jsonPath("$.redirectUrl").value("/auth/check-email"))
                .andReturn();

        // BLOCKER 1: No SecurityContext or session attributes
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertTrue(auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal()));

        var session = result.getRequest().getSession(false);
        if (session != null) {
            assertNull(session.getAttribute(
                    org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
            assertNull(session.getAttribute("user"));
        }
    }

    // ============================================================
    // BLOCKER 1 — @Valid validation failures → AuthResponse format
    // ============================================================

    @Test
    void register_blankEmail_returns400WithInvalidInput() throws Exception {
        String body = "{\"email\":\"\",\"password\":\"StrongPass1\",\"passwordConfirmation\":\"StrongPass1\",\"captchaToken\":\"x\"}";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void register_invalidEmail_returns400WithInvalidInput() throws Exception {
        String body = "{\"email\":\"not-an-email\",\"password\":\"StrongPass1\",\"passwordConfirmation\":\"StrongPass1\",\"captchaToken\":\"x\"}";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void register_blankPassword_returns400WithInvalidInput() throws Exception {
        String body = "{\"email\":\"a@b.com\",\"password\":\"\",\"passwordConfirmation\":\"\",\"captchaToken\":\"x\"}";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void register_shortPassword_returns400WithInvalidInput() throws Exception {
        // password shorter than 8 chars triggers @Size(min=8) validation
        String body = "{\"email\":\"a@b.com\",\"password\":\"Ab1\",\"passwordConfirmation\":\"Ab1\",\"captchaToken\":\"x\"}";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void register_blankPasswordConfirmation_returns400WithInvalidInput() throws Exception {
        String body = "{\"email\":\"a@b.com\",\"password\":\"StrongPass1\",\"passwordConfirmation\":\"\",\"captchaToken\":\"x\"}";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void register_missingCaptchaTokenField_returns400WithCaptchaInvalid() throws Exception {
        // captchaToken field is missing from JSON — ServiceException from AuthService
        doThrow(new ServiceException("auth.captcha.invalid", "CAPTCHA verification failed."))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"password\":\"StrongPass1\",\"passwordConfirmation\":\"StrongPass1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CAPTCHA_INVALID"));
    }

    // ============================================================
    // Registration — service errors → public codes (BLOCKER 1+2)
    // ============================================================

    @Test
    void register_invalidCaptcha_returnsCaptchaInvalidCode() throws Exception {
        doThrow(new ServiceException("auth.captcha.invalid", "int"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("t@t.com", "StrongPass1", "StrongPass1", "bad"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CAPTCHA_INVALID"))
                .andExpect(jsonPath("$.message").value("CAPTCHA verification failed. Please try again."));
    }

    @Test
    void register_weakPassword_returnsInvalidInputCode() throws Exception {
        doThrow(new ServiceException("auth.password.weak", "int"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("t@t.com", "WeakPass1", "WeakPass1", "c"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void register_passwordMismatch_returnsInvalidInputCode() throws Exception {
        doThrow(new ServiceException("auth.password.mismatch", "int"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("t@t.com", "StrongPass1", "StrongPass2", "c"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    // ============================================================
    // BLOCKER 2 — Duplicate paths produce identical full bodies
    // ============================================================

    @Test
    void register_duplicateEmail_preCheckAndDbRace_returnSameBody() throws Exception {
        // Pre-check duplicate
        doThrow(new ServiceException("auth.email.alreadyRegistered", "internal msg"))
                .when(authService).register(any(RegisterRequest.class));

        String body1 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("dup@t.com", "StrongPass1", "StrongPass1", "c"))))
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString();

        // DB race duplicate
        doThrow(new ServiceException("auth.email.duplicate", "internal race msg"))
                .when(authService).register(any(RegisterRequest.class));

        String body2 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("dup@t.com", "StrongPass1", "StrongPass1", "c"))))
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString();

        assertEquals(body1, body2, "Duplicate pre-check and DB race must return identical full response bodies");
    }

    @Test
    void register_duplicateEmail_returns409WithDuplicateCode() throws Exception {
        doThrow(new ServiceException("auth.email.alreadyRegistered", "int"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("existing@test.com", "StrongPass1", "StrongPass1", "c"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"))
                .andExpect(jsonPath("$.message").value("An account with this email already exists."))
                .andExpect(jsonPath("$.role").doesNotExist())
                .andExpect(jsonPath("$.redirectUrl").doesNotExist());
    }

    // ============================================================
    // BLOCKER 2 — Generic/ unexpected failure
    // ============================================================

    @Test
    void register_unexpectedException_returns500WithSafeFallback() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenThrow(new RuntimeException("unexpected DB crash"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("t@t.com", "StrongPass1", "StrongPass1", "c"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("REGISTRATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Registration failed. Please try again."));
    }

    @Test
    void register_serviceExceptionWithUnknownCode_returns500WithSafeFallback() throws Exception {
        doThrow(new ServiceException("auth.role.notFound", "Default role not found"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("t@t.com", "StrongPass1", "StrongPass1", "c"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("REGISTRATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Registration failed. Please try again."))
                .andExpect(jsonPath("$.role").doesNotExist());
    }

    // ============================================================
    // Verify email endpoint
    // ============================================================

    @Test
    void verifyEmail_validToken_redirectsToSuccess() throws Exception {
        when(verificationService.verify(anyString())).thenReturn(VerificationService.VerifyResult.SUCCESS);

        mockMvc.perform(get("/api/auth/verify-email").param("token", "raw-token"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "http://localhost:5173/app/auth/verified?status=success"));
    }

    @Test
    void verifyEmail_expiredToken_redirectsToExpired() throws Exception {
        when(verificationService.verify(anyString())).thenReturn(VerificationService.VerifyResult.TOKEN_EXPIRED);

        mockMvc.perform(get("/api/auth/verify-email").param("token", "expired"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "http://localhost:5173/app/auth/verified?status=expired"));
    }

    @Test
    void verifyEmail_invalidToken_redirectsToInvalid() throws Exception {
        when(verificationService.verify(anyString())).thenReturn(VerificationService.VerifyResult.TOKEN_INVALID);

        mockMvc.perform(get("/api/auth/verify-email").param("token", "bad"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "http://localhost:5173/app/auth/verified?status=invalid"));
    }

    @Test
    void verifyEmail_missingTokenParam_returnsInvalid() throws Exception {
        when(verificationService.verify(any())).thenReturn(VerificationService.VerifyResult.TOKEN_INVALID);

        mockMvc.perform(get("/api/auth/verify-email"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "http://localhost:5173/app/auth/verified?status=invalid"));
    }

    @Test
    void verifyEmail_blankToken_returnsInvalid() throws Exception {
        when(verificationService.verify("")).thenReturn(VerificationService.VerifyResult.TOKEN_INVALID);

        mockMvc.perform(get("/api/auth/verify-email").param("token", ""))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "http://localhost:5173/app/auth/verified?status=invalid"));
    }

    @Test
    void register_validationLog_doesNotContainPlaintextPassword() throws Exception {
        // Capture log events from AuthController logger
        Logger authControllerLogger = (Logger) LoggerFactory.getLogger(AuthController.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        authControllerLogger.addAppender(listAppender);

        try {
            // Submit a registration with distinctive plaintext password
            String distinctivePassword = "pL41nt3xt-s3cr3t-p4ss-12345!";
            String body = "{\"email\":\"\",\"password\":\"" + distinctivePassword
                    + "\",\"passwordConfirmation\":\"" + distinctivePassword
                    + "\",\"captchaToken\":\"x\"}";

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            // Assert the password does not appear in any log event
            for (ILoggingEvent event : listAppender.list) {
                String formatted = event.getFormattedMessage();
                assertFalse(formatted.contains(distinctivePassword),
                        "Validation log must not contain plaintext password. Found in: " + formatted);
                assertFalse(formatted.contains("pL41nt3xt"),
                        "Log must not contain password fragments");
            }
        } finally {
            authControllerLogger.detachAppender(listAppender);
        }
    }

    @Test
    void register_malformedJson_returns400WithInvalidInput() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{this is not valid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.role").doesNotExist());
    }

    @Test
    void register_emptyBody_returns400WithInvalidInput() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.role").doesNotExist());
    }
}
