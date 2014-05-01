package com.xebia.incubator.xebium.fastseleniumemulation;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.thoughtworks.selenium.webdriven.commands.AlertOverride;
import com.thoughtworks.selenium.webdriven.SeleneseCommand;

public class Type extends SeleneseCommand<Void> {
	private final AlertOverride alertOverride;
	private final ElementFinder finder;

	public Type(AlertOverride alertOverride, ElementFinder finder) {
		this.alertOverride = alertOverride;
		this.finder = finder;
	}

	@Override
	protected Void handleSeleneseCommand(WebDriver driver, String locator, String value) {
		alertOverride.replaceAlertMethod(driver);

		if (value == null) {
			value = "";
		}
		value = value.replace("\\10", Keys.ENTER);
		value = value.replace("\\13", Keys.RETURN);
		value = value.replace("\\27", Keys.ESCAPE);
		value = value.replace("\\38", Keys.ARROW_UP);
		value = value.replace("\\40", Keys.ARROW_DOWN);
		value = value.replace("\\37", Keys.ARROW_LEFT);
		value = value.replace("\\39", Keys.ARROW_RIGHT);

		WebElement element = finder.findElement(driver, locator);

		clear(element);
		element.sendKeys(value);
		triggerEvents(element, driver);

		return null;
	}

	// Make sure onchange/onblur are triggered
	private void triggerEvents(WebElement element, WebDriver driver) {
		if ("input".equalsIgnoreCase(element.getTagName())) {
			driver.findElement(By.tagName("body")).click();
		}
	}

	private void clear(WebElement element) {
		String tagName = element.getTagName();
		String type = element.getAttribute("type");

		if (("input".equalsIgnoreCase(tagName) || "textarea".equalsIgnoreCase(tagName))
				&& element.isEnabled()
				&& !"file".equalsIgnoreCase(type)) {
			element.clear();
		}
	}

}
