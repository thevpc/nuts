/**
 * ====================================================================
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import java.util.regex.Pattern;

/**
 * Central redaction of credentials before URLs/requests are logged or embedded
 * in exception messages. Internal to the runtime: no public API change.
 */
public final class NSecretRedactor {

    private static final String MASK = "***";

    private static final Pattern USERINFO = Pattern.compile(
            "([A-Za-z][A-Za-z0-9+.-]*://)([^/@\\s:]+):([^/@\\s]+)@");
    private static final Pattern QUERY_SECRET = Pattern.compile(
            "(?i)([?&])(ul|up|password|passwd|pwd|secret|token|api[_-]?key)=([^&\\s#]*)");
    private static final Pattern AUTH_HEADER = Pattern.compile(
            "(?i)(Authorization\\s*[:=]\\s*)(Basic\\s+|Bearer\\s+)?[^\\r\\n,;]+");

    private NSecretRedactor() {
    }

    /**
     * Replace every known credential carrier in the given text with a mask.
     */
    public static String redact(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String result = USERINFO.matcher(text).replaceAll("$1" + MASK + ":" + MASK + "@");
        result = QUERY_SECRET.matcher(result).replaceAll("$1$2=" + MASK);
        return AUTH_HEADER.matcher(result).replaceAll("$1" + MASK);
    }
}
