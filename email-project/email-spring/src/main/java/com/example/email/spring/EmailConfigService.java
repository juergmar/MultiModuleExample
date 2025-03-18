package com.example.email.spring;

import com.example.email.core.provider.MailProviderFactory;
import com.example.email.core.sender.ConfigurableEmailSender;
import com.example.email.spring.provider.SpringMailProviderRegistrar;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

/**
 * Service for managing email configuration.
 * Listens for context refresh events to initialize mail providers.
 */
@Service
public class EmailConfigService implements ApplicationListener<ContextRefreshedEvent> {

    private final EmailProperties emailProperties;
    private final ConfigurableEmailSender emailSender;

    public EmailConfigService(EmailProperties emailProperties, ConfigurableEmailSender emailSender) {
        this.emailProperties = emailProperties;
        this.emailSender = emailSender;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // This will be called when the application context is initialized
        ApplicationContext context = event.getApplicationContext();

        // Register the Spring mail provider if available
        boolean springRegistered = SpringMailProviderRegistrar.registerFromContext(
                context,
                emailProperties.isEnabled(),
                true
        );

        // Configure the email sender based on properties
        String providerName = emailProperties.getProvider().getName();
        if (providerName != null && !providerName.isEmpty()) {
            if (MailProviderFactory.hasProvider(providerName)) {
                emailSender.setMailProvider(providerName);
            } else {
                throw new IllegalStateException("Configured mail provider not found: " + providerName);
            }
        }
    }

    /**
     * Change the mail provider at runtime
     *
     * @param providerName The name of the provider to use
     */
    public void changeMailProvider(String providerName) {
        if (!MailProviderFactory.hasProvider(providerName)) {
            throw new IllegalArgumentException("Mail provider not found: " + providerName);
        }
        emailSender.setMailProvider(providerName);
    }

    /**
     * Get the names of all available mail providers
     *
     * @return Array of provider names
     */
    public String[] getAvailableProviders() {
        return MailProviderFactory.getProviderNames();
    }

    /**
     * Get the current mail provider name
     *
     * @return The name of the current mail provider
     */
    public String getCurrentProviderName() {
        return emailSender.getMailProviderName();
    }
}
