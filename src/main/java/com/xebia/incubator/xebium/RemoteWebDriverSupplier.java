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

import static org.openqa.selenium.remote.CapabilityType.PLATFORM;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.common.base.Supplier;

public class RemoteWebDriverSupplier implements Supplier<WebDriver> {

	private static final String REMOTE = "remote";

	private String remote;
	private Map<String, String> capabilities;

	public RemoteWebDriverSupplier(String json) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(json);
		} catch (JSONException e) {
			throw new RuntimeException("Unable to interpret browser information", e);
		}

		try {
			remote = jsonObject.getString(REMOTE);
			jsonObject.remove(REMOTE);
			capabilities = jsonObjectToMap(jsonObject);
		} catch (JSONException e) {
			throw new RuntimeException("Unable to fetch required fields from json string", e);
		}
	}

	private Map<String, String> jsonObjectToMap(JSONObject jsonObject) throws JSONException {
		// Assume you have a Map<String, String> in JSONObject
		@SuppressWarnings("unchecked")
		Iterator<String> nameItr = jsonObject.keys();
		Map<String, String> outMap = new HashMap<String, String>();
		while(nameItr.hasNext()) {
			String name = nameItr.next();
		    outMap.put(name, jsonObject.getString(name));
		}

	    String platform = outMap.get(PLATFORM);
	    if (platform != null) {
	    	outMap.put(PLATFORM, platform.toUpperCase());
	    }

		return  outMap;
	}


	public URL getRemote() {
		try {
			return new URL(remote);
		} catch (MalformedURLException e) {
			throw new RuntimeException("URL '" + remote + "' is not a valid URL");
		}
	}

	public Capabilities getCapabilities() {
		return new DesiredCapabilities(capabilities);
	}

	/**
	 * Create a new remote-webdriver. It can be configured according to the specs on
	 * https://saucelabs.com/docs/ondemand/additional-config.
	 *
	 * @return a fresh RemoteWebDriver instance
	 * @throws RuntimeException in case of any error
	 */
	public WebDriver get() {
		return new Augmenter().augment(new RemoteWebDriver(getRemote(), getCapabilities()));
	}


}
