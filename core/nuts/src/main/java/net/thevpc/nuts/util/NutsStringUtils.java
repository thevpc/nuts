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
package net.thevpc.nuts.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NutsPositionType;
import net.thevpc.nuts.reserved.NutsReservedUtils;
import net.thevpc.nuts.reserved.NutsReservedStringMapParser;
import net.thevpc.nuts.reserved.NutsReservedStringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author thevpc
 * @app.category Util
 * @since 0.8.1
 */
public class NutsStringUtils {
    private static final Pattern DOLLAR_PLACE_HOLDER_PATTERN = Pattern.compile("[$][{](?<name>([a-zA-Z._-]+))[}]");
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

    public static String formatAlign(String text, int size, NutsPositionType position) {
        if (text == null) {
            text = "";
        }
        int len = text.length();
        if (len >= size) {
            return text;
        }
        switch (position) {
            case FIRST: {
                StringBuilder sb = new StringBuilder(size);
                sb.append(text);
                for (int i = len; i < size; i++) {
                    sb.append(' ');
                }
                return sb.toString();
            }
            case LAST: {
                StringBuilder sb = new StringBuilder(size);
                for (int i = len; i < size; i++) {
                    sb.append(' ');
                }
                sb.append(text);
                return sb.toString();
            }
            case CENTER: {
                StringBuilder sb = new StringBuilder(size);
                int h = size / 2 + size % 2;
                for (int i = len; i < h; i++) {
                    sb.append(' ');
                }
                sb.append(text);
                h = size / 2;
                for (int i = len; i < h; i++) {
                    sb.append(' ');
                }
                return sb.toString();
            }
        }
        throw new UnsupportedOperationException();
    }

