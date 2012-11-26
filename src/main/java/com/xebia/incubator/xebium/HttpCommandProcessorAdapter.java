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

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.HttpCommandProcessor;

/**
 * <p>Make the HTTP Command Processor behave the same as a normal WebDriver command processor.
 * </p>
 * <p>It removes the status codes provided by the HttpCommandProcessor.
 * </p>
 *
 * @deprecated Deprecated like the rest of the Selenium 1 simulation stuff.
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
