package com.framework.config;

import java.io.InputStream;
import java.util.Properties;

public class Config {
    // Singleton pattern
    private static final Config INSTANCE = new Config();
    private final Properties properties;

    private Config() {
        properties = new Properties();
        // Load config.properties from classpath
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("config.properties file not found in classpath!");
            }
            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Critical error while reading config.properties", e);
        }
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    // Type-safe getters - fail fast if property is missing
    public String getBaseUrl() {
        return getRequiredProperty("baseUrl");
    }

    public String getBrowser() {
        return getRequiredProperty("browser");
    }

    public boolean isHeadless() {
        return Boolean.parseBoolean(getRequiredProperty("headless"));
    }

    // Get required property - throws exception if missing
    private String getRequiredProperty(String key) {
        // Check system property first (for CI/CD override)
        String value = System.getProperty(key);
        if (value != null) {
            return value;
        }

        // Then check config.properties
        value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required property '" + key + "' is not defined in config.properties or system properties!");
        }

        return value;
    }
}