package com.xebia.incubator.xebium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExtendedSeleniumCommandTest {

	@Test
	public void testAssertTextPresent() {
		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("assertTextPresent");
		
		assertFalse(command.isVerifyCommand());
		assertFalse(command.isNegateCommand());
		assertFalse(command.isWaitForCommand());
		assertFalse(command.isStoreCommand());
		assertFalse(command.isAndWaitCommand());
		assertTrue(command.isAssertCommand());
		assertEquals("isTextPresent", command.getSeleniumCommand());
	}

    @Test
    public void testVerifyTextPresent() {
        ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("verifyTextPresent");

        assertTrue(command.isVerifyCommand());
        assertFalse(command.isNegateCommand());
        assertFalse(command.isWaitForCommand());
        assertFalse(command.isStoreCommand());
        assertFalse(command.isAndWaitCommand());
        assertFalse(command.isAssertCommand());
        assertEquals("isTextPresent", command.getSeleniumCommand());
    }

    @Test
    public void testIsTextNotPresent() {
        ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("isTextNotPresent");

        assertFalse(command.isVerifyCommand());
        assertTrue(command.isNegateCommand());
        assertFalse(command.isWaitForCommand());
        assertFalse(command.isStoreCommand());
        assertFalse(command.isAndWaitCommand());
        assertFalse(command.isAssertCommand());
        assertEquals("isTextPresent", command.getSeleniumCommand());
    }

	@Test
	public void testVerifyTextNotPresent() {
		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("verifyTextNotPresent");
		
		assertTrue(command.isVerifyCommand());
		assertTrue(command.isNegateCommand());
		assertFalse(command.isWaitForCommand());
		assertFalse(command.isStoreCommand());
		assertFalse(command.isAndWaitCommand());
		assertFalse(command.isAssertCommand());
		assertEquals("isTextPresent", command.getSeleniumCommand());
	}
	
	@Test
	public void testWaitForTextPresent() {
		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("waitForTextPresent");
		
		assertFalse(command.isVerifyCommand());
		assertFalse(command.isNegateCommand());
		assertTrue(command.isWaitForCommand());
		assertFalse(command.isStoreCommand());
		assertFalse(command.isAndWaitCommand());
		assertFalse(command.isAssertCommand());
		assertEquals("isTextPresent", command.getSeleniumCommand());
	}

	
	@Test
	public void testWaitForNotTextPresent() {
		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("waitForNotTextPresent");
		
		assertFalse(command.isVerifyCommand());
		assertTrue(command.isNegateCommand());
		assertTrue(command.isWaitForCommand());
		assertFalse(command.isStoreCommand());
		assertFalse(command.isAndWaitCommand());
		assertFalse(command.isAssertCommand());
		assertEquals("isTextPresent", command.getSeleniumCommand());
	}

	@Test
	public void testStoreTextPresent() {
		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("storeTextPresent");
		
		assertFalse(command.isVerifyCommand());
		assertFalse(command.isNegateCommand());
		assertFalse(command.isWaitForCommand());
		assertTrue(command.isStoreCommand());
		assertFalse(command.isAndWaitCommand());
		assertFalse(command.isAssertCommand());
		assertEquals("isTextPresent", command.getSeleniumCommand());
	}


	@Test
	public void testClick() {
		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("click");
		
		assertFalse(command.isVerifyCommand());
		assertFalse(command.isNegateCommand());
		assertFalse(command.isWaitForCommand());
		assertFalse(command.isAndWaitCommand());
		assertFalse(command.isAssertCommand());
		assertEquals("click", command.getSeleniumCommand());
	}
	
	@Test
	public void testClickAndWait() {
		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("clickAndWait");
		
		assertFalse(command.isVerifyCommand());
		assertFalse(command.isNegateCommand());
		assertFalse(command.isWaitForCommand());
		assertTrue(command.isAndWaitCommand());
		assertFalse(command.isAssertCommand());
		assertEquals("click", command.getSeleniumCommand());
	}
	
	@Test
	public void testVerifyNotChecked() {
		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("verifyNotChecked");
		
		assertTrue(command.isVerifyCommand());
		assertTrue(command.isNegateCommand());
		assertFalse(command.isWaitForCommand());
		assertFalse(command.isAndWaitCommand());
		assertFalse(command.isAssertCommand());
		assertEquals("isChecked", command.getSeleniumCommand());
	}
	
	@Test
	public void testVerifyNotText() {
		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("verifyNotText");
		
		assertTrue(command.isVerifyCommand());
		assertTrue(command.isNegateCommand());
		assertFalse(command.isWaitForCommand());
		assertFalse(command.isAndWaitCommand());
		assertFalse(command.isAssertCommand());
		assertEquals("getText", command.getSeleniumCommand());
	}
	
	@Test
	public void testRequiredPollingCommands() {
		assertTrue(new ExtendedSeleniumCommand("waitForTextPresent").requiresPolling());
		assertFalse(new ExtendedSeleniumCommand("waitForPageToLoad").requiresPolling());
	}

	@Test
	public void testRegexpMatch() {
		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("verifyText");
		
		assertTrue(command.matches("regexp:A..D", "ABCD"));
		assertFalse(command.matches("regexp:A..D", "_ABCD_"));
		assertFalse(command.matches("regexp:A..D", "_DBCA_"));
	}

	@Test
	public void testRegexpiMatch() {
		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("verifyText");
		
		assertTrue(command.matches("regexpi:A..D", "ABCD"));
		assertFalse(command.matches("regexpi:A..D", "_ABCD_"));
		assertFalse(command.matches("regexpi:A..D", "_DBCA_"));
		assertTrue(command.matches("regexpi:A..D", "aBCD"));
		assertFalse(command.matches("regexpi:A..D", "_aBCd_"));
	}

	@Test
	public void testExactMatch() {
		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("verifyText");
		
		assertTrue(command.matches("exact:A..D", "A..D"));
		assertFalse(command.matches("exact:A..D", "ABCD"));
	}

	@Test
	public void testGlobMatch() {
		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("verifyText");
		
		assertTrue(command.matches("glob:A*D", "A..D"));
		assertTrue(command.matches("glob:A??D", "ABCD"));
		assertTrue(command.matches("glob:A*D", "A something in the middle D"));
		assertFalse(command.matches("glob:A?D", "ABCD"));
		assertFalse(command.matches("glob:A*D", "DCBA"));
		assertTrue(command.matches("glob:(14)", "(14)"));
	}

	@Test
	public void testGlobAsDefaultMatch() {
		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("verifyText");
		
		assertTrue(command.matches("A*D", "A..D"));
		assertTrue(command.matches("A??D", "ABCD"));
		assertTrue(command.matches("A*D", "A something in the middle D"));
		assertFalse(command.matches("A?D", "ABCD"));
		assertFalse(command.matches("A*D", "DCBA"));
		assertTrue(command.matches("(14)", "(14)"));
		assertTrue(command.matches("A(14)D", "A(14)D"));
		assertFalse(command.matches("A*D", null));
	}

	@Test
	public void testMultiLineRegexp() {
		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand("verifyText");
		String text = "This is a nice\ntext on multiple lines\n\nand it matches the requirements";
		
		assertTrue(command.matches("regexp:.*multiple lines.*", text));
		assertTrue(command.matches("regexpi:.*multiple lines.*", text));
		assertTrue(command.matches("glob:*multiple lines*", text));

	}
}
