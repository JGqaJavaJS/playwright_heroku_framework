package com.framework.tests;

import com.framework.core.manager.BrowserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;

/**
 * Base class for all TestNG test classes.
 * Manages the test lifecycle by setting up and tearing down the isolated
 * browser environment for each test class.
 */
public abstract class BaseTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Executes before the first test method in the current class.
     * Initializes an isolated BrowserContext and a fresh Page stack for this thread.
     */
    @BeforeClass
    public void setUpClass() {
        logger.info("=== Starting Test Class: {} ===", getClass().getSimpleName());
        BrowserManager.getManager().setupTestEnvironment();
    }

    /**
     * Executes after the last test method in the current class.
     * Closes all pages, the BrowserContext, and clears ThreadLocal storage.
     * alwaysRun = true ensures cleanup even if the test fails.
     */
    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        logger.info("=== Finished Test Class: {} ===", getClass().getSimpleName());
        BrowserManager.getManager().teardownTestEnvironment();
    }

    /**
     * Executes once after all tests in the suite have finished.
     * Closes the shared Browser and Playwright instances to free up system resources.
     * alwaysRun = true ensures global cleanup at the end of the run.
     */
    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        logger.info("=== Test Suite Finished. Closing shared browser resources ===");
        BrowserManager.getManager().closeSharedBrowser();
    }
}
