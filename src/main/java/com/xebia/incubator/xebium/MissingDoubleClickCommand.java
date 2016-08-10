package com.xebia.incubator.xebium;

import com.thoughtworks.selenium.webdriven.SeleneseCommand;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class MissingDoubleClickCommand extends SeleneseCommand<Void> {

    @Override
    protected Void handleSeleneseCommand(WebDriver driver, String locator,
            String value) {
        WebElement webElement = driver.findElement(By.xpath(locator));
        new Actions(driver).click(webElement).click().perform();
        return null;
    }

}
