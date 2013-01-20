package com.xebia.incubator.xebium;

import java.io.File;

/**
 * <p>A ConfigurableWebDriverSupplier can be configured through the fixture
 * methods of the {@link SeleniumDriverFixture}.</p>
 */
public interface ConfigurableWebDriverSupplier {

	public void setBrowser(String browser);

	public void setCustomProfilePreferencesFile(File customProfilePreferencesFile);

	public void setProfileDirectory(File profileDirectory);
}
