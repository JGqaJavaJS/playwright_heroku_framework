package com.framework.pages;

import com.framework.pages.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object for the Inputs page (https://the-internet.herokuapp.com/inputs).
 * Contains a single input field for testing text entry.
 */
public class InputsPage extends BasePage {

    /**
     * Default constructor.
     * Automatically binds this Page Object to the currently active Page from BrowserManager's stack.
     */
    public InputsPage() {
        super();
    }

    /**
     * Parameterized constructor for advanced scenarios.
     * Binds this Page Object to a specific Page instance provided by the caller.
     *
     * @param specificPage The exact Page instance this Page Object should interact with.
     */
    public InputsPage(Page specificPage) {
        super(specificPage);
    }

    private final Locator numberInput = page.locator("input[type='number']");

    /**
     * Opens the Inputs page by navigating to its relative path.
     */
    public void open() {
        logger.info("Opening Inputs page");
        navigateTo("/inputs");
    }

    /**
     * Enters the specified text into the number input field.
     *
     * @param text The text to enter into the input field.
     */
    public void enterNumber(String text) {
        logger.info("Entering '{}' into number input", text);
        enterText(numberInput, text);
    }

    /**
     * Gets the current value from the number input field.
     *
     * @return The text value currently in the input field.
     */
    public String getInputValue() {
        logger.info("Getting value from number input");

        // Micro-wait to ensure legacy JS has updated the DOM value property after fill()
        page.waitForTimeout(100);

        return getInputValue(numberInput);
    }
}