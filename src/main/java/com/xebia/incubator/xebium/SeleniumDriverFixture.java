/*
 * Copyright 2010-2012 Xebia b.v.
 * Copyright 2010-2012 Xebium contributers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.xebia.incubator.xebium;

import com.google.common.base.Supplier;
import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.HttpCommandProcessor;
import com.thoughtworks.selenium.SeleniumException;
import com.thoughtworks.selenium.webdriven.WebDriverCommandProcessor;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import static com.xebia.incubator.xebium.FitNesseUtil.*;
import static org.apache.commons.lang.StringUtils.join;

/**
 * Main fixture. Starts a browser session and execute commands.
 */
public class SeleniumDriverFixture {

	private static final Logger LOG = LoggerFactory.getLogger(SeleniumDriverFixture.class);

	private DefaultWebDriverSupplier defaultWebDriverSupplier = new DefaultWebDriverSupplier();

	private static final String ALIAS_PREFIX = "%";

	private CommandProcessor commandProcessor;

	// in milliseconds
	private long timeout = 30000;

	private long stepDelay = 0;

	private long pollDelay = 100;

	private boolean stopBrowserOnAssertion = true;

	private ScreenCapture screenCapture = new ScreenCapture();

	private LocatorCheck locatorCheck;

	private Map<String, String> aliases = new HashMap<String, String>();

	public SeleniumDriverFixture() {
		super();
	}

	private WebDriver defaultWebDriverInstance() {
		return defaultWebDriverSupplier.newWebDriver();
	}

	private CommandProcessor startWebDriverCommandProcessor(String browserUrl, WebDriver webDriver) {
		browserUrl = removeAnchorTag(browserUrl);
		WebDriverCommandProcessor driver = new WebDriverCommandProcessor(browserUrl, webDriver);
		addMissingSeleneseCommands(driver);
		return driver;
	}

	private void addMissingSeleneseCommands(WebDriverCommandProcessor driver) {
		driver.addMethod("sendKeys", driver.getMethod("typeKeys"));
	}

	/**
	 * Configure the custom Firefox preferences (javascript) file on the webdriver factory.
	 *
	 * @param filename
	 */
	public void loadCustomBrowserPreferencesFromFile(String filename) {
		defaultWebDriverSupplier.setCustomProfilePreferencesFile(new File(filename));
	}

	/**
	 * Configure the custom Firefox profiledirectory on the webdriver factory.
	 *
	 * @param directory
	 */
	public void loadFirefoxProfileFromDirectory(String directory) {
		defaultWebDriverSupplier.setCustomProfilePreferencesFile(new File(directory));
	}

	/**
	 * @param browser Name of the browser, as accepted by the DefaultWebDriverSupplier.
	 */
	private void setBrowser(String browser) {
		defaultWebDriverSupplier.setBrowser(browser);
	}

	/**
	 * <p><code>
	 * | start driver | <i>$Driver</i> | on url | <i>http://localhost</i> |
	 * </code></p>
	 *
	 * @param webDriver a WebDriver instance
	 * @param browserUrl
	 */
	public void startDriverOnUrl(final WebDriver webDriver, final String browserUrl) {
		setCommandProcessor(startWebDriverCommandProcessor(browserUrl, webDriver));
		setTimeoutOnSelenium();
		LOG.debug("Started command processor");
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
		setBrowser(browser);
		startDriverOnUrl(defaultWebDriverInstance(), browserUrl);
	}

	/**
	 * <p><code>
	 * | start browser | <i>firefox</i> | on url | <i>http://localhost</i> | using remote server |
	 * </code></p>
	 *
	 * @param browser
	 * @param browserUrl
	 * @deprecated This call requires a Selenium 1 server. It is advised to use WebDriver.
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
	 * @deprecated This call requires a Selenium 1 server. It is advised to use WebDriver.
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
	 * @deprecated This call requires a Selenium 1 server. It is advised to use WebDriver.
	 */
	public void startBrowserOnUrlUsingRemoteServerOnHostOnPort(final String browser, final String browserUrl, final String serverHost, final int serverPort) {
		setCommandProcessor(new HttpCommandProcessorAdapter(new HttpCommandProcessor(serverHost, serverPort, browser, removeAnchorTag(browserUrl))));
		commandProcessor.start();
		setTimeoutOnSelenium();
		LOG.debug("Started HTML command processor");
	}

