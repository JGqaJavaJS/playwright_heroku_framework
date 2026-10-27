package com.framework.core.manager;

import com.framework.config.Config;
import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * BrowserManager - Singleton that manages Playwright, Browser, BrowserContext, and Page instances.
 * Uses ThreadLocal isolation for Playwright and Browser to ensure 100% stability in parallel execution.
 * Supports multiple tabs per test via a page stack (Deque) stored in ThreadLocal.
 */
public class BrowserManager {

    private static final Logger logger = LoggerFactory.getLogger(BrowserManager.class);

    // Singleton instance.
    private static BrowserManager browserManager;

    // Isolated Playwright engine per thread to prevent WebSocket ID collisions.
    private static final ThreadLocal<Playwright> playwright = new ThreadLocal<>();

    // Isolated Browser instance per thread.
    private static final ThreadLocal<Browser> browser = new ThreadLocal<>();

    // Isolated incognito session (cookies/cache/local storage) per thread.
    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();

    // Stack of pages per thread.
    private static final ThreadLocal<Deque<Page>> pageStackThreadLocal = ThreadLocal.withInitial(ArrayDeque::new);

    private final Config config;

    // Private constructor for Singleton pattern.
    private BrowserManager() {
        this.config = Config.getInstance();
        logger.info("==========================================");
        logger.info("       BrowserManager Initialized         ");
        logger.info("==========================================");
        logger.info(" Target Browser : {}", config.getBrowser());
        logger.info(" Headless Mode  : {}", config.isHeadless());
        logger.info(" Base URL       : {}", config.getBaseUrl());
        logger.info("==========================================");
    }

    public static BrowserManager getManager() {
        if (browserManager == null) {
            synchronized (BrowserManager.class) {
                if (browserManager == null) {
                    browserManager = new BrowserManager();
                }
            }
        }
        return browserManager;
    }

    /**
     * Legacy method kept for BaseTest compatibility.
     * Actual initialization now happens per-thread in setupTestEnvironment().
     */
    public void initSharedBrowser() {
        logger.info("Browser initialization is now handled per-thread in setupTestEnvironment().");
    }

    /**
     * Legacy method kept for BaseTest compatibility.
     * Actual cleanup now happens per-thread in teardownTestEnvironment().
     */
    public void closeSharedBrowser() {
        logger.info("Suite finished. All thread resources are closed in teardownTestEnvironment().");
    }

    /**
     * Call this BEFORE each test class (e.g., in @BeforeClass).
     * Creates an isolated Playwright, Browser, Context, and Page stack for the current thread.
     */
    public void setupTestEnvironment() {
        String threadName = Thread.currentThread().getName();
        logger.info("Setting up test environment for thread: {}", threadName);

        // 1. Get or create Playwright for this specific thread
        Playwright currentPlaywright = playwright.get();
        if (currentPlaywright == null) {
            logger.info("Creating new Playwright instance for thread: {}", threadName);
            currentPlaywright = Playwright.create();
            playwright.set(currentPlaywright);
        }

        // 2. Get or create Browser for this specific thread
        Browser currentBrowser = browser.get();
        if (currentBrowser == null || !currentBrowser.isConnected()) {
            logger.info("Creating new Browser instance for thread: {}", threadName);
            String browserType = config.getBrowser();
            boolean headless = config.isHeadless();

            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                    .setHeadless(headless)
                    .setTimeout(30000);

            currentBrowser = switch (browserType) {
                case "firefox" -> currentPlaywright.firefox().launch(options);
                case "webkit" -> currentPlaywright.webkit().launch(options);
                case "chromium" -> currentPlaywright.chromium().launch(options);
                default -> throw new IllegalStateException("Unexpected browser value: " + browserType);
            };
            browser.set(currentBrowser);
        }

        // 3. Create Context and Page using the thread's browser
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1920, 1080)
                .setLocale("en-US")
                .setTimezoneId("UTC")
                .setIgnoreHTTPSErrors(true);

        BrowserContext context = currentBrowser.newContext(contextOptions);
        contextThreadLocal.set(context);

        Deque<Page> pageStack = new ArrayDeque<>();
        Page page = context.newPage();
        pageStack.push(page);
        pageStackThreadLocal.set(pageStack);

        page.setDefaultNavigationTimeout(15000);
        page.setDefaultTimeout(10000);

        page.onPopup(newPage -> {
            logger.info("Popup/new tab detected, adding to page stack for thread: {}", threadName);
            newPage.setDefaultNavigationTimeout(15000);
            newPage.setDefaultTimeout(10000);
            pageStack.push(newPage);
        });

        logger.info("Environment ready for thread: {}. Stack size: {}", threadName, pageStack.size());
    }

    /**
     * Call this AFTER each test class (e.g., in @AfterClass).
     * Closes all resources (Page, Context, Browser, Playwright) for the current thread.
     */
    public void teardownTestEnvironment() {
        String threadName = Thread.currentThread().getName();
        logger.info("Tearing down environment for thread: {}", threadName);

        try {
            // 1. Close all Pages
            Deque<Page> pageStack = pageStackThreadLocal.get();
            if (pageStack != null) {
                while (!pageStack.isEmpty()) {
                    Page page = pageStack.pop();
                    if (page != null && !page.isClosed()) {
                        page.close();
                    }
                }
            }

            // 2. Close Context
            BrowserContext context = contextThreadLocal.get();
            if (context != null) {
                context.close();
            }

            // 3. Close Browser
            Browser currentBrowser = browser.get();
            if (currentBrowser != null) {
                currentBrowser.close();
            }

            // 4. Close Playwright
            Playwright currentPlaywright = playwright.get();
            if (currentPlaywright != null) {
                currentPlaywright.close();
            }
        } catch (Exception e) {
            logger.error("Error closing test resources for thread: {}", threadName, e);
        } finally {
            // 5. Clear ThreadLocal storage to prevent memory leaks
            pageStackThreadLocal.remove();
            contextThreadLocal.remove();
            browser.remove();
            playwright.remove();

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
     */
    public BrowserContext getContext() {
        return contextThreadLocal.get();
    }

    /**
     * Switch to the most recently opened page (top of the stack).
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
     */
    public int getPageCount() {
        return pageStackThreadLocal.get().size();
    }
}