package com.example.email.core.service;

import com.example.email.core.model.Email;
import com.example.email.core.template.TemplateEngine;

import java.util.Map;

public abstract class EmailTemplateService {

    private final TemplateEngine templateEngine;
    private final EmailConfig emailConfig;

    protected EmailTemplateService(TemplateEngine templateEngine, EmailConfig emailConfig) {
        this.templateEngine = templateEngine;
        this.emailConfig = emailConfig;
    }

    protected String processTemplate(String templateName, Map<String, Object> model) {
        // Add company name to all templates if available
        if (emailConfig.getCompanyName() != null) {
            model.put("companyName", emailConfig.getCompanyName());
        }
        return templateEngine.process(templateName, model);
    }

    protected Email.Builder createEmailBuilder() {
        return Email.builder()
                .from(emailConfig.getFromAddress());
    }

    public interface EmailConfig {
        String getFromAddress();
        String getBaseUrl();
        String getCompanyName();
    }
}
