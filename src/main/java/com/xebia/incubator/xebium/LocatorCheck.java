package com.xebia.incubator.xebium;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.selenium.CommandProcessor;

public class LocatorCheck {

	private static final Logger LOG = LoggerFactory.getLogger(LocatorCheck.class);

	// keywords copied from org.openqa.selenium.WebDriverCommandProcessor
	private static final Set<String> LOCATOR_COMMANDS = new HashSet<String>(Arrays.asList(new String[] {
		"addSelection", // loc
		"assignId", // loc
		"click", // loc
		"check", // loc
		"doubleClick", // loc
		"dragdrop", // loc
		"dragAndDrop", // loc
		"dragAndDropToObject", // loc, loc
		"focus", // loc
		"highlight", // loc
		"removeSelection", // loc
		"select", // loc
		"submit", // loc
		"type", // loc
		"typeKeys", // loc
		"uncheck", // loc
	}));

	private CommandProcessor commandProcessor;
	
	
	public LocatorCheck(CommandProcessor commandProcessor) {
		super();
		this.commandProcessor = commandProcessor;
	}

	boolean verifyElementPresent(final ExtendedSeleniumCommand command, final String[] values) {
		final String methodName = command.getSeleniumCommand();
		
		if (LOCATOR_COMMANDS.contains(methodName)
				&& !commandProcessor.getBoolean("isElementPresent", new String[] { values[0] })) {
			LOG.warn("Element " + values[0] + " is not found.");
			return false;
		}
		return true;
	}
}
