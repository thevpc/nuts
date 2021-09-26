/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.bundles.string;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsUtilStrings;

import java.util.regex.Pattern;

/**
 *
 * @author vpc
 */
public class GlobUtils {

    private static final Pattern PATTERN_ALL = Pattern.compile(".*");

    public static Pattern ofExact(String pattern) {
        if (NutsBlankable.isBlank(pattern)) {
            return PATTERN_ALL;
        }
        return Pattern.compile(simpexpToRegexp(pattern, false));
    }
    public static Pattern ofContains(String pattern) {
        if (NutsBlankable.isBlank(pattern)) {
            return PATTERN_ALL;
        }
        return Pattern.compile(simpexpToRegexp(pattern, true));
    }

//    public static String simpexpToRegexp(String pattern) {
//        return simpexpToRegexp(pattern, false);
//    }

    /**
     * *
     * **
     *
     * @param pattern pattern
     * @param contains contains
     * @return regexp
     */
    private static String simpexpToRegexp(String pattern, boolean contains) {
        if (pattern == null) {
            pattern = "*";
        }
        int i = 0;
        char[] cc = pattern.toCharArray();
        StringBuilder sb = new StringBuilder();
        while (i < cc.length) {
            char c = cc[i];
            switch (c) {
                case '.':
                case '!':
                case '$':
                case '[':
                case ']':
                case '{':
                case '}':
                case '(':
                case ')':
                case '?':
                case '^':
                case '|':
                case '+':
                case '\\': {
                    sb.append('\\').append(c);
                    break;
                }
                case '*': {
//                    if (i + 1 < cc.length && cc[i + 1] == '*') {
//                        i++;
//                        sb.append("[a-zA-Z_0-9_$.-]*");
//                    } else {
//                        sb.append("[a-zA-Z_0-9_$-]*");
//                    }
                    sb.append(".*");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
            i++;
        }
        if (!contains) {
            sb.insert(0, '^');
            sb.append('$');
        }
        return sb.toString();
    }
}
