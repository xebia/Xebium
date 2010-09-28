package com.xebia.incubator.xebium;

public class FitNesseUtil {

	private FitNesseUtil() {

	}

	/**
	 * scriptName is something like
	 * '<a href="http://some.url/files/selenium/Suite">http://files/selenium/Suite</a>'.
	 * 
	 * @param scriptName
	 * @return
	 */
	public static String removeAnchorTag(String scriptName) {
		if (scriptName.startsWith("<a") && scriptName.endsWith("</a>")) {
			scriptName = scriptName.split(">", 2)[1].split("<", 2)[0];
//			LOG.debug("Extracted script name from URL: " + scriptName);
		}
		return scriptName;
	}
	
	
	
	
}
