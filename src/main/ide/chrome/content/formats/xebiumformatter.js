/*
 * Copyright 2010-2012 Xebia b.v.
 * Copyright 2010-2012 Xebium contributers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/**
* Format TestCase and return the source.
*
* @param testCase TestCase to format
* @param name The name of the test case, if any. It may be used to embed title into the source.
*/
function format(testCase, name) {
     var baseUrl = testCase.getBaseURL();
    
     var commandsText = '| script' + ('true' == options.libraryMode ? '' : ' | selenium driver fixture') + ' |\n';
     if ('true' == options.noStartStop) {
         commandsText += formatCommands(testCase.commands);
     } else {
    	 if (testCase.startBrowserLine) {
    		 commandsText += testCase.startBrowserLine + '\n';
    	 } else {
    		 commandsText += '| start browser | ' + options.browser + ' | on url | ' + baseUrl + ' |\n';
    	 }
		 commandsText += formatCommands(testCase.commands);
	     commandsText += '| stop browser |\n';
     }
     
     return commandsText;
}

/**
 * Format an array of commands to the snippet of source.
 * Used to copy the source into the clipboard.
 *
 * @param The array of commands to sort.
 */
function formatCommands(commands) {
    var commandsText = '';
    
    for (i = 0; i < commands.length; i++) {
        commandsText += getSourceForCommand(commands[i]) + "\n";
    }
    return  commandsText;
}


function getSourceForCommand(commandObj) {
	// For some commands, Xebium performs an extra "isElementPresent" check. See the LocatorCheck.java.
	var locatorCheck = {
			"addSelection": true, // loc
			"assignId": true, // loc
			"click": true, // loc
			"check": true, // loc
			"doubleClick": true, // loc
			"dragdrop": true, // loc
			"dragAndDrop": true, // loc
			"dragAndDropToObject": true, // loc, loc
			"focus": true, // loc
			"highlight": true, // loc
			"removeSelection": true, // loc
			"select": true, // loc
			"submit": true, // loc
			"type": true, // loc
			"typeKeys": true, // loc
			"uncheck": true // loc
		};

	function escape(s, oper) {
		s = s.replace(/\$\{(\w+)\}/g, "$$$1");
		if (oper === "check") {
			var match;
			if (match = /^regexpi?:(.*)$/.exec(s)) {
				// Cheating: regexpi is transformed to normal regexp.
				return "=~/" + match[1] + "/";
			} else if (match = /^exact:(.*)$/.exec(s)) {
				s = match[1];
			} else if (match = /^(?:glob:)?(.*)$/.exec(s)) {
				if (/\*/.test(match[1])) {
					return "=~/" + match[1].replace(/([\\.+?|\[\](){}^$])/g, "\\$1").replace(/\*/g, '.*') + "/";
				} else {
					s =  match[1];
				}
			}
		}
		if (/^https?:\/\//.test(s) || /^[A-Z][a-z0-9]+[A-Z]/.test(s) || /[@\|]/.test(s)) {
			return "!-" + s + "-!";
		}
		return s;
	}
	var text = null;

    if (commandObj.type == 'comment') {
    	return "| note | " + commandObj.comment + " |";
    } else if (commandObj.type == 'command') {
        // Set up variables to use for substitution
        var command = commandObj.command;
        var target = commandObj.target;
        var value = commandObj.value;
        var def = commandObj.getDefinition();
        //log.debug("command: " + command + " => " + (def ? def.name : "<NONE>") + " / " + def.isAccessor);
        
        if (/^store/.test(command)) {
         	if (value === '') {
             	return "| $" + target + "= | is | " + command + " |";
         	} else {
             	return "| $" + value + "= | is | " + command + " | on | " + escape(target) + " |";
         	}
     	}

        if (def && def.isAccessor && !/^waitFor/.test(command)) {
        	if (/^is/.test(def.name)) {
        		return "| ensure | do | " + command + " | on | " + escape(target) + " |";
        	} else if (value) {
         		return "| check | is | " + command + " | on | " + escape(target) + " | " + escape(value, "check") + " |";
    		} else {
         		return "| check | is | " + command + " | " + escape(target, "check") + " |";
    		}
        } else {
     		return ((locatorCheck[def.name] || /^waitFor/.test(command)) ? "| ensure " : "") + "| do | " + command + " | on | " + escape(target) + (value === '' ? "" : " | with | " + escape(value)) + " |";
        }
    }
    return "| note | !-Untranslatable: '" + commandObj.toString + "'-! |";
}

/**
 * Parse source and update TestCase. Throw an exception if any error occurs.
 *
 * @param testCase TestCase to update
 * @param source The source to parse
 */
