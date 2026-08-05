package com.resumainer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumainer.dto.ResendVerificationRequest;
import com.resumainer.service.AuthService;
import com.resumainer.service.ResendVerificationService;
import com.resumainer.service.VerificationService;
import com.resumainer.dao.AuthTokenDao;
import com.resumainer.dao.UserDao;
import com.resumainer.service.email.EmailMessage;
import com.resumainer.service.email.EmailService;
import com.resumainer.service.email.EmailTemplateService;
import com.resumainer.service.security.CaptchaResult;
import com.resumainer.service.security.CaptchaService;
import com.resumainer.service.security.ResendVerificationRateLimiter;
import com.resumainer.service.security.TrustedProxyClientIpExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import jakarta.validation.constraints.Size;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthControllerResendTest {

    private MockMvc mockMvc;
    private ResendVerificationService resendService;

    @BeforeEach
    void setUp() {
        resendService = mock(ResendVerificationService.class);
        AuthController controller = new AuthController(mock(AuthService.class),
                mock(VerificationService.class), resendService,
                new TrustedProxyClientIpExtractor("172.19.0.2"),
                "http://localhost:5173");
        mockMvc = standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
                .build();
    }

    @Test
    void resend_success_returnsCompleteGenericBodyForTrustedProxyClient() throws Exception {
        when(resendService.resend("user@example.com", "captcha", "198.51.100.8"))
                .thenReturn(ResendVerificationService.Result.success());

        mockMvc.perform(post("/api/auth/email-verification/resend")
                        .with(request -> { request.setRemoteAddr("172.19.0.2"); return request; })
                        .header("X-Real-IP", "198.51.100.8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\" User@Example.com \",\"captchaToken\":\"captcha\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "{\"success\":true,\"message\":\"If this account needs verification, "
                                + "a new verification email has been sent.\"}"));
    }

    @Test
    void resend_invalidCaptcha_returns400WithoutRetryHeader() throws Exception {
        when(resendService.resend(anyString(), any(), anyString()))
                .thenReturn(ResendVerificationService.Result.captchaInvalid());

        mockMvc.perform(post("/api/auth/email-verification/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"captchaToken\":\"bad\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Retry-After"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CAPTCHA_INVALID"));
    }

    @Test
    void resend_missingCaptchaToken_returnsCaptchaInvalid() throws Exception {
        when(resendService.resend(eq("user@example.com"), isNull(), anyString()))
                .thenReturn(ResendVerificationService.Result.captchaInvalid());

        mockMvc.perform(post("/api/auth/email-verification/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CAPTCHA_INVALID"));
    }

    @Test
    void resend_rateLimited_returns429CompleteBodyAndRetryAfter() throws Exception {
        when(resendService.resend(anyString(), anyString(), anyString()))
                .thenReturn(ResendVerificationService.Result.rateLimited(3599));

        mockMvc.perform(post("/api/auth/email-verification/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"captchaToken\":\"captcha\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "3599"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"))
                .andExpect(jsonPath("$.message").value(
                        "Too many verification email requests. Please try again later."));
    }

    @Test
    void resend_emailLongerThan255_returns400WithoutCallingServiceOrReservingResources() throws Exception {
        Size emailSize = ResendVerificationRequest.class.getDeclaredField("email").getAnnotation(Size.class);
        assertNotNull(emailSize, "resend email must have an explicit storage/limiter key boundary");
        assertEquals(255, emailSize.max());

        CaptchaService captcha = mock(CaptchaService.class);
        ResendVerificationRateLimiter limiter = mock(ResendVerificationRateLimiter.class);
        DataSource dataSource = mock(DataSource.class);
        ResendVerificationService realService = spy(new ResendVerificationService(
                captcha, limiter, mock(UserDao.class), mock(AuthTokenDao.class),
                mock(EmailTemplateService.class), mock(EmailService.class), dataSource,
                Clock.fixed(Instant.parse("2026-08-05T12:00:00Z"), ZoneOffset.UTC), 1440, 0));
        AuthController controller = new AuthController(mock(AuthService.class),
                mock(VerificationService.class), realService,
                new TrustedProxyClientIpExtractor("172.19.0.2"), "http://localhost:5173");
        MockMvc validationMockMvc = standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
                .build();
        String overlongEmail = "a@" + "abcdefghij.".repeat(24) + "com";

        validationMockMvc.perform(post("/api/auth/email-verification/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + overlongEmail + "\",\"captchaToken\":\"captcha\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString(overlongEmail))));

        verify(realService, never()).resend(anyString(), any(), anyString());
        verifyNoInteractions(captcha, limiter);
        verify(dataSource, never()).getConnection();
    }

    @Test
    void resend_realService_allAccountAndFailureOutcomes_returnExactSameHttpBody() throws Exception {
        assertExactGenericBodyFromRealService(null, false, false);

        UserDao.VerificationCandidate verified = verificationCandidate(true, false);
        assertExactGenericBodyFromRealService(verified, false, false);

        UserDao.VerificationCandidate deleted = verificationCandidate(false, true);
        assertExactGenericBodyFromRealService(deleted, false, false);

        assertExactGenericBodyFromRealService(eligibleUser(), false, false);
        assertExactGenericBodyFromRealService(eligibleUser(), true, false);
        assertExactGenericBodyFromRealService(eligibleUser(), false, true);
    }

    private void assertExactGenericBodyFromRealService(UserDao.VerificationCandidate user,
                                                       boolean persistenceFailure,
                                                       boolean providerFailure) throws Exception {
        CaptchaService captcha = mock(CaptchaService.class);
        ResendVerificationRateLimiter limiter = mock(ResendVerificationRateLimiter.class);
        UserDao users = mock(UserDao.class);
        AuthTokenDao tokens = mock(AuthTokenDao.class);
        EmailTemplateService templates = mock(EmailTemplateService.class);
        EmailService emails = mock(EmailService.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(captcha.verify("captcha")).thenReturn(CaptchaResult.success());
        when(limiter.reserve("user@example.com", "198.51.100.8"))
                .thenReturn(ResendVerificationRateLimiter.Decision.permit());
        when(dataSource.getConnection()).thenReturn(connection);
        when(users.findVerificationCandidateForUpdate("user@example.com", connection)).thenReturn(user);
        when(templates.createVerificationEmail(eq("user@example.com"), anyString()))
                .thenReturn(new EmailMessage("user@example.com", "subject", "html", "text"));
        if (persistenceFailure) {
            doThrow(new RuntimeException("write failed"))
                    .when(tokens).invalidateOldTokens(anyString(), anyString(), same(connection));
        }
        if (providerFailure) {
            doThrow(new RuntimeException("provider failed")).when(emails).send(any());
        }
        ResendVerificationService realService = new ResendVerificationService(
                captcha, limiter, users, tokens, templates, emails, dataSource,
                Clock.fixed(Instant.parse("2026-08-05T12:00:00Z"), ZoneOffset.UTC), 1440, 0);
        AuthController controller = new AuthController(mock(AuthService.class),
                mock(VerificationService.class), realService,
                new TrustedProxyClientIpExtractor("172.19.0.2"), "http://localhost:5173");
        MockMvc realMockMvc = standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
                .build();

        realMockMvc.perform(post("/api/auth/email-verification/resend")
                        .with(request -> { request.setRemoteAddr("172.19.0.2"); return request; })
                        .header("X-Real-IP", "198.51.100.8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"captchaToken\":\"captcha\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "{\"success\":true,\"message\":\"If this account needs verification, "
                                + "a new verification email has been sent.\"}"));
    }

    private static UserDao.VerificationCandidate eligibleUser() {
        return verificationCandidate(false, false);
    }

    private static UserDao.VerificationCandidate verificationCandidate(boolean verified, boolean deleted) {
        return new UserDao.VerificationCandidate(UUID.randomUUID(), 1L, verified, deleted);
    }

}
