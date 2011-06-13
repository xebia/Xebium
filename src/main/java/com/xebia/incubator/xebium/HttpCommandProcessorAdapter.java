package com.xebia.incubator.xebium;

import org.apache.commons.lang.NotImplementedException;

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.HttpCommandProcessor;

/**
 * <p>Make the HTTP Command Processor behave the same as a normal WebDriver command processor.
 * </p>
 * <p>It removes the status codes provided by the HttpCommandProcessor.
 * </p>
 */
public class HttpCommandProcessorAdapter implements CommandProcessor {

	private HttpCommandProcessor httpCommandProcessor;
	
	
	public HttpCommandProcessorAdapter(HttpCommandProcessor httpCommandProcessor) {
		super();
		this.httpCommandProcessor = httpCommandProcessor;
	}


	private String removeStatusCode(String output) {
		if (output.startsWith("OK,")) {
			return output.substring(3);
		}
		return output;
	}


	public String doCommand(String commandName, String[] args) {
		String output = httpCommandProcessor.doCommand(commandName, args);
		return removeStatusCode(output);
	}


	public boolean getBoolean(String arg0, String[] arg1) {
		throw new NotImplementedException();
	}


	public boolean[] getBooleanArray(String arg0, String[] arg1) {
		throw new NotImplementedException();
	}


	public Number getNumber(String arg0, String[] arg1) {
		throw new NotImplementedException();
	}


	public Number[] getNumberArray(String arg0, String[] arg1) {
		throw new NotImplementedException();
	}


	public String getRemoteControlServerLocation() {
		throw new NotImplementedException();
	}


	public String getString(String arg0, String[] arg1) {
		throw new NotImplementedException();
	}


	public String[] getStringArray(String arg0, String[] arg1) {
		throw new NotImplementedException();
	}


	public void setExtensionJs(String arg0) {
		throw new NotImplementedException();
	}


	public void start() {
		httpCommandProcessor.start();
	}


	public void start(String optionsString) {
		httpCommandProcessor.start(optionsString);
	}


	public void start(Object optionsObject) {
		httpCommandProcessor.start(optionsObject);
	}


	public void stop() {
		httpCommandProcessor.stop();
	}
}
