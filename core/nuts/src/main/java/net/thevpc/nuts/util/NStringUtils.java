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
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.reserved.NReservedUtils;
import net.thevpc.nuts.reserved.NReservedStringUtils;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * @author thevpc
 * @app.category Util
 * @since 0.8.1
 */
public class NStringUtils {
    private static final char[] BASE16_CHARS = "0123456789ABCDEF".toCharArray();


    /**
     * return normalized string without accents
     *
     * @param value value or null
     * @return normalized string without accents
     */
    public static String normalizeString(String value) {
        if (value == null) {
            return null;
        }
        String nfdNormalizedString = Normalizer.normalize(value, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    /**
     * return normalized file name by replacing any special character with a space and trimming the result
     *
     * @param name fine name to normalize
     * @return normalized string without accents
     */
    public static String normalizeFileName(String name) {
        char[] chars = NStringUtils.normalizeString(name).toCharArray();
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '°': {
                    chars[i] = 'o';
                    break;
                }
                case '"':
                case '\'':
                case '`':
                case '?':
                case '*':
                case ':':
                case '%':
                case '|':
                case '<':
                case '>':
                case '/':
                case '\\':
                case '{':
                case '}':
                case '[':
                case ']':
                case '(':
                case ')':
                case '$':
                {
                    chars[i] = ' ';
                    break;
                }
                default: {
                    if (chars[i] < 32) {
                        chars[i] = ' ';
                    }
                }
            }
        }
        return new String(chars).trim();
    }

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

    public static String coalesceNonNull(String... values) {
        return coalesceNonNull(values == null ? null : Arrays.asList(values));
    }

    public static String coalesceNonNull(List<String> values) {
        if (values != null) {
            for (String value : values) {
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static String coalesceNonEmpty(String... values) {
        return coalesceNonEmpty(values == null ? null : Arrays.asList(values));
    }

    public static String coalesceNonEmpty(List<String> values) {
        if (values != null) {
            for (String value : values) {
                if (!isEmpty(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    public static String coalesceNonBlank(String... values) {
        return coalesceNonBlank(values == null ? null : Arrays.asList(values));
    }

    public static String coalesceNonBlank(List<String> values) {
        if (values != null) {
            for (String value : values) {
                if (!NBlankable.isBlank(value)) {
                    return value;
                }
            }
        }
        return null;
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

    public static String formatAlign(String text, int size, NPositionType position) {
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

    public static String formatStringLiteral(String text) {
        return formatStringLiteral(text, QuoteType.DOUBLE);
    }

    public static String formatStringLiteral(String text, QuoteType quoteType) {
        return formatStringLiteral(text, quoteType, NSupportMode.ALWAYS);
    }

    public static String formatStringLiteral(String text, QuoteType quoteType, NSupportMode condition) {
        return formatStringLiteral(text, quoteType, condition, "");
    }

    public static String formatStringLiteral(String text, QuoteType quoteType, NSupportMode condition, String escapeChars) {
        StringBuilder sb = new StringBuilder();
        boolean requireQuotes = condition == NSupportMode.ALWAYS;
        boolean allowQuotes = condition != NSupportMode.NEVER;
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

    public static NOptional<List<String>> parsePropertyIdList(String s) {
        return NReservedUtils.parseStringIdList(s);
    }

    public static List<String> parsePropertyStringList(String s) {
        return NReservedStringUtils.parseAndTrimToDistinctList(s);
    }

    public static NOptional<Level> parseLogLevel(String value) {
        value = value == null ? "" : value.trim();
        if (value.isEmpty()) {
            return NOptional.ofNamedEmpty("log level");
        }
        switch (value.trim().toLowerCase()) {
            case "off": {
                return NOptional.of(Level.OFF);
            }
            case "verbose":
            case "finest": {
                return NOptional.of(Level.FINEST);
            }
            case "finer": {
                return NOptional.of(Level.FINER);
            }
            case "fine": {
                return NOptional.of(Level.FINE);
            }
            case "info": {
                return NOptional.of(Level.INFO);
            }
            case "all": {
                return NOptional.of(Level.ALL);
            }
            case "warning": {
                return NOptional.of(Level.WARNING);
            }
            case "severe": {
                return NOptional.of(Level.SEVERE);
            }
            case "config": {
                return NOptional.of(Level.CONFIG);
            }
        }
        Integer i = NLiteral.of(value).asInt().orNull();
        if (i != null) {
            switch (i) {
                case Integer.MAX_VALUE:
                    return NOptional.of(Level.OFF);
                case 1000:
                    return NOptional.of(Level.SEVERE);
                case 900:
                    return NOptional.of(Level.WARNING);
                case 800:
                    return NOptional.of(Level.INFO);
                case 700:
                    return NOptional.of(Level.CONFIG);
                case 500:
                    return NOptional.of(Level.FINE);
                case 400:
                    return NOptional.of(Level.FINER);
                case 300:
                    return NOptional.of(Level.FINEST);
                case Integer.MIN_VALUE:
                    return NOptional.of(Level.ALL);
            }
            return NOptional.of(new CustomLogLevel("LEVEL" + i, i));
        }
        String finalValue = value;
        return NOptional.ofError(s -> NMsg.ofC("invalid level %s", finalValue));
    }

    public static <T extends Enum> NOptional<T> parseEnum(String value, Class<T> type) {
        if (NBlankable.isBlank(value)) {
            return NOptional.ofEmpty(s -> NMsg.ofC("%s is empty", type.getSimpleName()));
        }
        String normalizedValue = NNameFormat.CONST_NAME.format(value);
        try {
            return NOptional.of((T) Enum.valueOf(type, normalizedValue));
        } catch (Exception notFound) {
            return NOptional.ofError(s -> NMsg.ofC(type.getSimpleName() + " invalid value : %s", value));
        }
    }


    public static <T extends Enum> NOptional<T> parseEnum(String value, Class<T> type, Function<EnumValue, NOptional<T>> mapper) {
        if (NBlankable.isBlank(value)) {
            return NOptional.ofEmpty(s -> NMsg.ofC("%s is empty", type.getSimpleName()));
        }
        String[] parsedValue = NNameFormat.parse(value);
        String normalizedValue = NNameFormat.CONST_NAME.format(parsedValue);
        if (mapper != null) {
            try {
                NOptional<T> o = mapper.apply(new EnumValue(
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
            return NOptional.of((T) Enum.valueOf(type, normalizedValue));
        } catch (Exception notFound) {
            return NOptional.ofError(s -> NMsg.ofC("%s invalid value : %s", type.getSimpleName(), value), notFound);
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


    public enum QuoteType implements NEnum {
        DOUBLE,
        SIMPLE,
        ANTI;
        /**
         * lower-cased identifier for the enum entry
         */
        private final String id;

        /**
         * default constructor
         */
        QuoteType() {
            this.id = NNameFormat.ID_NAME.format(name());
        }

        public static NOptional<QuoteType> parse(String value) {
            return NStringUtils.parseEnum(value, QuoteType.class);
        }

        /**
         * lower cased identifier.
         *
         * @return lower cased identifier
         */
        public String id() {
            return id;
        }
    }

    private static class CustomLogLevel extends Level {
        public CustomLogLevel(String name, int value) {
            super(name, value);
        }
    }
}
