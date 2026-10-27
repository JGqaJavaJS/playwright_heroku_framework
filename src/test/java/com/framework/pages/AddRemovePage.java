package com.framework.pages;

import com.framework.pages.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object for the Add/Remove Elements page.
 * Tests basic click interactions and dynamic element visibility.
 */
public class AddRemovePage extends BasePage {

    public AddRemovePage() {
        super();
    }

    public AddRemovePage(Page specificPage) {
        super(specificPage);
    }

    private final Locator addButton = page.locator("button", new Page.LocatorOptions().setHasText("Add Element"));
    private final Locator deleteButton = page.locator("button", new Page.LocatorOptions().setHasText("Delete"));

    /**
     * Opens the Add/Remove Elements page.
     */
    public void open() {
        logger.info("Opening Add/Remove Elements page");
        navigateTo("/add_remove_elements/");
    }

    /**
     * Clicks the 'Add Element' button to dynamically create a new 'Delete' button.
     */
    public void clickAddElement() {
        logger.info("Clicking 'Add Element' button");
        click(addButton);
    }

    /**
     * Checks if the 'Delete' button is currently visible on the page.
     *
     * @return true if the button is visible, false otherwise.
     */
    public boolean isDeleteButtonVisible() {
        logger.info("Checking if 'Delete' button is visible");
        return isVisible(deleteButton);
    }
}