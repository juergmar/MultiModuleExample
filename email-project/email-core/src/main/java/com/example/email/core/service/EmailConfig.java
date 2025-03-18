package com.example.email.core.service;

/**
 * Interface for email configuration.
 * Contains the basic configuration needed for email services.
 */
public interface EmailConfig {
    /**
     * Get the default from address for emails
     *
     * @return The from address
     */
    String getFromAddress();

    /**
     * Get the base URL for links in emails
     *
     * @return The base URL
     */
    String getBaseUrl();
}
