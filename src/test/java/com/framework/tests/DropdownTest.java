package com.framework.tests;

import com.framework.pages.DropdownPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DropdownTest extends BaseTest {

    @Test
    public void verifyDropdownSelection() {
        DropdownPage dropdownPage = new DropdownPage();
        dropdownPage.open();

        dropdownPage.selectOption("1");

        Assert.assertEquals(dropdownPage.getSelectedValue(), "1", "Option 1 should be selected");
        logger.info("Dropdown test passed successfully!");
    }
}
