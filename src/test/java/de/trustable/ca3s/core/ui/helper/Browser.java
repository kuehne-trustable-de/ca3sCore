/*
 * Copyright 2014-2016 Daniel Davison (http://github.com/ddavison) and Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.trustable.ca3s.core.ui.helper;

/**
 * An enumeration of Browsers that Selenium 2 uses.
 * @author ddavison
 *
 */
public enum Browser {
    NONE("none"),
    CHROME("chrome"),
    FIREFOX("firefox"),
    INTERNET_EXPLORER("ie"),
    EDGE("edge"),
    SAFARI("safari"),
    HTMLUNIT("htmlunit"),
    PHANTOMJS("phantomjs");

    String moniker;

    Browser(String moniker) {
        this.moniker = moniker;
    }
}
