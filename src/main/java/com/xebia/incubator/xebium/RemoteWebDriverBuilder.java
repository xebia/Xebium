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
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class RemoteWebDriverBuilder {

	private static final String REMOTE = "remote";
	
	private String remote;
	private Map<String, String> capabilities;

	public RemoteWebDriverBuilder(String json) {
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
	

	public URL getRemote() throws MalformedURLException {
		return new URL(remote);
	}
	
	public Capabilities getCapabilities() {		
		return new DesiredCapabilities(capabilities);
	}			
			
	/**
	 * Create a new remote-webdriver. It can be configured according to the specs on
	 * https://saucelabs.com/docs/ondemand/additional-config.
	 * 
	 * @return a fresh RemoteWebDriver instance
	 * @throws MalformedURLException 
	 */
	public WebDriver newDriver() throws MalformedURLException {
		RemoteWebDriver driver = new RemoteWebDriver(getRemote(), getCapabilities());
		
        //driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

        return driver;
	}


}
