package com.framework.tests;

import com.framework.core.manager.BrowserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

/**
 * Base class for all TestNG test classes.
 * Manages the lifecycle of the browser environment using TestNG annotations.
 */
public abstract class BaseTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Executes ONCE before any test in the suite.
     * Initializes the shared Playwright engine and Browser instance on the MAIN thread.
     */
    @BeforeSuite(alwaysRun = true)
    public void setUpSuite() {
        logger.info("=== Initializing Shared Browser (Suite Level) ===");
        BrowserManager.getManager().initSharedBrowser();
    }

    /**
     * Executes before the first test method in the current class.
     * Sets up a fresh Page stack. Reuses the existing BrowserContext for this thread
     * to prevent Playwright driver race conditions, but clears cookies/storage
     * to ensure complete isolation between test classes.
     */
    @BeforeClass
    public void setUpClass() {
        logger.info("=== Starting Test Class: {} ===", getClass().getSimpleName());
        BrowserManager.getManager().setupTestEnvironment();
    }

    /**
     * Executes after the last test method in the current class.
     * Closes all Pages created during this class, but intentionally keeps the
     * BrowserContext alive for the next class on this thread.
     * This avoids rapid close/open driver bugs.
     */
    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        logger.info("=== Finished Test Class: {} ===", getClass().getSimpleName());
        BrowserManager.getManager().teardownTestEnvironment();
    }

    /**
     * Executes once after all tests in the suite have finished.
     * Closes the shared Browser and Playwright instances.
     * This will also cleanly close any remaining BrowserContexts automatically.
     */
    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        logger.info("=== Test Suite Finished. Closing shared browser resources ===");
        BrowserManager.getManager().closeSharedBrowser();
    }
}