	void setCommandProcessor(CommandProcessor commandProcessor) {
		this.commandProcessor = commandProcessor;
		screenCapture.setCommandProcessor(commandProcessor);
		locatorCheck = new LocatorCheck(commandProcessor);
		LOG.info("Started new command processor (timeout: " + timeout + "ms, step delay: " + stepDelay + "ms, poll interval: " + pollDelay + "ms)");
	}

	void setScreenCapture(ScreenCapture screenCapture) {
		this.screenCapture = screenCapture;
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
		if (commandProcessor != null) {
			setTimeoutOnSelenium();
		}
	}

	/**
	 * <p><code>
	 * | set timeout to | 500 | seconds |
	 * </code></p>
	 *
	 * <p>Set the timeout, both local and on the running selenium server.</p>
	 *
	 * @param timeout Timeout in seconds
	 */
	public void setTimeoutToSeconds(long timeout) {
		setTimeoutTo(timeout * 1000);
	}

	/**
	 * Set the default timeout on the selenium instance.
	 */
	private void setTimeoutOnSelenium() {
		executeCommand("setTimeout", new String[] { "" + this.timeout });
		WebDriver.Timeouts timeouts = getWebDriver().manage().timeouts();
		timeouts.setScriptTimeout(this.timeout, TimeUnit.MILLISECONDS);
		timeouts.pageLoadTimeout(this.timeout, TimeUnit.MILLISECONDS);
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
	 * <p>In case of an assertion (assert* selenese command), close the browser.</p>
	 * <p><code>
	 * | set stop browser on assertion | true |
	 * </code></p>
	 *
	 * @param stopBrowserOnAssertion
	 */
	public void setStopBrowserOnAssertion(boolean stopBrowserOnAssertion) {
		this.stopBrowserOnAssertion = stopBrowserOnAssertion;
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
	public void saveScreenshotAfter(String policy) throws IOException {
		screenCapture.setScreenshotPolicy(policy);
	}

	/**
	 * <p><code>
	 * | save screenshot after | <i>failure</i> | in folder | <i>http://files/testResults/screenshots/${PAGE_NAME} |
	 * | save screenshot after | <i>error</i> |
	 * </code></p>
	 */
	public void saveScreenshotAfterInFolder(String policy, String baseDir) throws IOException {
		screenCapture.setScreenshotBaseDir(removeAnchorTag(baseDir));
		saveScreenshotAfter(policy);
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
		return executeDoCommand(command, new String[] { unalias(target) });
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
		return executeDoCommand(command, new String[] { unalias(target), unalias(value) });
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
		LOG.info("Obtain result from  | " + command + " |");
		return is(command, new String[]{});
	}

	/**
	 * Same as {@link #is(String)}, only with "on" statement, analog to "do-on" command.
	 *
	 * @param command
	 * @return
	 */
	public String isOn(final String command) {
		return is(command);
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
		LOG.info("Obtain result from | " + command + " | " + target + " |");
		return is(command, new String[] { unalias(target) });
	}

	public String is(final String command, final String[] parameters) {
		ExtendedSeleniumCommand seleniumCommand = new ExtendedSeleniumCommand(command);
		String output = executeCommand(seleniumCommand, parameters, stepDelay);

		if (seleniumCommand.isBooleanCommand() && seleniumCommand.isNegateCommand()) {
			if ("true".equals(output)) {
				output = "false";
			} else if ("false".equals(output)) {
				output = "true";
			} else {
				throw new IllegalStateException("Illegal boolean value: '" + output + "'");
			}
		}

		return output;
	}

	/**
	 * Same as {@link #isOn(String, String)}, only with "with" statement, analog to "do-on-with" command.
	 *
	 * @param command
	 * @param target
	 * @return
	 */
	public String isOnWith(final String command, final String target) {
		return isOn(command, target);
	}

	/**
	 * Add a new locator alias to the fixture.
	 *
	 * @param alias
	 * @param locator
	 */
	public void addAliasForLocator(String alias, String locator) {
		LOG.info("Add alias: '" + alias + "' for '" + locator + "'");
		aliases.put(alias, locator);
	}

	/**
	 * Clear the aliases table.
	 */
	public void clearAliases() {
		aliases.clear();
	}

	private String unalias(String value) {
		String result = value;
		if (value != null && value.startsWith(ALIAS_PREFIX)) {
			String alias = value.substring(ALIAS_PREFIX.length());
			String subst = aliases.get(alias);
			if (subst != null) {
				LOG.info("Expanded alias '" + alias + "' to '" + result + "'");
				result = subst;
			}
		}
		return result;
	}

	// The Below 2 functions (searchAllFrames) are used to search all frames for a specific xpath search, and also switchTo the frame that has the found web element

	public WebElement searchAllFrames(String elementXpath) {
		return searchAllFrames(getWebDriver(), elementXpath, new ArrayList<WebElement>());
	}

	public WebElement searchAllFrames(WebDriver driver, String elementXpath, List <WebElement> frameSwitches) {
		if (frameSwitches.size() == 0) //if starting then make sure to search from top
			driver.switchTo().defaultContent();
		WebElement element = null; //return null if not found or error during search.
		try {
			element = driver.findElement(By.xpath(elementXpath));// need to catch in case not found
		}catch(Exception e) {}
		if (element == null)
		{
			List <WebElement> framesList = null;
			try {
				framesList = driver.findElements(By.xpath("//iframe|//frame"));
				framesList = driver.findElements(By.cssSelector("iframe|frame"));
			}catch(Exception e) {}
			if (framesList == null)
				return null;
			for(WebElement frame:framesList){
				// need to start from the top and switch back to next frame
				driver.switchTo().defaultContent();
				for(WebElement nestedFrame:frameSwitches){//nested frames list
					driver.switchTo().frame(nestedFrame);
				}
				driver.switchTo().frame(frame);

				frameSwitches.add(frame);
				element =  searchAllFrames(driver, elementXpath, frameSwitches);
				if (element != null)
					return element; // element found
				else
					frameSwitches.remove(frame); //this frame had nothing so remove it from the list
			}
			return null; //element not found
		}
		else
			return element; //element found
	}

	private boolean executeDoCommand(final String methodName, final String[] values) {

		String output = null;
		boolean result = true;

		ExtendedSeleniumCommand command = new ExtendedSeleniumCommand(methodName);

		boolean locatorFlag = locatorCheck.verifyElementPresent(command, values);

		SeleniumCommandResult commandResult;

		if (!locatorFlag) { // if it is a locator command and verifyElementPresent fails
			String xpathStr = "";
			if (values.length > 0)
				xpathStr = values[0];
			if (searchAllFrames(xpathStr) != null)
				locatorFlag = true; //searchAllFrames found a matched and switchTo the correct frame
		}

		if (!locatorCheck.verifyElementPresent(command, values)) {
			commandResult = failure();
		} else if (command.requiresPolling()) {
			commandResult = executeDoCommandPolling(values, command);
		} else {
			commandResult = executeAndCheckResult(command, values, stepDelay);

			if (command.isCaptureEntirePageScreenshotCommand()) {
				writeToFile(values[0], commandResult.output);
			}
		}

		if (screenCapture.requireScreenshot(command, commandResult.result)) {
			screenCapture.captureScreenshot(methodName, values);
		}

		if (commandResult.failed() && command.isAssertCommand()) {
			if (stopBrowserOnAssertion) {
				stopBrowser();
			}
			throw new AssertionAndStopTestError(commandResult.output);
		}

		if (commandResult.hasException()) {
			throw new AssertionError(commandResult.getException());
		} else {
			return commandResult.result;
		}
	}

	private String encryptString(String password) { //encrypt exposed passwords or information stub

		if (password != null) {
			Cipher aes = null;
			try {
				aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
				aes.init(Cipher.ENCRYPT_MODE, generateKey());
				byte[] ciphertext = aes.doFinal(password.getBytes());
				password = "decrypt:" + bytesToHex(ciphertext); //prefix with "decrypt:"
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return password;
	}

	public String bytesToHex(byte[] bytes) { //generate hex values for data encryption
		final char[] hexArray = "0123456789ABCDEF".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for ( int j = 0; j < bytes.length; j++ ) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static SecretKeySpec generateKey() { //salting of the data stub
		String passStr="XebiumIsConnectedToTheSikuliBone";
		SecretKey tmp = null;
		try{
			String saltStr = "fijsd@#saltr9jsfizxnv";
			byte[] salt = saltStr.getBytes();
			int iterations = 43210;
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			tmp = factory.generateSecret(new PBEKeySpec(passStr.toCharArray(), salt, iterations, 128));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return new SecretKeySpec(tmp.getEncoded(), "AES");
	}

	public static  byte[] hexToBytes(String hex) {//generate bytes value for use internal to fixture
		int len = hex.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i+1), 16));
		}
		return data;
	}

	public static String processDecryptionString(DefaultWebDriverSupplier driver, String val) { //decrypt the value

		if (val.startsWith("decrypt:") ) {
			try {
				val = val.substring(val.indexOf(":") + 1);
				SecretKeySpec key = generateKey();
				Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
				aes.init(Cipher.DECRYPT_MODE, key);
				String cleartext = new String(aes.doFinal(hexToBytes(val)));
				return cleartext;
			}
			catch (Exception e) {
				//LOG.info("Exception processDecryptionString:" + e.getMessage());
				e.printStackTrace();
				return val;
			}
		}
		else
			return val;
	}

	private SeleniumCommandResult executeDoCommandPolling(String[] values, ExtendedSeleniumCommand command) {
		SeleniumCommandResult commandResult;
		long timeoutTime = System.currentTimeMillis() + timeout;

		do {
			commandResult = executeAndCheckResult(command, values, 0);
			if (commandResult.failed()) {
				delayIfNeeded(pollDelay);
			}
		} while (commandResult.failed() && timeoutTime > System.currentTimeMillis());

		LOG.info("WaitFor-command '" + command.getSeleniumCommand() +  (commandResult.succeeded() ? "' succeeded" : "' failed"));
		return commandResult;
	}

	private SeleniumCommandResult executeAndCheckResult(ExtendedSeleniumCommand command, String[] values, long delay) {
		try {
			String output = executeCommand(command, values, delay);

			if (command.requiresPolling() || command.isAssertCommand() || command.isVerifyCommand() || command.isWaitForCommand()) {
				String expected = values[values.length - 1];
				boolean result = checkResult(command, expected, output);
				LOG.info("Command '" + command.getSeleniumCommand() + "' returned '" + output + "' => " + (result ? "ok" : "not ok, expected '" + expected + "'"));

				return new SeleniumCommandResult(result, output, null);
			} else {
				LOG.info("Command '" + command.getSeleniumCommand() + "' returned '" + output + "'");
				return success(output);
			}
		} catch (Exception e) {
			return failure(e);
		}
	}

	private String executeCommand(final ExtendedSeleniumCommand command, final String[] values, long delay) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("executeCommand. Command: " + command.getSeleniumCommand() + " with values: [" + join(values, ", ") +"]");
		}

		if (commandProcessor == null) {
			throw new IllegalStateException("Command processor not running. " +
					"First start it by invoking startBrowserOnUrl.");
		}

		if ("pause".equals(command.getSeleniumCommand())) {
			pause(values[0]);
			return null;
		}

		if ("contextMenu".equals(command.getSeleniumCommand())) {
			try {
				WebElement webElement = getWebDriver().findElement(By.xpath(values[0]));
				new Actions(getWebDriver()).contextClick(webElement).perform();
				LOG.info("Performing | contextMenu | " + values[0] + " | " );
			} catch (Exception e) {
				LOG.warn("contextMenu command interrupted", e);
			}
			return null;
		}

		String output = getCommandOutput(command, values);

		waitForPageLoadIfNeeded(command);
		delayIfNeeded(delay);

		return output;
	}

