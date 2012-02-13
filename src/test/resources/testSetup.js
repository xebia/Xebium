/**
 *  Mock part of the apiDocumentation (see iedocs-core.xml)
 *  
 *  This is done by mocking a part of the DOM structure.
 *  Just enough to make it work.
 */
Command.apiDocument = {
		
	documentElement: {
		getElementsByTagName: function(tag) {
			
			function f(name) {
				return {
					attributes: {
						getNamedItem: function(attr) {
							if (attr === 'name') {
								return { value: name };
							}
							return null;
						}
					},
					getElementsByTagName: function() {
						return [];
					}
				}
			}
			
			function nodeList() {
				var nodes = arguments;
				
				this.item = function(i) {
					return nodes[i];
				}
				this.length = arguments.length;
			}
			
			if (tag === 'function') {
				return new nodeList(
						f('getText'), 
						f('isTextPresent'),
						f('open'),
						f('focus'),
						f('storeLocation'),
						f('storeTest'),
						f('verifyTest'),
						f('testWikiWord'),
						f('testVariable'),
						f('testEmail'),
						f('someUrl'),
						f('waitForText')
				);
			}
		}
	}
}
