package com.resumainer.config;

import com.resumainer.controller.AuthController;
import com.resumainer.dao.AuthTokenDao;
import com.resumainer.dao.UserDao;
import com.resumainer.service.AuthService;
import com.resumainer.service.ResendVerificationService;
import com.resumainer.service.VerificationService;
import com.resumainer.service.email.EmailService;
import com.resumainer.service.email.EmailTemplateService;
import com.resumainer.service.security.CaptchaService;
import com.resumainer.service.security.ResendVerificationRateLimiter;
import com.resumainer.service.security.TrustedProxyClientIpExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.sql.DataSource;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@SpringJUnitConfig
@ContextConfiguration(classes = Phase11ContextWiringTest.TestConfig.class)
@TestPropertySource(properties = {
        "app.frontend.public.base-url=http://localhost:5173",
        "app.auth.email-verification.ttl-minutes=1440",
        "app.auth.resend.cooldown-seconds=60",
        "app.auth.resend.hourly-limit=5",
        "app.auth.resend.daily-limit=20",
        "app.auth.resend.max-retained-keys=10000",
        "app.auth.resend.uniform-delay-ms=200",
        "app.auth.trusted-proxy-hosts=frontend"
})
class Phase11ContextWiringTest {

    @Autowired private AuthController authController;
    @Autowired private ResendVerificationService resendVerificationService;
    @Autowired private ResendVerificationRateLimiter rateLimiter;
    @Autowired private TrustedProxyClientIpExtractor clientIpExtractor;

    @Test
    void phase11FocusedWiringWithMockedCollaborators_starts() {
        assertNotNull(authController);
        assertNotNull(resendVerificationService);
        assertNotNull(rateLimiter);
        assertNotNull(clientIpExtractor);
    }

    @Configuration
    @Import({AuthController.class, ResendVerificationService.class,
            ResendVerificationRateLimiter.class, TrustedProxyClientIpExtractor.class})
    static class TestConfig {
        @Bean static PropertySourcesPlaceholderConfigurer placeholders() {
            return new PropertySourcesPlaceholderConfigurer();
        }
        @Bean Clock clock() { return Clock.systemUTC(); }
        @Bean AuthService authService() { return mock(AuthService.class); }
        @Bean VerificationService verificationService() { return mock(VerificationService.class); }
        @Bean CaptchaService captchaService() { return mock(CaptchaService.class); }
        @Bean UserDao userDao() { return mock(UserDao.class); }
        @Bean AuthTokenDao authTokenDao() { return mock(AuthTokenDao.class); }
        @Bean EmailTemplateService emailTemplateService() { return mock(EmailTemplateService.class); }
        @Bean EmailService emailService() { return mock(EmailService.class); }
        @Bean DataSource dataSource() { return mock(DataSource.class); }
    }
}
