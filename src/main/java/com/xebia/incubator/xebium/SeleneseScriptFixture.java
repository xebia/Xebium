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

import static com.xebia.incubator.xebium.FitNesseUtil.asFile;
import static com.xebia.incubator.xebium.FitNesseUtil.removeAnchorTag;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;
import org.openqa.selenium.server.htmlrunner.HTMLLauncher;

/**
 * Run Selenese (html) test suites.
 */
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
		browserURL = removeAnchorTag(url);

		startServer();
	}

	public void setTimeoutToSeconds(int timeoutInSeconds) {
		this.timeoutInSeconds = timeoutInSeconds;
	}
	
	public void setBrowserUrl(String browserUrl) {
		this.browserURL = removeAnchorTag(browserUrl);
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
	
}
