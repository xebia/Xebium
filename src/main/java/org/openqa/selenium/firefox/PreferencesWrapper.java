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

package org.openqa.selenium.firefox;

import java.io.Reader;


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
    public PreferencesWrapper(Reader reader) {
		super(reader);
    }
}
