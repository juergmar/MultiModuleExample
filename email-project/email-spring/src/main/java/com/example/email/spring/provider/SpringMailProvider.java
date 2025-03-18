package com.example.email.spring.provider;

import com.example.email.core.provider.EmailContext;
import com.example.email.core.provider.MailAttachment;
import com.example.email.core.provider.MailContext;
import com.example.email.core.provider.MailInterceptor;
import com.example.email.core.provider.MailProvider;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Spring implementation of the MailProvider interface.
 * Uses JavaMailSender to send emails.
 */
public class SpringMailProvider implements MailProvider {
    private static final Logger logger = LoggerFactory.getLogger(SpringMailProvider.class);

    private final JavaMailSender javaMailSender;
    private final boolean enabled;
    private final List<MailInterceptor> interceptors = new ArrayList<>();

    public SpringMailProvider(JavaMailSender javaMailSender, boolean enabled) {
        this.javaMailSender = javaMailSender;
        this.enabled = enabled;
    }

    /**
     * Create a SpringMailProvider with a list of interceptors
     *
     * @param javaMailSender The JavaMailSender to use
     * @param enabled Whether email sending is enabled
     * @param interceptors List of mail interceptors to apply
     */
    public SpringMailProvider(JavaMailSender javaMailSender, boolean enabled, List<MailInterceptor> interceptors) {
        this.javaMailSender = javaMailSender;
        this.enabled = enabled;
        if (interceptors != null) {
            this.interceptors.addAll(interceptors);
        }
    }

    /**
     * Add an interceptor to this provider
     *
     * @param interceptor The interceptor to add
     * @return This provider for chaining
     */
    public SpringMailProvider addInterceptor(MailInterceptor interceptor) {
        if (interceptor != null) {
            this.interceptors.add(interceptor);
        }
        return this;
    }

    /**
     * Add multiple interceptors to this provider
     *
     * @param interceptors The interceptors to add
     * @return This provider for chaining
     */
    public SpringMailProvider addInterceptors(List<MailInterceptor> interceptors) {
        if (interceptors != null) {
            this.interceptors.addAll(interceptors);
        }
        return this;
    }

    /**
     * Get all registered interceptors
     *
     * @return Unmodifiable list of interceptors
     */
    public List<MailInterceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

    @Override
    public void sendMail(MailContext context) throws MailProviderException {
        if (!(context instanceof EmailContext)) {
            throw new MailProviderException("SpringMailProvider requires an EmailContext");
        }

        EmailContext emailContext = (EmailContext) context;

        // Execute before send interceptors
        boolean proceed = true;
        for (MailInterceptor interceptor : interceptors) {
            try {
                if (!interceptor.beforeSend(context)) {
                    proceed = false;
                    break;
                }
            } catch (Exception e) {
                logger.error("Error in mail interceptor: " + e.getMessage(), e);
                proceed = false;
                break;
            }
        }

        if (!proceed) {
            logger.info("Email sending was cancelled by an interceptor");
            // Notify interceptors about cancellation
            notifyAfterSend(context, false);
            return;
        }

        if (!enabled) {
            logger.info("Email sending is disabled. Would have sent email with subject: {}", emailContext.getSubject());
            // Notify interceptors about disabled sending
            notifyAfterSend(context, false);
            return;
        }

        if (javaMailSender == null) {
            logger.warn("JavaMailSender is not configured. Cannot send email: {}", emailContext.getSubject());
            // Notify interceptors about failure
            notifyAfterSend(context, false);
            return;
        }

        boolean success = false;
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            boolean hasAttachments = !emailContext.getAttachments().isEmpty();
            MimeMessageHelper helper = new MimeMessageHelper(message, hasAttachments, "UTF-8");

            helper.setFrom(emailContext.getFrom());
            helper.setTo(emailContext.getTo().toArray(new String[0]));

            List<String> cc = emailContext.getCc();
            if (!cc.isEmpty()) {
                helper.setCc(cc.toArray(new String[0]));
            }

            List<String> bcc = emailContext.getBcc();
            if (!bcc.isEmpty()) {
                helper.setBcc(bcc.toArray(new String[0]));
            }

            helper.setSubject(emailContext.getSubject());
            helper.setText(emailContext.getContent(), emailContext.isHtml());

            // Add attachments if any
            if (hasAttachments) {
                for (MailAttachment attachment : emailContext.getAttachments()) {
                    addAttachment(helper, attachment);
                }
            }

            javaMailSender.send(message);
            logger.info("Email sent to {} with subject: {}", String.join(", ", emailContext.getTo()), emailContext.getSubject());
            success = true;
        } catch (MessagingException e) {
            logger.error("Failed to send email: " + e.getMessage(), e);
            throw new MailProviderException("Failed to send email: " + e.getMessage(), e);
        } finally {
            // Always notify interceptors about the result
            notifyAfterSend(context, success);
        }
    }

    /**
     * Notify all interceptors after sending attempt
     *
     * @param context The mail context
     * @param success Whether sending was successful
     */
    private void notifyAfterSend(MailContext context, boolean success) {
        for (MailInterceptor interceptor : interceptors) {
            try {
                interceptor.afterSend(context, success);
            } catch (Exception e) {
                logger.error("Error in mail interceptor afterSend: " + e.getMessage(), e);
            }
        }
    }

    private void addAttachment(MimeMessageHelper helper, MailAttachment attachment) throws MessagingException {
        MailAttachment.AttachmentSource source = attachment.getSource();

        if (source instanceof MailAttachment.PathAttachmentSource) {
            FileSystemResource resource = new FileSystemResource(
                    new File(((MailAttachment.PathAttachmentSource) source).getPath().toString()));
            helper.addAttachment(attachment.getName(), resource, attachment.getContentType());
        } else if (source instanceof MailAttachment.InputStreamAttachmentSource) {
            InputStreamResource resource = new InputStreamResource(
                    ((MailAttachment.InputStreamAttachmentSource) source).getInputStream());
            helper.addAttachment(attachment.getName(), resource, attachment.getContentType());
        } else if (source instanceof MailAttachment.ByteArrayAttachmentSource) {
            ByteArrayResource resource = new ByteArrayResource(
                    ((MailAttachment.ByteArrayAttachmentSource) source).getBytes());
            helper.addAttachment(attachment.getName(), resource, attachment.getContentType());
        }
    }
}
