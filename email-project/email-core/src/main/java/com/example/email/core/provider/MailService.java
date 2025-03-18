package com.example.email.core.provider;


import java.util.ArrayList;
import java.util.List;

/**
 * Service that manages the email sending process, including interceptor chain execution.
 * This class coordinates the interception process and delegates to the mail provider.
 */
public class MailService {

    private final List<MailInterceptor> interceptors = new ArrayList<>();
    private final MailProvider mailProvider;

    public MailService(MailProvider mailProvider) {
        this.mailProvider = mailProvider;
    }

    /**
     * Add an interceptor to the chain
     *
     * @param interceptor The interceptor to add
     * @return This service instance for chaining
     */
    public MailService addInterceptor(MailInterceptor interceptor) {
        interceptors.add(interceptor);
        return this;
    }

    /**
     * Remove an interceptor from the chain
     *
     * @param interceptor The interceptor to remove
     * @return true if the interceptor was removed
     */
    public boolean removeInterceptor(MailInterceptor interceptor) {
        return interceptors.remove(interceptor);
    }

    /**
     * Get the list of all registered interceptors
     *
     * @return An unmodifiable list of interceptors
     */
    public List<MailInterceptor> getInterceptors() {
        return List.copyOf(interceptors);
    }

    /**
     * Send an email with the given context, applying all interceptors
     *
     * @param context The email context
     * @return true if the email was sent successfully
     */
    public boolean sendMail(MailContext context) {
        boolean proceed = true;

        // Apply all interceptors before sending
        for (MailInterceptor interceptor : interceptors) {
            try {
                if (!interceptor.beforeSend(context)) {
                    proceed = false;
                    break;
                }
            } catch (Exception e) {
                proceed = false;
                break;
            }
        }

        boolean success = false;
        if (proceed) {
            try {
                mailProvider.sendMail(context);
                success = true;
            } catch (MailProvider.MailProviderException e) {
                success = false;
            }
        }

        // Apply all interceptors after sending
        for (MailInterceptor interceptor : interceptors) {
            try {
                interceptor.afterSend(context, success);
            } catch (Exception e) {
            }
        }

        return success;
    }
}
