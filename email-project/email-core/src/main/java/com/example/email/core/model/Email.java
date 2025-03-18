package com.example.email.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an email to be sent.
 */
public class Email {
    private final String from;
    private final List<String> to;
    private final List<String> cc;
    private final List<String> bcc;
    private final String subject;
    private final String content;
    private final boolean html;
    private final List<Attachment> attachments;
    private final Map<String, Object> modelAttributes;

    private Email(Builder builder) {
        this.from = builder.from;
        this.to = Collections.unmodifiableList(new ArrayList<>(builder.to));
        this.cc = Collections.unmodifiableList(new ArrayList<>(builder.cc));
        this.bcc = Collections.unmodifiableList(new ArrayList<>(builder.bcc));
        this.subject = builder.subject;
        this.content = builder.content;
        this.html = builder.html;
        this.attachments = Collections.unmodifiableList(new ArrayList<>(builder.attachments));
        this.modelAttributes = Collections.unmodifiableMap(new HashMap<>(builder.modelAttributes));
    }

    public String getFrom() {
        return from;
    }

    public List<String> getTo() {
        return to;
    }

    public List<String> getCc() {
        return cc;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public boolean isHtml() {
        return html;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public Map<String, Object> getModelAttributes() {
        return modelAttributes;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for Email
     */
    public static class Builder {
        private String from;
        private final List<String> to = new ArrayList<>();
        private final List<String> cc = new ArrayList<>();
        private final List<String> bcc = new ArrayList<>();
        private String subject;
        private String content;
        private boolean html = true;
        private final List<Attachment> attachments = new ArrayList<>();
        private final Map<String, Object> modelAttributes = new HashMap<>();

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder to(String to) {
            this.to.add(to);
            return this;
        }

        public Builder to(List<String> to) {
            this.to.addAll(to);
            return this;
        }

        public Builder cc(String cc) {
            this.cc.add(cc);
            return this;
        }

        public Builder cc(List<String> cc) {
            this.cc.addAll(cc);
            return this;
        }

        public Builder bcc(String bcc) {
            this.bcc.add(bcc);
            return this;
        }

        public Builder bcc(List<String> bcc) {
            this.bcc.addAll(bcc);
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder html(boolean html) {
            this.html = html;
            return this;
        }

        public Builder attachment(Attachment attachment) {
            this.attachments.add(attachment);
            return this;
        }

        public Builder attachments(List<Attachment> attachments) {
            this.attachments.addAll(attachments);
            return this;
        }

        public Builder modelAttribute(String key, Object value) {
            this.modelAttributes.put(key, value);
            return this;
        }

        public Builder modelAttributes(Map<String, Object> attributes) {
            this.modelAttributes.putAll(attributes);
            return this;
        }

        public Email build() {
            if (from == null || from.isEmpty()) {
                throw new IllegalStateException("Email must have a from address");
            }
            if (to.isEmpty()) {
                throw new IllegalStateException("Email must have at least one recipient");
            }
            if (subject == null) {
                throw new IllegalStateException("Email must have a subject");
            }
            if (content == null) {
                throw new IllegalStateException("Email must have content");
            }
            return new Email(this);
        }
    }
}
