/*
 * Copyright 2010-2012 Xebia b.v.
 * Copyright 2010-2012 Xebium contributers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
		"uncheck" // loc
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
