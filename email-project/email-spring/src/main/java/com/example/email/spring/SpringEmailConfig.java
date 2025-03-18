package com.example.email.spring;

import com.example.email.core.service.EmailConfig;
import com.example.email.core.service.EmailTemplateService;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for email template service.
 */
@Configuration
public class SpringEmailConfig implements EmailConfig {

    private final EmailProperties emailProperties;

    public SpringEmailConfig(EmailProperties emailProperties) {
        this.emailProperties = emailProperties;
    }

    @Override
    public String getFromAddress() {
        return emailProperties.getFromAddress();
    }

    @Override
    public String getBaseUrl() {
        return emailProperties.getBaseUrl();
    }

}
