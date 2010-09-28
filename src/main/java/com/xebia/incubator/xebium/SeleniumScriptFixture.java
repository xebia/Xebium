package com.xebia.incubator.xebium;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;
import org.openqa.selenium.server.htmlrunner.HTMLLauncher;

public class SeleniumScriptFixture {

	private static Logger LOG = Logger.getLogger(SeleniumScriptFixture.class);
	
	private String browser = "*firefox";
	private String browserURL = "http://google.com";
	private File outputFile = new File("SeleniumScriptFixture.html");
	private int timeoutInSeconds = 30;
	private boolean multiWindow = false;

	private SeleniumServer remoteControl;

	static {
		BasicConfigurator.configure();
	}

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
		this.outputFile = asFile(outputFileName);
	}
	
	public String runSuite(String scriptName) throws Exception {
		if (remoteControl == null) {
			throw new IllegalStateException("Remote control should have been started before tests are executed");
		}

		File suiteFile = asFile(scriptName);

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
	
	/**
	 * Obtain the script name from a wiki url. The URL may be in the format
	 * <code>http://files/selenium/Suite</code> and 
	 * <code>&lt;a href="/files/selenium/Suite"&gt;http://files/selenium/Suite&lt;/a&gt;</code>
	 * 
	 * @param scriptName
	 * @return a sane path name. Relative to the CWD.
	 */
	private File asFile(final String scriptName) {
		String fileName = FitNesseUtil.removeAnchorTag(scriptName).replaceAll("http:/", "FitNesseRoot");
		
		return new File(fileName);
	}

}
