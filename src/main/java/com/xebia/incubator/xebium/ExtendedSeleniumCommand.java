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

import com.google.common.base.CaseFormat;

import static org.apache.commons.lang.StringUtils.removeStartIgnoreCase;
import static org.apache.commons.lang.StringUtils.trim;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class provides calls to all operations defined in the Selenium IDE.
 * 
 * @author arjan
 *
 */
public class ExtendedSeleniumCommand {

    private enum Verb {
        GET, IS, STORE, VERIFY, ASSERT, WAIT_FOR;

        String getPrefix() {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
        }

        public String applyTo(String noun) {
            return getPrefix() + noun;
        }
    }

	private static final String AND_WAIT = "AndWait";

	// Matching types
	private static final String REGEXP = "regexp:";
	private static final String REGEXPI = "regexpi:";
	private static final String EXACT = "exact:";
	private static final String GLOB = "glob:";

	// keywords copied from org.openqa.selenium.WebDriverCommandProcessor
	private static final Set<String> WEB_DRIVER_COMMANDS = new HashSet<String>(Arrays.asList(new String[] {
		"addLocationStrategy",
		"addSelection", // loc
		"altKeyDown",
		"altKeyUp",
		"assignId", // loc
		"attachFile",
		"captureScreenshotToString",
		"click", // loc
		"check", // loc
		"chooseCancelOnNextConfirmation",
		"chooseOkOnNextConfirmation",
		"close",
		"createCookie",
		"controlKeyDown",
		"controlKeyUp",
		"deleteAllVisibleCookies",
		"deleteCookie",
		"doubleClick", // loc
		"dragdrop", // loc
		"dragAndDrop", // loc
		"dragAndDropToObject", // loc, loc
		"fireEvent",
		"focus", // loc
		"getAlert",
		"getAllButtons", // arr
		"getAllFields", // arr
		"getAllLinks", // arr
		"getAllWindowTitles", // arr
		"getAttribute",
		"getAttributeFromAllWindows", // arr
		"getBodyText",
		"getConfirmation",
		"getCookie",
		"getCookieByName",
		"getCssCount", // num
		"getElementHeight", // num
		"getElementIndex", // num
		"getElementPositionLeft", // num
		"getElementPositionTop", // num
		"getElementWidth", // num
		"getEval",
		"getExpression",
		"getHtmlSource",
		"getLocation",
		"getSelectedId",
		"getSelectedIds", // arr 
		"getSelectedIndex",
		"getSelectedIndexes", // arr
		"getSelectedLabel",
		"getSelectedLabels", // arr
		"getSelectedValue",
		"getSelectedValues", // arr
		"getSelectOptions", // arr
		"getSpeed",
		"getTable",
		"getText",
		"getTitle",
		"getValue",
		"getXpathCount",
		"goBack",
		"highlight", // loc
		"isAlertPresent",
		"isChecked",
		"isConfirmationPresent",
		"isCookiePresent",
		"isEditable",
		"isElementPresent",
		"isOrdered",
		"isSomethingSelected",
		"isTextPresent",
		"isVisible",
		"keyDown",
		"keyPress",
		"keyUp",
		"metaKeyDown",
		"metaKeyUp",
		"mouseOver",
		"mouseOut",
		"mouseDown",
		"mouseDownAt",
		"mouseMove",
		"mouseMoveAt",
		"mouseUp",
		"mouseUpAt",
		"open",
		"openWindow",
		"refresh",
		"removeAllSelections",
		"removeSelection", // loc
		"runScript",
		"select", // loc
		"selectFrame",
		"selectWindow",
		"setBrowserLogLevel",
		"setContext",
		"setSpeed",
		"setTimeout",
		"shiftKeyDown",
		"shiftKeyUp",
		"submit", // loc
		"type", // loc
		"typeKeys", // loc
		"uncheck", // loc
		"useXpathLibrary",
		"waitForCondition",
		"waitForFrameToLoad",
		"waitForPageToLoad",
		"waitForPopUp",
		"windowFocus",
		"windowMaximize"
	}));

//	GetCssCount.java
//	GetElementHeight.java
//	GetElementIndex.java
//	GetElementPositionLeft.java
//	GetElementPositionTop.java
//	GetElementWidth.java
//	GetXpathCount.java

