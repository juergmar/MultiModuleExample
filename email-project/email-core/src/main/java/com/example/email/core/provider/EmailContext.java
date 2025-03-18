package com.example.email.core.provider;

import java.util.ArrayList;
import java.util.List;

/**
 * Specialized MailContext for email operations.
 * Contains standard email fields and provides typed accessors.
 */
public class EmailContext extends MailContext {
    // Standard keys for email fields
    public static final String FROM = "from";
    public static final String TO = "to";
    public static final String CC = "cc";
    public static final String BCC = "bcc";
    public static final String SUBJECT = "subject";
    public static final String CONTENT = "content";
    public static final String IS_HTML = "isHtml";
    public static final String ATTACHMENTS = "attachments";

    /**
     * Create a new EmailContext with the required fields
     *
     * @param from The sender email address
     * @param to List of recipient email addresses
     * @param subject Email subject
     * @param content Email content
     * @param isHtml Whether the content is HTML
     */
    public EmailContext(String from, List<String> to, String subject, String content, boolean isHtml) {
        setAttribute(FROM, from);
        setAttribute(TO, new ArrayList<>(to)); // Create a new list to avoid external modification
        setAttribute(SUBJECT, subject);
        setAttribute(CONTENT, content);
        setAttribute(IS_HTML, isHtml);
        setAttribute(CC, new ArrayList<String>());
        setAttribute(BCC, new ArrayList<String>());
        setAttribute(ATTACHMENTS, new ArrayList<MailAttachment>());
    }

    // Convenience getters and setters

    public String getFrom() {
        return (String) getAttribute(FROM);
    }

    public EmailContext setFrom(String from) {
        setAttribute(FROM, from);
        return this;
    }

    @SuppressWarnings("unchecked")
    public List<String> getTo() {
        return (List<String>) getAttribute(TO);
    }

    public EmailContext setTo(List<String> to) {
        setAttribute(TO, new ArrayList<>(to));
        return this;
    }

    @SuppressWarnings("unchecked")
    public List<String> getCc() {
        return (List<String>) getAttribute(CC);
    }

    public EmailContext setCc(List<String> cc) {
        setAttribute(CC, new ArrayList<>(cc));
        return this;
    }

    public EmailContext addCc(String cc) {
        getCc().add(cc);
        return this;
    }

    @SuppressWarnings("unchecked")
    public List<String> getBcc() {
        return (List<String>) getAttribute(BCC);
    }

    public EmailContext setBcc(List<String> bcc) {
        setAttribute(BCC, new ArrayList<>(bcc));
        return this;
    }

    public EmailContext addBcc(String bcc) {
        getBcc().add(bcc);
        return this;
    }

    public String getSubject() {
        return (String) getAttribute(SUBJECT);
    }

    public EmailContext setSubject(String subject) {
        setAttribute(SUBJECT, subject);
        return this;
    }

    public String getContent() {
        return (String) getAttribute(CONTENT);
    }

    public EmailContext setContent(String content) {
        setAttribute(CONTENT, content);
        return this;
    }

    public boolean isHtml() {
        return (boolean) getAttribute(IS_HTML);
    }

    public EmailContext setHtml(boolean isHtml) {
        setAttribute(IS_HTML, isHtml);
        return this;
    }

    @SuppressWarnings("unchecked")
    public List<MailAttachment> getAttachments() {
        return (List<MailAttachment>) getAttribute(ATTACHMENTS);
    }

    public EmailContext setAttachments(List<MailAttachment> attachments) {
        setAttribute(ATTACHMENTS, new ArrayList<>(attachments));
        return this;
    }

    public EmailContext addAttachment(MailAttachment attachment) {
        getAttachments().add(attachment);
        return this;
    }
}
