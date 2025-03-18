package com.example.email.core.generator;

import java.util.List;

/**
 * Root object for the email definitions JSON.
 */
public class EmailDefinitions {
    private List<EmailDefinition> emails;

    public List<EmailDefinition> getEmails() {
        return emails;
    }

    public void setEmails(List<EmailDefinition> emails) {
        this.emails = emails;
    }
}
