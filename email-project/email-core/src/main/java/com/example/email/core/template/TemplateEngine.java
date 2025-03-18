package com.example.email.core.template;

import java.util.Map;

/**
 * Interface for all email template engines.
 * Implementations can use different template technologies (Thymeleaf, Freemarker, etc.)
 */
public interface TemplateEngine {

    /**
     * Process a template and produce rendered content
     *
     * @param templateName The name/path of the template to process
     * @param model The model containing data for template variables
     * @return The rendered content (typically HTML)
     */
    String process(String templateName, Map<String, Object> model);
}
