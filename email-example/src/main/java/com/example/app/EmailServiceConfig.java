package com.example.app;

import com.example.app.service.EmailService;
import com.example.email.core.sender.EmailSender;
import com.example.email.core.service.EmailTemplateService;
import com.example.email.core.template.TemplateEngine;
import com.example.email.example.generated.ExampleEmailService;
import com.example.email.spring.EmailProperties;
import com.example.email.spring.SpringEmailSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class EmailServiceConfig {

    @Bean
    public ExampleEmailService exampleEmailService(TemplateEngine emailTemplateEngine,
                                                   EmailTemplateService.EmailConfig emailConfig) {
        return new ExampleEmailService(emailTemplateEngine, emailConfig);
    }

    @Bean
    public EmailSender emailSender(JavaMailSender javaMailSender, EmailProperties emailProperties) {
        return new SpringEmailSender(javaMailSender, emailProperties);
    }

    @Bean
    public EmailService emailService(ExampleEmailService exampleEmailService, EmailSender emailSender) {
        return new EmailService(exampleEmailService, emailSender);
    }
}
