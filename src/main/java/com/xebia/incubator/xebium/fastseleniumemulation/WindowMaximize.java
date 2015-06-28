package com.xebia.incubator.xebium.fastseleniumemulation;

import org.openqa.selenium.WebDriver;
import com.thoughtworks.selenium.webdriven.SeleneseCommand;


public class WindowMaximize extends SeleneseCommand<Void> {


    @Override
    protected Void handleSeleneseCommand(WebDriver driver, String locator, String value) {
        driver.manage().window().maximize();
        return null;
    }
}
