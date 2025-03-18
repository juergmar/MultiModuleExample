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
     * Company name (used in templates)
     */
    private String companyName = "Company Name";

    /**
     * Whether to enable email sending (useful for development)
     */
    private boolean enabled = true;

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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
