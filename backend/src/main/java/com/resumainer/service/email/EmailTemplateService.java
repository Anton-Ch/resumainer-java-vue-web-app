package com.resumainer.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service for building bilingual email templates (EN then RU in a single email).
 *
 * <p>Generates HTML and plain-text bodies for email verification and
 * password reset emails. Each email contains English content first,
 * followed by Russian content, separated by a clear divider.
 *
 * <p>URLs are built deterministically from configured base URLs:
 * <ul>
 *   <li>Verification: {@code {backendBaseUrl}/api/auth/verify-email?token={encodedToken}}</li>
 *   <li>Password reset: {@code {frontendBaseUrl}/app/auth/reset-password?token={encodedToken}}</li>
 * </ul>
 *
 * <p>Following KISS: simple string-based templates with no template engine
 * dependency. HTML emails use inline styles for basic formatting.
 */
@Service
public class EmailTemplateService {

    private static final String BRAND = "ResumAIner";

    private final String frontendBaseUrl;
    private final String backendBaseUrl;

    public EmailTemplateService(
            @Value("${app.frontend.public.base-url}") String frontendBaseUrl,
            @Value("${app.backend.public.base-url}") String backendBaseUrl) {
        // Normalise trailing slashes so URLs never contain accidental //
        this.frontendBaseUrl = normaliseBaseUrl(frontendBaseUrl);
        this.backendBaseUrl = normaliseBaseUrl(backendBaseUrl);
    }

    /**
     * Create a bilingual email verification message (EN then RU).
     *
     * @param to       recipient email address
     * @param rawToken the raw verification token (will be URL-encoded in the link)
     * @return populated {@link EmailMessage}
     */
    public EmailMessage createVerificationEmail(String to, String rawToken) {
        String encodedToken = URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        String verifyLink = backendBaseUrl + "/api/auth/verify-email?token=" + encodedToken;

        String subject = "Verify your email / Подтвердите email — " + BRAND;

        String htmlBody = buildBilingualHtml(
                /* en */ "<h2>Welcome to " + BRAND + "!</h2>"
                        + "<p>Thank you for registering. Please verify your email address "
                        + "by clicking the link below:</p>"
                        + "<p><a href=\"" + escapeHtml(verifyLink) + "\">"
                        + escapeHtml(verifyLink) + "</a></p>"
                        + "<p>If you did not sign up for " + BRAND
                        + ", please ignore this email.</p>",
                /* ru */ "<h2>Добро пожаловать в " + BRAND + "!</h2>"
                        + "<p>Спасибо за регистрацию. Пожалуйста, подтвердите ваш email, "
                        + "перейдя по ссылке:</p>"
                        + "<p><a href=\"" + escapeHtml(verifyLink) + "\">"
                        + escapeHtml(verifyLink) + "</a></p>"
                        + "<p>Если вы не регистрировались в " + BRAND
                        + ", просто проигнорируйте это письмо.</p>"
        );
        String textBody = buildBilingualText(
                /* en */ "Welcome to " + BRAND + "!\n\n"
                        + "Thank you for registering. Please verify your email address "
                        + "by clicking the link:\n\n" + verifyLink + "\n\n"
                        + "If you did not sign up for " + BRAND
                        + ", please ignore this email.",
                /* ru */ "Добро пожаловать в " + BRAND + "!\n\n"
                        + "Спасибо за регистрацию. Пожалуйста, подтвердите ваш email, "
                        + "перейдя по ссылке:\n\n" + verifyLink + "\n\n"
                        + "Если вы не регистрировались в " + BRAND
                        + ", просто проигнорируйте это письмо."
        );

        return new EmailMessage(to, subject, htmlBody, textBody);
    }

    /**
     * Create a bilingual password reset email message (EN then RU).
     *
     * @param to       recipient email address
     * @param rawToken the raw reset token (will be URL-encoded in the link)
     * @return populated {@link EmailMessage}
     */
    public EmailMessage createPasswordResetEmail(String to, String rawToken) {
        String encodedToken = URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        String resetLink = frontendBaseUrl + "/app/auth/reset-password?token=" + encodedToken;

        String subject = "Reset your password / Восстановление пароля — " + BRAND;

        String htmlBody = buildBilingualHtml(
                /* en */ "<h2>Reset your password</h2>"
                        + "<p>You have requested to reset your password for " + BRAND
                        + ". Click the link below to set a new password:</p>"
                        + "<p><a href=\"" + escapeHtml(resetLink) + "\">"
                        + escapeHtml(resetLink) + "</a></p>"
                        + "<p>This link is valid for 15 minutes.</p>"
                        + "<p>If you did not request a password reset, "
                        + "please ignore this email.</p>",
                /* ru */ "<h2>Восстановление пароля</h2>"
                        + "<p>Вы запросили восстановление пароля для " + BRAND
                        + ". Перейдите по ссылке, чтобы задать новый пароль:</p>"
                        + "<p><a href=\"" + escapeHtml(resetLink) + "\">"
                        + escapeHtml(resetLink) + "</a></p>"
                        + "<p>Ссылка действительна в течение 15 минут.</p>"
                        + "<p>Если вы не запрашивали восстановление пароля, "
                        + "просто проигнорируйте это письмо.</p>"
        );
        String textBody = buildBilingualText(
                /* en */ "Reset your password\n\n"
                        + "You have requested to reset your password for " + BRAND
                        + ". Click the link below to set a new password:\n\n"
                        + resetLink + "\n\n"
                        + "This link is valid for 15 minutes.\n\n"
                        + "If you did not request a password reset, "
                        + "please ignore this email.",
                /* ru */ "Восстановление пароля\n\n"
                        + "Вы запросили восстановление пароля для " + BRAND
                        + ". Перейдите по ссылке, чтобы задать новый пароль:\n\n"
                        + resetLink + "\n\n"
                        + "Ссылка действительна в течение 15 минут.\n\n"
                        + "Если вы не запрашивали восстановление пароля, "
                        + "просто проигнорируйте это письмо."
        );

        return new EmailMessage(to, subject, htmlBody, textBody);
    }

    // ============================================================
    // Bilingual composition helpers
    // ============================================================

    /**
     * Builds a single HTML body with EN content first, RU content second.
     */
    private static String buildBilingualHtml(String enContent, String ruContent) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                + "<title>" + BRAND + "</title></head><body>"
                + "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;\">"
                + "<div lang=\"en\">" + enContent + "</div>"
                + "<hr style=\"margin: 24px 0; border: none; border-top: 1px solid #ccc;\">"
                + "<div lang=\"ru\">" + ruContent + "</div>"
                + "<hr><p style=\"color: #666; font-size: 12px; text-align: center;\">"
                + "This is an automated message, please do not reply. / "
                + "Это автоматическое письмо, пожалуйста, не отвечайте на него.</p>"
                + "</div></body></html>";
    }

    /**
     * Builds a single plain-text body with EN content first, RU content second.
     */
    private static String buildBilingualText(String enContent, String ruContent) {
        return enContent
                + "\n\n---\n\n"
                + ruContent
                + "\n\n---\n\n"
                + "This is an automated message, please do not reply. / "
                + "Это автоматическое письмо, пожалуйста, не отвечайте на него.";
    }

    /**
     * Removes trailing slash from a base URL to prevent double-slash when
     * appending path segments.
     */
    private static String normaliseBaseUrl(String url) {
        if (url == null || url.isEmpty()) return url;
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * Minimal HTML escaping for user-supplied content in emails.
     */
    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
