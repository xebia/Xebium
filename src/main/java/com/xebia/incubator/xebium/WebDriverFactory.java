package com.xebia.incubator.xebium;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;

/**
 * <p>A (static/singleton) container for the WebDriver Supplier that needs to be used
 * for creating new WebDriver instances.</p>
 */
public final class WebDriverFactory {
	private static final Logger LOG = LoggerFactory.getLogger(WebDriverFactory.class);

	private static Supplier<WebDriver> webDriverSupplier = new DefaultWebDriverSupplier();;

	public static void configureWebDriverSupplier(Supplier<WebDriver> webDriverSupplier) {
		LOG.info("Configured WebDriver supplier to {}", webDriverSupplier);
		WebDriverFactory.webDriverSupplier = webDriverSupplier;
	}

	/**
	 * @return A WebDriver obtained from the WebDriver Supplier.
	 */
	public static WebDriver getInstance() {
		LOG.info("Instantiating a fresh Selenium Driver Fixture with provider: {}", webDriverSupplier);
		return webDriverSupplier.get();
	}

	/**
	 * <p>This method is used for the "shorthand" configuration through {@link SeleniumDriverFixture}.
	 * </p>
	 * @return The currently configured WebDriver supplier.
	 */
	static Supplier<WebDriver> getSupplier() {
		return webDriverSupplier;
	}
}
