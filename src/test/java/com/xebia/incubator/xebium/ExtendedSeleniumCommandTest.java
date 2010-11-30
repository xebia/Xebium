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
	
	
}
