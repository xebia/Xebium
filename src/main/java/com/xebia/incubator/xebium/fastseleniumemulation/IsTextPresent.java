package com.xebia.incubator.xebium.fastseleniumemulation;

import org.openqa.selenium.WebDriver;
import com.thoughtworks.selenium.webdriven.SeleneseCommand;

public class IsTextPresent extends SeleneseCommand<Boolean> {

	/**
	 * @param locator for isTextPresent: actually the value...
	 * @param value for isTextPresent: actually ignored...
	 */
    @Override
    protected Boolean handleSeleneseCommand(WebDriver driver, String locator, String value) {
        return driver.getPageSource().contains(locator);
    }
}
