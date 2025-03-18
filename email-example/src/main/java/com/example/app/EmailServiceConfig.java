package com.example.app;

import com.example.email.core.provider.MailInterceptor;
import com.example.email.core.provider.MailProvider;
import com.example.email.core.provider.MailProviderFactory;
import com.example.email.core.sender.ConfigurableEmailSender;
import com.example.email.core.sender.EmailSender;
import com.example.email.core.template.TemplateEngine;
import com.example.email.example.generated.ExampleEmailService;
import com.example.email.spring.SpringEmailConfig;
import com.example.email.spring.EmailProperties;
import com.example.email.spring.provider.SpringMailProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

@Configuration
public class EmailServiceConfig {

    @Bean
    public ExampleEmailService exampleEmailService(TemplateEngine emailTemplateEngine,
                                                   SpringEmailConfig springEmailConfig) {
        return new ExampleEmailService(emailTemplateEngine, springEmailConfig);
    }

    @Bean
    public MailProvider springMailProvider(JavaMailSender javaMailSender,
                                           EmailProperties emailProperties,
                                           List<MailInterceptor> interceptors) {
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
    public EmailSender emailSender() {
        return new ConfigurableEmailSender("spring");
    }
}
