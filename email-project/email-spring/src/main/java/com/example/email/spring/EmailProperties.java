package com.example.email.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot configuration properties for email service.
 */
@ConfigurationProperties(prefix = "email")
public class EmailProperties {

    /**
     * Default sender email address
     */
    private String fromAddress;

    /**
     * Application base URL (used for links in emails)
     */
    private String baseUrl;

    /**
     * Whether to enable email sending (useful for development)
     */
    private boolean enabled = true;

    /**
     * Mail provider configuration
     */
    private Provider provider = new Provider();

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    /**
     * Mail provider configuration properties
     */
    public static class Provider {
        /**
         * Name of the mail provider to use.
         * If not specified, the default provider will be used.
         */
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
