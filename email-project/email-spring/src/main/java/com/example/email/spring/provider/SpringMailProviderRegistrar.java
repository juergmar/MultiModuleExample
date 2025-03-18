package com.example.email.spring.provider;

import com.example.email.core.provider.MailProviderFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Utility class for registering Spring mail provider with the factory.
 */
public class SpringMailProviderRegistrar {

    public static final String SPRING_PROVIDER_NAME = "spring";

    /**
     * Register the Spring mail provider with the mail provider factory
     *
     * @param javaMailSender The Spring JavaMailSender to use
     * @param enabled Whether email sending is enabled
     * @param makeDefault Whether to make this the default provider
     */
    public static void registerSpringProvider(JavaMailSender javaMailSender, boolean enabled, boolean makeDefault) {
        SpringMailProvider provider = new SpringMailProvider(javaMailSender, enabled);
        MailProviderFactory.registerProvider(SPRING_PROVIDER_NAME, provider, makeDefault);
    }

    /**
     * Register the Spring mail provider from an ApplicationContext
     *
     * @param applicationContext The Spring ApplicationContext
     * @param enabled Whether email sending is enabled
     * @param makeDefault Whether to make this the default provider
     * @return true if registration was successful, false if JavaMailSender was not found
     */
    public static boolean registerFromContext(ApplicationContext applicationContext, boolean enabled, boolean makeDefault) {
        try {
            JavaMailSender mailSender = applicationContext.getBean(JavaMailSender.class);
            registerSpringProvider(mailSender, enabled, makeDefault);
            return true;
        } catch (Exception e) {
            // JavaMailSender not available
            return false;
        }
    }
}
