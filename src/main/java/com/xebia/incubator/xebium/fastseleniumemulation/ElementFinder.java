package com.xebia.incubator.xebium.fastseleniumemulation;

import com.google.common.collect.Maps;
import com.thoughtworks.selenium.SeleniumException;
import org.openqa.selenium.*;
import com.thoughtworks.selenium.webdriven.JavascriptLibrary;

import java.util.Map;

public class ElementFinder extends com.thoughtworks.selenium.webdriven.ElementFinder {
	private final Map<String, String> additionalLocators = Maps.newHashMap();

	public ElementFinder(JavascriptLibrary javascriptLibrary) {
		super(javascriptLibrary);
	}

	@Override
	public void add(String strategyName, String implementation) {
		if (additionalLocators != null) {
			additionalLocators.put(strategyName, implementation);
		}
	}

	private String searchAdditionalStrategies(String locator) {
		int index = locator.indexOf('=');
		if (index == -1) {
			return null;
		}

		String key = locator.substring(0, index);
		return additionalLocators.get(key);
	}

	@Override
	public WebElement findElement(WebDriver driver, String locator) {
		WebElement toReturn = null;

		String strategy = searchAdditionalStrategies(locator);
		if (strategy != null) {
			String actualLocator = locator.substring(locator.indexOf('=') + 1);

			try {
				toReturn =
						(WebElement) ((JavascriptExecutor) driver).executeScript(strategy, actualLocator);

				if (toReturn == null) {
					throw new SeleniumException("Element " + locator + " not found");
				}

				return toReturn;
			} catch (WebDriverException e) {
				throw new SeleniumException("Element " + locator + " not found");
			}
		}

		try {
			return findElementDirectly(driver, locator);
		} catch (WebDriverException e) {
			throw new SeleniumException("Element " + locator + " not found", e);
		}
	}

	private WebElement findElementDirectly(WebDriver driver, String locator) {
		if (locator.startsWith("xpath=")) {
			return xpathWizardry(driver, locator.substring("xpath=".length()));
		}
		if (locator.startsWith("//")) {
			return xpathWizardry(driver, locator);
		}

		if (locator.startsWith("css=")) {
			String selector = locator.substring("css=".length());
			return driver.findElement(By.cssSelector(selector));
		}

		if (locator.startsWith("id=")) {
			String id = locator.substring("id=".length());
			return driver.findElement(By.id(id));
		}

		if (locator.startsWith("identifier=")) {
			String id = locator.substring("identifier=".length());
			return driver.findElement(By.id(id));
		}

		if (locator.startsWith("name=")) {
			String id = locator.substring("name=".length());
			return driver.findElement(By.name(id));
		}

		if (locator.startsWith("link=exact:")) {
			String link = locator.substring("link=exact:".length());
			return driver.findElement(By.linkText(link));
		}

		if (locator.startsWith("link=")) {
			String link = locator.substring("link=".length());
			try {
				return driver.findElement(By.linkText(link));
			} catch (NoSuchElementException e) {
				return driver.findElement(By.partialLinkText(link));
			}
		}

		// No explicit selector used - then default try id, then name
		try {
			return driver.findElement(By.id(locator));
		} catch (NoSuchElementException e) {
			return driver.findElement(By.name(locator));
		}
	}

	private WebElement xpathWizardry(WebDriver driver, String xpath) {
		try {
			return driver.findElement(By.xpath(xpath));
		} catch (WebDriverException ignored) {
			// Because we have inconsistent return values
		}

		if (xpath.endsWith("/")) {
			return driver.findElement(By.xpath(xpath.substring(0, xpath.length() - 1)));
		}

		throw new NoSuchElementException("Cannot find an element with the xpath: " + xpath);
	}
}
