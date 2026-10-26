package com.framework.tests;

import com.framework.pages.CheckboxesPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CheckboxesTest extends BaseTest {

    @Test
    public void verifyCheckboxesPageHeader() {
        // 1. Initialize Page Object
        CheckboxesPage checkboxesPage = new CheckboxesPage();

        // 2. Navigate to the specific page
        checkboxesPage.open();

        // 3. Verify the header text
        String actualHeader = checkboxesPage.getHeaderText();
        Assert.assertEquals(actualHeader, "Checkboxes", "Page header should be 'Checkboxes'");

        logger.info("Checkboxes page header verified successfully!");
    }
}
