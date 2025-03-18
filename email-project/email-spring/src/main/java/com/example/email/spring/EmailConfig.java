package com.example.email.spring;

import com.example.email.core.service.EmailTemplateService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for email template service.
 */
@Configuration
public class EmailConfig implements EmailTemplateService.EmailConfig {

    private final EmailProperties emailProperties;

    public EmailConfig(EmailProperties emailProperties) {
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

    @Override
    public String getCompanyName() {
        return emailProperties.getCompanyName();
    }
}
