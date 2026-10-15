package com.framework.core.manager;

import com.framework.config.Config;
import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BrowserManager - Singleton that manages Playwright, Browser, BrowserContext, and Page instances.
 * Uses a shared Browser for maximum performance, and ThreadLocal Context/Page for test isolation.
 */
public class BrowserManager {
    private static final Logger logger = LoggerFactory.getLogger(BrowserManager.class);

    // Singleton instance.
    // Holds the single instance of the manager.
    // Ensures the browser is launched only once for all tests,
    // while providing isolated, clean contexts (tabs) for each individual test.
    private static BrowserManager browserManager;

    // The core Playwright engine.
    // Created only once to launch browsers. Heavy to initialize, so we keep it static.
    private static Playwright playwright;

    // The actual browser window (e.g., Chromium).
    // Shared across all tests to save RAM and startup time.
    // Individual tests get isolated contexts (like Incognito tabs) from this single browser.
    private static Browser browser;

    // Isolated incognito session (cookies/cache/local storage) per thread.
    // The test author controls its lifecycle (can be reset per test, per class, or per suite).
    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();

    // Default active page/tab per thread.
    // Created and destroyed according to the lifecycle chosen by the test author.
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

    private final Config config;

    // Private constructor for Singleton pattern
    private BrowserManager() {
        this.config = Config.getInstance();
        // Log active configuration immediately upon initialization
        logger.info("==========================================");
        logger.info("       BrowserManager Initialized         ");
        logger.info("==========================================");
        logger.info(" Target Browser : {}", config.getBrowser());
        logger.info(" Headless Mode  : {}", config.isHeadless());
        logger.info(" Base URL       : {}", config.getBaseUrl());
        logger.info("==========================================");
    }

    /**
     * Returns the single BrowserManager instance (creates it on first call).
     */
    public static synchronized BrowserManager getInstance() {
        if (browserManager == null) {
            browserManager = new BrowserManager();
        }
        return browserManager;
    }

    /**
     * Call this ONCE before all tests (e.g., in @BeforeAll)
     * Initializes shared browser with detailed configuration logging
     */
    public void initSharedBrowser() {
        if (playwright == null) {
            logger.info("Initializing shared Playwright instance");
            playwright = Playwright.create();
        }

        if (browser == null || !browser.isConnected()) {
            String browserType = config.getBrowser();
            boolean headless = config.isHeadless();

            logger.info("=== BROWSER INITIALIZATION ===");
            logger.info("Browser Type : {}", browserType);
            logger.info("Headless Mode: {}", headless);
            logger.info("Base URL     : {}", config.getBaseUrl());
            logger.info("==============================");

            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                    .setHeadless(headless)
                    .setTimeout(30000); // Global timeout for browser launch

            browser = switch (browserType.toLowerCase()) {
                case "firefox" -> playwright.firefox().launch(options);
                case "webkit" -> playwright.webkit().launch(options);
                case "chromium" -> playwright.chromium().launch(options);
                default -> playwright.chromium().launch(options);
            };

            logger.info("Shared {} browser launched successfully", browserType);
        }
    }

    /**
     * Call this ONCE after all tests (e.g., in @AfterAll)
     */
    public void closeSharedBrowser() {
        logger.info("Closing shared browser resources");
        if (browser != null && browser.isConnected()) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    /**
     * Call this BEFORE each test (e.g., in @BeforeEach) or @BeforeAll
     * Creates an isolated context with stable environment settings to prevent flaky tests
     */
    public void setupTestEnvironment() {
        String threadName = Thread.currentThread().getName();
        logger.info("Setting up isolated BrowserContext for thread: {}", threadName);

        // Configure context for maximum stability and consistency across environments
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1920, 1080) // Standard desktop resolution
                .setLocale("en-US")          // Consistent language/formatting
                .setTimezoneId("UTC")        // Consistent timezone (prevents date-related flakes)
                .setIgnoreHTTPSErrors(true); // Avoid SSL certificate issues in test environments

        BrowserContext context = browser.newContext(contextOptions);
        contextThreadLocal.set(context);

        Page page = context.newPage();
        pageThreadLocal.set(page);

        // Set reasonable default timeouts for page actions
        page.setDefaultNavigationTimeout(15000);
        page.setDefaultTimeout(10000);

        logger.info("BrowserContext and Page created successfully for thread: {}", threadName);
    }

    /**
     * Call this AFTER each test (e.g., in @AfterEach) or @BeforeAll
     */
    public void teardownTestEnvironment() {
        String threadName = Thread.currentThread().getName();
        logger.info("Tearing down test environment for thread: {}", threadName);

        try {
            Page page = pageThreadLocal.get();
            if (page != null && !page.isClosed()) {
                page.close();
            }

            BrowserContext context = contextThreadLocal.get();
            if (context != null) {
                context.close();
            }
        } catch (Exception e) {
            logger.error("Error closing test resources for thread: {}", threadName, e);
        } finally {
            pageThreadLocal.remove();
            contextThreadLocal.remove();
        }
    }

    /**
     * Get the active Page for the current thread
     */
    public Page getPage() {
        return pageThreadLocal.get();
    }

    /**
     * Get the active BrowserContext for the current thread.
     * Context contains isolated session data: cookies, localStorage, cache, and browser settings.
     * Each test gets its own context to ensure complete isolation from other tests.
     */
    public BrowserContext getContext() {
        return contextThreadLocal.get();
    }

    /**
     * Navigate to a specific URL
     */
    public void navigateTo(String url) {
        logger.info("Navigating to: {}", url);
        getPage().navigate(url);
    }

    /**
     * Navigate to the base URL from configuration
     */
    public void navigateToBaseUrl() {
        navigateTo(config.getBaseUrl());
    }

    /**
     * Take a screenshot of the current page
     */
    public byte[] takeScreenshot() {
        logger.debug("Taking screenshot");
        return getPage().screenshot();
    }
}