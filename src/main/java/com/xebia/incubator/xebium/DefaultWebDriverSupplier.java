package com.xebia.incubator.xebium;

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.OperaProduct;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.PreferencesWrapper;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class DefaultWebDriverSupplier implements ConfigurableWebDriverSupplier {

	private static final Logger LOG = LoggerFactory.getLogger(SeleniumDriverFixture.class);

	private String browser;

    private File customProfilePreferencesFile;

	private File profileDirectory;

	public DefaultWebDriverSupplier() {
	}

	public WebDriver newWebDriver() {
		WebDriver driver;
		if ("firefox".equalsIgnoreCase(browser)) {
			FirefoxProfile profile;
			// Load FireFox-profile if present
			if (profileDirectory != null) {
				profile = new FirefoxProfile(profileDirectory);
				LOG.info("Firefox profile successfully loaded");
			}
			else {
				profile = new FirefoxProfile();
			}

			if (customProfilePreferencesFile != null) {
				PreferencesWrapper prefs = loadFirefoxPreferences();

				prefs.addTo(profile);
				try {
					StringWriter writer = new StringWriter(512);
					prefs.writeTo(writer);
					LOG.info("Added properties to firefox profile: " + writer.toString());
				} catch (IOException e) {
					LOG.error("Unable to log firefox profile settings", e);
				}
			}

			// Ensure we deal with untrusted and unverified hosts.
			profile.setAcceptUntrustedCertificates(true);
			profile.setAssumeUntrustedCertificateIssuer(true);

			driver = new FirefoxDriver(profile);
		} else if ("iexplore".equalsIgnoreCase(browser)) {
			driver = new InternetExplorerDriver();
		} else if ("chrome".equalsIgnoreCase(browser)) {
			driver = new ChromeDriver();
		} else if ("safari".equalsIgnoreCase(browser)) {
			driver = new SafariDriver();
		} else if ("htmlUnit".equalsIgnoreCase(browser)) {
			driver = new HtmlUnitDriver();
		} else if ("htmlUnit+js".equalsIgnoreCase(browser)) {
			driver = new HtmlUnitDriver(true);
		} else if ("opera".equalsIgnoreCase(browser)) {
            driver = new OperaDriver();
        } else if ("opera-mobile-tablet".equalsIgnoreCase(browser)) {
            DesiredCapabilities capabilities = DesiredCapabilities.opera();

            // tell opera mobile to use the tablet ui
            capabilities.setCapability("opera.product", OperaProduct.MOBILE);
            capabilities.setCapability("opera.arguments", "-tabletui -displaysize 860x600");

            driver = new OperaDriver(capabilities);
        } else if ("opera-mobile-phone".equalsIgnoreCase(browser)) {
            DesiredCapabilities capabilities = DesiredCapabilities.opera();

            // tell opera mobile to use the mobile handset ui
            capabilities.setCapability("opera.product", OperaProduct.MOBILE);
            capabilities.setCapability("opera.arguments", "-mobileui");

            driver = new OperaDriver(capabilities);
        } else if ("phantomjs".equalsIgnoreCase(browser)) {
            driver = new PhantomJSDriver(DesiredCapabilities.phantomjs());
		} else {
			try {
				driver = new RemoteWebDriverSupplier(browser).get();
			} catch (Exception e) {
				throw new RuntimeException("Unknown browser type. Should be one of 'firefox', 'iexplore', 'chrome', " +
                        "'opera', 'opera-mobile-tablet', 'opera-mobile-phone', 'htmlUnit' or 'htmlUnit+js'", e);
			}
		}
		return driver;
	}

	private PreferencesWrapper loadFirefoxPreferences() {
		PreferencesWrapper prefs;
		FileReader reader;
		try {
			reader = new FileReader(customProfilePreferencesFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		try {
			prefs = new PreferencesWrapper(reader);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				LOG.error("Unable to close firefox profile settings file", e);
			}
		}
		return prefs;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

	public void setCustomProfilePreferencesFile(
			File customProfilePreferencesFile) {
		this.customProfilePreferencesFile = customProfilePreferencesFile;
	}

	public void setProfileDirectory(File profileDirectory) {
		this.profileDirectory = profileDirectory;
	}

	@Override
	public String toString() {
		return getClass().getName();
	}
}
