/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.NutsWorkspaceOptions;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author thevpc
 * @app.category Internal
 */
public class PrivateNutsLog {

    /**
     * Universal Data and time format "yyyy-MM-dd HH:mm:ss.SSS"
     */
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());
    private static final Pattern LOG_PARAM_PATTERN = Pattern.compile("\\{(?<v>[0-9]+)}");
    private NutsWorkspaceOptions options;

    public void log(Level lvl, NutsLogVerb logVerb, String message) {
        log(lvl, logVerb, message, new Object[0]);
    }

    public void log(Level lvl, NutsLogVerb logVerb, String message, Object object) {
        log(lvl, logVerb, message, new Object[]{object});
    }

    public void log(Level lvl, NutsLogVerb logVerb, String message, Object[] objects) {
        if (isLoggable(lvl)) {
            Matcher m = LOG_PARAM_PATTERN.matcher(message);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String v = m.group("v");
                m.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(objects[Integer.parseInt(v)])));
            }
            m.appendTail(sb);
            doLog(lvl, logVerb, sb.toString());
        }
    }

    public void log(Level lvl, String message, Throwable err) {
        if (isLoggable(lvl)) {
            doLog(lvl, NutsLogVerb.FAIL, message);
            err.printStackTrace(System.err);
        }
    }

    private void doLog(Level lvl, NutsLogVerb logVerb, String s) {
        PrivateNutsTerm.errln("%s %-7s %-7s : %s", DEFAULT_DATE_TIME_FORMATTER.format(Instant.now()), lvl, logVerb, s);
    }

    public boolean isLoggable(Level lvl) {
        if (options == null) {
            return false;
        }
        return options.getLogConfig() != null && lvl.intValue() >= options.getLogConfig().getLogTermLevel().intValue();
    }

    public void setOptions(NutsWorkspaceOptions options) {
        this.options = options;
    }
}
