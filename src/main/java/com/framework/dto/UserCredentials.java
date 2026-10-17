package com.framework.dto;

import java.util.Map;

/**
 * Holds arbitrary credentials data for a single user.
 * Values are never logged to avoid leaking sensitive information.
 */
public record UserCredentials(String username, Map<String, String> properties) {

    public UserCredentials {
        properties = Map.copyOf(properties);
    }

    public String getString(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.get(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }
}