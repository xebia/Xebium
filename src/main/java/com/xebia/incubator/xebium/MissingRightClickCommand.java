package com.xebia.incubator.xebium;

import com.thoughtworks.selenium.webdriven.SeleneseCommand;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.ContextClickAction;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.internal.Locatable;

public class MissingRightClickCommand extends SeleneseCommand<Void> {

    @Override
    protected Void handleSeleneseCommand(WebDriver driver, String locator,
            String value) {
        Mouse mouse = ((HasInputDevices) driver).getMouse();

        ContextClickAction rightClick = new ContextClickAction(mouse,
                (Locatable) (driver.findElement(By.xpath(locator))));

        rightClick.perform();
        return null;
    }

}
