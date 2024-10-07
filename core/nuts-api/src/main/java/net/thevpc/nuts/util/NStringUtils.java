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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.expr.NToken;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.reserved.NReservedUtils;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author thevpc
 * @app.category Util
 * @since 0.8.1
 */
public class NStringUtils {

    public static final String DEFAULT_VAR_NAME = "var";
    private static final char[] BASE16_CHARS = new char[]{'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    private NStringUtils() {
    }

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
    public static CharSequence trim(CharSequence value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return value.toString().trim();
        }
        int len0 = value.length();
        int len = len0;
        int st = 0;
        while ((st < len) && (value.charAt(st) <= ' ')) {
            st++;
        }
        while ((st < len) && (value.charAt(len - 1) <= ' ')) {
            len--;
        }
        return ((st > 0) || (len < len0)) ? value.subSequence(st, len) : value.toString();
    }

    /**
     * @param value value
     * @return trimmed value (never null)
     */
    public static CharSequence trimLeft(CharSequence value) {
        if (value == null) {
            return "";
        }
        int len = value.length();
        if (len == 0) {
            return value.toString();
        }
        int st = 0;
        while ((st < len) && (value.charAt(st) <= ' ')) {
            st++;
        }
        if (st > 0) {
            return value.subSequence(st, len);
        }
        return value.toString();
    }

    /**
     * @param value value
     * @return trimmed value (never null)
     */
    public static CharSequence trimRight(CharSequence value) {
        if (value == null) {
            return "";
        }
        int len = value.length();
        if (len == 0) {
            return value.toString();
        }
        int st = len;
        while ((st > 0) && (value.charAt(st - 1) <= ' ')) {
            st--;
        }
        if (st < len) {
            return value.subSequence(0, st);
        }
        return value.toString();
    }

    /**
     * @param value value
     * @return trimmed value (never null)
     */
    public static String trimLeft(String value) {
        if (value == null) {
            return "";
        }
        int len = value.length();
        if (len == 0) {
            return value;
        }
        int st = 0;
        while ((st < len) && (value.charAt(st) <= ' ')) {
            st++;
        }
        if (st > 0) {
            return value.substring(st, len);
        }
        return value;
    }

    /**
     * @param value value
     * @return trimmed value (never null)
     */
    public static String trimRight(String value) {
        if (value == null) {
            return "";
        }
        int len = value.length();
        if (len == 0) {
            return value;
        }
        int st = len;
        while ((st > 0) && (value.charAt(st - 1) <= ' ')) {
            st--;
        }
        if (st < len) {
            return value.substring(0, st);
        }
        return value;
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

    /**
     * @param value value
     * @return trimmed value (never null)
     */
    public static String trimToNull(CharSequence value) {
        if (value == null) {
            return null;
        }
        String t = trim(value).toString();
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }

    /**
     * @param value value
     * @return trimmed value (never null)
     */
    public static String trimLeftToNull(CharSequence value) {
        if (value == null) {
            return null;
        }
        String t = trimLeft(value).toString();
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }

    /**
     * @param value value
     * @return trimmed value (never null)
     */
    public static String trimRightToNull(CharSequence value) {
        if (value == null) {
            return null;
        }
        String t = trimRight(value).toString();
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }

    public static String firstNonNull(String... values) {
        return firstNonNull(values == null ? null : Arrays.asList(values));
    }

    public static String firstNonNull(List<String> values) {
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

    public static String firstNonEmpty(String... values) {
        return firstNonEmpty(values == null ? null : Arrays.asList(values));
    }

    public static String firstNonEmpty(List<String> values) {
        if (values != null) {
            for (String value : values) {
                if (!isEmpty(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    public static String firstNonBlank(String a, String b) {
        if (!NBlankable.isBlank(a)) {
            return a;
        }
        if (!NBlankable.isBlank(b)) {
            return b;
        }
        return null;
    }

    public static String firstNonBlank(String... values) {
        return firstNonBlank(values == null ? null : Arrays.asList(values));
    }

    public static String firstNonBlank(List<String> values) {
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
        return formatStringLiteral(text, NQuoteType.DOUBLE);
    }

    public static String formatStringLiteral(String text, NQuoteType quoteType) {
        return formatStringLiteral(text, quoteType, NSupportMode.ALWAYS);
    }

    public static String formatStringLiteral(String text, NQuoteType quoteType, NSupportMode condition) {
        return formatStringLiteral(text, quoteType, condition, "");
    }

    public static String formatStringLiteral(String text, NQuoteType quoteType, NSupportMode condition, String escapeChars) {
        if (text == null) {
            return "null";
        }
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
                    if (quoteType == NQuoteType.DOUBLE) {
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
                    if (quoteType == NQuoteType.SIMPLE) {
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
                    if (quoteType == NQuoteType.ANTI) {
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
        if (sb.length() == 0) {
            requireQuotes = true;
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
        return NReservedLangUtils.parseAndTrimToDistinctList(s);
    }


    public static List<String> split(String value, String chars) {
        return split(value, chars, true, false);
    }

    public static String repeat(char c, int count) {
        char[] e = new char[count];
        Arrays.fill(e, c);
        return new String(e);
    }

    public static String repeat(String str, int count) {
        if (count < 0) {
            throw new ArrayIndexOutOfBoundsException(count);
        }
        switch (count) {
            case 0:
                return "";
            case 1:
                return str;
        }
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static String alignLeft(String s, int width) {
        StringBuilder sb = new StringBuilder();
        if (s != null) {
            sb.append(s);
            int x = width - sb.length();
            if (x > 0) {
                sb.append(repeat(' ', x));
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
                sb.insert(0, repeat(' ', x));
            }
        }
        return sb.toString();
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

    public static byte[] fromHexString(String s) {
        int len = s.length();
        if (len == 0) {
            return new byte[0];
        }
        if (s.length() % 2 == 1) {
            s = s + "0";
            len++;
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            char c1 = s.charAt(i);
            char c2 = s.charAt(i + 1);
            data[i / 2] = (byte) ((Character.digit(c1, 16) << 4)
                    + Character.digit(c2, 16));
        }
        return data;
    }

    /**
     * replace all placeholders in a text string with values from a given mapper.
     * Here is an example :
     * <pre>
     *     String result=NMsgVarTextParser.replacePlaceholder("a${b}ad","${(?&lt;var&gt;[a-z]+)}",x->x+x);
     *     // result is abbad
     * </pre>
     *
     * @param text text to replace the placeholders in
     * @param regexp regular expression of the placeholder. The regexp MUST define the 'var' group
     * @param mapper mapper function that replaces each placeholder. When it returns null, no changes are made
     * @return text with all placeholders replaces with values from <code>mapper</code>
     */
    public static String replacePlaceholder(String text, String regexp, Function<String, String> mapper) {
        return replacePlaceholder(text, Pattern.compile(regexp), null, mapper);
    }

    /**
     * replace all placeholders in a text string with values from a given mapper.
     * Here is an example :
     * <pre>
     *     String result=NMsgVarTextParser.replacePlaceholder("a${b}ad","${(?&lt;var&gt;[a-z]+)}","var",x->x+x);
     *     // result is abbad
     * </pre>
     *
     * @param text text to replace the placeholders in
     * @param regexp regular expression of the placeholder. The regexp MUST define the varName
     * @param varName the varName in the regex, defaults to <code>NMsgVarTextParser.DEFAULT_VAR_NAME</code> aka <code>"var"</code>
     * @param mapper mapper function that replaces each placeholder. When it returns null, no changes are made
     * @return text with all placeholders replaces with values from <code>mapper</code>
     */
    public static String replacePlaceholder(String text, String regexp, String varName, Function<String, String> mapper) {
        return replacePlaceholder(text, Pattern.compile(regexp), varName, mapper);
    }

    /**
     * replace all placeholders in a text string with values from a given mapper.
     * Here is an example :
     * <pre>
     *     String result=NMsgVarTextParser.replacePlaceholder("a${b}ad",Pattern.compile("${(?&lt;var&gt;[a-z]+)}"),"var",x->x+x);
     *     // result is abbad
     * </pre>
     *
     * @param text text to replace the placeholders in
     * @param regexp regular expression of the placeholder. The regexp MUST define the varName
     * @param varName the varName in the regex, defaults to <code>NMsgVarTextParser.DEFAULT_VAR_NAME</code> aka <code>"var"</code>
     * @param mapper mapper function that replaces each placeholder. When it returns null, no changes are made
     * @return text with all placeholders replaces with values from <code>mapper</code>
     */
    public static String replacePlaceholder(String text, Pattern regexp, String varName, Function<String, String> mapper) {
        if (text == null) {
            return "";
        }
        if (mapper == null) {
            return "";
        }
//        NAssert.requireNonNull(regexp, "regexp");
//        if (NBlankable.isBlank(patternVarName)) {
//            patternVarName = DEFAULT_VAR_NAME;
//        }
//        Matcher matcher = regexp.matcher(text);
//        StringBuffer sb = new StringBuffer();
//        while (matcher.find()) {
//            String name = matcher.group(patternVarName);
//            String all = matcher.group();
//            String v = mapper.apply(name);
//            if (v == null) {
//                v = all;
//            }
//            matcher.appendReplacement(sb, Matcher.quoteReplacement(v));
//        }
//        matcher.appendTail(sb);
//        return sb.toString();

        return parsePlaceHolder(text,regexp,varName)
                .map(t -> {
                    switch (t.ttype) {
                        case NToken.TT_VAR: {
                            String x = mapper.apply(t.sval);
                            if (x == null) {
                                return t.image;
                            }
                            return x;
                        }
                    }
                    return t.sval;
                }).collect(Collectors.joining());
    }

    public static Stream<NToken> parsePlaceHolder(String text, Pattern pattern, String patternVarName) {
        NAssert.requireNonNull(pattern, "pattern");
        if (text == null) {
            return Stream.empty();
        }
        final String TT_DEFAULT_STR = NToken.typeString(NToken.TT_DEFAULT);
        final String TT_VAR_STR = NToken.typeString(NToken.TT_VAR);
        return iterToStream(new Iterator<NToken>() {
            final String vn;
            final Matcher matcher;
            int last;
            final List<NToken> buffer = new ArrayList<>(2);

            {
                if (NBlankable.isBlank(patternVarName)) {
                    vn = DEFAULT_VAR_NAME;
                } else {
                    vn = patternVarName;
                }
                matcher = pattern.matcher(text);
            }

            private boolean ready() {
                return !buffer.isEmpty();
            }

            @Override
            public boolean hasNext() {
                if (ready()) {
                    return true;
                }
                if (matcher.find()) {
                    String name = matcher.group(patternVarName);
                    String all = matcher.group();
                    int start = matcher.start();
                    if (start > last) {
                        String t = text.substring(last, start);
                        buffer.add(NToken.of(NToken.TT_DEFAULT, t, 0, 0, t, TT_DEFAULT_STR));
                    }
                    last = start + all.length();
                    buffer.add(NToken.of(NToken.TT_VAR, name, 0, 0, all, TT_VAR_STR));
                    return true;
                }
                if (last < text.length()) {
                    String t = text.substring(last);
                    buffer.add(NToken.of(NToken.TT_DEFAULT, t, 0, 0, t, TT_DEFAULT_STR));
                    last = text.length();
                }
                return ready();
            }

            @Override
            public NToken next() {
                NAssert.requireTrue(ready(), "token ready");
                return buffer.remove(0);
            }
        });
    }

    public static String replaceDollarPlaceHolder(String text, Function<String, String> mapper) {
        if (mapper == null) {
            return "";
        }
        return parseDollarPlaceHolder(text)
                .map(t -> {
                    switch (t.ttype) {
                        case NToken.TT_DOLLAR:
                        case NToken.TT_DOLLAR_BRACE: {
                            String x = mapper.apply(t.sval);
                            if (x == null) {
                                throw new IllegalArgumentException("var not found " + t.sval);
                            }
                            return x;
                        }
                    }
                    return t.sval;
                }).collect(Collectors.joining());
    }

    public static Stream<NToken> parseDollarPlaceHolder(String text) {
        final String TT_DEFAULT_STR = NToken.typeString(NToken.TT_DEFAULT);
        final String TT_DOLLAR_BRACE_STR = NToken.typeString(NToken.TT_DOLLAR_BRACE);
        final String TT_DOLLAR_STR = NToken.typeString(NToken.TT_DOLLAR);
        return iterToStream(new Iterator<NToken>() {
            final char[] t = (text == null ? new char[0] : text.toCharArray());
            int p = 0;
            final int length = t.length;
            final StringBuilder sb = new StringBuilder(length);
            final StringBuilder n = new StringBuilder(length);
            final StringBuilder ni = new StringBuilder(length);
            final List<NToken> buffer = new ArrayList<>(2);

            private boolean ready() {
                return !buffer.isEmpty();
            }

            @Override
            public boolean hasNext() {
                if (ready()) {
                    return true;
                }
                while (p < length) {
                    fillOnce();
                    if (ready()) {
                        return true;
                    }
                }
                if (sb.length() > 0) {
                    buffer.add(NToken.of(NToken.TT_DEFAULT, sb.toString(), 0, 0, sb.toString(), TT_DEFAULT_STR));
                    sb.setLength(0);
                }
                return ready();
            }

            private void fillOnce() {
                char c = t[p];
                if (c == '$' && p + 1 < length && t[p + 1] == '{') {
                    p += 2;
                    n.setLength(0);
                    ni.setLength(0);
                    ni.append(c).append('{');
                    while (p < length) {
                        c = t[p];
                        if (c != '}') {
                            n.append(c);
                            ni.append(c);
                            p++;
                        } else {
                            ni.append(c);
                            break;
                        }
                    }
                    if (sb.length() > 0) {
                        buffer.add(NToken.of(NToken.TT_DEFAULT, sb.toString(), 0, 0, sb.toString(), TT_DEFAULT_STR));
                        sb.setLength(0);
                    }
                    buffer.add(NToken.of(NToken.TT_DOLLAR_BRACE, n.toString(), 0, 0, ni.toString(), TT_DOLLAR_BRACE_STR));
                } else if (c == '$' && p + 1 < length && isValidVarStart(t[p + 1])) {
                    p++;
                    n.setLength(0);
                    ni.setLength(0);
                    ni.append(c);
                    while (p < length) {
                        c = t[p];
                        if (isValidVarPart(c)) {
                            n.append(c);
                            ni.append(c);
                            p++;
                        } else {
                            p--;
                            break;
                        }
                    }
                    if (sb.length() > 0) {
                        buffer.add(NToken.of(NToken.TT_DEFAULT, sb.toString(), 0, 0, sb.toString(), TT_DEFAULT_STR));
                        sb.setLength(0);
                    }
                    buffer.add(NToken.of(NToken.TT_DOLLAR, n.toString(), 0, 0, ni.toString(), TT_DOLLAR_STR));
                } else {
                    sb.append(c);
                }
                p++;
            }

            @Override
            public NToken next() {
                NAssert.requireTrue(ready(), "token ready");
                return buffer.remove(0);
            }
        });

    }

    public static boolean isValidVarPart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
    }

    public static boolean isValidVarStart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private static <T> Stream<T> iterToStream(Iterator<T> it) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, 0), false);
    }

}
