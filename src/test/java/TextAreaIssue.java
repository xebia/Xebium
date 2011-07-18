

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverCommandProcessor;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

public class TextAreaIssue {

	WebDriver driver;
	
	@Before
	public void setUp() {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setJavascriptEnabled(true);
		driver = new FirefoxDriver(DesiredCapabilities.firefox());
	}
	
	@After
	public void tearDown() {
		driver.close();
	}

	@Test
	public void exposeIssue() throws InterruptedException {

		WebDriverCommandProcessor commandProcessor = new WebDriverCommandProcessor("http://fitnesse.org/", driver);
		commandProcessor.doCommand("open", new String[] { "/FrontPage.PluginsPage?edit" });

		commandProcessor.doCommand("type", new String[] { "pageContent", "test text" });
		Thread.sleep(1000);
		assertEquals("test text", commandProcessor.doCommand("getValue", new String[] { "pageContent" }));
		
		commandProcessor.doCommand("type", new String[] { "pageContent", "" });
		Thread.sleep(1000);
		assertEquals("", commandProcessor.doCommand("getValue", new String[] { "pageContent" }));
		
		commandProcessor.doCommand("type", new String[] { "pageContent", "final words" });
		Thread.sleep(1000);
		assertEquals("final words", commandProcessor.doCommand("getValue", new String[] { "pageContent" }));
	}
}
