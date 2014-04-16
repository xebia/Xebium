package com.xebia.incubator.xebium.fastseleniumemulation;

import com.thoughtworks.selenium.webdriven.JavascriptLibrary;
import com.thoughtworks.selenium.webdriven.WebDriverCommandProcessor;
import com.thoughtworks.selenium.webdriven.commands.*;
import org.openqa.selenium.WebDriver;

/**
 * The Selenium WebDriverCommandProcessor relies heavily on javascript.
 *
 * This variation replaces some commands with (hopefully) more efficient webdriver calls, providing better
 * performance at the cost of some backwards-compatibility.
 */
public class FastWebDriverCommandProcessor extends WebDriverCommandProcessor {

    private boolean enableAlertOverrides = true;

    public FastWebDriverCommandProcessor(String baseUrl, WebDriver webDriver) {
        super(baseUrl, webDriver);

		setUpMethodMap();
	}

    @Override
    public void setEnableAlertOverrides(boolean enableAlertOverrides) {
        this.enableAlertOverrides = enableAlertOverrides;
        super.setEnableAlertOverrides(enableAlertOverrides);
    }

    @Override
    public void start(Object o) {
        super.start(o);

		setUpMethodMap();
	}

	private void setUpMethodMap() {
		JavascriptLibrary javascriptLibrary = new JavascriptLibrary();
        AlertOverride alertOverride = new AlertOverride(enableAlertOverrides);
        ElementFinder elementFinder = new ElementFinder(javascriptLibrary);

		// Methods for which we use the original implementation, but now with our custom finder
		// Yes, terrible duplication, but this is the only way we keep sort-of compatible with the original without
		// resorting to even uglier methods like reflection...
		addMethod("addLocationStrategy", new AddLocationStrategy(elementFinder));
		addMethod("addSelection", new AddSelection(javascriptLibrary, elementFinder));
		addMethod("assignId", new AssignId(javascriptLibrary, elementFinder));
		addMethod("attachFile", new AttachFile(elementFinder));
		addMethod("click", new Click(alertOverride, elementFinder));
		addMethod("clickAt", new ClickAt(alertOverride, elementFinder));
		addMethod("check", new Check(alertOverride, elementFinder));
		addMethod("doubleClick", new DoubleClick(alertOverride, elementFinder));
		addMethod("dragdrop", new DragAndDrop(elementFinder));
		addMethod("dragAndDrop", new DragAndDrop(elementFinder));
		addMethod("dragAndDropToObject", new DragAndDropToObject(elementFinder));
		addMethod("fireEvent", new FireEvent(elementFinder, javascriptLibrary));
		addMethod("focus", new FireNamedEvent(elementFinder, javascriptLibrary, "focus"));
		addMethod("getAttribute", new GetAttribute(javascriptLibrary, elementFinder));
		addMethod("getElementHeight", new GetElementHeight(elementFinder));
		addMethod("getElementIndex", new GetElementIndex(elementFinder,
				javascriptLibrary));
		addMethod("getElementPositionLeft", new GetElementPositionLeft(elementFinder));
		addMethod("getElementPositionTop", new GetElementPositionTop(elementFinder));
		addMethod("getElementWidth", new GetElementWidth(elementFinder));
		addMethod("getSelectedId", new FindFirstSelectedOptionProperty(javascriptLibrary,
				elementFinder, "id"));
		addMethod("getSelectedIds", new FindSelectedOptionProperties(javascriptLibrary,
				elementFinder, "id"));
		addMethod("getSelectedIndex", new FindFirstSelectedOptionProperty(javascriptLibrary,
				elementFinder, "index"));
		addMethod("getSelectedIndexes", new FindSelectedOptionProperties(javascriptLibrary,
				elementFinder, "index"));
		addMethod("getSelectedLabel", new FindFirstSelectedOptionProperty(javascriptLibrary,
				elementFinder, "text"));
		addMethod("getSelectedLabels", new FindSelectedOptionProperties(javascriptLibrary,
				elementFinder, "text"));
		addMethod("getSelectedValue", new FindFirstSelectedOptionProperty(javascriptLibrary,
				elementFinder, "value"));
		addMethod("getSelectedValues", new FindSelectedOptionProperties(javascriptLibrary,
				elementFinder, "value"));
		addMethod("getSelectOptions", new GetSelectOptions(javascriptLibrary, elementFinder));
		addMethod("getTable", new GetTable(elementFinder, javascriptLibrary));
		addMethod("getText", new com.thoughtworks.selenium.webdriven.commands.GetText(javascriptLibrary, elementFinder));
		addMethod("getValue", new GetValue(elementFinder));
		addMethod("highlight", new Highlight(elementFinder, javascriptLibrary));
		addMethod("isChecked", new IsChecked(elementFinder));
		addMethod("isEditable", new IsEditable(elementFinder));
		addMethod("isElementPresent", new IsElementPresent(elementFinder));
		addMethod("isOrdered", new IsOrdered(elementFinder, javascriptLibrary));
		addMethod("isSomethingSelected", new IsSomethingSelected(javascriptLibrary));
		addMethod("isTextPresent", new com.thoughtworks.selenium.webdriven.commands.IsTextPresent(javascriptLibrary));
		addMethod("isVisible", new IsVisible(elementFinder));
		addMethod("keyPress", new TypeKeys(alertOverride, elementFinder));
		addMethod("mouseOver", new MouseEvent(elementFinder, javascriptLibrary, "mouseover"));
		addMethod("mouseOut", new MouseEvent(elementFinder, javascriptLibrary, "mouseout"));
		addMethod("mouseDown", new MouseEvent(elementFinder, javascriptLibrary, "mousedown"));
		addMethod("mouseDownAt", new MouseEventAt(elementFinder, javascriptLibrary,
				"mousedown"));
		addMethod("mouseMove", new MouseEvent(elementFinder, javascriptLibrary, "mousemove"));
		addMethod("mouseMoveAt", new MouseEventAt(elementFinder, javascriptLibrary,
				"mousemove"));
		addMethod("mouseUp", new MouseEvent(elementFinder, javascriptLibrary, "mouseup"));
		addMethod("mouseUpAt", new MouseEventAt(elementFinder, javascriptLibrary, "mouseup"));
		addMethod("removeAllSelections", new RemoveAllSelections(elementFinder));
		addMethod("removeSelection", new RemoveSelection(javascriptLibrary, elementFinder));
		addMethod("select",
				new SelectOption(alertOverride, javascriptLibrary, elementFinder));
		addMethod("submit", new Submit(alertOverride, elementFinder));
		addMethod("typeKeys", new TypeKeys(alertOverride, elementFinder));
		addMethod("uncheck", new Uncheck(alertOverride, elementFinder));

		// Methods for which we use another default implementation
		addMethod("type", new Type(alertOverride, elementFinder));

		// Methods for which we use a custom implementation
        addMethod("getText", new GetText(elementFinder));
		// Not really equivalent... see also http://rostislav-matl.blogspot.nl/2011/03/moving-to-selenium-2-on-webdriver-part.html
        //addMethod("isTextPresent", new IsTextPresent());

        addMethod("windowMaximize", new WindowMaximize());
    }
}