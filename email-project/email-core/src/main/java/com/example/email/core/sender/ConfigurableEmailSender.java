package com.example.email.core.sender;

import com.example.email.core.model.Attachment;
import com.example.email.core.model.Email;
import com.example.email.core.provider.EmailContext;
import com.example.email.core.provider.MailAttachment;
import com.example.email.core.provider.MailProvider;
import com.example.email.core.provider.MailProviderFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of EmailSender that supports switching between
 * different mail providers at runtime.
 */
public class ConfigurableEmailSender implements EmailSender {
    private String providerName;

    /**
     * Create a ConfigurableEmailSender with the default mail provider
     */
    public ConfigurableEmailSender() {
        this.providerName = null;
    }

    /**
     * Create a ConfigurableEmailSender with a specific mail provider
     *
     * @param providerName The name of the mail provider to use
     */
    public ConfigurableEmailSender(String providerName) {
        this.providerName = providerName;
    }

    /**
     * Set the mail provider to use
     *
     * @param providerName The name of the mail provider
     */
    public void setMailProvider(String providerName) {
        if (!MailProviderFactory.hasProvider(providerName)) {
            throw new IllegalArgumentException("Unknown mail provider: " + providerName);
        }
        this.providerName = providerName;
    }

    /**
     * Get the current mail provider name
     *
     * @return The name of the current mail provider, or null if using the default
     */
    public String getMailProviderName() {
        return providerName;
    }

    /**
     * Get the mail provider to use
     *
     * @return The mail provider
     */
    protected MailProvider getMailProvider() {
        if (providerName == null) {
            return MailProviderFactory.getDefaultProvider();
        }
        return MailProviderFactory.getProvider(providerName);
    }

    @Override
    public void send(Email email) throws EmailSendException {
        try {
            // Convert Email to EmailContext
            EmailContext context = createEmailContext(email);

            // Get the mail provider and send directly
            MailProvider provider = getMailProvider();
            provider.sendMail(context);
        } catch (Exception e) {
            throw new EmailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Convert the Email model to an EmailContext
     *
     * @param email The email to convert
     * @return A new EmailContext
     */
    protected EmailContext createEmailContext(Email email) {
        EmailContext context = new EmailContext(
                email.getFrom(),
                email.getTo(),
                email.getSubject(),
                email.getContent(),
                email.isHtml()
        );

        // Add CC and BCC
        context.setCc(email.getCc());
        context.setBcc(email.getBcc());

        // Convert attachments
        List<MailAttachment> attachments = new ArrayList<>();
        for (Attachment attachment : email.getAttachments()) {
            attachments.add(convertAttachment(attachment));
        }
        context.setAttachments(attachments);

        return context;
    }

    private MailAttachment convertAttachment(Attachment attachment) {
        Attachment.AttachmentSource source = attachment.getSource();

        if (source instanceof Attachment.PathAttachmentSource) {
            return MailAttachment.fromPath(
                    ((Attachment.PathAttachmentSource) source).getPath(),
                    attachment.getName(),
                    attachment.getContentType()
            );
        } else if (source instanceof Attachment.InputStreamAttachmentSource) {
            return MailAttachment.fromInputStream(
                    ((Attachment.InputStreamAttachmentSource) source).getInputStream(),
                    attachment.getName(),
                    attachment.getContentType()
            );
        } else if (source instanceof Attachment.ByteArrayAttachmentSource) {
            return MailAttachment.fromBytes(
                    ((Attachment.ByteArrayAttachmentSource) source).getBytes(),
                    attachment.getName(),
                    attachment.getContentType()
            );
        }

        throw new IllegalArgumentException("Unsupported attachment source type: " + source.getClass().getName());
    }
}
