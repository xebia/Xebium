package com.xebia.incubator.xebium;

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

	private static final String GET = "get";

	private static final String IS = "is";

	private static final String STORE = "store";

	private static final String VERIFY = "verify";

	private static final String ASSERT = "assert";

	private static final String WAIT_FOR = "waitFor";
	
	private static final String AND_WAIT = "AndWait";

	// Matching types
	private static final String REGEXP = "regexp:";
	private static final String REGEXPI = "regexpi:";
	private static final String EXACT = "exact:";
	private static final String GLOB = "glob:";

	// keywords copied from org.openqa.selenium.WebDriverCommandProcessor
	private static final Set<String> WEB_DRIVER_COMMANDS = new HashSet<String>(Arrays.asList(new String[] {
		"addLocationStrategy",
		"addSelection",
		"altKeyDown",
		"altKeyUp",
		"assignId",
		"attachFile",
		"captureScreenshotToString",
		"click",
		"check",
		"chooseCancelOnNextConfirmation",
		"chooseOkOnNextConfirmation",
		"close",
		"createCookie",
		"controlKeyDown",
		"controlKeyUp",
		"deleteAllVisibleCookies",
		"deleteCookie",
		"doubleClick",
		"dragdrop",
		"dragAndDrop",
		"dragAndDropToObject",
		"fireEvent",
		"focus",
		"getAlert",
		"getAllButtons",
		"getAllFields",
		"getAllLinks",
		"getAllWindowTitles",
		"getAttribute",
		"getAttributeFromAllWindows",
		"getBodyText",
		"getConfirmation",
		"getCookie",
		"getCookieByName",
		"getElementHeight",
		"getElementIndex",
		"getElementPositionLeft",
		"getElementPositionTop",
		"getElementWidth",
		"getEval",
		"getExpression",
		"getHtmlSource",
		"getLocation",
		"getSelectedId",
		"getSelectedIds",
		"getSelectedIndex",
		"getSelectedIndexes",
		"getSelectedLabel",
		"getSelectedLabels",
		"getSelectedValue",
		"getSelectedValues",
		"getSelectOptions",
		"getSpeed",
		"getTable",
		"getText",
		"getTitle",
		"getValue",
		"getXpathCount",
		"goBack",
		"highlight",
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
		"removeSelection",
		"runScript",
		"select",
		"selectFrame",
		"selectWindow",
		"setBrowserLogLevel",
		"setContext",
		"setSpeed",
		"setTimeout",
		"shiftKeyDown",
		"shiftKeyUp",
		"submit",
		"type",
		"typeKeys",
		"uncheck",
		"useXpathLibrary",
		"waitForCondition",
		"waitForFrameToLoad",
		"waitForPageToLoad",
		"waitForPopUp",
		"windowFocus",
		"windowMaximize"
	}));

	private String methodName;
	
	public ExtendedSeleniumCommand(String methodName) {
		this.methodName = methodName;
	}

	public static boolean isSupportedByWebDriver(String methodName) {
		return WEB_DRIVER_COMMANDS.contains(methodName);
	}
	
	public boolean isWaitForCommand() {
		return methodName.startsWith(WAIT_FOR);
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
		return methodName.startsWith(ASSERT);
	}
	
	public boolean isVerifyCommand() {
		return methodName.startsWith(VERIFY);
	}
	
	public boolean isStoreCommand() {
		return methodName.startsWith(STORE);
	}
	
	public boolean isCaptureEntirePageScreenshotCommand() {
		return methodName.startsWith("captureEntirePageScreenshot");
	}

	public boolean isBooleanCommand() {
		return getSeleniumCommand().startsWith(IS);
	}
	
	public String getSeleniumCommand() {
		// for commands like "waitForCondition"
		if (WEB_DRIVER_COMMANDS.contains(methodName)) {
			return methodName;
		}
		
		String seleniumName = methodName;
		
		if (isAssertCommand() || isVerifyCommand() || isStoreCommand() || isWaitForCommand()) {
			String noun = seleniumName;
			
			if (isNegateCommand()) {
				//noun = noun.substring(0, noun.length() - NOT_PRESENT.length()) + "Present";
				noun = noun.replaceAll("([a-z])Not([A-Z])", "$1$2");
			}
			
			// ASSERT.length() == VERIFY.length()
			noun = noun.substring(isStoreCommand() ? STORE.length() :
				(isWaitForCommand() ? WAIT_FOR.length() : ASSERT.length()));
			
			if (isSupportedByWebDriver(IS + noun)) {
				seleniumName = IS + noun;
			} else if (isSupportedByWebDriver(GET + noun)) {
				seleniumName = GET + noun;
			}
		} else if (isCaptureEntirePageScreenshotCommand()) {
			seleniumName = "captureScreenshotToString";
		}
		
		if (isAndWaitCommand()) {
			seleniumName = seleniumName.substring(0, seleniumName.length() - AND_WAIT.length());
		}
		return seleniumName;
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
		if (isBooleanCommand()) {
			result = (isNegateCommand() ? "false" : "true").equals(actual);
			
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
		return result;
	}

	private Pattern globToRegExp(String pattern) {
		return Pattern.compile(pattern.replaceAll("\\?", ".").replaceAll("\\*", ".*"), Pattern.DOTALL);
	}

}
