package com.framework.core.manager;

import com.framework.config.Config;
import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * BrowserManager - Singleton that manages Playwright, Browser, BrowserContext, and Page instances.
 * Uses a shared Browser for maximum performance, and ThreadLocal Context/Page for test isolation.
 * Supports multiple tabs per test via a page stack (Deque) stored in ThreadLocal.
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

    // Stack of pages per thread.
    // The top of the stack is the currently active page.
    // When a new tab/popup is opened, it is pushed onto the stack.
    // When closed, it is popped, and the previous page becomes active.
    private static final ThreadLocal<Deque<Page>> pageStackThreadLocal = ThreadLocal.withInitial(ArrayDeque::new);

    private final Config config;

    // Private constructor for Singleton pattern.
    // Automatically initializes the shared browser so the caller does not need
    // to invoke initSharedBrowser() separately.
    private BrowserManager() {
        this.config = Config.getInstance();
        logger.info("==========================================");
        logger.info("       BrowserManager Initialized         ");
        logger.info("==========================================");
        logger.info(" Target Browser : {}", config.getBrowser());
        logger.info(" Headless Mode  : {}", config.isHeadless());
        logger.info(" Base URL       : {}", config.getBaseUrl());
        logger.info("==========================================");
        initSharedBrowser();
    }

    /**
     * Returns the single BrowserManager instance (creates it on first call).
     */
    public static synchronized BrowserManager getManager() {
        if (browserManager == null) {
            browserManager = new BrowserManager();
        }
        return browserManager;
    }

    /**
     * Initializes shared Playwright and Browser instances.
     * Called automatically from the constructor, but kept public for explicit re-initialization
     * if the browser was closed during the test run.
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

            browser = switch (browserType) {
                case "firefox" -> playwright.firefox().launch(options);
                case "webkit" -> playwright.webkit().launch(options);
                case "chromium" -> playwright.chromium().launch(options);
                default -> throw new IllegalStateException("Unexpected browser value: " + browserType);
            };

            logger.info("Shared {} browser launched successfully", browserType);
        }
    }

    /**
     * Call this ONCE after all tests (e.g., in @AfterSuite).
     * Closes the shared browser and Playwright engine.
     * All per-test contexts and pages should already be closed by teardownTestEnvironment().
     */
    public void closeSharedBrowser() {
        logger.info("Closing shared browser resources");
        if (browser != null && browser.isConnected()) {
            browser.close();
            logger.info("Shared browser closed");
        }
        if (playwright != null) {
            playwright.close();
            logger.info("Playwright engine closed");
        }
    }

    /**
     * Call this BEFORE each test (e.g., in @BeforeMethod).
     * Creates an isolated context with stable environment settings to prevent flaky tests.
     * Initializes a fresh page stack for the current thread.
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

        // Create the first page and push it onto the stack
        Page page = context.newPage();
        Deque<Page> pageStack = pageStackThreadLocal.get();
        pageStack.clear();
        pageStack.push(page);

        // Set reasonable default timeouts for page actions
        page.setDefaultNavigationTimeout(15000);
        page.setDefaultTimeout(10000);

        // Subscribe to new page creation events (both popups and new tabs via target="_blank")
        // so they are automatically added to the thread's page stack
        page.onPopup(newPage -> {
            logger.info("Popup/new tab detected, adding to page stack for thread: {}", threadName);
            newPage.setDefaultNavigationTimeout(15000);
            newPage.setDefaultTimeout(10000);
            pageStack.push(newPage);
        });

        logger.info("BrowserContext and initial Page created successfully for thread: {}. Stack size: {}",
                threadName, pageStack.size());
    }

    /**
     * Call this AFTER each test (e.g., in @AfterMethod).
     * Closes all pages in the stack, the context, and clears ThreadLocal storage.
     */
    public void teardownTestEnvironment() {
        String threadName = Thread.currentThread().getName();
        logger.info("Tearing down test environment for thread: {}", threadName);

        try {
            Deque<Page> pageStack = pageStackThreadLocal.get();
            // Close all pages in the stack, from top to bottom
            while (!pageStack.isEmpty()) {
                Page page = pageStack.pop();
                if (page != null && !page.isClosed()) {
                    page.close();
                    logger.debug("Closed page from stack for thread: {}", threadName);
                }
            }

            BrowserContext context = contextThreadLocal.get();
            if (context != null) {
                context.close();
                logger.debug("Closed BrowserContext for thread: {}", threadName);
            }
        } catch (Exception e) {
            logger.error("Error closing test resources for thread: {}", threadName, e);
        } finally {
            pageStackThreadLocal.remove();
            contextThreadLocal.remove();
            logger.info("ThreadLocal storage cleared for thread: {}", threadName);
        }
    }

    /**
     * Get the currently active Page for the current thread (top of the stack).
     * Returns null if no page has been created yet.
     */
    public Page getPage() {
        Deque<Page> pageStack = pageStackThreadLocal.get();
        return pageStack.isEmpty() ? null : pageStack.peek();
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
     * Switch to the most recently opened page (top of the stack).
     * Useful when a new tab/popup was opened and you need to interact with it.
     *
     * @return the newly active Page
     * @throws IllegalStateException if the stack is empty
     */
    public Page switchToLastOpenedPage() {
        Deque<Page> pageStack = pageStackThreadLocal.get();
        if (pageStack.isEmpty()) {
            throw new IllegalStateException("No pages in stack for thread: " + Thread.currentThread().getName());
        }
        Page activePage = pageStack.peek();
        logger.info("Switched to last opened page for thread: {}. URL: {}",
                Thread.currentThread().getName(), activePage.url());
        return activePage;
    }

    /**
     * Close the currently active page and switch to the previous one.
     * Useful when you are done with a popup/tab and want to return to the parent page.
     *
     * @return the Page that became active after closing the current one
     * @throws IllegalStateException if there is only one page in the stack
     */
    public Page closeCurrentPage() {
        Deque<Page> pageStack = pageStackThreadLocal.get();
        if (pageStack.size() <= 1) {
            throw new IllegalStateException("Cannot close the last remaining page in the stack for thread: "
                    + Thread.currentThread().getName());
        }
        Page closedPage = pageStack.pop();
        if (closedPage != null && !closedPage.isClosed()) {
            closedPage.close();
            logger.info("Closed current page for thread: {}", Thread.currentThread().getName());
        }
        Page activePage = pageStack.peek();
        logger.info("Switched back to previous page for thread: {}. URL: {}",
                Thread.currentThread().getName(), activePage.url());
        return activePage;
    }

    /**
     * Get the total number of open pages in the current thread's stack.
     * Useful for debugging and assertions.
     */
    public int getPageCount() {
        return pageStackThreadLocal.get().size();
    }

    /**
     * Navigate the currently active page to a specific URL.
     */
    public void navigateTo(String url) {
        logger.info("Navigating to: {}", url);
        getPage().navigate(url);
    }

    /**
     * Navigate the currently active page to the base URL from configuration.
     */
    public void navigateToBaseUrl() {
        navigateTo(config.getBaseUrl());
    }

    /**
     * Take a screenshot of the currently active page.
     */
    public byte[] takeScreenshot() {
        logger.debug("Taking screenshot of current page");
        return getPage().screenshot();
    }
}