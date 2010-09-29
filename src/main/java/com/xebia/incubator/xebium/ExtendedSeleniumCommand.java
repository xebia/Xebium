package com.xebia.incubator.xebium;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides calls to all operations defined in the Selenium IDE.
 * 
 * @author arjan
 *
 */
public class ExtendedSeleniumCommand {

	private static final String VERIFY = "verify";

	private static final String ASSERT = "assert";

	private static final String NOT_PRESENT = "NotPresent";

	private static final String AND_WAIT = "AndWait";

	// keywords copied from org.openqa.selenium.WebDriverCommandProcessor
	private static final Set<String> SELENIUM_COMMANDS = new HashSet<String>(Arrays.asList(new String[] {
		"addLocationStrategy",
		"addScript",
		"addSelection",
		"allowNativeXpath",
		"altKeyDown",
		"altKeyUp",
		"answerOnNextPrompt",
		"assertErrorOnNext",
		"assertFailureOnNext",
		"assertSelected",
		"assignId",
		"attachFile",
		"break",
		"captureEntirePageScreenshot",
		"captureEntirePageScreenshotToString",
		"captureScreenshot",
		"captureScreenshotToString",
		"check",
		"chooseCancelOnNextConfirmation",
		"chooseOkOnNextConfirmation",
		"click",
		"clickAt",
		"close",
		"contextMenu",
		"contextMenuAt",
		"controlKeyDown",
		"controlKeyUp",
		"createCookie",
		"deleteAllVisibleCookies",
		"deleteCookie",
		"deselectPopUp",
		"doubleClick",
		"doubleClickAt",
		"dragAndDrop",
		"dragAndDropToObject",
		"dragdrop",
		"echo",
		"fireEvent",
		"focus",
		"getAlert",
		"getAllButtons",
		"getAllFields",
		"getAllLinks",
		"getAllWindowIds",
		"getAllWindowNames",
		"getAllWindowTitles",
		"getAttribute",
		"getAttributeFromAllWindows",
		"getBodyText",
		"getConfirmation",
		"getCookie",
		"getCookieByName",
		"getCursorPosition",
		"getElementHeight",
		"getElementIndex",
		"getElementPositionLeft",
		"getElementPositionTop",
		"getElementWidth",
		"getEval",
		"getExpression",
		"getHtmlSource",
		"getLocation",
		"getMouseSpeed",
		"getPrompt",
		"getSelectOptions",
		"getSelectedId",
		"getSelectedIds",
		"getSelectedIndex",
		"getSelectedIndexes",
		"getSelectedLabel",
		"getSelectedLabels",
		"getSelectedValue",
		"getSelectedValues",
		"getSpeed",
		"getTable",
		"getText",
		"getTitle",
		"getValue",
		"getWhetherThisFrameMatchFrameExpression",
		"getWhetherThisWindowMatchWindowExpression",
		"getXpathCount",
		"goBack",
		"highlight",
		"ignoreAttributesWithoutValue",
		"isAlertPresent",
		"isChecked",
		"isConfirmationPresent",
		"isCookiePresent",
		"isEditable",
		"isElementPresent",
		"isOrdered",
		"isPromptPresent",
		"isSomethingSelected",
		"isTextPresent",
		"isVisible",
		"keyDown",
		"keyDownNative",
		"keyPress",
		"keyPressNative",
		"keyUp",
		"keyUpNative",
		"metaKeyDown",
		"metaKeyUp",
		"mouseDown",
		"mouseDownAt",
		"mouseDownRight",
		"mouseDownRightAt",
		"mouseMove",
		"mouseMoveAt",
		"mouseOut",
		"mouseOver",
		"mouseUp",
		"mouseUpAt",
		"mouseUpRight",
		"mouseUpRightAt",
		"open",
		"openWindow",
		"pause",
		"refresh",
		"removeAllSelections",
		"removeScript",
		"removeSelection",
		"retrieveLastRemoteControlLogs",
		"rollup",
		"runScript",
		"select",
		"selectFrame",
		"selectPopUp",
		"selectWindow",
		"setBrowserLogLevel",
		"setContext",
		"setCursorPosition",
		"setMouseSpeed",
		"setSpeed",
		"setTimeout",
		"shiftKeyDown",
		"shiftKeyUp",
		"shutDownSeleniumServer",
		"store",
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

	public boolean isAndWaitCommand() {
		return methodName.endsWith(AND_WAIT);
	}
	
	// TODO: process this in JS
	public boolean isNegateCommand() {
		return methodName.endsWith(NOT_PRESENT);
	}
	
	public boolean isAssertCommand() {
		return methodName.startsWith(ASSERT);
	}
	
	public boolean isVerifyCommand() {
		return methodName.startsWith(VERIFY);
	}
	
	public String getSeleniumCommand() {
		if (SELENIUM_COMMANDS.contains(methodName)) {
			return methodName;
		} else if (isAssertCommand() || isVerifyCommand()) {
			String name = methodName.substring(6);
			
			if (isNegateCommand()) {
				name = name.substring(0, name.length() - NOT_PRESENT.length()) + "Present";
			}
			
			if (SELENIUM_COMMANDS.contains("is" + name)) {
				return "is" + name;
			} else if (SELENIUM_COMMANDS.contains("get" + name)) {
				return "get" + name;
			}
		} else if (isAndWaitCommand()) {
			return methodName.substring(0, methodName.length() - AND_WAIT.length());
		}
		return null;
	}
}
