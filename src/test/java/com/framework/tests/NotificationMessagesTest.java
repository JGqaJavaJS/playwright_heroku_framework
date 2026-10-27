package com.framework.tests;

import com.framework.pages.NotificationMessagesPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for the Notification Messages page.
 * Verifies that clicking the link generates a valid notification message.
 */
public class NotificationMessagesTest extends BaseTest {

    @Test
    public void verifyNotificationMessageAppears() {
        // 1. Initialize Page Object
        NotificationMessagesPage notificationPage = new NotificationMessagesPage();

        // 2. Navigate to the page
        notificationPage.open();

        // 3. Perform action: click 'click here'
        notificationPage.clickClickHere();

        // 4. Verify the flash message contains one of the expected texts
        // The site randomly shows either "Action successful" or "Action unsuccessful"
        String message = notificationPage.getFlashMessageText();
        Assert.assertTrue(
                message.contains("Action successful") || message.contains("Action unsuccesful"),
                "Flash message should contain a valid notification text. Actual: " + message
        );

        logger.info("Notification Messages test passed successfully!");
    }
}