package com.example.email.spring;

import com.example.email.core.sender.EmailSender;
import com.example.email.core.template.TemplateEngine;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
@EnableConfigurationProperties(EmailProperties.class)
@AutoConfigureAfter({ThymeleafAutoConfiguration.class, MailSenderAutoConfiguration.class})
public class EmailAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(JavaMailSender.class)
    public EmailSender emailSender(JavaMailSender javaMailSender, EmailProperties emailProperties) {
        return new SpringEmailSender(javaMailSender, emailProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public TemplateEngine emailTemplateEngine(org.thymeleaf.ITemplateEngine thymeleafEngine) {
        return new ThymeleafTemplateEngine(thymeleafEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public EmailConfig emailConfig(EmailProperties emailProperties) {
        return new EmailConfig(emailProperties);
    }
}
