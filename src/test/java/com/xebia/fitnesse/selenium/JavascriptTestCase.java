package com.xebia.fitnesse.selenium;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

public class JavascriptTestCase {

	final static String TEST_ENVIRONMENT = "src/test/resources/testCase.js";
	final static String FILE_UNDER_TEST = "src/main/ide/chrome/content/formats/xebiumformatter.js";

	final String EOL = System.getProperty("line.separator");

	private Context context;
	private ScriptableObject globalScope;

    @Before
	public void init() throws Exception {
		context = Context.enter();
		globalScope = context.initStandardObjects();
		String script;
		script = loadScript(TEST_ENVIRONMENT);
		assertNotNull(script);
		eval(script);
		
		script = loadScript(FILE_UNDER_TEST);
		assertNotNull(script);
		eval(script);
	}

    @After
	public void destroy() throws Exception {
		Context.exit();
	}

	/**
	 * Executes an arbitrary expression.<br>
	 * It fails if the expression throws a JsAssertException.<br>
	 * It fails if the expression throws a RhinoException.<br>
	 * 
	 * Code from JsTester (http://jstester.sf.net/)
	 */
	public Object eval(String expr) {
		Object value = null;
		try {
			value = context.evaluateString(globalScope, expr, "", 1, null);
		} catch (JavaScriptException jse) {
			Scriptable jsAssertException = (Scriptable) globalScope.get(
					"currentException", globalScope);
			jse.printStackTrace();
			String message = (String) jsAssertException.get("message",
					jsAssertException);
			if (message != null) {
				fail(message);
			}
		} catch (RhinoException re) {
			fail(re.getMessage());
		}
		return value;
	}

