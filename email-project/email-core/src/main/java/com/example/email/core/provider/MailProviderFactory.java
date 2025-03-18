package com.example.email.core.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Factory for creating and registering mail providers.
 * Supports both programmatic registration and service loader discovery.
 */
public class MailProviderFactory {
    private static final Map<String, MailProvider> providers = new HashMap<>();
    private static String defaultProviderName = null;
    private static boolean initialized = false;

    /**
     * Register a mail provider
     *
     * @param name The name of the provider
     * @param provider The provider implementation
     * @param makeDefault Whether to make this the default provider
     */
    public static void registerProvider(String name, MailProvider provider, boolean makeDefault) {
        providers.put(name, provider);
        if (makeDefault || defaultProviderName == null) {
            defaultProviderName = name;
        }
    }

    /**
     * Get a mail provider by name
     *
     * @param name The name of the provider
     * @return The mail provider
     * @throws IllegalArgumentException If no provider with the given name exists
     */
    public static MailProvider getProvider(String name) {
        initialize();
        MailProvider provider = providers.get(name);
        if (provider == null) {
            throw new IllegalArgumentException("No mail provider found with name: " + name);
        }
        return provider;
    }

    /**
     * Get the default mail provider
     *
     * @return The default mail provider
     * @throws IllegalStateException If no default provider is configured
     */
    public static MailProvider getDefaultProvider() {
        initialize();
        if (defaultProviderName == null) {
            throw new IllegalStateException("No default mail provider configured");
        }
        return providers.get(defaultProviderName);
    }

    /**
     * Set the default provider by name
     *
     * @param name The name of the provider to set as default
     * @throws IllegalArgumentException If no provider with the given name exists
     */
    public static void setDefaultProvider(String name) {
        initialize();
        if (!providers.containsKey(name)) {
            throw new IllegalArgumentException("No mail provider found with name: " + name);
        }
        defaultProviderName = name;
    }

    /**
     * Check if a provider with the given name exists
     *
     * @param name The name to check
     * @return true if a provider with the given name exists
     */
    public static boolean hasProvider(String name) {
        initialize();
        return providers.containsKey(name);
    }

    /**
     * Get the names of all registered providers
     *
     * @return Array of provider names
     */
    public static String[] getProviderNames() {
        initialize();
        return providers.keySet().toArray(new String[0]);
    }

    /**
     * Initialize the factory by discovering providers using ServiceLoader
     */
    private static synchronized void initialize() {
        if (initialized) {
            return;
        }

        // Discover providers using ServiceLoader
        ServiceLoader<MailProviderRegistrar> registrars = ServiceLoader.load(MailProviderRegistrar.class);
        for (MailProviderRegistrar registrar : registrars) {
            registrar.registerProviders();
        }

        initialized = true;
    }

    /**
     * Interface for mail provider registrars
     */
    public interface MailProviderRegistrar {
        void registerProviders();
    }
}
