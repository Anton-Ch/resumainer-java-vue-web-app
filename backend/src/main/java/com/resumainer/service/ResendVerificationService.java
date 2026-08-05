package com.resumainer.service;

import com.resumainer.dao.AuthTokenDao;
import com.resumainer.dao.UserDao;
import com.resumainer.model.AuthToken;
import com.resumainer.service.email.EmailMessage;
import com.resumainer.service.email.EmailService;
import com.resumainer.service.email.EmailTemplateService;
import com.resumainer.service.security.CaptchaResult;
import com.resumainer.service.security.CaptchaService;
import com.resumainer.service.security.ResendVerificationRateLimiter;
import com.resumainer.util.TokenHashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

/** Coordinates CAPTCHA, rate limiting, token replacement, and email delivery. */
@Service
public class ResendVerificationService {

    private static final Logger log = LoggerFactory.getLogger(ResendVerificationService.class);
    private static final String TOKEN_TYPE = "EMAIL_VERIFICATION";

    private final CaptchaService captchaService;
    private final ResendVerificationRateLimiter rateLimiter;
    private final UserDao userDao;
    private final AuthTokenDao authTokenDao;
    private final EmailTemplateService emailTemplateService;
    private final EmailService emailService;
    private final DataSource dataSource;
    private final Clock clock;
    private final int verificationTtlMinutes;
    private final long uniformDelayMillis;

    @Autowired
    public ResendVerificationService(CaptchaService captchaService,
                                     ResendVerificationRateLimiter rateLimiter,
                                     UserDao userDao,
                                     AuthTokenDao authTokenDao,
                                     EmailTemplateService emailTemplateService,
                                     EmailService emailService,
                                     DataSource dataSource,
                                     Clock clock,
                                     @Value("${app.auth.email-verification.ttl-minutes:1440}")
                                     int verificationTtlMinutes,
                                     @Value("${app.auth.resend.uniform-delay-ms:200}")
                                     long uniformDelayMillis) {
        this.captchaService = captchaService;
        this.rateLimiter = rateLimiter;
        this.userDao = userDao;
        this.authTokenDao = authTokenDao;
        this.emailTemplateService = emailTemplateService;
        this.emailService = emailService;
        this.dataSource = dataSource;
        this.clock = clock;
        this.verificationTtlMinutes = verificationTtlMinutes;
        this.uniformDelayMillis = uniformDelayMillis;
    }

    /** Constructor for deterministic unit setup with the documented delay default. */
    public ResendVerificationService(CaptchaService captchaService,
                                     ResendVerificationRateLimiter rateLimiter,
                                     UserDao userDao,
                                     AuthTokenDao authTokenDao,
                                     EmailTemplateService emailTemplateService,
                                     EmailService emailService,
                                     DataSource dataSource,
                                     Clock clock,
                                     int verificationTtlMinutes) {
        this(captchaService, rateLimiter, userDao, authTokenDao, emailTemplateService,
                emailService, dataSource, clock, verificationTtlMinutes, 200);
    }

    /**
     * Processes a resend request in the required side-effect order.
     * Invalid CAPTCHA never consumes quota. Eligible token replacement is one
     * explicit JDBC transaction; email delivery happens only after commit.
     */
    public Result resend(String email, String captchaToken, String clientIp) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);

        CaptchaResult captcha = captchaService.verify(captchaToken);
        if (!captcha.isSuccess()) {
            return Result.captchaInvalid();
        }

        ResendVerificationRateLimiter.Decision decision = rateLimiter.reserve(normalizedEmail, clientIp);
        if (!decision.allowed()) {
            return Result.rateLimited(decision.retryAfterSeconds());
        }

        long processingStarted = System.nanoTime();

        String rawToken = null;
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            UserDao.VerificationCandidate candidate =
                    userDao.findVerificationCandidateForUpdate(normalizedEmail, conn);
            if (isEligible(candidate)) {
                rawToken = TokenHashUtil.generateRawToken();
                AuthToken token = new AuthToken(candidate.id().toString(), TOKEN_TYPE,
                        TokenHashUtil.hashToken(rawToken),
                        LocalDateTime.now(clock).plusMinutes(verificationTtlMinutes));
                authTokenDao.invalidateOldTokens(candidate.id().toString(), TOKEN_TYPE, conn);
                authTokenDao.insert(token, conn);
            }
            conn.commit();
        } catch (Exception e) {
            rollbackQuietly(conn);
            log.warn("Verification resend database processing failed");
            applyUniformDelay(processingStarted);
            return Result.success();
        } finally {
            closeQuietly(conn);
        }

        if (rawToken != null) {
            try {
                EmailMessage message = emailTemplateService.createVerificationEmail(normalizedEmail, rawToken);
                emailService.send(message);
            } catch (Exception e) {
                // Deliberately omit recipient, token, message body, and provider exception details.
                log.warn("Verification resend email delivery failed after commit");
            }
        }
        applyUniformDelay(processingStarted);
        return Result.success();
    }

    private void applyUniformDelay(long startedNanos) {
        long elapsedMillis = (System.nanoTime() - startedNanos) / 1_000_000;
        long remaining = uniformDelayMillis - elapsedMillis;
        if (remaining > 0) {
            try {
                Thread.sleep(remaining);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean isEligible(UserDao.VerificationCandidate candidate) {
        return candidate != null && !candidate.deleted() && !candidate.emailVerified()
                && candidate.statusId() != null && candidate.statusId() == 1L;
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ignored) { }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) { }
        }
    }

    public enum Status { SUCCESS, CAPTCHA_INVALID, RATE_LIMITED }

    public record Result(Status status, long retryAfterSeconds) {
        public static Result success() { return new Result(Status.SUCCESS, 0); }
        public static Result captchaInvalid() { return new Result(Status.CAPTCHA_INVALID, 0); }
        public static Result rateLimited(long seconds) { return new Result(Status.RATE_LIMITED, seconds); }
    }
}
