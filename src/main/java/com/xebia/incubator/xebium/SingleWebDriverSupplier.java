package com.xebia.incubator.xebium;

import java.io.File;

import org.openqa.selenium.WebDriver;

import com.google.common.base.Supplier;

public class SingleWebDriverSupplier implements Supplier<WebDriver> {

	private ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();
	private Supplier<WebDriver> webDriverSupplier = new DefaultWebDriverSupplier();

	public WebDriver get() {
		WebDriver webDriver = this.webDriver.get();
		if (webDriver == null) {
			webDriver = webDriverSupplier.get();
			this.webDriver.set(webDriver);
		}
		return webDriver;
	}

	public void setWebDriverSupplier(Supplier<WebDriver> webDriverSupplier) {
		this.webDriverSupplier = webDriverSupplier;
	}

    /**
     * @param filename
     */
    public void loadCustomBrowserPreferencesFromFile(String filename) {
    	if (webDriverSupplier instanceof DefaultWebDriverSupplier) {
    		((DefaultWebDriverSupplier) webDriverSupplier).setCustomProfilePreferencesFile(new File(filename));
    	} else {
    		throw new RuntimeException("You've configured a custom WebDriverProvider, therefore you can not configure the 'load custom browser preferences from file' property");
    	}
    }

	/**
	 * @param directory
	 */
	public void loadFirefoxProfileFromDirectory(String directory) {
    	if (webDriverSupplier instanceof DefaultWebDriverSupplier) {
    		((DefaultWebDriverSupplier) webDriverSupplier).setProfileDirectory(new File(directory));
    	} else {
    		throw new RuntimeException("You've configured a custom WebDriverProvider, therefore you can not configure the 'load firefox profile from directory' property");
    	}
	}

	/**
	 * @param browser Name of the browser, as accepted by the DefaultWebDriverSupplier.
	 */
	public void setBrowser(String browser) {
    	if (webDriverSupplier instanceof DefaultWebDriverSupplier) {
    		((DefaultWebDriverSupplier) webDriverSupplier).setBrowser(browser);
    	} else {
    		throw new RuntimeException("You've configured a custom WebDriverProvider, therefore you can not configure the 'browser' property");
    	}
	}

	public void setAsDefault() {
		WebDriverFactory.configureWebDriverSupplier(this);
	}
}
