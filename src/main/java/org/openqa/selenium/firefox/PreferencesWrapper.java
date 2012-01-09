package org.openqa.selenium.firefox;

import java.io.File;


/**
 * Wrapper class for Preferences.
 * 
 * This class serves as a workaround for generating Preferences objects outside Selenium's own packages, as the
 * Preferences class itself is set to 'package-private'.
 * 
 */
public class PreferencesWrapper extends Preferences {

    /**
     * @param file
     *        a file containing Firefox preferences. The file is expected to contain .js style firefox preferences.
     */
    public PreferencesWrapper(File file) {
        super(file);
    }
}
