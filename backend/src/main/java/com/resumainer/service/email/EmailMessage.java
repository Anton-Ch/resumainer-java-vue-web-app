package com.resumainer.service.email;

import java.util.Objects;

/**
 * Immutable value object representing an email message to be sent.
 *
 * <p>Carries recipient, subject, and both HTML and plain-text body variants.
 * Following KISS: simple data carrier with no behaviour.
 */
public final class EmailMessage {

    private final String to;
    private final String subject;
    private final String htmlBody;
    private final String textBody;

    /**
     * @param to       recipient email address (must not be null or blank)
     * @param subject  email subject (must not be null or blank)
     * @param htmlBody HTML body content (must not be null)
     * @param textBody plain-text body content (must not be null)
     */
    public EmailMessage(String to, String subject, String htmlBody, String textBody) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("to must not be null or blank");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be null or blank");
        }
        this.to = to;
        this.subject = subject;
        this.htmlBody = Objects.requireNonNull(htmlBody, "htmlBody must not be null");
        this.textBody = Objects.requireNonNull(textBody, "textBody must not be null");
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public String getTextBody() {
        return textBody;
    }

    /**
     * Safe toString — does not expose body content, only metadata.
     */
    @Override
    public String toString() {
        return "EmailMessage{to='" + to + "', subject='" + subject
                + "', htmlLength=" + htmlBody.length()
                + ", textLength=" + textBody.length() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailMessage that)) return false;
        return to.equals(that.to)
                && subject.equals(that.subject)
                && htmlBody.equals(that.htmlBody)
                && textBody.equals(that.textBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(to, subject, htmlBody, textBody);
    }
}
