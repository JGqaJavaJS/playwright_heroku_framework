package com.framework.tests;

import com.framework.core.manager.BrowserManager;
import com.framework.pages.base.BasePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.*;

/**
 * Base class for all TestNG test classes.
 * Manages the lifecycle of the thread-isolated browser environment and Allure reporting.
 */
public abstract class BaseTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Executes ONCE before any test in the suite.
     * Legacy hook kept for structural consistency. Actual initialization is per-thread.
     */
    @BeforeSuite(alwaysRun = true)
    public void setUpSuite() {
        logger.info("=== Initializing Suite Level Hooks ===");
        BrowserManager.getManager().initSharedBrowser();
    }

    /**
     * Executes before the first test method in the current class.
     * Sets up an isolated Playwright, Browser, Context, and Page stack for the current thread.
     */
    @BeforeClass
    public void setUpClass() {
        logger.info("=== Starting Test Class: {} ===", getClass().getSimpleName());
        BrowserManager.getManager().setupTestEnvironment();
    }

    /**
     * Executes after the last test method in the current class.
     * Tears down and closes all resources (Page, Context, Browser, Playwright)
     * for the current thread to prevent memory leaks and driver race conditions.
     */
    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        logger.info("=== Finished Test Class: {} ===", getClass().getSimpleName());
        BrowserManager.getManager().teardownTestEnvironment();
    }

    /**
     * Executes once after all tests in the suite have finished.
     * Legacy hook kept for structural consistency. Actual cleanup is per-thread.
     */
    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        logger.info("=== Test Suite Finished. Closing shared browser resources ===");
        BrowserManager.getManager().closeSharedBrowser();
    }

    /**
     * Automatically captures and attaches a screenshot to Allure if the test fails.
     * Delegates all screenshot logic (naming, timestamping, saving) to BasePage.
     */
    @AfterMethod(alwaysRun = true)
    public void tearDownMethod(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            logger.warn("Test FAILED: {}. Capturing screenshot for Allure report.", result.getName());
            BasePage.attachFailureScreenshot(result);
        }
    }

}