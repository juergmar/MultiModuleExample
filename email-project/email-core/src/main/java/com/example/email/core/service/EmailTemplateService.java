package com.example.email.core.service;

import com.example.email.core.model.Email;
import com.example.email.core.template.TemplateEngine;

import java.util.HashMap;
import java.util.Map;

public abstract class EmailTemplateService {

    private final TemplateEngine templateEngine;
    private final EmailConfig emailConfig;

    protected EmailTemplateService(TemplateEngine templateEngine, EmailConfig emailConfig) {
        this.templateEngine = templateEngine;
        this.emailConfig = emailConfig;
    }

    /**
     * Process a template with the given model
     *
     * @param templateName The template name
     * @param model The model data
     * @return The processed template content
     */
    protected String processTemplate(String templateName, Map<String, Object> model) {
        // Transform the flat dot notation model into a nested structure
        // No need for additional transformation - the model is already structured correctly
        return templateEngine.process(templateName, model);
    }


    /**
     * Create a basic email builder with from address set
     *
     * @return A new email builder
     */
    protected Email.Builder createEmailBuilder() {
        return Email.builder()
                .from(emailConfig.getFromAddress());
    }

    /**
     * Get the email configuration
     *
     * @return The email configuration
     */
    protected EmailConfig getEmailConfig() {
        return emailConfig;
    }
}
