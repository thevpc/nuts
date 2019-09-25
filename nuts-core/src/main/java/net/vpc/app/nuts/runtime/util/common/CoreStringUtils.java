/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.util.common;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import net.vpc.common.strings.StringConverter;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreStringUtils {

    private static final Pattern PATTENR_ALL = Pattern.compile(".*");
    public static final Pattern DOLLAR_PLACE_HOLDER_PATTERN = Pattern.compile("[$][{](?<name>([^}]+))[}]");

    private static Pattern DOLLAR_PATTERN = Pattern.compile("\\$\\{(?<key>[^}]*)}");

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

    public static Pattern toPattern(String pattern) {
        if (isBlank(pattern)) {
            return PATTENR_ALL;
        }
        return Pattern.compile(simpexpToRegexp(pattern, false));
    }

    public static String simpexpToRegexp(String pattern) {
        return simpexpToRegexp(pattern, false);
    }

    /**
     * *
     * **
     *
     * @param pattern
     * @param contains
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

    public static String replaceVars(String format, Function<String, String> map) {
        return replaceVars(format, map, new HashSet());
    }

    private static String replaceVars(String format, Function<String, String> map, Set<String> visited) {
        if (format == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        Matcher m = DOLLAR_PATTERN.matcher(format);
        while (m.find()) {
            String key = m.group("key");
            if (visited.contains(key)) {
                m.appendReplacement(sb, key);
            } else {
                Set<String> visited2 = new HashSet<>(visited);
                visited2.add(key);
                String replacement = map.apply(key);
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
     * @param compact if true, quotes will not be used unless necessary
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
                    if (entrySeparators!=null && entrySeparators.indexOf(c) >= 0) {
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

    public static String dblQuote(String text) {
        return dblQuote(text,false,null);
    }
    
    /**
     * @param text
     * @param compact if true, quotes will not be used unless necessary
     * @param entrySeparators
     * @return
     */
    public static String dblQuote(String text, boolean compact, String entrySeparators) {
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
                case '\"': {
                    q = true;
                    sb.append("\\").append(c);
                    break;
                }
                default: {
                    if (entrySeparators!=null && entrySeparators.indexOf(c) >= 0) {
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
            sb.insert(0, '\"');
            sb.append('\"');
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
            throw new UncheckedIOException(ex);
        }
        return false;
    }

    public static char[] strToChr(String s) {
        if (s == null) {
            return null;
        }
        return s.toCharArray();
    }

    public static String chrToStr(char[] s) {
        if (s == null) {
            return null;
        }
        return new String(s);
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param value
     * @return
     */
    public static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param str
     * @return
     */
    public static String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        String trimmed = str.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param string
     * @return
     */
    public static boolean isBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    public static boolean isBlank(char[] string) {
        if (string == null || string.length == 0) {
            return true;
        }
        for (char c : string) {
            if (c > ' ') {
                return false;
            }
        }
        return true;
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param s
     * @param converter
     * @return
     */
    public static String replaceDollarPlaceHolders(String s, Map<String, String> converter) {
        return replaceDollarPlaceHolders(s, new MapToFunction(converter));
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param s
     * @param converter
     * @return
     */
    public static String replaceDollarPlaceHolders(String s, Function<String, String> converter) {
        Matcher matcher = DOLLAR_PLACE_HOLDER_PATTERN.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group("name");
            String x = converter == null ? null : converter.apply(name);
            if (x == null) {
                x = "${" + name + "}";
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(x));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static class MapToFunction<K, V> implements Function<K, V> {

        private final Map<K, V> converter;

        public MapToFunction(Map<K, V> converter) {
            this.converter = converter;
        }

        @Override
        public V apply(K t) {
            if (converter == null) {
                return null;
            }
            return converter.get(t);
        }
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param sep
     * @param items
     * @return
     */
    public static String join(String sep, Collection<String> items) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> i = items.iterator();
        if (i.hasNext()) {
            sb.append(i.next());
        }
        while (i.hasNext()) {
            sb.append(sep);
            sb.append(i.next());
        }
        return sb.toString();
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param c
     * @return
     */
    public static StringBuilder clear(StringBuilder c) {
        return c.delete(0, c.length());
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param ex
     * @return
     */
    public static String exceptionToString(Throwable ex) {
        for (Class aClass : new Class[]{
            NullPointerException.class,
            ArrayIndexOutOfBoundsException.class,
            ClassCastException.class,
            UnsupportedOperationException.class,
            ReflectiveOperationException.class,}) {
            if (aClass.isInstance(ex)) {
                return ex.toString();
            }
        }
        String message = ex.getMessage();
        if (message == null) {
            message = ex.toString();
        }
        return message;
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param cmd
     * @return
     */
    public static String coalesce(String... cmd) {
        for (String string : cmd) {
            if (!isBlank(string)) {
                return string;
            }
        }
        return null;
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param x
     * @param width
     * @return
     */
    public static String fillString(char x, int width) {
        char[] cc = new char[width];
        Arrays.fill(cc, x);
        return new String(cc);
    }

    public static String fillString(String x, int width) {
        StringBuilder sb = new StringBuilder();
        fillString(x, width, sb);
        return sb.toString();
    }

    public static void fillString(char x, int width, StringBuilder sb) {
        if (width <= 0) {
            return;
        }
        sb.ensureCapacity(sb.length() + width);
        for (int i = 0; i < width; i++) {
            sb.append(x);
        }
    }

    public static void fillString(String x, int width, StringBuilder sb) {
        if (width <= 0) {
            return;
        }
        sb.ensureCapacity(sb.length() + (width * x.length()));
        for (int i = 0; i < width; i++) {
            sb.append(x);
        }
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param s
     * @param width
     * @return
     */
    public static String alignLeft(String s, int width) {
        StringBuilder sb = new StringBuilder();
        if (s != null) {
            sb.append(s);
            int x = width - sb.length();
            if (x > 0) {
                sb.append(fillString(' ', x));
            }
        }
        return sb.toString();
    }

    public static String alignRight(String s, int width) {
        StringBuilder sb = new StringBuilder();
        if (s != null) {
            sb.append(s);
            int x = width - sb.length();
            if (x > 0) {
                sb.insert(0, fillString(' ', x));
            }
        }
        return sb.toString();
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param text
     * @param entrySeparators
     * @return
     */
    public static Map<String, String> parseMap(String text, String entrySeparators) {
        return parseMap(text, "=", entrySeparators);
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param text
     * @param eqSeparators
     * @param entrySeparators
     * @return
     */
    public static Map<String, String> parseMap(String text, String eqSeparators, String entrySeparators) {
        Map<String, String> m = new LinkedHashMap<>();
        StringReader reader = new StringReader(text == null ? "" : text);
        while (true) {
            StringBuilder key = new StringBuilder();
            int r = 0;
            try {
                r = readToken(reader, eqSeparators + entrySeparators, key);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            String t = key.toString();
            if (r == -1) {
                if (!t.isEmpty()) {
                    m.put(t, null);
                }
                break;
            } else {
                char c = (char) r;
                if (eqSeparators.indexOf(c) >= 0) {
                    StringBuilder value = new StringBuilder();
                    try {
                        r = readToken(reader, entrySeparators, value);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    m.put(t, value.toString());
                    if (r == -1) {
                        break;
                    }
                } else {
                    //
                }
            }
        }
        return m;
    }

    /**
     * copied from StringUtils (in order to remove dependency)
     *
     * @param reader
     * @param stopTokens
     * @param result
     * @return
     * @throws IOException
     */
    public static int readToken(Reader reader, String stopTokens, StringBuilder result) throws IOException {
        while (true) {
            int r = reader.read();
            if (r == -1) {
                return -1;
            }
            if (r == '\"' || r == '\'') {
                char s = (char) r;
                while (true) {
                    r = reader.read();
                    if (r == -1) {
                        throw new RuntimeException("Expected " + '\"');
                    }
                    if (r == s) {
                        break;
                    }
                    if (r == '\\') {
                        r = reader.read();
                        if (r == -1) {
                            throw new RuntimeException("Expected " + '\"');
                        }
                        switch ((char) r) {
                            case 'n': {
                                result.append('\n');
                                break;
                            }
                            case 'r': {
                                result.append('\r');
                                break;
                            }
                            case 'f': {
                                result.append('\f');
                                break;
                            }
                            default: {
                                result.append((char) r);
                            }
                        }
                    } else {
                        char cr = (char) r;
                        result.append(cr);
                    }
                }
            } else {
                char cr = (char) r;
                if (stopTokens != null && stopTokens.indexOf(cr) >= 0) {
                    return cr;
                }
                result.append(cr);
            }
        }
    }

    public static List<String> split(String str, String separators, boolean trim) {
        if (str == null) {
            return Collections.EMPTY_LIST;
        }
        StringTokenizer st = new StringTokenizer(str, separators);
        List<String> result = new ArrayList<>();
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            if (trim) {
                s = s.trim();
            }
            result.add(s);
        }
        return result;
    }
}