	// Commands that return 
	private static final Set<String> ARRAY_COMMANDS = new HashSet<String>(Arrays.asList(new String[] {
		"getAllButtons", // arr
		"getAllFields", // arr
		"getAllLinks", // arr
		"getAllWindowTitles", // arr
		"getAttributeFromAllWindows", // arr
		"getSelectedIds", // arr 
		"getSelectedIndexes", // arr
		"getSelectedLabels", // arr
		"getSelectedValues", // arr
		"getSelectOptions" // arr
	}));

	private String methodName;
	
	// The real selenium command name, assigned by getSeleniumCommand() on first invocation.
	private String seleniumCommandName;
	
	public ExtendedSeleniumCommand(String methodName) {
		this.methodName = methodName;
	}

	public static boolean isSupportedByWebDriver(String methodName) {
		return WEB_DRIVER_COMMANDS.contains(methodName);
	}
	
	public boolean isWaitForCommand() {
		return isCommand(methodName, Verb.WAIT_FOR) && !isSupportedByWebDriver(methodName);
	}
	
	/**
	 * Command is a "waitFor-" command, but is not supported natively by Selenium,
	 * hence it should act as "verify-" command and in case of failure retries
	 * should be done.
	 * 
	 * @return
	 */
	public boolean requiresPolling() {
		return isWaitForCommand() && !isSupportedByWebDriver(methodName);
	}
	
	public boolean isAndWaitCommand() {
		return  methodName.endsWith(AND_WAIT);
	}
	
	// TODO: process this in JS
	public boolean isNegateCommand() {
		return methodName.matches(".*[a-z]Not[A-Z].*");
	}
	
	public boolean isAssertCommand() {
		return isCommand(methodName, Verb.ASSERT);
	}

    public boolean isVerifyCommand() {
		return isCommand(methodName, Verb.VERIFY);
	}
	
	public boolean isStoreCommand() {
		return isCommand(methodName, Verb.STORE);
	}
	
	public boolean isCaptureEntirePageScreenshotCommand() {
		return methodName.startsWith("captureEntirePageScreenshot");
	}

	public boolean isBooleanCommand() {
		return isCommand(getSeleniumCommand(), Verb.IS);
	}

    private boolean isCommand(String name, Verb... verbs) {
        for (Verb verb : verbs) {
            if (name.startsWith(verb.getPrefix())) {
                return true;
            }
        }
        return false;
    }

    public String getSeleniumCommand() {
		
		// fast track: once resolved return the previous value.
		if (seleniumCommandName != null) {
			return seleniumCommandName;
		}
		
		// for commands like "waitForCondition"
		if (isSupportedByWebDriver(methodName)) {
			seleniumCommandName = methodName;
			return methodName;
		}
		
		String seleniumName = methodName;
		
		if (isCommand(seleniumName, Verb.ASSERT, Verb.VERIFY, Verb.STORE, Verb.WAIT_FOR, Verb.IS)) {
			String noun = getNoun(seleniumName);

			if (isSupportedByWebDriver(Verb.IS.applyTo(noun))) {
				seleniumName = Verb.IS.applyTo(noun);
			} else if (isSupportedByWebDriver(Verb.GET.applyTo(noun))) {
				seleniumName = Verb.GET.applyTo(noun);
			}
		} else if (isCaptureEntirePageScreenshotCommand()) {
			seleniumName = "captureScreenshotToString";
		}
		
		if (isAndWaitCommand()) {
			seleniumName = seleniumName.substring(0, seleniumName.length() - AND_WAIT.length());
		}
		
		seleniumCommandName = seleniumName;
		
		return seleniumName;
	}

