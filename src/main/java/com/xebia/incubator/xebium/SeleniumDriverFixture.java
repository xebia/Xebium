package com.xebia.incubator.xebium;

import static org.apache.commons.lang.StringUtils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriverCommandProcessor;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.HttpCommandProcessor;
import com.thoughtworks.selenium.SeleniumException;

public class SeleniumDriverFixture {

	private static final String REGEXP = "regexp:";

	private static Logger LOG = Logger.getLogger(SeleniumDriverFixture.class);

	private CommandProcessor commandProcessor;

	private long timeout = 30000;
	
	// TODO: decide: move to method or move to separate fixture.
	static {
		BasicConfigurator.configure();
	}

	public CommandProcessor detectWebDriverCommandProcessor(final String browser, String browserUrl) {
		browserUrl = FitNesseUtil.removeAnchorTag(browserUrl);
		Capabilities capabilities;

		if ("firefox".equalsIgnoreCase(browser)) {
			capabilities = DesiredCapabilities.firefox();
		} else if ("iexplore".equalsIgnoreCase(browser)) {
			capabilities = DesiredCapabilities.internetExplorer();
		} else if ("chrome".equalsIgnoreCase(browser)) {
			capabilities = DesiredCapabilities.chrome();
		} else if ("htmlUnit".equalsIgnoreCase(browser)) {
			capabilities = DesiredCapabilities.htmlUnit();
		} else {
			throw new RuntimeException("Unknown browser type. Should be one of 'firefox', 'iexplore', 'chrome' or 'htmlUnit'");
		}
		return new WebDriverCommandProcessor(browserUrl, capabilities);
	}

	public void startBrowserOnUrl(final String browser, final String browserUrl) {
		commandProcessor = detectWebDriverCommandProcessor(browser, browserUrl);
		commandProcessor.start();
		LOG.debug("Started command processor");
	}

	public void startBrowserOnHostOnPortOnUrl(final String browserStartCommand, final String serverHost, final int serverPort, final String browserUrl) {
		commandProcessor = new HttpCommandProcessor(serverHost, serverPort, browserStartCommand, FitNesseUtil.removeAnchorTag(browserUrl));
		commandProcessor.start();
		LOG.debug("Started HTML command processor");
	}

	public boolean doOn(final String command, final String target) {
		LOG.info("Performing | " + command + " | " + target + " |");
		return executeDoCommand(command, new String[] { target });
	}

	public boolean doOnWith(final String command, final String target, final String value) {
		LOG.info("Performing | " + command + " | " + target + " | " + value + " |");
		return executeDoCommand(command, new String[] { target, value });
	}

	public String is(final String command) {
		return executeCommand(command, new String[] { });
	}

	public String isOn(final String command, final String target) {
		return executeCommand(command, new String[] { target });
	}

	private String executeCommand(final String methodName, final String[] values) {
		return executeCommand(new ExtendedSeleniumCommand(methodName), values);
	}

	private boolean executeDoCommand(final String methodName, final String[] values) {
		
		if ("pause".equals(methodName)) {
			try {
				Thread.sleep(Integer.parseInt(values[0]));
			} catch (Exception e) {
				LOG.warn("Pause command interrupted", e);
			}
			return true;
		}
		
		final ExtendedSeleniumCommand command = new ExtendedSeleniumCommand(methodName);
		final String output = executeCommand(command, values);

		if (command.isVerifyCommand() || command.isWaitForCommand()) {
			return checkResult(output, command, values[values.length - 1]);
		} else if (command.isAssertCommand()) {
			if (!checkResult(output, command, values[values.length - 1])) {
				throw new AssertionError(output);
			}
		} else if (command.isCaptureEntirePageScreenshotCommand()) {
			writeToFile(values[0], output);
		}
		return true;
	}

	private String executeCommand(final ExtendedSeleniumCommand command, final String[] values) {
		final String valuesString = join(values, ", ");
		LOG.debug("executeCommand. Command: " + command.getSeleniumCommand() + " with values: [" + valuesString +"]");
		if (commandProcessor == null) {
			throw new IllegalStateException("Command processor not running. First start it by invoking startBrowserOnUrl");
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
		return output;
	}

	private boolean checkResult(final String output, final ExtendedSeleniumCommand command,
			final String value) {
		boolean result;
		if (command.isNegateCommand()) {
			result = command.isBooleanCommand() ? "false".equals(output) : !smartCompare(value, output);
		} else {
			result = command.isBooleanCommand() ? "true".equals(output) : smartCompare(value, output);
		}
		LOG.info("command " + command.getSeleniumCommand() + " with value " + value + " compared to output " + output + " is: " + result);
		return result;
	}

	private boolean smartCompare(final String value, final String output) {
		if (isRegularExpression(value)) {
			return compareRegex(value, output);
		} else {
			return value.equals(output);
		}
	}

	private boolean compareRegex(final String value, final String output) {
		final String regex = trim(removeStartIgnoreCase(value, REGEXP));
		LOG.debug("compareRegex: regex="+regex);
		return trim(output).matches(regex);
	}

	private boolean isRegularExpression(final String value) {
		return startsWithIgnoreCase(value, REGEXP);
	}

	private void writeToFile(final String filename, final String output) {
		// TODO: strip URL part or something. Whatever we need to make it accessable through the web
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
