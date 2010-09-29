package com.xebia.incubator.xebium;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverCommandProcessor;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.HttpCommandProcessor;
import com.thoughtworks.selenium.SeleniumException;

public class SeleniumDriverFixture {

	private static Logger LOG = Logger.getLogger(SeleniumDriverFixture.class);
	
	private CommandProcessor commandProcessor;

	// TODO: decide: move to method or move to separate fixture.
	static {
		BasicConfigurator.configure();
	}

	public CommandProcessor detectWebDriverCommandProcessor(String browser, String browserUrl) {
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
	
	public void startBrowserOnUrl(String browser, String browserUrl) {

        commandProcessor = detectWebDriverCommandProcessor(browser, browserUrl);
        commandProcessor.start();
		// Deal with commands:
		// - starting with 'assert' or 'verify' -> 'is' or 'get' + nullcheck
		// - ending on 'AndWait' -> command + waitForPageToLoad

        LOG.debug("Started command processor");
	}

	public void startBrowserOnHostOnPortOnUrl(String browserStartCommand, String serverHost, int serverPort, String browserUrl) {
		commandProcessor = new HttpCommandProcessor(serverHost, serverPort, browserStartCommand, FitNesseUtil.removeAnchorTag(browserUrl));
		commandProcessor.start();
        LOG.debug("Started HTML command processor");
	}
	
//	private CommandProcessor detectCommandProcessor(String serverHost, int serverPort, String browserStartCommand, String browserURL) {
//		if ("*webdriver".equals(browserStartCommand)) {
//			return new WebDriverCommandProcessor(browserURL);
//		} else if ("*firefox-wd".equals(browserStartCommand)) {
//			return new WebDriverCommandProcessor(browserURL, DesiredCapabilities.firefox());
//		} else if ("*iexplore-wd".equals(browserStartCommand)) {
//			return new WebDriverCommandProcessor(browserURL, DesiredCapabilities.internetExplorer());
//		}
//		return new HttpCommandProcessor(serverHost, serverPort, browserStartCommand, browserURL);
//	}

	public boolean do_(String command) {
		LOG.info("Performing | " + command + " |");
		return internalDoCommand(command, null);
	}
	
	public boolean doOn(String command, String target) {
		LOG.info("Performing | " + command + " | " + target + " |");
		return internalDoCommand(command, new String[] { target });
	}
	
	public boolean doOnWith(String command, String target, String value) {
		LOG.info("Performing | " + command + " | " + target + " | " + value + " |");
		return internalDoCommand(command, new String[] { target, value });
	}

	public boolean internalDoCommand(String command, String[] values) {
		if (commandProcessor == null) {
			throw new IllegalStateException("Command processor not running. First start it by invoking startBrowserOnUrl");
		}
		// Deal with commands:
		// - starting with 'assert' or 'verify' -> 'is'
		// - ending on 'AndWait' -> command + waitForPageToLoad
		
		// TODO: check if command exists
		// TODO: is does not exist: derive default command from command name (assert*/verify*/*NotPresent/*AndWait)
		//       and register as new command.
		try {
			 String output = commandProcessor.doCommand(command, values);
			 if (output != null && LOG.isDebugEnabled()) {
				 LOG.debug("Command processor returned '" + output + "'");
			 }
			 return true;
		} catch (SeleniumException e) {
			LOG.error("Execution of command failed: " + e.getMessage());
			return false;
		}
	}

	public void stopBrowser() {
        this.commandProcessor.stop();
        LOG.info("Command processor stopped");
        this.commandProcessor = null;
	}


}
