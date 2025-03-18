package com.example.email.core.provider;

/**
 * Interface for mail providers that handle the actual sending of emails.
 * Implementations can use different technologies (SMTP, API-based services, etc.)
 */
public interface MailProvider {
    /**
     * Send an email using the provided mail context
     *
     * @param context The mail context containing all information needed to send the email
     * @throws MailProviderException If there is an error sending the email
     */
    void sendMail(MailContext context) throws MailProviderException;

    /**
     * Exception thrown when there is an error sending an email
     */
    class MailProviderException extends Exception {
        public MailProviderException(String message) {
            super(message);
        }

        public MailProviderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
