package com.example.email.core.generator;

import java.util.List;

/**
 * Represents a single email definition from the JSON schema.
 */
public class EmailDefinition {
    private String identifier;
    private String subject;
    private String templateText;
    private List<VariableDefinition> variables;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTemplateText() {
        return templateText;
    }

    public void setTemplateText(String templateText) {
        this.templateText = templateText;
    }

    public List<VariableDefinition> getVariables() {
        return variables;
    }

    public void setVariables(List<VariableDefinition> variables) {
        this.variables = variables;
    }

    /**
     * Get the method name for this email template.
     *
     * @return The method name
     */
    public String getMethodName() {
        return "create" + capitalize(identifier) + "Email";
    }

    /**
     * Get the full render method name for this email template.
     *
     * @return The render method name
     */
    public String getRenderMethodName() {
        return "render" + capitalize(identifier) + "Email";
    }

    /**
     * Get the template path for this email.
     *
     * @return The template path
     */
    public String getTemplatePath() {
        return "email/" + identifier;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
