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
package net.thevpc.nuts;

import net.thevpc.nuts.boot.PrivateNutsIdListParser;
import net.thevpc.nuts.boot.PrivateNutsStringMapParser;
import net.thevpc.nuts.boot.PrivateNutsUtilStrings;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author thevpc
 * @app.category Util
 * @since 0.8.1
 */
public class NutsUtilStrings {
    private static final Pattern DOLLAR_PLACE_HOLDER_PATTERN = Pattern.compile("[$][{](?<name>([a-zA-Z]+))[}]");
    private static final char[] BASE16_CHARS = "0123456789ABCDEF".toCharArray();


    /**
     * @param value value
     * @return trimmed value (never null)
     */
    public static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    /**
     * @param value value
     * @return trimmed value (never null)
     */
    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }

    public static Boolean parseBoolean(String value, Boolean emptyValue, Boolean errorValue) {
        if (value == null || value.trim().isEmpty()) {
            return emptyValue;
        }
        value = value.trim().toLowerCase();
        if (value.matches("true|enable|enabled|yes|always|y|on|ok|t|o")) {
            return true;
        }
        if (value.matches("false|disable|disabled|no|none|never|n|off|ko|f")) {
            return false;
        }
        return errorValue;
    }

    public static String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = BASE16_CHARS[v >>> 4];
            hexChars[j * 2 + 1] = BASE16_CHARS[v & 15];
        }
        return new String(hexChars);
    }

    public static char toHexChar(int nibble) {
        return BASE16_CHARS[nibble & 15];
    }

    public static String compressString(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\0': {
                    sb.append("\\0");
                    break;
                }
                case '\n': {
                    sb.append("\\n");
                    break;
                }
                case '\r': {
                    sb.append("\\r");
                    break;
                }
                case '\t': {
                    sb.append("\\t");
                    break;
                }
                case '\f': {
                    sb.append("\\f");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public static String leftAlign(String s, int size) {
        int len = s.length();
        StringBuilder sb = new StringBuilder(Math.max(len, size));
        sb.append(s);
        for (int i = len; i < size; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
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

    public static String replaceDollarString(String path, Function<String, String> m) {
        Matcher matcher = DOLLAR_PLACE_HOLDER_PATTERN.matcher(path);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String x = m.apply(matcher.group("name"));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(x));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String formatPeriodMilli(long period) {
        StringBuilder sb = new StringBuilder();
        boolean started = false;
        int h = (int) (period / (1000L * 60L * 60L));
        int mn = (int) ((period % (1000L * 60L * 60L)) / 60000L);
        int s = (int) ((period % 60000L) / 1000L);
        int ms = (int) (period % 1000L);
        if (h > 0) {
            sb.append(formatRight(String.valueOf(h), 2)).append("h ");
            started = true;
        }
        if (mn > 0 || started) {
            sb.append(formatRight(String.valueOf(mn), 2)).append("mn ");
            started = true;
        }
        if (s > 0 || started) {
            sb.append(formatRight(String.valueOf(s), 2)).append("s ");
            //started=true;
        }
        sb.append(formatRight(String.valueOf(ms), 3)).append("ms");
        return sb.toString();
    }

    public static String formatRight(String str, int size) {
        StringBuilder sb = new StringBuilder(size);
        sb.append(str);
        while (sb.length() < size) {
            sb.insert(0, ' ');
        }
        return sb.toString();
    }

    public static String simpleQuotes(String text) {
        return formatStringLiteral(text, QuoteType.SIMPLE, QuoteCondition.QUOTE_ALWAYS, null);
    }

    /**
     * @param text         text
     * @param compact      if true, quotes will not be used unless necessary
     * @param escapedChars escapedChars, can be null
     * @return quotes
     */
    public static String simpleQuotes(String text, boolean compact, String escapedChars) {
        StringBuffer sb = new StringBuffer();
        boolean requireQuotes = !compact;
        for (char c : text.toCharArray()) {
            switch (c) {
                case '\n': {
                    requireQuotes = true;
                    sb.append("\\n");
                    break;
                }
                case '\f': {
                    requireQuotes = true;
                    sb.append("\\f");
                    break;
                }
                case '\r': {
                    requireQuotes = true;
                    sb.append("\\r");
                    break;
                }
                case '\'':
                case '\"':
                case '\\': {
                    requireQuotes = true;
                    sb.append("\\").append(c);
                    break;
                }
                default: {
                    if (escapedChars != null && escapedChars.indexOf(c) >= 0) {
                        requireQuotes = true;
                        sb.append(c);
                    } else {
                        sb.append(c);
                    }
                    break;
                }
            }
        }
        if (requireQuotes) {
            sb.insert(0, '\'');
            sb.append('\'');
        }
        return sb.toString();
    }

    public static String dblQuotes(String text) {
        return dblQuotes(text, false, null);
    }

    /**
     * @param text            text
     * @param compact         if true, quotes will not be used unless necessary
     * @param entrySeparators entrySeparators extra characters to escape
     * @return double quotes
     */
    public static String dblQuotes(String text, boolean compact, String entrySeparators) {
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
                    if (entrySeparators != null && entrySeparators.indexOf(c) >= 0) {
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

    public static String formatStringLiteral(String text, QuoteType quoteType, QuoteCondition condition, String extraSeparators) {
        StringBuilder sb = new StringBuilder();
        boolean requireQuotes = condition == QuoteCondition.QUOTE_ALWAYS;
        boolean allowQuotes = condition != QuoteCondition.QUOTE_NEVER;
        for (char c : text.toCharArray()) {
            switch (c) {
                case '\n': {
                    sb.append("\\n");
                    if (!requireQuotes && allowQuotes) {
                        requireQuotes = true;
                    }
                    break;
                }
                case '\f': {
                    sb.append("\\f");
                    if (!requireQuotes && allowQuotes) {
                        requireQuotes = true;
                    }
                    break;
                }
                case '\r': {
                    sb.append("\\r");
                    if (!requireQuotes && allowQuotes) {
                        requireQuotes = true;
                    }
                    break;
                }
                case '\t': {
                    sb.append("\\t");
                    if (!requireQuotes && allowQuotes) {
                        requireQuotes = true;
                    }
                    break;
                }
                case '\"': {
                    if (quoteType == QuoteType.DOUBLE) {
                        sb.append("\\").append(c);
                        if (!requireQuotes && allowQuotes) {
                            requireQuotes = true;
                        }
                    } else {
                        sb.append(c);
                    }
                    break;
                }
                case '\'': {
                    if (quoteType == QuoteType.SIMPLE) {
                        sb.append("\\").append(c);
                        if (!requireQuotes && allowQuotes) {
                            requireQuotes = true;
                        }
                    } else {
                        sb.append(c);
                    }
                    break;
                }
                case '`': {
                    if (quoteType == QuoteType.ANTI) {
                        sb.append("\\").append(c);
                        if (!requireQuotes && allowQuotes) {
                            requireQuotes = true;
                        }
                    } else {
                        sb.append(c);
                    }
                    break;
                }
                default: {
                    if (extraSeparators != null && extraSeparators.indexOf(c) >= 0) {
                        sb.append("\\").append(c);
                        if (!requireQuotes && allowQuotes) {
                            requireQuotes = true;
                        }
                    } else {
                        sb.append(c);
                    }
                    break;
                }
            }
        }
        if (requireQuotes) {
            switch (quoteType){
                case DOUBLE:{
                    sb.insert(0, '\"');
                    sb.append('\"');
                    break;
                }
                case SIMPLE:{
                    sb.insert(0, '\'');
                    sb.append('\'');
                    break;
                }
                case ANTI:{
                    sb.insert(0, '`');
                    sb.append('`');
                    break;
                }
            }
        }
        return sb.toString();
    }

    public static NutsOptional<Map<String, String>> parseDefaultMap(String text) {
        return parseMap(text, "=", "&","");
    }

    public static NutsOptional<Map<String, String>> parseMap(String text, String eqSeparators, String entrySeparators) {
        return parseMap(text,eqSeparators,entrySeparators,"");
    }

    public static NutsOptional<Map<String, String>> parseMap(String text, String eqSeparators, String entrySeparators, String escapeChars) {
        return PrivateNutsStringMapParser.of(eqSeparators, entrySeparators,escapeChars).parse(text);
    }

    public static String formatDefaultMap(Map<String, String> map) {
        return formatMap(map, "=", "&", "",true);
    }

    public static String formatMap(Map<String, String> map, String eqSeparators, String entrySeparators, boolean sort) {
        return formatMap(map, eqSeparators, entrySeparators, "",sort);
    }

    public static String formatMap(Map<String, String> map, String eqSeparators, String entrySeparators, String escapeChars, boolean sort) {
        return PrivateNutsStringMapParser.of(eqSeparators, entrySeparators,escapeChars).format(map, sort);
    }

    public static NutsOptional<List<String>> parsePropertyIdList(String s) {
        return PrivateNutsIdListParser.parseStringIdList(s);
    }

    public static List<String> parsePropertyStringList(String s) {
        return PrivateNutsUtilStrings.parseAndTrimToDistinctList(s);
    }


    public enum QuoteType {
        DOUBLE,
        SIMPLE,
        ANTI,
    }

    public enum QuoteCondition {
        QUOTE_ALWAYS,
        QUOTE_REQUIRED,
        QUOTE_NEVER,
    }

}
