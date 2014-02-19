package com.xebia.incubator.xebium;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.thoughtworks.selenium.CommandProcessor;
import org.openqa.selenium.WebDriverException;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class SeleniumDriverFixtureTest {

	@Mock
	private CommandProcessor commandProcessor;

	@Mock
	private ScreenCapture screenCapture;

	private SeleniumDriverFixture seleniumDriverFixture;

	@Before
	public void setup() throws Exception {
		this.seleniumDriverFixture = new SeleniumDriverFixture();
		seleniumDriverFixture.setCommandProcessor(commandProcessor);
		seleniumDriverFixture.setScreenCapture(screenCapture);
	}

    @Test
    public void shouldDoVerifyRegularTextPresent() {
        given(commandProcessor.doCommand("isTextPresent", new String[] { "foo" })).willReturn("true");
        final boolean result = seleniumDriverFixture.doOn("verifyTextPresent", "foo");
        assertThat(result, is(true));
    }

    @Test
    public void checkIsRegularTextPresent() {
        given(commandProcessor.doCommand("isTextPresent", new String[] { "foo" })).willReturn("true");
        final String result = seleniumDriverFixture.isOn("isTextPresent", "foo");
        assertThat(result, is("true"));
    }

    @Test
    public void shouldDoVerifyRegularTextNotPresent() {
        given(commandProcessor.doCommand("isTextPresent", new String[] { "foo" })).willReturn("false");
        final boolean result = seleniumDriverFixture.doOn("verifyTextNotPresent", "foo");
        assertThat(result, is(true));
    }

    @Test
    public void checkIsRegularTextNotPresent() {
        given(commandProcessor.doCommand("isTextPresent", new String[] { "foo" })).willReturn("true");
        final String result = seleniumDriverFixture.isOn("isTextNotPresent", "foo");
        assertThat(result, is("false"));
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

    @Test
    public void shouldResolveAlias() {
        String expectedString = "Het laatste nieuws het eerst op nu.nl";
        String alias = "laatsteNieuws";

        given(commandProcessor.doCommand(anyString(), isA(String[].class))).willReturn(expectedString);
        seleniumDriverFixture.addAliasForLocator(alias, expectedString);
        final boolean result = seleniumDriverFixture.doOnWith("verifyText", "//*[@id='masthead']/div/h1", "%" + alias);
        assertThat(result, is(true));
    }

    @Test
    public void shouldIgnoreMissingAlias() {
        given(commandProcessor.doCommand(anyString(), isA(String[].class))).willReturn("%foo");
        final boolean result = seleniumDriverFixture.doOnWith("verifyText", "//*[@id='masthead']/div/h1", "%foo");
        assertThat(result, is(true));
    }

    @Test
    public void shouldIgnoreEmptyAlias() {
        given(commandProcessor.doCommand(anyString(), isA(String[].class))).willReturn("%");
        final boolean result = seleniumDriverFixture.doOnWith("verifyText", "//*[@id='masthead']/div/h1", "%");
        assertThat(result, is(true));
    }

	@Test
	public void shouldTakeScreenshotOnError() throws IOException {
		seleniumDriverFixture.saveScreenshotAfter("FAILURE");

		when(commandProcessor.getBoolean("isElementPresent", new String[] { "id=verwijderen" })).thenReturn(true);
		when(commandProcessor.doCommand("click", new String[] {"id=verwijderen"}))
				.thenThrow(new WebDriverException("Click failed: ReferenceError: Can't find variable: handle"));
		when(screenCapture.requireScreenshot(any(ExtendedSeleniumCommand.class), anyBoolean()))
				.thenReturn(true);

		try {
			seleniumDriverFixture.doOn("clickAndWait", "id=verwijderen");
		} catch (Throwable t) {
			// Not sure whether we want to propagate this exception... that's the current behaviour though.
		}

		verify(screenCapture).captureScreenshot("clickAndWait", new String[] { "id=verwijderen" });
	}
}
