package com.xebia.incubator.xebium;

import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.trim;
import static com.xebia.incubator.xebium.FitNesseUtil.asFile;
import static com.xebia.incubator.xebium.FitNesseUtil.removeAnchorTag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverCommandProcessor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.HttpCommandProcessor;
import com.thoughtworks.selenium.SeleniumException;

/**
 * Main fixture. Starts a browser session and execute commands.
 */
public class SeleniumDriverFixture {

	enum ScreenshotPolicy {
		NONE,
		FAILURE,
		STEP
	}

	private static final Logger LOG = LoggerFactory.getLogger(SeleniumDriverFixture.class);

	private CommandProcessor commandProcessor;

	private long timeout = 30000;
	
	private long stepDelay = 0;
	
	private long pollDelay = 100;

	private ScreenCapture screenCapture;
	
	private ScreenshotPolicy screenshotPolicy = ScreenshotPolicy.NONE;

	public SeleniumDriverFixture() {
		LOG.info("Instantiating a fresh Selenium Driver Fixture");
	}
	
	private CommandProcessor startWebDriverCommandProcessor(final String browser, String browserUrl) {
		browserUrl = removeAnchorTag(browserUrl);
		WebDriver driver;
		
		if ("firefox".equalsIgnoreCase(browser)) {
			FirefoxProfile profile = new FirefoxProfile();
			// Ensure we deal with untrusted and unverified hosts.
			profile.setAcceptUntrustedCertificates(true);
			profile.setAssumeUntrustedCertificateIssuer(true);
			// Allow Basic Authentication without confirmation
			profile.setPreference("network.http.phishy-userpass-length", 255);
			driver = new FirefoxDriver(profile);
		} else if ("iexplore".equalsIgnoreCase(browser)) {
			driver = new InternetExplorerDriver();
		} else if ("chrome".equalsIgnoreCase(browser)) {
			driver = new ChromeDriver();
		} else if ("htmlUnit".equalsIgnoreCase(browser)) {
			driver = new HtmlUnitDriver(true);
		} else {
			throw new RuntimeException("Unknown browser type. Should be one of 'firefox', 'iexplore', 'chrome' or 'htmlUnit'");
		}
		return new WebDriverCommandProcessor(browserUrl, driver);
	}

	/**
	 * <p><code>
	 * | start browser | <i>firefox</i> | on url | <i>http://localhost</i> |
	 * </code></p>
	 * 
	 * @param browser
	 * @param browserUrl
	 */
	public void startBrowserOnUrl(final String browser, final String browserUrl) {
		commandProcessor = startWebDriverCommandProcessor(browser, browserUrl);
		LOG.debug("Started command processor");
		screenCapture = new ScreenCapture(commandProcessor);
	}

	/**
	 * <p><code>
	 * | start browser | <i>firefox</i> | on url | <i>http://localhost</i> | using remote server |
	 * </code></p>
	 * 
	 * @param browser
	 * @param browserUrl
	 */
	public void startBrowserOnUrlUsingRemoteServer(final String browser, final String browserUrl) {
		startBrowserOnUrlUsingRemoteServerOnHost(browser, browserUrl, "localhost");
	}

	/**
	 * <p><code>
	 * | start browser | <i>firefox</i> | on url | <i>http://localhost</i> | using remote server on host | <i>localhost</i> |
	 * </code></p>
	 * 
	 * @param browser
	 * @param browserUrl
	 * @param serverHost
	 */
	public void startBrowserOnUrlUsingRemoteServerOnHost(final String browser, final String browserUrl, final String serverHost) {
		startBrowserOnUrlUsingRemoteServerOnHostOnPort(browser, browserUrl, serverHost, 4444);
	}

