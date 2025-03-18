package com.example.email.core.provider;

import java.util.HashMap;
import java.util.Map;

/**
 * An extendable context object for email processing.
 * This allows for customization and modification of emails
 * during the sending process without modifying the core interfaces.
 */
public class MailContext {
    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * Get an attribute from the context
     *
     * @param key The attribute key
     * @return The attribute value or null if not found
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Set an attribute in the context
     *
     * @param key The attribute key
     * @param value The attribute value
     * @return This context instance for chaining
     */
    public MailContext setAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    /**
     * Check if the context has an attribute
     *
     * @param key The attribute key
     * @return true if the attribute exists
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    /**
     * Remove an attribute from the context
     *
     * @param key The attribute key
     * @return The previous value or null if not found
     */
    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }

    /**
     * Get all attributes as an unmodifiable map
     *
     * @return Unmodifiable map of attributes
     */
    public Map<String, Object> getAttributes() {
        return Map.copyOf(attributes);
    }
}
