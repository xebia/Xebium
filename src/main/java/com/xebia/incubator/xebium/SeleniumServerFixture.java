package com.xebia.incubator.xebium;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;
import org.openqa.selenium.server.cli.RemoteControlLauncher;


/**
 * Start and stop Selenium server from within FitNesse.
 */
public class SeleniumServerFixture {

	private static Logger LOG = LoggerFactory.getLogger(SeleniumServerFixture.class);

	private static SeleniumServer seleniumProxy;

	/**
	 * Start server.
	 */
	public void startSeleniumServer() {
		startSeleniumServer("");
	}
	
	/**
	 * Start server with arguments.
	 * 
	 * @param args Arguments, same as when started from command line
	 */
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

	/**
	 * Stop the server. Make sure you call this, otherwise the SliM server will not stop.
	 */
	public void stopSeleniumServer() {
		if (seleniumProxy == null) {
			throw new IllegalStateException("There is no Selenium remote server running");
		}
		seleniumProxy.stop();
		seleniumProxy = null;
	}
}
