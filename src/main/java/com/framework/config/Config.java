package com.framework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Singleton configuration holder.
 * Loads properties from config.properties once, merges with system properties,
 * validates required values, and provides type-safe access.
 */
public class Config {

    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static final Config INSTANCE = new Config();

    // Required properties loaded and validated at startup
    private final String baseUrl;
    private final String browser;
    private final boolean headless;

    // Merged properties: system properties override file properties.
    // Used for optional parameters like testSuite, timeout, etc.
    private final Properties resolvedProperties;

    private Config() {
        // Load properties from file. If the file is missing or empty, start with empty properties.
        Properties fileProperties = loadFromFile();
        this.resolvedProperties = mergeWithSystemProperties(fileProperties);

        // Fail fast for required properties
        this.baseUrl = getRequired("baseUrl");
        this.browser = validateBrowser(getRequired("browser"));
        this.headless = Boolean.parseBoolean(getRequired("headless"));

        logger.info("Configuration loaded:");
        resolvedProperties.forEach((key, value) -> logger.info("  {}={}", key, value));
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getBrowser() {
        return browser;
    }

    public boolean isHeadless() {
        return headless;
    }

    /**
     * Returns the property value or the provided default if not defined.
     */
    public String getString(String key, String defaultValue) {
        return resolvedProperties.getProperty(key, defaultValue);
    }

    /**
     * Returns the boolean property value or the provided default if not defined.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = resolvedProperties.getProperty(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    /**
     * Returns the int property value or the provided default if not defined.
     * Logs an error if the value exists but is not a valid number.
     */
    public int getInt(String key, int defaultValue) {
        String value = resolvedProperties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.error("Property '{}' must be a number. Current value: '{}'. Using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    private Properties loadFromFile() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                logger.warn("config.properties not found in classpath. Using system properties only.");
                return properties;
            }
            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Critical error while reading config.properties", e);
        }
        return properties;
    }

    private Properties mergeWithSystemProperties(Properties fileProperties) {
        Properties merged = new Properties();
        merged.putAll(fileProperties);
        // system properties override file properties; missing keys keep their file value or remain absent
        merged.putAll(System.getProperties());
        return merged;
    }

    // Reads from merged properties (system props override file props).
    private String getRequired(String key) {
        String value = resolvedProperties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required property '" + key + "' is not defined in config.properties or system properties!");
        }
        return value;
    }

    private String validateBrowser(String browser) {
        String lower = browser.toLowerCase();
        if (!List.of("chromium", "firefox", "webkit").contains(lower)) {
            throw new IllegalArgumentException(
                    "Invalid browser '" + browser + "'. Allowed values: chromium, firefox, webkit"
            );
        }
        return lower;
    }
}