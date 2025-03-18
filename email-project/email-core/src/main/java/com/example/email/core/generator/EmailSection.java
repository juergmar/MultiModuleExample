package com.example.email.core.generator;

/**
 * Represents a section in an email template.
 * Sections can be defined in the base layout and replaced with content from email definitions.
 */
public class EmailSection {
    private String name;
    private String content;

    public EmailSection(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "EmailSection{" +
                "name='" + name + '\'' +
                ", content length=" + (content != null ? content.length() : 0) +
                '}';
    }
}