	private void waitForPageLoadIfNeeded(ExtendedSeleniumCommand command) {
		if (command.isAndWaitCommand()) {
			commandProcessor.doCommand("waitForPageToLoad", new String[] { "" + timeout });
		}
	}

	private String getCommandOutput(ExtendedSeleniumCommand command, String[] values) {
		try {
			if (command.returnTypeIsArray()) {
				return executeArrayCommand(command.getSeleniumCommand(), values);
			} else {
				return executeCommand(command.getSeleniumCommand(), values);
			}
		} catch (final SeleniumException e) {
			String output = "Execution of command failed: " + e.getMessage();
			LOG.error(output);

			if (!(command.isAssertCommand() || command.isVerifyCommand() || command.isWaitForCommand())) {
				throw e;
			}

			return output;
		}
	}

	private void delayIfNeeded(long delay) {
		if (delay > 0) {
			try {
				Thread.sleep(delay);
			} catch (Exception e) {
				LOG.warn("Step delay sleep command interrupted", e);
			}
		}
	}

	private void pause(String delayInMillis) {
		try {
			Thread.sleep(Long.parseLong(delayInMillis));
		} catch (Exception e) {
			LOG.warn("Pause command interrupted", e);
		}
	}

	private String executeCommand(String methodName, final String[] values) {
		String output = commandProcessor.doCommand(methodName, values);

		if (output != null && LOG.isDebugEnabled()) {
			LOG.debug("Command processor returned '" + output + "'");
		}

		return output;
	}

