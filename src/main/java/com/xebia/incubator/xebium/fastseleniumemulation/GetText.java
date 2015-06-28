package com.xebia.incubator.xebium.fastseleniumemulation;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.thoughtworks.selenium.webdriven.ElementFinder;
import com.thoughtworks.selenium.webdriven.JavascriptLibrary;
import com.thoughtworks.selenium.webdriven.SeleneseCommand;

public class GetText extends SeleneseCommand<String> {
    private final ElementFinder finder;

    public GetText(ElementFinder finder) {
        this.finder = finder;
    }

    @Override
    protected String handleSeleneseCommand(WebDriver driver, String locator, String ignored) {
        WebElement element = finder.findElement(driver, locator);
        return element.getText();
    }
}
