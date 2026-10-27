package com.framework.pages.base;

import com.framework.config.Config;
import com.framework.core.manager.BrowserManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all Page Objects.
 * Provides common methods for interacting with web elements with built-in waits and logging.
 */
public abstract class BasePage {
    protected static final Logger logger = LoggerFactory.getLogger(BasePage.class);
    protected final Page page;

    // Constructor that gets the current Page from BrowserManager
    protected BasePage() {
        this.page = BrowserManager.getManager().getPage();
        logger.info("Initialized page '{}' with the current active thread page", getClass().getSimpleName());
    }

    /**
     * Parameterized constructor for advanced scenarios (100% coverage).
     * Binds to a specific Page instance provided by the caller.
     * Use this when dealing with multiple tabs, pop-ups, or isolated contexts.
     * Example usage:
     * Page newTab = BrowserManager.getInstance().getContext().newPage();
     * HelpPage helpPage = new HelpPage(newTab);
     * @param specificPage The exact Page instance this Page Object should interact with.
     */
    protected BasePage(Page specificPage) {
        this.page = specificPage;
        logger.info("Initialized page '{}' with a specific Page instance", getClass().getSimpleName());
    }

    // ==================== ELEMENT INTERACTION METHODS ====================

    /**
     * Clicks on the element with explicit logging.
     * Note: Playwright's native click() already auto-waits for the element
     * to be attached, visible, stable, and enabled. No explicit wait needed.
     */
    protected void click(Locator locator) {
        logger.info("Clicking on element: {}", locator);
        locator.click();
    }

    /**
     * Types text into the input field with explicit logging.
     * Note: Playwright's native fill() is a "super-method". It automatically
     * waits for the element to be visible/enabled, focuses it, CLEARS the existing
     * value, and types the new text. No manual click() or clear() needed.
     */
    protected void enterText(Locator locator, String text) {
        logger.info("Typing '{}' into element: {}", text, locator);
        locator.fill(text);
    }

    /**
     * Get text content of element with automatic wait
     */
    protected String getText(Locator locator) {
        logger.info("Getting text from element: {}", locator);
        waitForVisible(locator);
        String text = locator.textContent();
        logger.debug("Retrieved text: {}", text);
        return text;
    }

    /**
     * Gets the current value of an input, textarea, or select element.
     *
     * @param locator The locator of the input element.
     * @return The current string value of the input field.
     */
    protected String getInputValue(Locator locator) {
        logger.info("Getting input value from element: {}", locator);
        String value = locator.inputValue();
        logger.debug("Retrieved input value: '{}'", value);
        return value;
    }

    /**
     * Checks if element is visible.
     * Playwright automatically waits for the element to be attached to the DOM before evaluating visibility.
     * Returns true if visible, false otherwise (does not throw TimeoutException).
     */
    protected boolean isVisible(Locator locator) {
        logger.debug("Checking if element is visible: {}", locator);
        return locator.isVisible();
    }

    // ==================== WAIT METHODS ====================

    /**
     * Wait until element is visible on page (default timeout from Page settings)
     */
    protected void waitForVisible(Locator locator) {
        logger.debug("Waiting for element to be visible: {}", locator);
        locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    /**
     * Wait until element is hidden or removed from DOM
     */
    protected void waitForHidden(Locator locator) {
        logger.debug("Waiting for element to be hidden: {}", locator);
        locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    // ==================== NAVIGATION METHODS ====================

    /**
     * Navigate to a specific URL.
     * If the URL already starts with "http", it is used as is.
     * Otherwise, it is appended to the base URL.
     */
    protected void navigateTo(String url) {
        String targetUrl = url.startsWith("http") ? url : Config.getInstance().getBaseUrl() + url;
        logger.info("Navigating to URL: {}", targetUrl);
        page.navigate(targetUrl);
    }

    /**
     * Navigate to base URL from configuration
     */
    protected void navigateToBaseUrl() {
        String baseUrl = Config.getInstance().getBaseUrl();
        logger.info("Navigating to base URL: {}", baseUrl);
        page.navigate(baseUrl);
    }

    /**
     * Get current page URL
     */
    protected String getCurrentUrl() {
        String url = page.url();
        logger.debug("Current URL: {}", url);
        return url;
    }

    /**
     * Get current page title
     */
    protected String getPageTitle() {
        String title = page.title();
        logger.debug("Page title: {}", title);
        return title;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Takes a screenshot of the current page and returns it as a byte array.
     * Can be called manually from tests for custom handling.
     *
     * @return byte array of the screenshot image (PNG format).
     */
    public byte[] takeScreenshot() {
        logger.info("Taking screenshot of current page");
        return page.screenshot();
    }

    /**
     * Captures a screenshot, generates a unique human-readable filename with timestamp,
     * and attaches it to the Allure report.
     * Static method to be called from BaseTest @AfterMethod without needing a Page Object instance.
     *
     * @param result The TestNG ITestResult containing test class and method names.
     */
    public static void attachFailureScreenshot(org.testng.ITestResult result) {
        try {
            Page page = BrowserManager.getManager().getPage();
            if (page != null && !page.isClosed()) {
                // Generate human-readable, unique filename: ClassName_MethodName_YYYY-MM-DD_HH-mm-ss
                String timestamp = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                String className = result.getTestClass().getRealClass().getSimpleName();
                String methodName = result.getName();
                String screenshotName = className + "_" + methodName + "_" + timestamp;

                logger.info("Capturing failure screenshot: {}", screenshotName);
                byte[] screenshotBytes = page.screenshot();

                // Attach to Allure with explicit PNG extension
                io.qameta.allure.Allure.addAttachment(
                        screenshotName,
                        "image/png",
                        new java.io.ByteArrayInputStream(screenshotBytes),
                        "png"
                );
            } else {
                logger.warn("Cannot capture screenshot: page is null or closed.");
            }
        } catch (Exception e) {
            logger.error("Failed to capture screenshot: {}", e.getMessage(), e);
        }
    }

    /**
     * Scroll element into view
     */
    protected void scrollToElement(Locator locator) {
        logger.debug("Scrolling to element: {}", locator);
        locator.scrollIntoViewIfNeeded();
    }
}