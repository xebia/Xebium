package com.xebia.incubator.xebium;

import static org.apache.commons.lang.StringUtils.join;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverCommandProcessor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.HttpCommandProcessor;
import com.thoughtworks.selenium.SeleniumException;

public class SeleniumDriverFixture {

	private static Logger LOG = Logger.getLogger(SeleniumDriverFixture.class);

	private CommandProcessor commandProcessor;

	private long timeout = 30000;
	
	private long stepDelay = 0;
	
	// TODO: decide: move to method or move to separate fixture.
	static {
		BasicConfigurator.configure();
	}

	public CommandProcessor startWebDriverCommandProcessor(final String browser, String browserUrl) {
		browserUrl = FitNesseUtil.removeAnchorTag(browserUrl);
		WebDriver driver;
		
		if ("firefox".equalsIgnoreCase(browser)) {
			driver = new FirefoxDriver();
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

	public void startBrowserOnUrl(final String browser, final String browserUrl) {
		commandProcessor = startWebDriverCommandProcessor(browser, browserUrl);
		LOG.debug("Started command processor");
	}

	public void startBrowserOnHostOnPortOnUrl(final String browserStartCommand, final String serverHost, final int serverPort, final String browserUrl) {
		commandProcessor = new HttpCommandProcessor(serverHost, serverPort, browserStartCommand, FitNesseUtil.removeAnchorTag(browserUrl));
		commandProcessor.start();
		LOG.debug("Started HTML command processor");
	}

	/**
	 * <code>
	 * | set timeout to | 500 |
	 * </code>
	 * 
	 * Set the timeout, both local and on the running selenium server.
	 * @param timeout Timeout in milliseconds (ms)
	 */
	public void setTimeoutTo(long timeout) {
		this.timeout = timeout;
		doOn("setTimeout", "" + timeout);
	}
		
	/**
	 * Set delay between steps.
	 * <code>
	 * | set step delay to | 500 |
	 * | set step delay to | slow |
	 * | set step delay to | fast |
	 * </code>
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
	 * <code>
	 * | ensure | do | <i>open</i> | on | <i>/</i> |
	 * </code>
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
	 * <code>
	 * | ensure | do | <i>type</i> | on | <i>searchString</i> | with | <i>some text</i> |
	 * </code>
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
	 * <code>
	 * | <i>$title=</i> | is | <i>getTitle</i> |
	 * </code>
	 * 
	 * @param command
	 * @return
	 */
	public String is(final String command) {
		LOG.info("Storing result from  | " + command + " |");
		return executeCommand(new ExtendedSeleniumCommand(command), new String[] { });
	}

	/**
	 * <code>
	 * | <i>$pageName=,/i> | is | <i>getText</i> | on | <i>//span</i> |
	 * </code>
	 * 
	 * @param command
	 * @param target
	 * @return
	 */
	public String isOn(final String command, final String target) {
		LOG.info("Storing result from | " + command + " | " + target + " |");
		return executeCommand(new ExtendedSeleniumCommand(command), new String[] { target });
	}

	private boolean executeDoCommand(final String methodName, final String[] values) {
		
		final ExtendedSeleniumCommand command = new ExtendedSeleniumCommand(methodName);
		final String output = executeCommand(command, values);

		if (command.isVerifyCommand() || command.isWaitForCommand()) {
			return checkResult(command, values[values.length - 1], output);
		} else if (command.isAssertCommand()) {
			if (!checkResult(command, values[values.length - 1], output)) {
				throw new AssertionError(output);
			}
		} else if (command.isCaptureEntirePageScreenshotCommand()) {
			writeToFile(values[0], output);
		}
		return true;
	}

	private String executeCommand(final ExtendedSeleniumCommand command, final String[] values) {
		LOG.debug("executeCommand. Command: " + command.getSeleniumCommand() + " with values: [" + join(values, ", ") +"]");
		
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
			output = commandProcessor.doCommand(command.getSeleniumCommand(), values);

			if (output != null && LOG.isDebugEnabled()) {
				LOG.debug("Command processor returned '" + output + "'");
			}

			if (command.isAndWaitCommand()) {
				commandProcessor.doCommand("waitForPageToLoad", new String[] { "" + timeout });
			}
		} catch (final SeleniumException e) {
			LOG.error("Execution of command failed: " + e.getMessage());
		}
		
		if (this.stepDelay > 0) {
			try {
				Thread.sleep(this.stepDelay);
			} catch (Exception e) {
				LOG.warn("Step delay sleep command interrupted", e);
			}
		}
		return output;
	}

	private boolean checkResult(ExtendedSeleniumCommand command,
			String expected, String actual) {
		boolean result = command.matches(expected, actual);
		LOG.info("command " + command.getSeleniumCommand() + " with value '" + expected + "' compared to output '" + actual + "' is: " + result);
		return result;
	}

	private void writeToFile(final String filename, final String output) {
		File file = FitNesseUtil.asFile(filename);
		try {
			FileOutputStream w = new FileOutputStream(file);
			w.write(Base64.decodeBase64(output));
			w.close();
		} catch (IOException e) {
			throw new RuntimeException("Unable to create writer for file " + file, e);
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
