package com.xebia.incubator.xebium;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.openqa.selenium.Platform;

public class RemoteWebDriverBuilderTest {

	@Test
	public void loadSettingsFromJson() throws MalformedURLException {
		RemoteWebDriverSupplier builder = new RemoteWebDriverSupplier("{ \"remote\": \"http://localhost\", \"platform\": \"vista\", \"browserName\": \"firefox\" }");
		assertEquals(new URL("http://localhost"), builder.getRemote());
		assertEquals(Platform.VISTA, builder.getCapabilities().getPlatform());
		assertEquals("firefox", builder.getCapabilities().getBrowserName());
	}

	@Test
	public void loadSettingsFromJsonWithFullblownUrl() throws MalformedURLException {
		RemoteWebDriverSupplier builder = new RemoteWebDriverSupplier("{ \"remote\": \"http://amolenaar:12345678-90ab-cdef-1234-etcetc@ondemand.saucelabs.com:80/wd/hub\", \"platform\": \"vista\", \"browserName\": \"firefox\" }");
		assertEquals(new URL("http://amolenaar:12345678-90ab-cdef-1234-etcetc@ondemand.saucelabs.com:80/wd/hub"), builder.getRemote());
		assertEquals(Platform.VISTA, builder.getCapabilities().getPlatform());
		assertEquals("firefox", builder.getCapabilities().getBrowserName());
	}

	@Test(expected=RuntimeException.class)
	public void loadInvalidSettings() throws MalformedURLException {
		RemoteWebDriverSupplier builder = new RemoteWebDriverSupplier("\"remote\": \"http://localhost\", \"platform\": \"vista\", \"browserName\": \"firefox\"");
		assertEquals(new URL("http://localhost"), builder.getRemote());
		assertEquals(Platform.VISTA, builder.getCapabilities().getPlatform());
		assertEquals("firefox", builder.getCapabilities().getBrowserName());
	}

}
