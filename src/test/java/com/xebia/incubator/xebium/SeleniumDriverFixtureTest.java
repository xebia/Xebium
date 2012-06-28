package com.xebia.incubator.xebium;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.thoughtworks.selenium.CommandProcessor;

@RunWith(MockitoJUnitRunner.class)
public class SeleniumDriverFixtureTest {

	@Mock
	private CommandProcessor commandProcessor;

	private SeleniumDriverFixture seleniumDriverFixture;

	@Before
	public void setup() throws Exception {
		this.seleniumDriverFixture = new SeleniumDriverFixture();
		seleniumDriverFixture.setCommandProcessor(commandProcessor);
	}

	@Test
	public void shouldVerifyRegularTextWithRegularExpressions() throws Exception {
		given(commandProcessor.doCommand(anyString(), isA(String[].class))).willReturn("Di 9 november 2010. Het laatste nieuws het eerst op nu.nl");
		final boolean result = seleniumDriverFixture.doOnWith("verifyText", "//*[@id='masthead']/div/h1", "regexp:.*Het laatste nieuws het eerst op nu.nl");
		assertThat(result, is(true));
	}

	@Test
	public void shouldNegateIfCommandRequiresIt() throws Exception {
		given(commandProcessor.doCommand(anyString(), isA(String[].class))).willReturn("Di 9 november 2010. Het laatste nieuws het eerst op nu.nl");
		final boolean result = seleniumDriverFixture.doOnWith("verifyNotText", "//*[@id='masthead']/div/h1", "regexp:.*Het laatste nieuws het eerst op nu.nl");
		assertThat(result, is(false));
	}

	@Test
	public void shouldMatchWithoutRegularExpression() throws Exception {
		given(commandProcessor.doCommand(anyString(), isA(String[].class))).willReturn("Di 9 november 2010. Het laatste nieuws het eerst op nu.nl");
		final boolean result = seleniumDriverFixture.doOnWith("verifyText", "//*[@id='masthead']/div/h1",  "*Het laatste nieuws het eerst op nu.nl");
		assertThat(result, is(true));
	}
	
	@Test
	public void shouldMatchMultiValueStrings() {
		given(commandProcessor.getStringArray(anyString(), isA(String[].class))).willReturn(new String[] { "Suite", "Test", "Normal" });
		final boolean result = seleniumDriverFixture.doOnWith("verifySelectOptions", "//foo",  "Suite,Test,Normal");
		assertThat(result, is(true));
	}
}