	private String executeArrayCommand(String methodName, final String[] values) {
		String[] output = commandProcessor.getStringArray(methodName, values);

		if (output != null && LOG.isDebugEnabled()) {
			LOG.debug("Command processor returned '" + Arrays.asList(output) + "'");
		}

		return stringArrayToString(output);
	}

	private boolean checkResult(ExtendedSeleniumCommand command, String expected, String actual) {
		return command.matches(expected, actual);
	}

	private void writeToFile(final String filename, final String output) {
		File file = asFile(filename);
		try {
			createParentDirectoryIfNeeded(file);

			ScreenCapture.writeToFile(file, output);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void createParentDirectoryIfNeeded(File file) throws IOException {
		if (file.getParentFile() != null && !file.getParentFile().exists()) {
			if (!file.getParentFile().mkdirs()) {
				throw new IOException("Could not create parent directory " + file.getParent());
			}
		}
	}

	public void stopBrowser() {
		commandProcessor.stop();
		commandProcessor = null;

		LOG.info("Command processor stopped");
	}

	public CommandProcessor getCommandProcessor() {
		return commandProcessor;
	}

	public WebDriver getWebDriver() {
		return commandProcessor instanceof WebDriverCommandProcessor
				? ((WebDriverCommandProcessor) commandProcessor).getWrappedDriver()
						: null;
	}

	private static class SeleniumCommandResult {
		private final boolean result;

		private final String output;

		private final Exception exception;

		private SeleniumCommandResult(boolean result, String output, Exception e) {
			this.result = result;
			this.output = output;
			this.exception = e;
		}

		public boolean failed() {
			return !result;
		}

		public boolean succeeded() {
			return result;
		}

		public boolean hasException() {
			return exception != null;
		}

		public Exception getException() {
			return exception;
		}
	}

	private static SeleniumCommandResult success(String output) {
		return new SeleniumCommandResult(true, output, null);
	}

	private static SeleniumCommandResult failure() {
		return new SeleniumCommandResult(false, null, null);
	}

	private SeleniumCommandResult failure(Exception e) {
		return new SeleniumCommandResult(false, null, e);
	}


}
