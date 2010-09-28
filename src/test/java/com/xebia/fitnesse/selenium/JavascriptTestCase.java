package com.xebia.fitnesse.selenium;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

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

	final static String TEST_ENVIRONMENT = "testCase.js";
	final static String FILE_UNDER_TEST = "FitnesseSeleniumIDE.js";

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
			InputStream in = getClass().getClassLoader().getResourceAsStream(name);
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
		
		assertEquals("| script |\n" +
						"| start | selenium driver |\n" +
						"| start browser | firefox | with selenium rc | localhost | on port | 4444 | point at | !-http://example.com-! |\n" +
						"| shutdown browser |\n" 
						, result);
	}
	
    @Test
    public void testExecuteCommandOnTargetWithValueToSelenese() {
        eval("var cmd = getCommandForSource('| ensure | do | ${command} | on | ${target} | with | ${value} |');");
        assertEquals("${command}", eval("cmd.command"));
        assertEquals("${target}", eval("cmd.target"));
        assertEquals("${value}", eval("cmd.value"));
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
                "| script |\n" +
				"| start | selenium driver |\n" +
				"| start browser | firefox | with selenium rc | localhost | on port | 4444 | point at | !-http://example.com-! |\n" +
				"| ensure | do | open | on | http://myurl.com |\n" +
                "| ensure | do | open | on | foo | with | bar |\n" +
				"| shutdown browser |\n"
				, result);
    }

    @Test
    public void shouldExecuteCommandOnTargetWithValueToSelenese() {
		// Only open and check command should be parsed
		eval("var fittable = '| script |\\n' +" +
				"'| start | selenium driver |\\n' +" +
				"'| start browser | firefox | with selenium rc | localhost | on port | 4444 | point at | http://example.com |\\n' +" +
				"'| ensure | do | open | on | http://www.google.com | with | dummy |\\n' +" +
                "'| shutdown browser |\\n';");
		eval("var tc = new TestCase();");
		eval("parse(tc, fittable);");
		eval("var commands = tc.commands;");

		assertEquals(1.0, eval("commands.length"));
		assertEquals("open", eval("commands[0].command"));
        assertEquals("http://www.google.com", eval("commands[0].target"));
        assertEquals("dummy", eval("commands[0].value"));
    }

}