function parse(testCase, source) {
     var commands = [];

    var lines = source.split(/\n/g);

    for (i = 0; i < lines.length; i++) {
        // Catch some special cases (?)
        // TODO: extract BaseUrl -> testCase.setBaseURL(url)
        log.debug('Parsing line ' + i + ': "' + lines[i] + '"');
        var command;
        
        try {
        	command = getCommandForSource(lines[i]);
        } catch (err) {
        	if (err == 'unparsable') {
	    		if (/\t/.test(lines[i])) {
	    			// Line may be pasted directly from FitNesse page output (<-, -> for variable substitutions)
	    			var line = '| ' + lines[i].replace(/<-\[.*\]\s*\t/, '=\t').replace(/->\[.*?\]/g, '').replace(/\t/g, ' | ') + ' |';
	    			try {
	    				command = getCommandForSource(line);
	    			} catch (err) {
	    	        	if (err == 'unparsable') {
	    	        		command = new Comment("Can't parse line: '" + line + "'");
	    	        	} else {
	    	        		log.error(err);
	    	        	}
	    			}
	    		} else {
	    			command = new Comment("Can't parse line: '" + lines[i] + "'");
	    		}
        	} else {
        		log.error(err);
        	}
        }
        
        if (command) {
            if (command.baseUrl) {
            	testCase.setBaseURL(command.baseUrl);
            	testCase.startBrowserLine = command.line;
            } else {
            	commands.push(command);
            }
        }
    }
    
     testCase.commands = commands;
}

function getCommandForSource(line) {
	function unescape(s, oper) {
		var m;
		// Deal with output messages from FitNesse
		if (m = /^\[.*\] expected \[(.*)\]$/.exec(s)) {
			s = m[1];
		} else if (m = /^(\/.*\/) (?:not )?found in: .*$/.exec(s)) {
			s = '=~' + m[1];
		}
		
		if (oper === "check") {
			if (m = /^=~\/(.*)\/$/.exec(s)) {
				s = m[1];
				if (/[^\\][*.+?|\[\](){}^$]/.test(s.replace(/\.\*/g, ''))) {
					// left out check for "[^\\]\\" (double backslash) since that will always succeed for escape
					s = 'regexp:' + s;
				} else {
					// No unescaped regexp special characters here, convert to glob pattern
					s = s.replace(/\.\*/g, '*').replace(/\\([\\.+?|\[\](){}^$])/g, "$1");
				}
			} else if (/\*/.test(s)) {
				s = 'exact:' + s;
			}
		}
		// Clear escape characters from text section
		if (m = /^!-(.+?)-!$/.exec(s)) { s = m[1]; }
		// Convert variable from $fit to ${selenese} style
		s = s.replace(/\$(\w+)/g, '${$1}');
		return s;
	}
	
	var match;
	
	// | check/ensure | is/do | ${command} |[ on | ]${target} |[ [with |] ${value} |]
	if (match = /^\|\s*(?:(ensure|check)\s*\|\s*|)(?:do|is)\s*\|\s*([^\|\s]+)\s*\|\s*(?:on\s*\|(?:\s*((?:!-.*?-!)?|[^\|]+?)\s*\|(?:\s*(?:with\s*\|\s*|)((?:!-.*?-!)|[^\|]+?)\s*\||)|)|((?:!-.*?-!)|[^\|]+?)\s*\|)/.exec(line)) {
		return new Command(match[2],
				match[3] ? unescape(match[3])
						: (match[5] ? unescape(match[5]) : undefined),
				match[4] ? unescape(match[4], match[1]) : undefined);

	// format: | $value= | is | ${command} | on | ${target} |
	} else if (match = /^\|\s*\$([^\|\s]+)=\s*\|\s*is\s*\|\s*([^\|\s]+)\s*\|\s*on\s*\|\s*((!-.*-!)|[^\|]+?)\s*\|/.exec(line)) {
		// Keep get->store mapping here for legacy
		return new Command(match[2].replace(/^get/, 'store'), unescape(match[3]), unescape(match[1]));

	// format: | $value= | is | ${command} |
	} else if (match = /^\|\s*\$([^\|\s]+)=\s*\|\s*is\s*\|\s*([^\|\s]+)\s*\|/.exec(line)) {
		// Keep get->store mapping here for legacy
		return new Command(match[2].replace(/^get/, 'store'), unescape(match[1]));

	// format: | note | ${text} |
	} else if (match = /^\|\s*note\s*\|\s*(.+?)\s*\|\s*$/.exec(line)) {
		return new Comment(match[1]);
		
	// format: | start browser | someBrowser | on url | http://example.com |
	} else if (match = /^\|\s*start\s+browser\s*\|\s*([^\|\s]+)\s*\|\s*on\s+url\s*\|\s*([^\|]+?)\s*\|/.exec(line)) {
		return {
			'line': line,
			'browser': match[1],
			'baseUrl': match[2]
		};
		
	// Ignore | script/scenario/start browser/stop browser |, log the rest
	} else if (!/^\s*$/.test(line)
			&& !/^\|\s*script\s*\|.*/i.test(line)
			&& !/^\|\s*scenario\s*\|.*/i.test(line)
			&& !/^\|\s*stop\s+browser\s*\|.*/.test(line)) {
		throw "unparsable";
	}
}

this.options = {
    'browser': 'firefox',
    'libraryMode': false,
    'noStartStop': false
}

this.configForm =
        '<description>Default browser</description>' +
        '<textbox id="options_browser" />' +
        '<checkbox id="options_libraryMode" label="Format as if the selenium driver fixture is defined as library"/>' + 
        '<checkbox id="options_noStartStop" label="Do not add start/stop browser lines"/>'; 

