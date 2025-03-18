package com.example.email.core.provider;

/**
 * Interface for components that can intercept and modify email processing.
 * Interceptors are called before and after email sending, allowing for
 * customization, logging, monitoring, or other cross-cutting concerns.
 */
public interface MailInterceptor {
    /**
     * Process the mail context before it is sent.
     * This method can modify the context or cancel the sending process.
     *
     * @param context The mail context that can be modified
     * @return true if the email should be sent, false to cancel sending
     */
    boolean beforeSend(MailContext context);

    /**
     * Process the mail context after the mail has been sent or sending was attempted.
     *
     * @param context The mail context (potentially modified by beforeSend)
     * @param success Whether the email was sent successfully
     */
    void afterSend(MailContext context, boolean success);
}
