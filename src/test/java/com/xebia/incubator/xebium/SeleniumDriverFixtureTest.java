package com.xebia.incubator.xebium;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;


@Ignore
public class SeleniumDriverFixtureTest {

	@Test
	public void testDefaultTest() {
//		|script|selenium driver fixture|
		SeleniumDriverFixture fixture = new SeleniumDriverFixture();
//		| ensure | start browser | firefox | on url | http://www.google.com |
		fixture.startBrowserOnUrl("firefox", "http://www.google.com");
//		| ensure | do | open | on | / |
		assertTrue(fixture.doOn("open", "/"));
//		| ensure | do | type | on | q | with | xebium is the new test solution |
		assertTrue(fixture.doOnWith("type", "q", "blah"));
//		| stop browser |
		fixture.stopBrowser();
	}
}
