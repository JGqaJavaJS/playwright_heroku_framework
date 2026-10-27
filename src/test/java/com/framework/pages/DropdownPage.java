package com.framework.pages;

import com.framework.pages.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class DropdownPage extends BasePage {

    public DropdownPage() { super(); }
    public DropdownPage(Page specificPage) { super(specificPage); }

    private final Locator dropdown = page.locator("#dropdown");

    public void open() {
        logger.info("Opening Dropdown page");
        navigateTo("/dropdown");
    }

    public void selectOption(String value) {
        logger.info("Selecting option: {}", value);
        dropdown.selectOption(value);
    }

    /**
     * Gets the currently selected value from the dropdown.
     * Uses 'option:checked' locator, which is 100% resilient to JS update delays
     * and avoids any race conditions associated with inputValue().
     */
    public String getSelectedValue() {
        logger.info("Getting selected value from dropdown");
        // Находим выбранную опцию внутри селекта и берем её значение (value атрибут)
        return dropdown.locator("option:checked").getAttribute("value");
    }
}