    private String getNoun(String seleniumName) {
        String verbAndNoun;

        if (isNegateCommand()) {
            //noun = noun.substring(0, noun.length() - NOT_PRESENT.length()) + "Present";
            verbAndNoun = seleniumName.replaceAll("([a-z])Not([A-Z])", "$1$2");
        } else {
            verbAndNoun = seleniumName;
        }

        return removeVerb(verbAndNoun);
    }

    private String removeVerb(String verbAndNoun) {
        for (Verb verb : Verb.values()) {
            String prefix = verb.getPrefix();
            if (verbAndNoun.startsWith(prefix)) {
                return verbAndNoun.substring(prefix.length());
            }
        }
        throw new IllegalArgumentException("'" + verbAndNoun + "' does not start with a verb");
    }

    /**
	 * <p><i>(From the Selenium docs)</i></p>
	 * <p>
	 * Various Pattern syntaxes are available for matching string values:
	 * </p>
	 * <ul>
	 * <li><strong>glob:</strong><em>pattern</em>: Match a string against a
	 * "glob" (aka "wildmat") pattern. "Glob" is a kind of limited
	 * regular-expression syntax typically used in command-line shells. In a
	 * glob pattern, "*" represents any sequence of characters, and "?"
	 * represents any single character. Glob patterns match against the entire
	 * string.</li>
	 * <li><strong>regexp:</strong><em>regexp</em>: Match a string using a
	 * regular-expression. The full power of JavaScript regular-expressions is
	 * available.</li>
	 * <li><strong>regexpi:</strong><em>regexpi</em>: Match a string using a
	 * case-insensitive regular-expression.</li>
	 * <li><strong>exact:</strong><em>string</em>:
	 * 
	 * Match a string exactly, verbatim, without any of that fancy wildcard
	 * stuff.</li>
	 * </ul>
	 * <p>
	 * If no pattern prefix is specified, Selenium assumes that it's a "glob"
	 * pattern.
	 * </p>
	 * <p>
	 * For commands that return multiple values (such as verifySelectOptions),
	 * the string being matched is a comma-separated list of the return values,
	 * where both commas and backslashes in the values are backslash-escaped.
	 * When providing a pattern, the optional matching syntax (i.e. glob,
	 * regexp, etc.) is specified once, as usual, at the beginning of the
	 * pattern.
	 * </p>
	 * 
	 * @param expected expected result, optionally prefixed
	 * @param actual Actual result coming from Selenium
	 * @return is it a match?
	 */
	public boolean matches(String expected, String actual) {
		boolean result;
		
		// Be graceful with empty strings
		if (actual == null) {
			actual = "";
		}
		
		if (isBooleanCommand()) {
			result = "true".equals(actual);
			
		} else if (expected.startsWith(REGEXP)) {
			final String regex = trim(removeStartIgnoreCase(expected, REGEXP));
			result = Pattern.compile(regex, Pattern.DOTALL).matcher(actual).matches();
			
		} else if (expected.startsWith(REGEXPI)) {
			final String regex = trim(removeStartIgnoreCase(expected, REGEXPI));
			result = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(actual).matches();
			
		} else if (expected.startsWith(EXACT)){
			final String str = trim(removeStartIgnoreCase(expected, EXACT));
			result = str.equals(actual);
			
		} else {
			// "glob:"
			final String pattern;
			if (expected.startsWith(GLOB)) {
				pattern = trim(removeStartIgnoreCase(expected, GLOB));
			} else {
				pattern = expected;
			}
			result = globToRegExp(pattern).matcher(actual).matches();
		}
		if (isNegateCommand()) {
			result = !result;
		}
		return result;
	}

	private Pattern globToRegExp(String glob) {
	    return Pattern.compile(
	        "^\\Q" 
	        + glob.replace("*", "\\E.*\\Q")
	              .replace("?", "\\E.\\Q") 
	        + "\\E$", Pattern.DOTALL);
	}

	/**
	 * @return true if an array is returned.
	 */
	public boolean returnTypeIsArray() {
		return ARRAY_COMMANDS.contains(getSeleniumCommand());
	}

}
