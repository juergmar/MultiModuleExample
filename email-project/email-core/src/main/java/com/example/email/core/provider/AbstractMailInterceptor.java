package com.example.email.core.provider;

/**
 * Base implementation of MailInterceptor with default behavior.
 * Classes can extend this and override only the methods they need.
 */
public abstract class AbstractMailInterceptor implements MailInterceptor {
    @Override
    public boolean beforeSend(MailContext context) {
        // Default implementation allows sending
        return true;
    }

    @Override
    public void afterSend(MailContext context, boolean success) {
        // Default implementation does nothing
    }
}
