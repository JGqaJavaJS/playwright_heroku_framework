package com.framework.pages;

import com.framework.pages.base.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object for the Notification Messages page.
 * Tests clicking a link and reading a dynamically generated flash message.
 */
public class NotificationMessagesPage extends BasePage {

    public NotificationMessagesPage() {
        super();
    }

    public NotificationMessagesPage(Page specificPage) {
        super(specificPage);
    }

    private final Locator clickHereLink = page.locator("a", new Page.LocatorOptions().setHasText("click here"));
    private final Locator flashMessage = page.locator("#flash");

    /**
     * Opens the Notification Messages page.
     */
    public void open() {
        logger.info("Opening Notification Messages page");
        navigateTo("/notification_message_rendered");
    }

    /**
     * Clicks the 'click here' link to trigger a new notification message.
     */
    public void clickClickHere() {
        logger.info("Clicking 'click here' link");
        click(clickHereLink);
    }

    /**
     * Gets the text of the flash notification message.
     *
     * @return The text content of the notification message.
     */
    public String getFlashMessageText() {
        logger.info("Getting flash message text");
        return getText(flashMessage);
    }
}
