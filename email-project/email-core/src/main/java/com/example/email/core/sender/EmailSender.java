package com.example.email.core.sender;

import com.example.email.core.model.Email;

/**
 * Interface for all email senders.
 * Implementations can use different mailing technologies (SMTP, API-based services, etc.)
 */
public interface EmailSender {

    /**
     * Send an email
     *
     * @param email The email to send
     * @throws EmailSendException If there is an error sending the email
     */
    void send(Email email) throws EmailSendException;

    /**
     * Exception thrown when there is an error sending an email
     */
    class EmailSendException extends Exception {
        public EmailSendException(String message) {
            super(message);
        }

        public EmailSendException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
