package com.xebia.incubator.xebium;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Create a GUI Map containing friendly names for locators (e.g. xpaths and ids)
 * <p><code>
 * | Table:Gui Map                |<br>
 * | friendly name | //some/xpath |
 * </code></p>
 */
public class GuiMap {
	private static HashMap<String,String> locators = new HashMap<String,String>();
	
	/**
	 * Get a locator from the GUI Map
	 *
	 * @param key a friendly name for a locator
	 * @return a locator (e.g. an xpath or an id)
	 */
	public static String getLocator(final String key) {
		String value = locators.get(key);
		return (value != null) ? value : key;
	}
	
	/**
	 * Add the friendly names and corresponding locators to the GUI Map
	 *
	 * <p><code>
	 * | Table:Gui Map |
	 * </code></p>
	 *
	 */
	public static List doTable(List<List<String>> table) {
		for(Iterator<List<String>> tableIterator = table.iterator(); tableIterator.hasNext(); ) {
			List<String> row = tableIterator.next();
			if (row.size() >= 2) {
				// the friendly name must be in the first column
				String key = new String(row.get(0));				
				// the locator must be in the second column
				String value = new String(row.get(1));
				
				// add the row to the GUI Map
				locators.put(key,value);				
				
				// make sure that the returned table doesn't result in errors in the testcase
				row.set(0,"pass");
				row.set(1, "pass");
				
				// anything from the third column is considered a comment
				if (row.size() > 2) {
					for(int i = 2; i < row.size(); i++) {
						row.set(i, "ignore");
					}
				}
			}
			
			// this row only has one column, must be a comment
			else if (row.size() == 1) {
				row.set(0, "ignore");
			}
		}
		return table;
	}
	
}