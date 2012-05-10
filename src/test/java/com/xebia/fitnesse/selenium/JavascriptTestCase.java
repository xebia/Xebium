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
import org.junit.BeforeClass;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

public class JavascriptTestCase {

	final static String TEST_ENVIRONMENT = "src/test/resources/testCase.js";
	final static String TEST_SETUP = "src/test/resources/testSetup.js";
	final static String FILE_UNDER_TEST = "src/main/ide/chrome/content/formats/xebiumformatter.js";

	final static String EOL = System.getProperty("line.separator");

	private static ContextFactory contextFactory;

	private Context context;
	private ScriptableObject globalScope;

	@BeforeClass
	public static void classInit() {
    	contextFactory = new ContextFactory();
	}

	@Before
	public void init() throws Exception {
		context = contextFactory.enterContext();
		globalScope = context.initStandardObjects();
		
		loadScript(TEST_ENVIRONMENT);
		loadScript(TEST_SETUP);
		loadScript(FILE_UNDER_TEST);
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

	public void loadScript(String name) throws IOException {
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
		assertTrue(contents.length() > 0);
		eval(contents.toString());
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
    public void shouldParseCheckCommand() {
        eval("var cmd = getCommandForSource('| check | do | command | on | blah | output |');");
        assertEquals("command", eval("cmd.command"));
        assertEquals("blah", eval("cmd.target"));
        assertEquals("output", eval("cmd.value"));
    }

    @Test
    public void shouldParseBareCheckCommand() {
        eval("var cmd = getCommandForSource('| check | do | command | output |');");
        assertEquals("command", eval("cmd.command"));
        assertEquals("output", eval("cmd.target"));
        assertEquals("", eval("cmd.value"));
    }

    @Test
    public void shouldParseActionCommand() {
        eval("var cmd = getCommandForSource('| do | command | on | blah | with | output |');");
        assertEquals("command", eval("cmd.command"));
        assertEquals("blah", eval("cmd.target"));
        assertEquals("output", eval("cmd.value"));
    }

    @Test
    public void shouldParseBareActionCommand() {
        eval("var cmd = getCommandForSource('| do | command | on | output |');");
        assertEquals("command", eval("cmd.command"));
        assertEquals("output", eval("cmd.target"));
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
    public void testExecuteCommandOnComplexGlobPatternToSelenese() {
        eval("var cmd = getCommandForSource('| ensure | do | waitForText | on | css=td.CVFormatCell > div | with | glob:Aantal resultaten in het overzicht is: *361* |');");
        assertEquals("waitForText", eval("cmd.command"));
        assertEquals("css=td.CVFormatCell > div", eval("cmd.target"));
        assertEquals("glob:Aantal resultaten in het overzicht is: *361*", eval("cmd.value"));
    }

    @Test
    public void testExecuteCommandOnComplexXPathPatternToSelenese() {
        eval("var cmd = getCommandForSource('| ensure | do | type | on | !-//*[@class=\"PromptViewCell\"]//input[contains (@id,\\'_8_2_D\\')]-! | with | 01-03-2011 |');");
        assertEquals("type", eval("cmd.command"));
        assertEquals("//*[@class=\"PromptViewCell\"]//input[contains (@id,'_8_2_D')]", eval("cmd.target"));
        assertEquals("01-03-2011", eval("cmd.value"));
    }

    @Test
    public void testExecuteCommandOnVariableWithEmbeddedVariableToSelenese() {
        eval("var cmd = getCommandForSource('| ensure | do | waitForText | on | link=$myVariable | with | Text and $myVariable |');");
        assertEquals("waitForText", eval("cmd.command"));
        assertEquals("link=${myVariable}", eval("cmd.target"));
        assertEquals("Text and ${myVariable}", eval("cmd.value"));
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
				"| do | open | on | !-http://myurl.com-! |\n" +
                "| do | open | on | foo | with | bar |\n" +
				"| stop browser |\n"
				, result);
    }

    @Test
    public void shouldStoreCommandOnTargetToFitnesse() {
		eval("var tc = new TestCase(); tc.baseUrl = 'http://example.com';");
		eval("var commands = [];");
        eval("commands.push(new Command('storeLocation', 'locVar'))");
        eval("commands.push(new Command('storeTest', 'foo', 'bar'))");
        eval("commands.push(new Command('verifyText', 'foo', '${locVar}'))");
		eval("tc.commands = commands;");
		String result = (String) eval("format(tc, 'name');");
		assertEquals(
                "| script | selenium driver fixture |\n" +
				"| start browser | firefox | on url | http://example.com |\n" +
				"| $locVar= | is | storeLocation |\n" +
                "| $bar= | is | storeTest | on | foo |\n" +
                "| check | is | verifyText | on | foo | $locVar |\n" +
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
                "| do | someUrl | on | !-http://example.com-! |\n" +
                "| do | testWikiWord | on | !-WikiWord-! | with | !-W10W-! |\n" +
                "| do | testVariable | on | foo | with | $locVar |\n" +
                "| do | testEmail | on | !-anonymous@example.com-! |\n" +
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
                "| do | someUrl | on | !-http://example.com-! |\n" +
                "| do | testWikiWord | on | !-WikiWord-! | with | !-W10W-! |\n" +
                "| do | testVariable | on | foo | with | $locVar |\n" +
                "| do | testEmail | on | !-anonymous@example.com-! |\n" +
				"| stop browser |\n"
				, result);
    }
 
    @Test
    public void shouldParseRightActionToFitnesse() {
		eval("var tc = new TestCase(); tc.baseUrl = 'http://example.com';");
		eval("var commands = [];");
        eval("commands.push(new Command('open', 'http://example.com'))");
        eval("commands.push(new Command('verifyText', 'css=h1', 'Header'))");
        eval("commands.push(new Command('waitForTextNotPresent', '//c[blah=*]/d/e'))");
        eval("commands.push(new Command('focus', 'input'))");
		eval("tc.commands = commands;");
		String result = (String) eval("format(tc, 'name');");
		assertEquals(
                "| script | selenium driver fixture |\n" +
				"| start browser | firefox | on url | http://example.com |\n" +
                "| do | open | on | !-http://example.com-! |\n" +
                "| check | is | verifyText | on | css=h1 | Header |\n" +
                "| ensure | do | waitForTextNotPresent | on | //c[blah=*]/d/e |\n" +
                "| ensure | do | focus | on | input |\n" +
				"| stop browser |\n"
				, result);
    }
    

    @Test
    public void shoudlParseVariablesInText() {
		eval("var tc = new TestCase(); tc.baseUrl = 'http://example.com';");
		eval("var commands = [];");
        eval("commands.push(new Command('verifyText', 'link=${myVariable}', 'Text${myVariable} Text'))");
		eval("tc.commands = commands;");
		String result = (String) eval("format(tc, 'name');");
		assertEquals(
                "| script | selenium driver fixture |\n" +
				"| start browser | firefox | on url | http://example.com |\n" +
                "| check | is | verifyText | on | link=$myVariable | Text$myVariable Text |\n" +
				"| stop browser |\n"
				, result);
    }

    @Test
    public void shoudlEscapeBars() {
		eval("var tc = new TestCase(); tc.baseUrl = 'http://example.com';");
		eval("var commands = [];");
        eval("commands.push(new Command('verifyText', 'Some | text', 'more | text'))");
		eval("tc.commands = commands;");
		String result = (String) eval("format(tc, 'name');");
		assertEquals(
                "| script | selenium driver fixture |\n" +
				"| start browser | firefox | on url | http://example.com |\n" +
                "| check | is | verifyText | on | !-Some | text-! | !-more | text-! |\n" +
				"| stop browser |\n"
				, result);
    }
 
    @Test
    public void shoudlConvertExact() {
		eval("var tc = new TestCase(); tc.baseUrl = 'http://example.com';");
		eval("var commands = [];");
        eval("commands.push(new Command('verifyText', 'field', 'exact:Out*'))");
		eval("tc.commands = commands;");
		String result = (String) eval("format(tc, 'name');");
		assertEquals(
                "| script | selenium driver fixture |\n" +
				"| start browser | firefox | on url | http://example.com |\n" +
                "| check | is | verifyText | on | field | Out* |\n" +
				"| stop browser |\n"
				, result);
    }

    @Test
    public void shoudlConvertGlob() {
		eval("var tc = new TestCase(); tc.baseUrl = 'http://example.com';");
		eval("var commands = [];");
        eval("commands.push(new Command('verifyText', 'field', 'glob:Test'))");
        eval("commands.push(new Command('verifyText', 'field', 'glob:Test*Te?[]{}().*+^$st'))");
        eval("commands.push(new Command('verifyText', 'field', 'Test'))");
        eval("commands.push(new Command('verifyText', 'field', 'Test*Test*Test'))");
        eval("commands.push(new Command('verifyText', 'field', 'X[ea]b*ium'))");
        
		eval("tc.commands = commands;");
		String result = (String) eval("format(tc, 'name');");
		assertEquals(
                "| script | selenium driver fixture |\n" +
				"| start browser | firefox | on url | http://example.com |\n" +
                "| check | is | verifyText | on | field | Test |\n" +
                "| check | is | verifyText | on | field | =~/Test.*Te\\?\\[\\]\\{\\}\\(\\)\\..*\\+\\^\\$st/ |\n" +
                "| check | is | verifyText | on | field | Test |\n" +
                "| check | is | verifyText | on | field | =~/Test.*Test.*Test/ |\n" +
                "| check | is | verifyText | on | field | =~/X\\[ea\\]b.*ium/ |\n" +
				"| stop browser |\n"
				, result);
    }
 
    @Test
    public void shouldAlwaysMakeEnsureCommandsForWaitForCommands() {
		eval("var tc = new TestCase(); tc.baseUrl = 'http://example.com';");
		eval("var commands = [];");
        eval("commands.push(new Command('verifyText', 'field', 'Test'));");
        eval("commands.push(new Command('waitForText', 'field', 'Test'));");
        
		eval("tc.commands = commands;");
		String result = (String) eval("format(tc, 'name');");
		assertEquals(
                "| script | selenium driver fixture |\n" +
				"| start browser | firefox | on url | http://example.com |\n" +
                "| check | is | verifyText | on | field | Test |\n" +
                "| ensure | do | waitForText | on | field | with | Test |\n" +
				"| stop browser |\n"
				, result);
    }
    
    
    @Test
    public void shoudlConvertRegexp() {
		eval("var tc = new TestCase(); tc.baseUrl = 'http://example.com';");
		eval("var commands = [];");
        eval("commands.push(new Command('verifyText', 'field', 'regexp:.*A.*'))");
        eval("commands.push(new Command('verifyText', 'field', 'regexpi:.*A.*'))");
        eval("commands.push(new Command('verifyText', 'field', 'regexp:.*AsdFgh.*'))");
		eval("tc.commands = commands;");
		String result = (String) eval("format(tc, 'name');");
		assertEquals(
                "| script | selenium driver fixture |\n" +
				"| start browser | firefox | on url | http://example.com |\n" +
                "| check | is | verifyText | on | field | =~/.*A.*/ |\n" +
                "| check | is | verifyText | on | field | =~/.*A.*/ |\n" +
                "| check | is | verifyText | on | field | =~/.*AsdFgh.*/ |\n" +
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
    public void shouldParseExactToSeleneseWheneverPossible() {
		// Only open and check command should be parsed
		eval("var fittable = '| script | selenium driver fixture |\\n' +" +
				"'| start browser | firefox | on url | http://example.com |\\n' +" +
				"'| check | is | verifyText | on | locator | Foo*Bar |\\n' +" +
				"'| check | is | verifyText | on | locator | Foo*Bar* |\\n' +" +
                "'| stop browser |\\n';");
		eval("var tc = new TestCase();");
		eval("parse(tc, fittable);");
		eval("var commands = tc.commands;");

		assertEquals(2.0, eval("commands.length"));
		assertEquals("verifyText", eval("commands[0].command"));
        assertEquals("locator", eval("commands[0].target"));
        assertEquals("exact:Foo*Bar", eval("commands[0].value"));
        assertEquals("exact:Foo*Bar*", eval("commands[1].value"));
    }

    @Test
    public void shouldParseGlobToSeleneseWheneverPossible() {
		// Double escaped because of double eval
		eval("var fittable = '| script | selenium driver fixture |\\n' +" +
				"'| start browser | firefox | on url | http://example.com |\\n' +" +
				"'| check | is | verifyText | on | locator | =~/Foo.*Bar/ |\\n' +" +
				"'| check | is | verifyText | on | locator | =~/Foo.*Bar.*/ |\\n' +" +
				"'| check | is | verifyText | on | locator | =~/Foo.*Bar.*Baz/ |\\n' +" +
				"'| check | is | verifyText | on | locator | =~/Foo\\\\\\\\.*\\\\.\\\\?\\\\[\\\\]\\\\(\\\\)\\\\{\\\\}.*Baz/ |\\n' +" +
                "'| stop browser |\\n';");
		eval("var tc = new TestCase();");
		eval("parse(tc, fittable);");
		eval("var commands = tc.commands;");

		assertEquals(4.0, eval("commands.length"));
		assertEquals("verifyText", eval("commands[0].command"));
        assertEquals("locator", eval("commands[0].target"));
        assertEquals("Foo*Bar", eval("commands[0].value"));
        assertEquals("Foo*Bar*", eval("commands[1].value"));
        assertEquals("Foo*Bar*Baz", eval("commands[2].value"));
        assertEquals("Foo\\*.?[](){}*Baz", eval("commands[3].value"));
    }

    @Test
    public void shouldParseRegexpToSelenese() {
		// Only open and check command should be parsed
		eval("var fittable = '| script | selenium driver fixture |\\n' +" +
				"'| start browser | firefox | on url | http://example.com |\\n' +" +
				"'| check | is | verifyText | on | locator | =~/Blah.*T?/ |\\n' +" +
                "'| stop browser |\\n';");
		eval("var tc = new TestCase();");
		eval("parse(tc, fittable);");
		eval("var commands = tc.commands;");

		assertEquals(1.0, eval("commands.length"));
		assertEquals("verifyText", eval("commands[0].command"));
        assertEquals("locator", eval("commands[0].target"));
        assertEquals("regexp:Blah.*T?", eval("commands[0].value"));
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

    @Test
    public void shouldParseFailedCheckCommands() {
    	eval("var fittable = 'ensure\tdo\twaitForElementPresent\t[false] expected [link=Xebium]\\n' +" +
    	"'check\tis\tverifyText\ton\tlink=Xebium\t[Execution of command failed: Element link=Xebium not found] expected [Xebium]\\n' +" +
    	"'check\tis\twaitForTitle\t[Google] expected [xebia/Xebium á GitHub[?]]\\n' +" +
    	"'check\tis\tverifyText\ton\tcss=h1\t/.*Page[a-z]?/ found in: FrontPage\\n' +" +
    	"'check\tis\tverifyText\ton\tcss=h1\t/.*Page[a-z]?/ not found in: FrontPage';");

		eval("var tc = new TestCase();");
		eval("parse(tc, fittable);");
		eval("var commands = tc.commands;");

		assertEquals(5.0, eval("commands.length"));

		assertEquals(eval("commands[0].comment").toString(), "waitForElementPresent", eval("commands[0].command"));
        assertEquals("link=Xebium", eval("commands[0].target"));
        assertEquals("", eval("commands[0].value"));

		assertEquals(eval("commands[1].comment").toString(), "verifyText", eval("commands[1].command"));
        assertEquals("link=Xebium", eval("commands[1].target"));
        assertEquals("Xebium", eval("commands[1].value"));

		assertEquals(eval("commands[2].comment").toString(), "waitForTitle", eval("commands[2].command"));
        assertEquals("xebia/Xebium á GitHub[?]", eval("commands[2].target"));
        assertEquals("", eval("commands[2].value"));

		assertEquals(eval("commands[3].comment").toString(), "verifyText", eval("commands[3].command"));
        assertEquals("css=h1", eval("commands[3].target"));
        assertEquals("regexp:.*Page[a-z]?", eval("commands[3].value"));

		assertEquals(eval("commands[4].comment").toString(), "verifyText", eval("commands[4].command"));
        assertEquals("css=h1", eval("commands[4].target"));
        assertEquals("regexp:.*Page[a-z]?", eval("commands[4].value"));

    }
}
