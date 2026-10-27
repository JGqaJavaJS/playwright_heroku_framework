package com.framework.tests;

import com.framework.pages.AddRemovePage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for the Add/Remove Elements page.
 * Verifies that clicking 'Add Element' dynamically creates a 'Delete' button.
 */
public class AddRemoveTest extends BaseTest {

    @Test
    public void verifyAddElementAppears() {
        // 1. Initialize Page Object
        AddRemovePage addRemovePage = new AddRemovePage();

        // 2. Navigate to the page
        addRemovePage.open();

        // 3. Perform action: click 'Add Element'
        addRemovePage.clickAddElement();

        // 4. Verify the result: the 'Delete' button should now be visible
        Assert.assertTrue(addRemovePage.isDeleteButtonVisible(),
                "Delete button should appear after clicking 'Add Element'");

        logger.info("Add/Remove test passed successfully!");
    }
}