/**
* Format TestCase and return the source.
*
* @param testCase TestCase to format
* @param name The name of the test case, if any. It may be used to embed title into the source.
*/
function format(testCase, name) {
    var baseUrl = testCase.baseUrl || options.baseUrl;
    
	var commandsText = '| script |\n';
	commandsText += '| start | selenium driver |\n';
	commandsText += '| start browser | firefox | with selenium rc | ' + options.host + ' | on port | ' + options.port + ' | point at | !-' + baseUrl + '-! |\n';
    commandsText += formatCommands(testCase.commands);
 	commandsText += '| shutdown browser |\n';
 
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
        var command = commands[i];
        if (command.value === '') {
            commandsText += "| ensure | do | " + command.command + " | on | " + command.target + " |\n";
        } else {
            commandsText += "| ensure | do | " + command.command + " | on | " + command.target + " | with | " + command.value + " |\n";
        }
 	}
 	return  commandsText;
}

function getSourceForCommand(commandObj) {
	var text = null;

 	if (commandObj.type == 'command') {
 		// Set up variables to use for substitution
     	var command = commandObj.command;
     	var target = commandObj.target;
     	var value = commandObj.value;
     	
     	var template = getCommandTemplate(commandObj);
     	text = template.replace(/\$\{([a-zA-Z0-9_\.]+)\}/g,
			function(str, p1, offset, s) {
            	result = eval(p1);
            	return result != null ? result : '';
        	});
 	}

	return text;
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
	line = trim(line);
	if (!line || line === "") return;

	log.debug('Getting source for "' + line + '"');

    var re = new RegExp("^\\|[^\\|]+\\|[^\\|]+\\|([^\\|]+)\\|[^\\|]+\\|([^\\|]+)\\|[^\\|]+\\|([^\\|]+)\\|$");
    var result = re.exec(line);
    if (result) {
        return new Command(trim(result[1]), trim(result[2]), trim(result[3]));
    }
}

function trim(text) {
	return ((text && text.toString()) || "").replace( /^\s+|\s+$/g, "" );
}

this.options = {
	'port': '4444',
	'host': 'localhost',
	'baseUrl': 'http://localhost'
}

this.configForm =
        '<description>Selenium RC default host</description>' +
        '<textbox id="options_host" />' +
        '<description>Selenium RC default port</description>' +
        '<textbox id="options_port" />' +
        '<description>Selenium RC default base URL</description>' +
        '<textbox id="options_baseUrl" />';
        