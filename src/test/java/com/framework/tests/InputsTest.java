package com.framework.tests;

import com.framework.pages.InputsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for the Inputs page.
 * Verifies that text can be entered into an input field and retrieved correctly.
 */
public class InputsTest extends BaseTest {

    /**
     * Test that verifies entering text into an input field and reading it back.
     */
    @Test
    public void verifyInputFieldAcceptsText() {
        // 1. Initialize Page Object
        InputsPage inputsPage = new InputsPage();

        // 2. Navigate to the Inputs page
        inputsPage.open();

        // 3. Enter text into the input field
        String testInput = "12345";
        inputsPage.enterNumber(testInput);

        // 4. Verify the input field contains the entered text
        String actualValue = inputsPage.getInputValue();
        Assert.assertEquals(actualValue, testInput,
                "Input field should contain the entered text");

        logger.info("Inputs test passed successfully!");
    }
}
