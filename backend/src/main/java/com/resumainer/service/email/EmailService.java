package com.resumainer.service.email;

/**
 * Service interface for sending email messages.
 *
 * <p>Following KISS: single send method. Implementations handle
 * delivery via external providers (Resend, etc.) or dev-only logging.
 *
 * <p>Implementations must NEVER log the Resend API key or expose it
 * in {@code toString()}, exception messages, or any other output.
 */
public interface EmailService {

    /**
     * Send an email message.
     *
     * @param message the email to send (must not be null)
     * @throws EmailException if sending fails or is not configured for production
     */
    void send(EmailMessage message) throws EmailException;
}
