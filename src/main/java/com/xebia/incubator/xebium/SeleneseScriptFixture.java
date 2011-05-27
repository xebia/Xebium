package com.xebia.incubator.xebium;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;
import org.openqa.selenium.server.htmlrunner.HTMLLauncher;

public class SeleneseScriptFixture {

	private static Logger LOG = LoggerFactory.getLogger(SeleneseScriptFixture.class);
	
	private String browser = "*firefox";
	private String browserURL = "http://google.com";
	private File outputFile = new File("SeleniumScriptFixture.html");
	private int timeoutInSeconds = 30;
	private boolean multiWindow = false;

	private SeleniumServer remoteControl;

    protected String getBrowserCode(String browser) {
        if ("IE".equalsIgnoreCase(browser))
            return "*iehta";
        if ("FIREFOX".equalsIgnoreCase(browser))
            return "*firefox";
        if ("SAFARI".equalsIgnoreCase(browser))
            return "*safari";
        if ("OPERA".equalsIgnoreCase(browser))
            return "*opera";
        return browser;
    }

	public void startServer() throws Exception {
		RemoteControlConfiguration configuration = new RemoteControlConfiguration();
		configuration.setProxyInjectionModeArg(true);
		configuration.setPort(4444);
		
		remoteControl = new SeleniumServer(configuration);
		remoteControl.start();
	}

	public void startServerWithBrowserOnUrl(String browser, String url) throws Exception {
		this.browser = getBrowserCode(browser);
		browserURL = FitNesseUtil.removeAnchorTag(url);

		startServer();
	}

	public void setTimeoutToSeconds(int timeoutInSeconds) {
		this.timeoutInSeconds = timeoutInSeconds;
	}
	
	public void setBrowserUrl(String browserUrl) {
		this.browserURL = FitNesseUtil.removeAnchorTag(browserUrl);
	}
	
	public void setOutputFile(String outputFileName) {
		this.outputFile = FitNesseUtil.asFile(outputFileName);
	}
	
	public String runSuite(String scriptName) throws Exception {
		if (remoteControl == null) {
			throw new IllegalStateException("Remote control should have been started before tests are executed");
		}

		File suiteFile = FitNesseUtil.asFile(scriptName);

		String result = null;
		try {
			LOG.info("Server started, launching test suite");
			HTMLLauncher launcher = new HTMLLauncher(remoteControl);
			result = launcher.runHTMLSuite(browser, browserURL, suiteFile, outputFile, timeoutInSeconds, multiWindow);
			LOG.info("Finished execution of test suite, result = " + result);
		} catch (Exception e) {
			LOG.error("Failed to run test suite", e);
			throw e;
		}
		
		LOG.debug("End of RunScript");
		return result;
	}

	public void stopServer() {
		remoteControl.stop();
		remoteControl = null;
	}
	
}