	/**
	 * <p><code>
	 * | start browser | <i>firefox</i> | on url | <i>http://localhost</i> | using remote server on host | <i>localhost</i> | on port | <i>4444</i> |
	 * </code></p>
	 * 
	 * @param browser
	 * @param browserUrl
	 * @param serverHost
	 * @param serverPort
	 */
	public void startBrowserOnUrlUsingRemoteServerOnHostOnPort(final String browser, final String browserUrl, final String serverHost, final int serverPort) {
		commandProcessor = new HttpCommandProcessorAdapter(new HttpCommandProcessor(serverHost, serverPort, browser, removeAnchorTag(browserUrl)));
		commandProcessor.start();
		LOG.debug("Started HTML command processor");
		screenCapture = new ScreenCapture(commandProcessor);
	}

	/**
	 * <p><code>
	 * | set timeout to | 500 |
	 * </code></p>
	 * 
	 * <p>Set the timeout, both local and on the running selenium server.</p>
	 * 
	 * @param timeout Timeout in milliseconds (ms)
	 */
	public void setTimeoutTo(long timeout) {
		this.timeout = timeout;
		doOn("setTimeout", "" + timeout);
	}
		
	/**
	 * <p>Set delay between steps.</p>
	 * <p><code>
	 * | set step delay to | 500 |
	 * | set step delay to | slow |
	 * | set step delay to | fast |
	 * </code></p>
	 * 
	 * @param stepDelay delay in milliseconds
	 */
	public void setStepDelayTo(String stepDelay) {
		if ("slow".equals(stepDelay)) {
			this.stepDelay = 1000;
		} else if ("fast".equals(stepDelay)) {
			this.stepDelay = 0;
		} else {
			this.stepDelay = Long.parseLong(stepDelay);
		}
	}
	
	/**
	 * Instruct the driver to create screenshots
	 * <p><code>
	 * | save screenshot after | <i>failure</i> |
	 * | save screenshot after | <i>error</i> |
	 * </code></p>
	 * 
	 * <p><code>
	 * | save screenshot after | <i>every step</i> |
	 * | save screenshot after | <i>step</i> |
	 * </code></p>
	 * 
	 * <p><code>
	 * | save screenshot after | <i>nothing</i> |
	 * | save screenshot after | <i>none</i> |
	 * </code></p>
	 */
	public void saveScreenshotAfter(String crit) {
		if ("none".equals(crit) || "nothing".equals(crit)) {
			screenshotPolicy  = ScreenshotPolicy.NONE;
		} else if ("failure".equals(crit) || "error".equals(crit)) {
			screenshotPolicy  = ScreenshotPolicy.FAILURE;
		} else if ("step".equals(crit) || "every step".equals(crit)) {
			screenshotPolicy  = ScreenshotPolicy.STEP;
		}
		LOG.info("Screenshot policy set to " + screenshotPolicy);
	}
	
	/**
	 * <p><code>
	 * | save screenshot after | <i>failure</i> | in folder | <i>http://files/testResults/screenshots/${PAGE_NAME} |
	 * | save screenshot after | <i>error</i> |
	 * </code></p>
	 */
	public void saveScreenshotAfterInFolder(String crit, String baseDir) {
		saveScreenshotAfter(crit);
		screenCapture.setScreenshotBaseDir(removeAnchorTag(baseDir));
	}
	
	/**
	 * <p><code>
	 * | ensure | do | <i>open</i> | on | <i>/</i> |
	 * </code></p>
	 * 
	 * @param command
	 * @param target
	 * @return
	 */
	public boolean doOn(final String command, final String target) {
		LOG.info("Performing | " + command + " | " + target + " |");
		return executeDoCommand(command, new String[] { target });
	}

	/**
	 * <p><code>
	 * | ensure | do | <i>type</i> | on | <i>searchString</i> | with | <i>some text</i> |
	 * </code></p>
	 * 
	 * @param command
	 * @param target
	 * @param value
	 * @return
	 */
	public boolean doOnWith(final String command, final String target, final String value) {
		LOG.info("Performing | " + command + " | " + target + " | " + value + " |");
		return executeDoCommand(command, new String[] { target, value });
	}

	/**
	 * <p><code>
	 * | <i>$title=</i> | is | <i>getTitle</i> |
	 * </code></p>
	 * 
	 * @param command
	 * @return
	 */
	public String is(final String command) {
		LOG.info("Storing result from  | " + command + " |");
		return executeCommand(new ExtendedSeleniumCommand(command), new String[] { }, stepDelay);
	}

