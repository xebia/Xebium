package com.xebia.incubator.xebium.fastphantomjsdriver;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;

/** Variation on the official PhantomJSDriver that uses our FastDriverCommandExecutor. */
public class FastPhantomJSDriver extends RemoteWebDriver implements TakesScreenshot {

	/**
	 * Creates a new PhantomJSDriver instance.
	 *
	 * @param desiredCapabilities The capabilities required from PhantomJS/GhostDriver.
	 * @see org.openqa.selenium.phantomjs.PhantomJSDriverService#createDefaultService() for configuration details.
	 */
	public FastPhantomJSDriver(Capabilities desiredCapabilities) {
		this(PhantomJSDriverService.createDefaultService(desiredCapabilities), desiredCapabilities);
	}

	/**
	 * Creates a new PhantomJSDriver instance. The {@code service} will be started along with the
	 * driver, and shutdown upon calling {@link #quit()}.
	 *
	 * @param service             The service to use.
	 * @param desiredCapabilities The capabilities required from PhantomJS/GhostDriver.
	 */
	public FastPhantomJSDriver(DriverService service, Capabilities desiredCapabilities) {
		super(new FastDriverCommandExecutor(service), desiredCapabilities);
	}

	/**
	 * Take screenshot of the current window.
	 *
	 * @param target The target type/format of the Screenshot
	 * @return Screenshot of current window, in the requested format
	 * @see TakesScreenshot#getScreenshotAs(org.openqa.selenium.OutputType)
	 */
	//@Override
	public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
		// Get the screenshot as base64 and convert it to the requested type (i.e. OutputType<T>)
		String base64 = (String) execute(DriverCommand.SCREENSHOT).getValue();
		return target.convertFromBase64Png(base64);
	}
}