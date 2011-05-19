package com.xebia.incubator.xebium;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;
import org.openqa.selenium.server.cli.RemoteControlLauncher;


public class SeleniumServerFixture {

	private static Logger LOG = Logger.getLogger(SeleniumServerFixture.class);

	private static SeleniumServer seleniumProxy;

	public void startSeleniumServer() {
		startSeleniumServer("");
	}
	
	public void startSeleniumServer(final String args) {
		if (seleniumProxy != null) {
			throw new IllegalStateException("There is already a Selenium remote server running");
		}
		
		try {
	        final RemoteControlConfiguration configuration;

	        LOG.info("Starting server with arguments: '" + args + "'");
	        
	        //String[] argv = StringUtils.isNotBlank(args) ? StringUtils.split(args) : new String[] {};
	        String[] argv = StringUtils.split(args);
	        
	        configuration = RemoteControlLauncher.parseLauncherOptions(argv);
	        //checkArgsSanity(configuration);

	        System.setProperty("org.openqa.jetty.http.HttpRequest.maxFormContentSize", "0"); // default max is 200k; zero is infinite
	        seleniumProxy = new SeleniumServer(isSlowConnection(), configuration);
	        seleniumProxy.start();
		} catch (Exception e) {
			//server.stop();
			LOG.info("Server stopped");
		}
	}
	
	private boolean isSlowConnection() {
		return false;
	}

	public void stopSeleniumServer() {
		if (seleniumProxy == null) {
			throw new IllegalStateException("There is no Selenium remote server running");
		}
		seleniumProxy.stop();
		seleniumProxy = null;
	}
}
