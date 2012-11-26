package com.xebia.incubator.xebium;

import java.io.File;

import org.openqa.selenium.WebDriver;

import com.google.common.base.Supplier;

/**
 * <p>A ConfigurableWebDriverSupplier can be configured through the fixture
 * methods of the {@link SeleniumDriverFixture}.</p>
 */
public interface ConfigurableWebDriverSupplier extends Supplier<WebDriver>{

	public void setBrowser(String browser);

	public void setCustomProfilePreferencesFile(File customProfilePreferencesFile);

	public void setProfileDirectory(File profileDirectory);
}
