/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.util;

import net.vpc.app.nuts.NutsIOException;
import net.vpc.app.nuts.NutsObjectConverter;
import net.vpc.common.strings.StringUtils;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreStringUtils {

    private static Pattern pattern = Pattern.compile("\\$\\{(?<key>[^}]*)}");
    public static final Pattern DOLLAR_PLACE_HOLDER_PATTERN = Pattern.compile("[$][{](?<name>([a-zA-Z]+))[}]");

    public static int parseInt(String v1, int defaultValue) {
        try {
            if (StringUtils.isEmpty(v1)) {
                return defaultValue;
            }
            return Integer.parseInt(StringUtils.trim(v1));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    public static int getStartingInt(String v1) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < v1.length(); i++) {
            char c = v1.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            return Integer.parseInt(sb.toString());
        }
        return -1;
    }

    public static boolean isInt(String v1) {
        try {
            if (v1.length() == 0) {
                return false;
            }
            Integer.parseInt(v1);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String simpexpToRegexp(String pattern) {
        return simpexpToRegexp(pattern, false);
    }

    /**
     * *
     * **
     *
     * @param pattern
     * @return
     */
    public static String simpexpToRegexp(String pattern, boolean contains) {
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
                case '(':
                case ')':
                case '?':
                case '^':
                case '|':
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

    public static String replaceVars(String format, NutsObjectConverter<String, String> map) {
        return replaceVars(format, map, new HashSet());
    }

    private static String replaceVars(String format, NutsObjectConverter<String, String> map, Set<String> visited) {
        if (format == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        Matcher m = pattern.matcher(format);
        while (m.find()) {
            String key = m.group("key");
            if (visited.contains(key)) {
                m.appendReplacement(sb, key);
            } else {
                Set<String> visited2 = new HashSet<>(visited);
                visited2.add(key);
                String replacement = map.convert(key);
                if (replacement != null) {//replace if founded key exists in map
                    replacement = replaceVars(replacement, map, visited2);
                    m.appendReplacement(sb, escapeReplacementStrings(replacement));
                } else {//do not replace, or to be precise replace with same value
                    m.appendReplacement(sb, escapeReplacementStrings(m.group()));
                }
            }
        }
        m.appendTail(sb);

        return sb.toString();
    }

    public static String escapeCoteStrings(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\':
                case '\'': {
                    sb.append("\\");
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

    public static String escapeReplacementStrings(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\':
                case '{':
                case '}':
                case '$': {
                    sb.append("\\");
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

    /**
     * @param text
     * @param compact         if true, quotes will not be used unless necessary
     * @param entrySeparators
     * @return
     */
    public static String simpleQuote(String text, boolean compact, String entrySeparators) {
        StringBuilder sb = new StringBuilder();
        boolean q = !compact;
        for (char c : text.toCharArray()) {
            switch (c) {
                case '\n': {
                    q = true;
                    sb.append("\\n");
                    break;
                }
                case '\f': {
                    q = true;
                    sb.append("\\f");
                    break;
                }
                case '\r': {
                    q = true;
                    sb.append("\\r");
                    break;
                }
                case '\'':
                case '\"': {
                    q = true;
                    sb.append("\\").append(c);
                    break;
                }
                default: {
                    if (entrySeparators.indexOf(c) >= 0) {
                        q = true;
                        sb.append("\\").append(c);
                    } else {
                        sb.append(c);
                    }
                    break;
                }
            }
        }
        if (q) {
            sb.insert(0, '\'');
            sb.append('\'');
        }
        return sb.toString();
    }

    public static List<String> split(Collection<String> stringCollection, String separators) {
        List<String> splitted = new ArrayList<>();
        for (String str : stringCollection) {
            for (String s1 : split(str, separators)) {
                if (!s1.isEmpty()) {
                    splitted.add(s1);
                }
            }
        }
        return splitted;
    }

    public static List<String> split(String str, String separators) {
        if (str == null) {
            return Collections.EMPTY_LIST;
        }
        StringTokenizer st = new StringTokenizer(str, separators);
        List<String> result = new ArrayList<>();
        while (st.hasMoreElements()) {
            result.add(st.nextToken());
        }
        return result;
    }

    public static boolean containsVars(String value) {
        return value != null && value.contains("${");
    }

    public static boolean containsTopWord(String word, String line) {
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(line));
        int last_ttype = -1;
        try {
            while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                switch (tokenizer.ttype) {
                    case StreamTokenizer.TT_WORD: {
                        if (word.equals(tokenizer.sval)) {
                            if (last_ttype != '.') {
                                return true;
                            }
                        }
                    }
                }
                last_ttype = tokenizer.ttype;
            }
        } catch (IOException ex) {
            throw new NutsIOException(ex);
        }
        return false;
    }

    public static String replaceDollarString(String path, NutsObjectConverter<String, String> m) {
        Matcher matcher = DOLLAR_PLACE_HOLDER_PATTERN.matcher(path);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String x = m.convert(matcher.group("name"));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(x));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
