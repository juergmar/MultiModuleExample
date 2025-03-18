package com.example.email.spring;

import com.example.email.core.provider.MailInterceptor;
import com.example.email.core.provider.MailProvider;
import com.example.email.core.provider.MailProviderFactory;
import com.example.email.core.sender.ConfigurableEmailSender;
import com.example.email.core.sender.EmailSender;
import com.example.email.core.template.TemplateEngine;
import com.example.email.spring.provider.SpringMailProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

@Configuration
@EnableConfigurationProperties(EmailProperties.class)
@AutoConfigureAfter({ThymeleafAutoConfiguration.class, MailSenderAutoConfiguration.class})
public class EmailAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MailProvider.class)
    @ConditionalOnBean(JavaMailSender.class)
    public MailProvider mailProvider(JavaMailSender javaMailSender,
                                     EmailProperties emailProperties,
                                     List<MailInterceptor> interceptors) {
        // Create the provider with interceptors
        SpringMailProvider provider = new SpringMailProvider(
                javaMailSender,
                emailProperties.isEnabled(),
                interceptors
        );

        // Register the provider with the factory
        MailProviderFactory.registerProvider("spring", provider, true);

        return provider;
    }

    @Bean
    @ConditionalOnMissingBean(EmailSender.class)
    public EmailSender emailSender(EmailProperties emailProperties) {
        String providerName = emailProperties.getProvider().getName();
        if (providerName != null && !providerName.isEmpty()) {
            return new ConfigurableEmailSender(providerName);
        }
        return new ConfigurableEmailSender();
    }

    @Bean
    @ConditionalOnMissingBean
    public TemplateEngine emailTemplateEngine(org.thymeleaf.ITemplateEngine thymeleafEngine) {
        return new ThymeleafTemplateEngine(thymeleafEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringEmailConfig emailConfig(EmailProperties emailProperties) {
        return new SpringEmailConfig(emailProperties);
    }
}
