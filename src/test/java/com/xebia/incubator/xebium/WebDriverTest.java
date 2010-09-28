package com.xebia.incubator.xebium;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverCommandProcessor;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class WebDriverTest {

	@Test
	public void testWebDriver() throws MalformedURLException {
		// We could use any driver for our tests...
		DesiredCapabilities capabilities = new DesiredCapabilities();

		// ... but only if it supports javascript
		capabilities.setJavascriptEnabled(true);

		// Get a handle to the driver. This will throw an exception
		// if a matching driver cannot be located
		WebDriver driver = new FirefoxDriver(DesiredCapabilities.firefox());

		// Query the driver to find out more information
		 Capabilities actualCapabilities = ((RemoteWebDriver) driver).getCapabilities();

		// And now use it
		driver.get("http://www.google.com");
		
		driver.close();
	}
	
	@Test
	public void testWebDriverCommandProcessor() throws MalformedURLException {
		
		WebDriverCommandProcessor processor = new WebDriverCommandProcessor("http://www.google.com", DesiredCapabilities.firefox());
		
		processor.start();
		
		// And now use it
		processor.doCommand("open", new String[] { "/" });
		processor.doCommand("type", new String[] { "q", "xebium is the new test solution" });
	}

}
