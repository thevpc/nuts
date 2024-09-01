/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.xtra.glob;

import net.thevpc.nuts.util.NBlankable;

import java.util.regex.Pattern;

/**
 * @author thevpc
 */
public class GlobUtils {

    public static final Pattern PATTERN_ALL = Pattern.compile(".*");

    public static Pattern ofExact(String pattern) {
        if (NBlankable.isBlank(pattern)) {
            return PATTERN_ALL;
        }
        return Pattern.compile(simpexpToRegexp(pattern, false));
    }

    public static Pattern ofContains(String pattern) {
        if (NBlankable.isBlank(pattern)) {
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
     * @param pattern  pattern
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


    public static Pattern glob(String o, String separators) {
        return Pattern.compile(globString(o, separators));
    }

    public static String globString(String o, String separators) {
        if (separators == null || separators.length() == 0) {
            separators = "/";
        }
        for (char s : separators.toCharArray()) {
            switch (s) {
                case '{':
                case '}':
                case '(':
                case ')':
                case '<':
                case '>':
                case '*':
                case '?': {
                    throw new IllegalArgumentException("unsupported glob separator " + s);
                }
            }
        }
        char s = separators.charAt(0);
        while (true) {
            if (o.endsWith(s + "**" + s + "*")) {
                o = o.substring(0, o.length() - 5);
            } else if (o.endsWith(s + "**")) {
                o = o.substring(0, o.length() - 3);
            } else if (o.endsWith(s + "*")) {
                o = o.substring(0, o.length() - 2);
            } else {
                break;
            }
        }
        if (o.isEmpty()) {
            return (".*");
        }
        StringBuilder sb = new StringBuilder();
        char[] chars = o.toCharArray();
        String escapedSeparators = escapeSeparators(separators);
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '.':
                case '{':
                case '}':
                case '<':
                case '>': {
                    sb.append('\\');
                    sb.append(c);
                    break;
                }
                case '*': {
                    if (i + 1 < chars.length && chars[i + 1] == '*') {
                        if (i + 2 < chars.length && chars[i + 2] == s) {
                            i++;
                            if (i + 3 < chars.length) {
                                sb.append(".*[" + escapedSeparators + "]");
                            } else {
                                sb.append(".*");
                            }
                        } else {
                            i++;
                            sb.append(".*");
                        }
                    } else {
                        sb.append("[^" + escapedSeparators + "]*");
                    }
                    break;
                }
                case '?': {
                    sb.append("[^").append(escapedSeparators).append("]?");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return (sb.toString());
    }

    private static String escapeSeparators(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\': {
                    sb.append('\\');
                    sb.append(c);
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
