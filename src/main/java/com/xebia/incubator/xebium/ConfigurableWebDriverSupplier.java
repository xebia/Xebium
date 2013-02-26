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
package com.xebia.incubator.xebium;

import java.io.File;

/**
 * <p>A ConfigurableWebDriverSupplier can be configured through the fixture
 * methods of the {@link SeleniumDriverFixture}.</p>
 */
public interface ConfigurableWebDriverSupplier {

    public void setBrowser(String browser);

    public void setCustomProfilePreferencesFile(File customProfilePreferencesFile);

    public void setProfileDirectory(File profileDirectory);
}
