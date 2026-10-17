package com.framework.config;

import com.framework.dto.UserCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Singleton that loads sensitive user credentials from secrets.properties.
 * Values are never logged.
 */
public class CredentialsConfig {

    private static final CredentialsConfig INSTANCE = new CredentialsConfig();
    private static final Logger logger = LoggerFactory.getLogger(CredentialsConfig.class);

    private static final String USER_PREFIX = "user";
    private static final String KEY_SEPARATOR = "\\.";
    private static final int EXPECTED_KEY_PARTS = 3;

    private final Map<String, UserCredentials> users = new HashMap<>();

    private CredentialsConfig() {
        load();
    }

    public static CredentialsConfig getInstance() {
        return INSTANCE;
    }

    public Map<String, UserCredentials> getAllUsers() {
        return Map.copyOf(users);
    }

    public Set<String> getAllUsernames() {
        return Set.copyOf(users.keySet());
    }

    /**
     * Returns credentials for a specific user by name.
     * Throws IllegalStateException if the user is not found.
     */
    public UserCredentials getUser(String username) {
        UserCredentials user = users.get(username);
        if (user == null) {
            throw new IllegalStateException("User '" + username + "' not found in secrets.properties");
        }
        return user;
    }

    /**
     * Returns credentials for the user configured via 'current.user' property.
     * Throws IllegalStateException if 'current.user' is not set or user is not found.
     */
    public UserCredentials getCurrentUser() {
        String username = Config.getInstance().getString("current.user", null);
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Property 'current.user' is not defined in config.properties or system properties. " +
                            "Set it via current.user=<username> or -Dcurrent.user=<username>"
            );
        }
        return getUser(username);
    }

    private void load() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("secrets.properties")) {
            if (inputStream == null) {
                logger.warn("secrets.properties not found in classpath. No credentials loaded.");
                return;
            }
            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Critical error while reading secrets.properties", e);
        }

        parseUsers(properties);
        logger.info("Loaded credentials for users: {}", users.keySet());
    }

    private void parseUsers(Properties properties) {
        Map<String, Map<String, String>> tempUsers = new HashMap<>();

        for (String key : properties.stringPropertyNames()) {
            String[] parts = key.split(KEY_SEPARATOR, EXPECTED_KEY_PARTS);

            if (parts.length != EXPECTED_KEY_PARTS || !USER_PREFIX.equals(parts[0])) {
                logger.warn("Ignoring invalid secrets property: {}", key);
                continue;
            }

            String username = parts[1];
            String propertyName = parts[2];
            String value = properties.getProperty(key);

            tempUsers
                    .computeIfAbsent(username, k -> new HashMap<>())
                    .put(propertyName, value);
        }

        tempUsers.forEach((username, propertiesMap) ->
                users.put(username, new UserCredentials(username, propertiesMap))
        );
    }
}