	public String loadScript(String name) throws IOException {
		BufferedReader reader = null;
		StringBuffer contents = new StringBuffer();
		
		try {
			InputStream in = new FileInputStream(new File(name));
			reader = new BufferedReader(new InputStreamReader(in));
			String text = null;

			// repeat until all lines is read
			while ((text = reader.readLine()) != null) {
				contents.append(text).append(EOL);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return contents.toString();
	}

	/**
	 * Check the working of the Command object.
	 */
	@Test
	public void testCommand() {
		Object result;
		
		result = eval("var cmd = new Command('one', 'two', 'three');");
		assertTrue(result instanceof Undefined);
		
		result = eval("typeof cmd");
		assertEquals("object", (String) result);
		result = eval("cmd.type");
		assertEquals("command", (String) result);
	}
	
	@Test
	public void testEmptyTestCase() {
		String result = (String) eval("var tc = new TestCase(); tc.baseUrl = 'http://example.com'; format(tc, 'name');");
		
		assertEquals("| script | selenium driver fixture |\n" +
					 "| start browser | firefox | on url | http://example.com |\n" +
					 "| stop browser |\n" 
					 , result);
	}

    @Test
    public void testLibraryModeOption() {
		String result = (String) eval("var tc = new TestCase(); tc.baseUrl = 'http://example.com'; options.libraryMode = 'true'; format(tc, 'name');");
		
		assertEquals("| script |\n" +
					 "| start browser | firefox | on url | http://example.com |\n" +
					 "| stop browser |\n" 
					 , result);
    }

    @Test
    public void testNoStartStopOption() {
		String result = (String) eval("var tc = new TestCase(); options.noStartStop = 'true'; format(tc, 'name');");
		
		assertEquals("| script | selenium driver fixture |\n"
					 , result);
    }

	@Test
	public void testStartBrowserLine() {
		String result = (String) eval("var tc = new TestCase(); tc.startBrowserLine = '| start browser | foo |  bar | baz |'; format(tc, 'name');");
		
		assertEquals("| script | selenium driver fixture |\n" +
					 "| start browser | foo |  bar | baz |\n" +
					 "| stop browser |\n" 
					 , result);
	}

    @Test
    public void testExecuteCommandOnTargetWithValueToSelenese() {
        eval("var cmd = getCommandForSource('| ensure | do | {command} | on | {target} | with | complex value |');");
        assertEquals("{command}", eval("cmd.command"));
        assertEquals("{target}", eval("cmd.target"));
        assertEquals("complex value", eval("cmd.value"));
    }

    @Test
    public void testExecuteCommandOnTargetWithValueWithSpacesToSelenese() {
        eval("var cmd = getCommandForSource('| ensure | do | command | on | some  target | with | some  value |');");
        assertEquals("command", eval("cmd.command"));
        assertEquals("some  target", eval("cmd.target"));
        assertEquals("some  value", eval("cmd.value"));
    }


    @Test
    public void testExecuteCommandOnTargetToSelenese() {
        eval("var cmd = getCommandForSource('| ensure | do | command | on | target value|');");
        assertEquals("command", eval("cmd.command"));
        assertEquals("target value", eval("cmd.target"));
        assertEquals("", eval("cmd.value"));
    }

    @Test
    public void testExecuteCommandOnEscapedTargetToSelenese() {
        eval("var cmd = getCommandForSource('| ensure | do | command | on | !-target-! |');");
        assertEquals("command", eval("cmd.command"));
        assertEquals("target", eval("cmd.target"));
        assertEquals("", eval("cmd.value"));
    }
    
    @Test
    public void testExecuteCommandOnEscapedUrlTargetToSelenese() {
        eval("var cmd = getCommandForSource('| ensure | do | command | on | !-http://target-! |');");
        assertEquals("command", eval("cmd.command"));
        assertEquals("http://target", eval("cmd.target"));
        assertEquals("", eval("cmd.value"));
    }
    
    @Test
    public void shouldParseLenient() {
        // Can't parse line: '| ensure  | do  | deleteAllVisibleCookies  | on |'
        eval("var cmd = getCommandForSource('| ensure | do | command | on |');");
        assertEquals("command", eval("cmd.command"));
        assertEquals("", eval("cmd.target"));
        assertEquals("", eval("cmd.value"));
    }

    @Test
    public void testExecuteCommandOnEscapedTargetWithEscapedValueToSelenese() {
        eval("var cmd = getCommandForSource('| ensure | do | command | on | !-target-! | with | !-value-!|');");
        assertEquals("command", eval("cmd.command"));
        assertEquals("target", eval("cmd.target"));
        assertEquals("value", eval("cmd.value"));
    }

    @Test
    public void testExecuteCommandOnVariableToSelenese() {
        eval("var cmd = getCommandForSource('| ensure | do | command | on | $target|');");
        assertEquals("command", eval("cmd.command"));
        assertEquals("${target}", eval("cmd.target"));
        assertEquals("", eval("cmd.value"));
    }

    @Test
    public void testExecuteCommandOnVariableWithVariableToSelenese() {
        eval("var cmd = getCommandForSource('| ensure | do | command | on | $target | with | $value|');");
        assertEquals("command", eval("cmd.command"));
        assertEquals("${target}", eval("cmd.target"));
        assertEquals("${value}", eval("cmd.value"));
    }

    @Test
    public void testExecuteCommandOnTextSeparatedByBars() {
    	eval("var cmd = getCommandForSource('| ensure | do | verifyTitle | on | !-example.com | Examplish stuff | Home-! |');");
        assertEquals("verifyTitle", eval("cmd.command"));
        assertEquals("example.com | Examplish stuff | Home", eval("cmd.target"));
        assertEquals("", eval("cmd.value"));

    	eval("var cmd = getCommandForSource('| ensure | do | command | on | blah | with | !-example.com | Examplish stuff | Home-! |');");
        assertEquals("command", eval("cmd.command"));
        assertEquals("blah", eval("cmd.target"));
        assertEquals("example.com | Examplish stuff | Home", eval("cmd.value"));

    }
    
    @Test
    public void testExecuteCommentToSelenese() {
        eval("var cmd = getCommandForSource('| note | My comments |');");
        assertEquals("My comments", eval("cmd.comment"));
    }

    @Test
    public void testExecuteCommentWithMoreTextToSelenese() {
        eval("var cmd = getCommandForSource('| note | My comments |  and some more |  ');");
        assertEquals("My comments |  and some more", eval("cmd.comment"));
    }
    
    @Test
    public void shouldExecuteCommandOnTargetToFitnesse() {
		eval("var tc = new TestCase(); tc.baseUrl = 'http://example.com';");
		eval("var commands = [];");
        eval("commands.push(new Command('open', 'http://myurl.com'))");
        eval("commands.push(new Command('open', 'foo', 'bar'))");
		eval("tc.commands = commands;");
		String result = (String) eval("format(tc, 'name');");
		assertEquals(
                "| script | selenium driver fixture |\n" +
				"| start browser | firefox | on url | http://example.com |\n" +
				"| ensure | do | open | on | !-http://myurl.com-! |\n" +
                "| ensure | do | open | on | foo | with | bar |\n" +
				"| stop browser |\n"
				, result);
    }

    @Test
    public void shouldStoreCommandOnTargetToFitnesse() {
		eval("var tc = new TestCase(); tc.baseUrl = 'http://example.com';");
		eval("var commands = [];");
        eval("commands.push(new Command('storeLocation', 'locVar'))");
        eval("commands.push(new Command('storeTest', 'foo', 'bar'))");
        eval("commands.push(new Command('verifyTest', 'foo', '${locVar}'))");
		eval("tc.commands = commands;");
		String result = (String) eval("format(tc, 'name');");
		assertEquals(
                "| script | selenium driver fixture |\n" +
				"| start browser | firefox | on url | http://example.com |\n" +
				"| $locVar= | is | storeLocation |\n" +
                "| $bar= | is | storeTest | on | foo |\n" +
                "| ensure | do | verifyTest | on | foo | with | $locVar |\n" +
				"| stop browser |\n"
				, result);
    }

   
    @Test
    public void shouldWikiWordsAndUrlsToFitnesse() {
		eval("var tc = new TestCase(); tc.baseUrl = 'http://example.com';");
		eval("var commands = [];");
        eval("commands.push(new Command('someUrl', 'http://example.com'))");
        eval("commands.push(new Command('testWikiWord', 'WikiWord', 'W10W'))");
        eval("commands.push(new Command('testVariable', 'foo', '${locVar}'))");
        eval("commands.push(new Command('testEmail', 'anonymous@example.com'))");
		eval("tc.commands = commands;");
		String result = (String) eval("format(tc, 'name');");
		assertEquals(
                "| script | selenium driver fixture |\n" +
				"| start browser | firefox | on url | http://example.com |\n" +
                "| ensure | do | someUrl | on | !-http://example.com-! |\n" +
                "| ensure | do | testWikiWord | on | !-WikiWord-! | with | !-W10W-! |\n" +
                "| ensure | do | testVariable | on | foo | with | $locVar |\n" +
                "| ensure | do | testEmail | on | !-anonymous@example.com-! |\n" +
				"| stop browser |\n"
				, result);
    }

    @Test
    public void shouldParseStartBrowserLineToFitnesse() {
		eval("var tc = new TestCase(); tc.baseUrl = 'http://example.com';");
		eval("var commands = [];");
        eval("commands.push(new Command('someUrl', 'http://example.com'))");
        eval("commands.push(new Command('testWikiWord', 'WikiWord', 'W10W'))");
        eval("commands.push(new Command('testVariable', 'foo', '${locVar}'))");
        eval("commands.push(new Command('testEmail', 'anonymous@example.com'))");
		eval("tc.commands = commands;");
		String result = (String) eval("format(tc, 'name');");
		assertEquals(
                "| script | selenium driver fixture |\n" +
				"| start browser | firefox | on url | http://example.com |\n" +
                "| ensure | do | someUrl | on | !-http://example.com-! |\n" +
                "| ensure | do | testWikiWord | on | !-WikiWord-! | with | !-W10W-! |\n" +
                "| ensure | do | testVariable | on | foo | with | $locVar |\n" +
                "| ensure | do | testEmail | on | !-anonymous@example.com-! |\n" +
				"| stop browser |\n"
				, result);
    }
    
    @Test
    public void shouldExecuteCommandOnTargetWithValueToSelenese() {
		// Only open and check command should be parsed
		eval("var fittable = '| script | selenium driver fixture |\\n' +" +
				"'| start browser | firefox | on url | http://example.com |\\n' +" +
				"'| ensure | do | open | on | http://www.google.com | with | dummy |\\n' +" +
                "'| stop browser |\\n';");
		eval("var tc = new TestCase();");
		eval("parse(tc, fittable);");
		eval("var commands = tc.commands;");

		assertEquals(1.0, eval("commands.length"));
		assertEquals("open", eval("commands[0].command"));
        assertEquals("http://www.google.com", eval("commands[0].target"));
        assertEquals("dummy", eval("commands[0].value"));
    }

    @Test
    public void shouldExecuteStoreCommandToSelenese() {
		// Only open and check command should be parsed
		eval("var fittable = '| script | selenium driver fixture |\\n' +" +
				"'| $MyVar= | is | getValue | on | link=Test |\\n' +" +
                "'| stop browser |\\n';");
		eval("var tc = new TestCase();");
		eval("parse(tc, fittable);");
		eval("var commands = tc.commands;");

		assertEquals(1.0, eval("commands.length"));
		assertEquals("storeValue", eval("commands[0].command"));
        assertEquals("link=Test", eval("commands[0].target"));
        assertEquals("MyVar", eval("commands[0].value"));
    }

    @Test
    public void shouldMakeCommentsForNonFitnesseLines() {
		// Only open and check command should be parsed
		eval("var fittable = '| script | selenium driver fixture |\\n' +" +
				"'some crap\\n' +" +
				"'different crap\\n' +" +
                "'| stop browser |\\n';");
		eval("var tc = new TestCase();");
		eval("parse(tc, fittable);");
		eval("var commands = tc.commands;");

		assertEquals(2.0, eval("commands.length"));
		assertEquals("Can't parse line: 'some crap'", eval("commands[0].comment"));
    }

    @Test
    public void shouldIgnoreFitnesseTableHeadings() {
		// Only open and check command should be parsed
		eval("var fittable = '| script | selenium driver fixture |\\n' +" +
				"'| scenario | some name |\\n' +" +
				"'| SCRIPT | some name |\\n' +" +
				"'| ScEnArIo | some name |\\n' +" +
				"'| start browser | firefox | on url | http://example.com |\\n' +" +
				"'| ensure | do | something | on | |' +" +
				"'different crap\\n' +" +
                "'| stop browser |\\n';");
		eval("var tc = new TestCase();");
		eval("parse(tc, fittable);");
		eval("var commands = tc.commands;");

		assertEquals(1.0, eval("commands.length"));
		assertEquals("something", eval("commands[0].command"));
    }

    @Test
    public void shouldParseDataPastedFromBrowser() {
		// Only open and check command should be parsed
		eval("var fittable = 'script \tselenium driver fixture\\n' +" +
				"'start browser \tfirefox \ton url \thttp://example.com\\n' +" +
				"'ensure \tdo \topen \ton \thttp://www.google.com \twith \tdummy\\n' +" +
                "'stop browser\\n';");
		eval("var tc = new TestCase();");
		eval("parse(tc, fittable);");
		eval("var commands = tc.commands;");

		assertEquals(2.0, eval("commands.length"));
		assertEquals("open", eval("commands[0].command"));
        assertEquals("http://www.google.com", eval("commands[0].target"));
        assertEquals("dummy", eval("commands[0].value"));

    }

    @Test
    public void shouldParseSubstitutedVariables() {
    	eval("var fittable = 'script\tselenium driver fixture\\n' +" +
			    "'ensure\tdo\topen\ton\t/FitNesse.ProjectXebium.ExampleSuite.VariablesExample\\n' +" +
			    "'ensure\tdo\tsetTimeout\ton\t1000\\n' +" +
			    "'$pageName<-[VariablesExample]\tis\tgetText\ton\t//span\\n' +" +
			    "'$title<-[FitNesse.ProjectXebium.ExampleSuite.VariablesExample]\tis\tgetTitle\\n' +" +
			    "'ensure\tdo\tclick\ton\tlink=Search\\n' +" +
			    "'ensure\tdo\ttype\ton\tsearchString\twith\t$pageName->[VariablesExample]\\n' +" +
			    "'ensure\tdo\twaitForTextPresent\ton\t$title->[FitNesse.ProjectXebium.ExampleSuite.VariablesExample]\\n' +" +
			    "'$DIFF<-[9749]\tis\tgetEval\ton\t$END->[1306741646307] - $START->[1306741636558]\\n' +" +
			    "'stop browser';");

		eval("var tc = new TestCase();");
		eval("parse(tc, fittable);");
		eval("var commands = tc.commands;");

		assertEquals(9.0, eval("commands.length"));

		assertEquals(eval("commands[2].comment").toString(), "storeText", eval("commands[2].command"));
        assertEquals("//span", eval("commands[2].target"));
        assertEquals("pageName", eval("commands[2].value"));

		assertEquals(eval("commands[3].comment").toString(), "storeTitle", eval("commands[3].command"));
        assertEquals("title", eval("commands[3].target"));

		assertEquals(eval("commands[5].comment").toString(), "type", eval("commands[5].command"));
        assertEquals("searchString", eval("commands[5].target"));
        assertEquals("${pageName}", eval("commands[5].value"));

		assertEquals(eval("commands[6].comment").toString(), "waitForTextPresent", eval("commands[6].command"));
        assertEquals("${title}", eval("commands[6].target"));

		assertEquals(eval("commands[7].comment").toString(), "storeEval", eval("commands[7].command"));
        assertEquals("${END} - ${START}", eval("commands[7].target"));
        assertEquals("DIFF", eval("commands[7].value"));
    }

}