	/**
	 * <p><code>
	 * | <i>$pageName=</i> | is | <i>getText</i> | on | <i>//span</i> |
	 * </code></p>
	 * 
	 * @param command
	 * @param target
	 * @return
	 */
	public String isOn(final String command, final String target) {
		LOG.info("Storing result from | " + command + " | " + target + " |");
		return executeCommand(new ExtendedSeleniumCommand(command), new String[] { target }, stepDelay);
	}

	private boolean executeDoCommand(final String methodName, final String[] values) {
		
		final ExtendedSeleniumCommand command = new ExtendedSeleniumCommand(methodName);

		String output;
		boolean result = true;
		
		if (command.requiresPolling()) {
			long timeoutTime = System.currentTimeMillis() + timeout;
			
			do {
				output = executeCommand(command, values, pollDelay);
				result = checkResult(command, values[values.length - 1], output);
			} while (!result && timeoutTime > System.currentTimeMillis());

		} else {

			output = executeCommand(command, values, stepDelay);

			if (command.isCaptureEntirePageScreenshotCommand()) {
				writeToFile(values[0], output);
				result = true;
			} else if (command.isAssertCommand() || command.isVerifyCommand() || command.isWaitForCommand()) {
				result = checkResult(command, values[values.length - 1], output);
			}
		}
		
		if ((!command.isAssertCommand() && !command.isVerifyCommand() &&screenshotPolicy == ScreenshotPolicy.STEP)
				|| (!result && screenshotPolicy == ScreenshotPolicy.FAILURE)) {
			screenCapture.captureScreenshot(methodName, values);
		}

		if (!result && command.isAssertCommand()) {
			throw new AssertionError(output);
		}
		
		return result;
	}

	private String executeCommand(final ExtendedSeleniumCommand command, final String[] values, long delay) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("executeCommand. Command: " + command.getSeleniumCommand() + " with values: [" + join(values, ", ") +"]");
		}
		
		if (commandProcessor == null) {
			throw new IllegalStateException("Command processor not running. First start it by invoking startBrowserOnUrl");
		}
		
		// Handle special cases first
		if ("pause".equals(command.getSeleniumCommand())) {
			try {
				Thread.sleep(Long.parseLong(values[0]));
			} catch (Exception e) {
				LOG.warn("Pause command interrupted", e);
			}
			return null;
		}
		
		String output = null;
		try {
			output = executeCommand(command.getSeleniumCommand(), values);
			
			if (command.isAndWaitCommand()) {
				commandProcessor.doCommand("waitForPageToLoad", new String[] { "" + timeout });
			}
		} catch (final SeleniumException e) {
			LOG.error("Execution of command failed: " + e.getMessage());
		}
		
		if (delay > 0) {
			try {
				Thread.sleep(delay);
			} catch (Exception e) {
				LOG.warn("Step delay sleep command interrupted", e);
			}
		}
		return output;
	}

	private String executeCommand(String methodName, final String[] values) {
		String output = commandProcessor.doCommand(methodName, values);

		if (output != null && LOG.isDebugEnabled()) {
			LOG.debug("Command processor returned '" + output + "'");
		}

		return output;
	}

	private boolean checkResult(ExtendedSeleniumCommand command, String expected, String actual) {
		boolean result = command.matches(expected, actual);
		LOG.info("command " + command.getSeleniumCommand() + " with value '" + expected + "' compared to output '" + actual + "' is: " + result);
		return result;
	}

	private void writeToFile(final String filename, final String output) {
		File file = asFile(filename);
		try {
			ScreenCapture.writeToFile(file, output);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void stopBrowser() {
		this.commandProcessor.stop();
		this.commandProcessor = null;
		LOG.info("Command processor stopped");
	}

	/**
	 * Setter for unit tests. Never invoke from non-unit tests!!
	 */
	public void setCommandProcessorForUnitTest(final CommandProcessor commandProcessor) {
		this.commandProcessor = commandProcessor;
	}
}
