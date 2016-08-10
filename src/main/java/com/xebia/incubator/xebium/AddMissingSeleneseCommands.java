package com.xebia.incubator.xebium;

import com.thoughtworks.selenium.webdriven.SeleneseCommand;
import com.thoughtworks.selenium.webdriven.WebDriverCommandProcessor;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.ContextClickAction;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.internal.Locatable;

public class AddMissingSeleneseCommands {

    public void addMissingSeleneseCommands(WebDriverCommandProcessor driver) {
        driver.addMethod("sendKeys", driver.getMethod("typeKeys"));

        driver.addMethod("contextClick", new SeleneseCommand<Void>() {

            @Override
            protected Void handleSeleneseCommand(WebDriver driver,
                    String locator, String value) {
                Mouse mouse = ((HasInputDevices) driver).getMouse();

                ContextClickAction rightClick = new ContextClickAction(mouse,
                        (Locatable) (driver.findElement(By.xpath(locator))));

                rightClick.perform();
                return null;
            }

        });

        /*
         * The following code to be removed when
         * 
         * https://bugs.chromium.org/p/chromedriver/issues/detail?id=755
         * 
         * and
         * 
         * https://bugs.chromium.org/p/chromedriver/issues/detail?id=782
         * 
         * are resolved
         */
        if (driver.getWrappedDriver().toString().toLowerCase()
                .contains("chrome")) {

            driver.addMethod("doubleClick", new SeleneseCommand<Void>() {

                @Override
                protected Void handleSeleneseCommand(WebDriver driver,
                        String locator, String value) {
                    WebElement webElement =
                            driver.findElement(By.xpath(locator));
                    new Actions(driver).click(webElement).click().perform();
                    return null;
                }
            });
        }

    }
}
