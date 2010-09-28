package com.xebia.incubator.xebium;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriverCommandProcessor;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.SeleniumException;

public class SeleniumDriverFixture {

	private static Logger LOG = Logger.getLogger(SeleniumDriverFixture.class);
	
	private CommandProcessor commandProcessor;

	// TODO: decide: move to method or move to separate fixture.
	static {
		BasicConfigurator.configure();
	}

	public CommandProcessor detectWebDriverCommandProcessor(String browser, String browserUrl) {
		if ("firefox".equals(browser)) {
			return new WebDriverCommandProcessor(browserUrl, DesiredCapabilities.firefox());
		} else if ("iexplore".equals(browser)) {
			return new WebDriverCommandProcessor(browserUrl, DesiredCapabilities.internetExplorer());
		} else if ("chrome".equals(browser)) {
			return new WebDriverCommandProcessor(browserUrl, DesiredCapabilities.chrome());
		} else if ("htmlUnit".equals(browser)) {
			return new WebDriverCommandProcessor(browserUrl, DesiredCapabilities.htmlUnit());
		}
		throw new RuntimeException("Unknown browser type. Should be one of 'firefox', 'iexplore', 'chrome' or 'htmlUnit'");
	}
	
	public void startBrowserOnUrl(String browser, String browserUrl) {

        commandProcessor = detectWebDriverCommandProcessor(browser, browserUrl);
        commandProcessor.start();
        LOG.debug("Started command processor");
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
	}


}
