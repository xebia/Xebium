package com.xebia.incubator.xebium;

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


	public boolean getBoolean(String command, String[] args) {
		return httpCommandProcessor.getBoolean(command, args);
	}


	public boolean[] getBooleanArray(String command, String[] args) {
		return httpCommandProcessor.getBooleanArray(command, args);
	}


	public Number getNumber(String command, String[] args) {
		return httpCommandProcessor.getNumber(command, args);
	}


	public Number[] getNumberArray(String command, String[] args) {
		return httpCommandProcessor.getNumberArray(command, args);
	}


	public String getRemoteControlServerLocation() {
		return httpCommandProcessor.getRemoteControlServerLocation();
	}


	public String getString(String command, String[] args) {
		return httpCommandProcessor.getString(command, args);
	}


	public String[] getStringArray(String command, String[] args) {
		return httpCommandProcessor.getStringArray(command, args);
	}


	public void setExtensionJs(String extensionJs) {
		httpCommandProcessor.setExtensionJs(extensionJs);
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
