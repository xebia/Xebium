/**
* Format TestCase and return the source.
*
* @param testCase TestCase to format
* @param name The name of the test case, if any. It may be used to embed title into the source.
*/
function format(testCase, name) {
     var baseUrl = testCase.baseUrl || options.baseUrl;
    
     var commandsText = '| script | selenium driver fixture |\n';
     commandsText += '| start browser | ' + options.browser + ' | on url | ' + baseUrl + ' |\n';
     commandsText += formatCommands(testCase.commands);
     commandsText += '| stop browser |\n';
 
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
    var text = null;

    if (commandObj.type == 'comment') {
    	return "| note | " + commandObj.comment + " |";
    } else if (commandObj.type == 'command') {
         // Set up variables to use for substitution
         var command = commandObj.command;
         var target = commandObj.target;
         var value = commandObj.value;
         
         if (/^store/.test(command)) {
         	if (value === '') {
             	return "| $" + target + "= | is | " + command.replace(/^store/, "get") + " |";
         	} else {
             	return "| $" + value + "= | is | " + command.replace(/^store/, "get") + " | on | " + target + " |";
         	}
     	} else if (value === '') {
             return "| ensure | do | " + command + " | on | " + target + " |";
        } else {
         	if (/[A-Z].*[a=z][A-Z]/.test(value)) { value = "!-" + value + "-!"; }
            return "| ensure | do | " + command + " | on | " + target + " | with | " + value + " |";
        }
    }
    return "| note | Untranslatable: '" + commandObj.toString + "' |";
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
        // TODO: extract BaseUrl
    log.info('Iterating source for line "' + i + '"');
        var command = getCommandForSource(lines[i]);
        if (command) commands.push(command);    
    }
    
     testCase.commands = commands;
}

function getCommandForSource(line) {
	// | ensure | do | ${command} | on | ${target} | with | ${value} |
	if (match = /^\|\s*ensure\s*\|\s*do\s*\|\s*([^\|\s]+)\s*\|\s*on\s*\|\s*([^\|\s]+)\s*\|\s*with\s*\|\s*([^\|\s]+)\s*\|\s*/.exec(line)) {
		return new Command(match[1], match[2], match[3]);

	// | ensure | do | ${command} | on | ${target} |
	} else if (match = /^\|\s*ensure\s*\|\s*do\s*\|\s*([^\|\s]+)\s*\|\s*on\s*\|\s*([^\|\s]+)\s*\|\s*/.exec(line)) {
		return new Command(match[1], match[2]);

	// format: | $value= | is | ${command} | on | ${target} |
	} else if (match = /^\|\s*\$([^\|\s]+)=\s*\|\s*is\s*\|\s*([^\|\s]+)\s*\|\s*on\s*\|\s*([^\|\s]+)\s*\|\s*/.exec(line)) {
		return new Command(match[2].replace(/^get/, 'store'), match[3], match[1]);

	// format: | $value= | is | ${command} |
	} else if (match = /^\|\s*\$([^\|\s]+)=\s*\|\s*is\s*\|\s*([^\|\s]+)\s*\|\s*/.exec(line)) {
		return new Command(match[2].replace(/^get/, 'store'), match[1]);

	// format: | note | ${text} |
	} else if (match = /^\|\s*note\s*\|\s*(.+?)\s*\|\s*/.exec(line)) {
		return new Comment(match[1]);
	}
}

this.options = {
    'browser': 'firefox',
    'baseUrl': 'http://localhost'
}

this.configForm =
        '<description>Default browser</description>' +
        '<textbox id="options_browser" />' +
        '<description>Default base URL</description>' +
        '<textbox id="options_baseUrl" />';
        
