package com.example.email.spring;

import com.example.email.core.model.Attachment;
import com.example.email.core.model.Email;
import com.example.email.core.sender.EmailSender;
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
import java.nio.file.Path;

/**
 * Spring implementation of the email sender.
 * No longer using @Component annotation to avoid bean conflicts.
 */
public class SpringEmailSender implements EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(SpringEmailSender.class);

    private final JavaMailSender javaMailSender;
    private final EmailProperties emailProperties;

    public SpringEmailSender(JavaMailSender javaMailSender, EmailProperties emailProperties) {
        this.javaMailSender = javaMailSender;
        this.emailProperties = emailProperties;
    }

    @Override
    public void send(Email email) throws EmailSendException {
        if (!emailProperties.isEnabled()) {
            logger.info("Email sending is disabled. Would have sent email with subject: {}", email.getSubject());
            return;
        }

        if (javaMailSender == null) {
            logger.warn("JavaMailSender is not configured. Cannot send email: {}", email.getSubject());
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(email.getFrom());
            helper.setTo(email.getTo().toArray(new String[0]));

            if (!email.getCc().isEmpty()) {
                helper.setCc(email.getCc().toArray(new String[0]));
            }

            if (!email.getBcc().isEmpty()) {
                helper.setBcc(email.getBcc().toArray(new String[0]));
            }

            helper.setSubject(email.getSubject());
            helper.setText(email.getContent(), email.isHtml());

            // Add attachments
            for (Attachment attachment : email.getAttachments()) {
                addAttachment(helper, attachment);
            }

            javaMailSender.send(message);
            logger.info("Email sent to {} with subject: {}", String.join(", ", email.getTo()), email.getSubject());
        } catch (MessagingException e) {
            throw new EmailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }

    private void addAttachment(MimeMessageHelper helper, Attachment attachment) throws MessagingException {
        Attachment.AttachmentSource source = attachment.getSource();

        if (source instanceof Attachment.PathAttachmentSource) {
            Path path = ((Attachment.PathAttachmentSource) source).getPath();
            FileSystemResource resource = new FileSystemResource(new File(path.toString()));
            helper.addAttachment(attachment.getName(), resource, attachment.getContentType());
        } else if (source instanceof Attachment.InputStreamAttachmentSource) {
            InputStreamResource resource = new InputStreamResource(((Attachment.InputStreamAttachmentSource) source).getInputStream());
            helper.addAttachment(attachment.getName(), resource, attachment.getContentType());
        } else if (source instanceof Attachment.ByteArrayAttachmentSource) {
            byte[] bytes = ((Attachment.ByteArrayAttachmentSource) source).getBytes();
            ByteArrayResource resource = new ByteArrayResource(bytes);
            helper.addAttachment(attachment.getName(), resource, attachment.getContentType());
        }
    }
}