    public static String replaceDollarString(String text, Function<String, String> m) {
        Matcher matcher = DOLLAR_PLACE_HOLDER_PATTERN.matcher(text);
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
            sb.append(formatAlign(String.valueOf(h), 2, NutsPositionType.LAST)).append("h ");
            started = true;
        }
        if (mn > 0 || started) {
            sb.append(formatAlign(String.valueOf(mn), 2, NutsPositionType.LAST)).append("mn ");
            started = true;
        }
        if (s > 0 || started) {
            sb.append(formatAlign(String.valueOf(s), 2, NutsPositionType.LAST)).append("s ");
            //started=true;
        }
        sb.append(formatAlign(String.valueOf(ms), 3, NutsPositionType.LAST)).append("ms");
        return sb.toString();
    }

    public static String formatStringLiteral(String text) {
        return formatStringLiteral(text, QuoteType.DOUBLE);
    }

    public static String formatStringLiteral(String text, QuoteType quoteType) {
        return formatStringLiteral(text, quoteType, NutsSupportMode.ALWAYS);
    }

    public static String formatStringLiteral(String text, QuoteType quoteType, NutsSupportMode condition) {
        return formatStringLiteral(text, quoteType, condition, "");
    }

    public static String formatStringLiteral(String text, QuoteType quoteType, NutsSupportMode condition, String escapeChars) {
        StringBuilder sb = new StringBuilder();
        boolean requireQuotes = condition == NutsSupportMode.ALWAYS;
        boolean allowQuotes = condition != NutsSupportMode.NEVER;
        for (char c : text.toCharArray()) {
            switch (c) {
                case ' ': {
                    if (allowQuotes) {
                        sb.append(" ");
                        requireQuotes = true;
                    } else {
                        sb.append("\\ ");
                    }
                    break;
                }
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
                    if (escapeChars != null && escapeChars.indexOf(c) >= 0) {
                        if (allowQuotes) {
                            sb.append(c);
                            requireQuotes = true;
                        } else {
                            sb.append("\\").append(c);
                        }
                    } else {
                        sb.append(c);
                    }
                    break;
                }
            }
        }
        if (requireQuotes) {
            switch (quoteType) {
                case DOUBLE: {
                    sb.insert(0, '\"');
                    sb.append('\"');
                    break;
                }
                case SIMPLE: {
                    sb.insert(0, '\'');
                    sb.append('\'');
                    break;
                }
                case ANTI: {
                    sb.insert(0, '`');
                    sb.append('`');
                    break;
                }
            }
        }
        return sb.toString();
    }

    public static NutsOptional<Map<String, String>> parseDefaultMap(String text) {
        return parseMap(text, "=", "&", "");
    }

    public static NutsOptional<Map<String, String>> parseMap(String text, String eqSeparators, String entrySeparators) {
        return parseMap(text, eqSeparators, entrySeparators, "");
    }

    public static NutsOptional<Map<String, String>> parseMap(String text, String eqSeparators, String entrySeparators, String escapeChars) {
        return NutsReservedStringMapParser.of(eqSeparators, entrySeparators, escapeChars).parse(text);
    }

    public static String formatDefaultMap(Map<String, String> map) {
        return formatMap(map, "=", "&", "?", true);
    }

    public static String formatMap(Map<String, String> map, String eqSeparators, String entrySeparators, boolean sort) {
        return formatMap(map, eqSeparators, entrySeparators, "", sort);
    }

    public static String formatMap(Map<String, String> map, String eqSeparators, String entrySeparators, String escapeChars, boolean sort) {
        return NutsReservedStringMapParser.of(eqSeparators, entrySeparators, escapeChars).format(map, sort);
    }

    public static NutsOptional<List<String>> parsePropertyIdList(String s) {
        return NutsReservedUtils.parseStringIdList(s);
    }

    public static List<String> parsePropertyStringList(String s) {
        return NutsReservedStringUtils.parseAndTrimToDistinctList(s);
    }

    public static NutsOptional<Level> parseLogLevel(String value) {
        value = value == null ? "" : value.trim();
        if (value.isEmpty()) {
            return NutsOptional.ofNamedEmpty("log level");
        }
        switch (value.trim().toLowerCase()) {
            case "off": {
                return NutsOptional.of(Level.OFF);
            }
            case "verbose":
            case "finest": {
                return NutsOptional.of(Level.FINEST);
            }
            case "finer": {
                return NutsOptional.of(Level.FINER);
            }
            case "fine": {
                return NutsOptional.of(Level.FINE);
            }
            case "info": {
                return NutsOptional.of(Level.INFO);
            }
            case "all": {
                return NutsOptional.of(Level.ALL);
            }
            case "warning": {
                return NutsOptional.of(Level.WARNING);
            }
            case "severe": {
                return NutsOptional.of(Level.SEVERE);
            }
            case "config": {
                return NutsOptional.of(Level.CONFIG);
            }
        }
        Integer i = NutsValue.of(value).asInt().orNull();
        if (i != null) {
            switch (i) {
                case Integer.MAX_VALUE:
                    return NutsOptional.of(Level.OFF);
                case 1000:
                    return NutsOptional.of(Level.SEVERE);
                case 900:
                    return NutsOptional.of(Level.WARNING);
                case 800:
                    return NutsOptional.of(Level.INFO);
                case 700:
                    return NutsOptional.of(Level.CONFIG);
                case 500:
                    return NutsOptional.of(Level.FINE);
                case 400:
                    return NutsOptional.of(Level.FINER);
                case 300:
                    return NutsOptional.of(Level.FINEST);
                case Integer.MIN_VALUE:
                    return NutsOptional.of(Level.ALL);
            }
            return NutsOptional.of(new CustomLogLevel("LEVEL" + i, i));
        }
        String finalValue = value;
        return NutsOptional.ofError(s -> NutsMessage.ofCstyle("invalid level %s", finalValue));
    }

    public static <T extends Enum> NutsOptional<T> parseEnum(String value, Class<T> type) {
        if (NutsBlankable.isBlank(value)) {
            return NutsOptional.ofEmpty(s -> NutsMessage.ofCstyle("%s is empty", type.getSimpleName()));
        }
        String normalizedValue = NutsNameFormat.CONST_NAME.formatName(value);
        try {
            return NutsOptional.of((T) Enum.valueOf(type, normalizedValue));
        } catch (Exception notFound) {
            return NutsOptional.ofError(s -> NutsMessage.ofCstyle(type.getSimpleName() + " invalid value : %s", value));
        }
    }


    public static <T extends Enum> NutsOptional<T> parseEnum(String value, Class<T> type, Function<EnumValue, NutsOptional<T>> mapper) {
        if (NutsBlankable.isBlank(value)) {
            return NutsOptional.ofEmpty(s -> NutsMessage.ofCstyle("%s is empty", type.getSimpleName()));
        }
        String[] parsedValue = NutsNameFormat.parseName(value);
        String normalizedValue = NutsNameFormat.CONST_NAME.formatName(parsedValue);
        if (mapper != null) {
            try {
                NutsOptional<T> o = mapper.apply(new EnumValue(
                        value,
                        normalizedValue,
                        parsedValue
                ));
                if (o != null) {
                    return o;
                }
            } catch (Exception notFound) {
                //just ignore
            }
        }
        try {
            return NutsOptional.of((T) Enum.valueOf(type, normalizedValue));
        } catch (Exception notFound) {
            return NutsOptional.ofError(s -> NutsMessage.ofCstyle("%s invalid value : %s", type.getSimpleName(), value), notFound);
        }
    }

    public static class EnumValue {
        private String value;
        private String normalizedValue;
        private String[] parsedValue;

        public EnumValue(String value, String normalizedValue, String[] parsedValue) {
            this.value = value;
            this.normalizedValue = normalizedValue;
            this.parsedValue = parsedValue;
        }

        public String getValue() {
            return value;
        }

        public String getNormalizedValue() {
            return normalizedValue;
        }

        public String[] getParsedValue() {
            return parsedValue;
        }
    }


    public enum QuoteType {
        DOUBLE,
        SIMPLE,
        ANTI,
    }

    private static class CustomLogLevel extends Level {
        public CustomLogLevel(String name, int value) {
            super(name, value);
        }
    }

    public static List<String> split(String value, String chars) {
        return split(value, chars, true, false);
    }

    public static List<String> split(String value, String chars, boolean trim, boolean ignoreEmpty) {
        if (value == null) {
            value = "";
        }
        StringTokenizer st = new StringTokenizer(value, chars, true);
        List<String> all = new ArrayList<>();
        boolean wasSep = true;
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            if (chars.indexOf(s.charAt(0)) >= 0) {
                if (wasSep) {
                    s = "";
                    if (!ignoreEmpty) {
                        all.add(s);
                    }
                }
                wasSep = true;
            } else {
                wasSep = false;
                if (trim) {
                    s = s.trim();
                }
                if (!ignoreEmpty || !s.isEmpty()) {
                    all.add(s);
                }
            }
        }
        if (wasSep) {
            if (!ignoreEmpty) {
                all.add("");
            }
        }
        return all;
    }
}
