package com.framework.pages;

import com.framework.pages.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class CheckboxesPage extends BasePage {

    /**
     * Default constructor.
     * Automatically binds this Page Object to the currently active Page from BrowserManager's stack.
     * Use this for standard scenarios where you work with a single tab.
     */
    public CheckboxesPage() {
        super();
    }

    /**
     * Parameterized constructor for advanced scenarios.
     * Binds this Page Object to a specific Page instance provided by the caller.
     * Use this when dealing with multiple tabs, pop-ups, or isolated contexts.
     * Example: after opening a new tab, pass it here to ensure all actions target that specific tab.
     *
     * @param specificPage The exact Page instance this Page Object should interact with.
     */
    public CheckboxesPage(Page specificPage) {
        super(specificPage);
    }

    private final Locator header = page.locator("h3");

    public void open() {
        logger.info("Opening Checkboxes page");
        navigateTo("/checkboxes");
    }

    public String getHeaderText() {
        logger.info("Getting header text from Checkboxes page");
        return getText(header);
    }
}