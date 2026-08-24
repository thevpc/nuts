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
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.expr.NToken;
import net.thevpc.nuts.text.NPositionType;
import net.thevpc.nuts.internal.NReservedLangUtils;
import net.thevpc.nuts.internal.NReservedUtils;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
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

    /**
     * N string utils.
     *
     * @return n string utils result
     */
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
     * Checks if is blank.
     *
     * @param value value
     * @return is blank result
     */
    public static boolean isBlank(String value) {
        if (value == null) {
            return true;
        }
        int len = value.length();
        if (len == 0) {
            return true;
        }
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if is blank.
     *
     * @param value value
     * @return is blank result
     */
    public static boolean isBlank(char[] value) {
        if (value == null) {
            return true;
        }
        int len = value.length;
        if (len == 0) {
            return true;
        }
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(value[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if is blank.
     *
     * @param value value
     * @return is blank result
     */
    public static boolean isBlank(CharSequence value) {
        if (value == null) {
            return true;
        }
        int len = value.length();
        if (len == 0) {
            return true;
        }
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param value value
     * @return stripped value (never null)
     * @since 1.0.0 dropped trim in favor of strip
     */
    public static String strip(String value) {
        if (value == null) {
            return "";
        }
        int len0 = value.length();
        int len = len0;
        int st = 0;
        while ((st < len) && Character.isWhitespace(value.charAt(st))) {
            st++;
        }
        while ((st < len) && Character.isWhitespace(value.charAt(len - 1))) {
            len--;
        }
        return ((st > 0) || (len < len0)) ? value.substring(st, len) : value;
    }

    /**
     * @param value value
     * @return stripped value (never null)
     */
    public static CharSequence strip(CharSequence value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            /**
             * Strip.
             *
             * @param value.toString() value.to string()
             * @return strip result
             */
            return strip(value.toString());
        }
        int len0 = value.length();
        int len = len0;
        int st = 0;
        while ((st < len) && Character.isWhitespace(value.charAt(st))) {
            st++;
        }
        while ((st < len) && Character.isWhitespace(value.charAt(len - 1))) {
            len--;
        }
      /**
       * Return.
       *
       * @param value.subSequence(st value.sub sequence(st
       * @param value.toString( value.to string(
       */
        return ((st > 0) || (len < len0)) ? value.subSequence(st, len) : value.toString();
    }

    /**
     * @param value value
     * @return stripped value (never null)
     */
    public static CharSequence stripLeft(CharSequence value) {
        if (value == null) {
            return "";
        }
        int len = value.length();
        if (len == 0) {
            return value.toString();
        }
        int st = 0;
        while ((st < len) && Character.isWhitespace(value.charAt(st))) {
            st++;
        }
        if (st > 0) {
            return value.subSequence(st, len);
        }
        return value.toString();
    }

    /**
     * @param value value
     * @return stripped value (never null)
     */
    public static CharSequence stripRight(CharSequence value) {
        if (value == null) {
            return "";
        }
        int len = value.length();
        if (len == 0) {
            return value.toString();
        }
        int st = len;
        while ((st > 0) && Character.isWhitespace(value.charAt(st - 1))) {
            st--;
        }
        if (st < len) {
            return value.subSequence(0, st);
        }
        return value.toString();
    }

    /**
     * @param value value
     * @return stripped value (never null)
     */
    public static String stripLeft(String value) {
        if (value == null) {
            return "";
        }
        int len = value.length();
        if (len == 0) {
            return value;
        }
        int st = 0;
        while ((st < len) && Character.isWhitespace(value.charAt(st))) {
            st++;
        }
        if (st > 0) {
            return value.substring(st, len);
        }
        return value;
    }

    /**
     * @param value value
     * @return stripped value (never null)
     */
    public static String stripRight(String value) {
        if (value == null) {
            return "";
        }
        int len = value.length();
        if (len == 0) {
            return value;
        }
        int st = len;
        while ((st > 0) && Character.isWhitespace(value.charAt(st - 1))) {
            st--;
        }
        if (st < len) {
            return value.substring(0, st);
        }
        return value;
    }

    /**
     * @param value value
     * @return stripped value (never null)
     */
    public static String stripToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = strip(value);
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }

    /**
     * @param value value
     * @return stripped value (never null)
     */
    public static String stripToNull(CharSequence value) {
        if (value == null) {
            return null;
        }
        String t = strip(value).toString();
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }

    /**
     * @param value value
     * @return stripped value (never null)
     */
    public static String stripLeftToNull(CharSequence value) {
        if (value == null) {
            return null;
        }
        String t = stripLeft(value).toString();
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }

    /**
     * @param value value
     * @return stripped value (never null)
     */
    public static String stripRightToNull(CharSequence value) {
        if (value == null) {
            return null;
        }
        String t = stripRight(value).toString();
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }

    /**
     * First index of.
     *
     * @param string string
     * @param chars chars
     * @return first index of result
     */
    public static int firstIndexOf(String string, char... chars) {
        if (string == null || chars == null || string.isEmpty()) {
            return -1;
        }
        int stringLen = string.length();
        int charsLen = chars.length;
        for (int i = 0; i < stringLen; i++) {
            char c = string.charAt(i);
            for (int j = 0; j < charsLen; j++) {
                if (c == chars[j]) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * First non null.
     *
     * @param values values
     * @return first non null result
     */
    public static String firstNonNull(String... values) {
        /**
         * First non null.
         *
         * @param Arrays.asList(values) arrays.as list(values)
         * @return first non null result
         */
        return firstNonNull(values == null ? null : Arrays.asList(values));
    }

    /**
     * First non null.
     *
     * @param values values
     * @return first non null result
     */
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

    /**
     * Checks if is empty.
     *
     * @param value value
     * @return is empty result
     */
    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * First non empty.
     *
     * @param values values
     * @return first non empty result
     */
    public static String firstNonEmpty(String... values) {
        /**
         * First non empty.
         *
         * @param Arrays.asList(values) arrays.as list(values)
         * @return first non empty result
         */
        return firstNonEmpty(values == null ? null : Arrays.asList(values));
    }

    /**
     * First non empty.
     *
     * @param values values
     * @return first non empty result
     */
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

    /**
     * First non blank stripped to null.
     *
     * @param a a
     * @param b b
     * @return first non blank stripped to null result
     */
    public static String firstNonBlankStrippedToNull(String a, String b) {
        if (!NBlankable.isBlank(a)) {
            /**
             * Strip to null.
             *
             * @param a a
             * @return strip to null result
             */
            return stripToNull(a);
        }
        if (!NBlankable.isBlank(b)) {
            /**
             * Strip to null.
             *
             * @param b b
             * @return strip to null result
             */
            return stripToNull(b);
        }
        return null;
    }

    /**
     * First non blank stripped.
     *
     * @param a a
     * @param b b
     * @return first non blank stripped result
     */
    public static String firstNonBlankStripped(String a, String b) {
        if (!NBlankable.isBlank(a)) {
            /**
             * Strip.
             *
             * @param a a
             * @return strip result
             */
            return strip(a);
        }
        if (!NBlankable.isBlank(b)) {
            /**
             * Strip.
             *
             * @param b b
             * @return strip result
             */
            return strip(b);
        }
        return "";
    }

    /**
     * First non blank.
     *
     * @param a a
     * @param b b
     * @return first non blank result
     */
    public static String firstNonBlank(String a, String b) {
        if (!NBlankable.isBlank(a)) {
            return a;
        }
        if (!NBlankable.isBlank(b)) {
            return b;
        }
        return null;
    }

    /**
     * First non blank.
     *
     * @param values values
     * @return first non blank result
     */
    public static String firstNonBlank(String... values) {
        /**
         * First non blank.
         *
         * @param Arrays.asList(values) arrays.as list(values)
         * @return first non blank result
         */
        return firstNonBlank(values == null ? null : Arrays.asList(values));
    }

    /**
     * First non blank stripped.
     *
     * @param values values
     * @return first non blank stripped result
     */
    public static String firstNonBlankStripped(String... values) {
        /**
         * First non blank stripped.
         *
         * @param Arrays.asList(values) arrays.as list(values)
         * @return first non blank stripped result
         */
        return firstNonBlankStripped(values == null ? null : Arrays.asList(values));
    }

    /**
     * First non blank stripped to null.
     *
     * @param values values
     * @return first non blank stripped to null result
     */
    public static String firstNonBlankStrippedToNull(String... values) {
        /**
         * First non blank stripped to null.
         *
         * @param Arrays.asList(values) arrays.as list(values)
         * @return first non blank stripped to null result
         */
        return firstNonBlankStrippedToNull(values == null ? null : Arrays.asList(values));
    }

    /**
     * First non blank stripped to null.
     *
     * @param values values
     * @return first non blank stripped to null result
     */
    public static String firstNonBlankStrippedToNull(List<String> values) {
        if (values != null) {
            for (String value : values) {
                if (!NBlankable.isBlank(value)) {
                    /**
                     * Strip to null.
                     *
                     * @param value value
                     * @return strip to null result
                     */
                    return stripToNull(value);
                }
            }
        }
        return null;
    }

    /**
     * First non blank stripped.
     *
     * @param values values
     * @return first non blank stripped result
     */
    public static String firstNonBlankStripped(List<String> values) {
        if (values != null) {
            for (String value : values) {
                if (!NBlankable.isBlank(value)) {
                    /**
                     * Strip.
                     *
                     * @param value value
                     * @return strip result
                     */
                    return strip(value);
                }
            }
        }
        return null;
    }

    /**
     * First non blank.
     *
     * @param values values
     * @return first non blank result
     */
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

    /**
     * Format align.
     *
     * @param text text
     * @param size size
     * @param position position
     * @return format align result
     */
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
        /**
         * Unsupported operation exception.
         *
         * @return unsupported operation exception result
         */
        throw new UnsupportedOperationException();
    }

    /**
     * Format string literal.
     *
     * @param text text
     * @return format string literal result
     */
    public static String formatStringLiteral(String text) {
        /**
         * Format string literal.
         *
         * @param text text
         * @param NElementType.DOUBLE_QUOTED_STRING n element type.double_quoted_string
         * @return format string literal result
         */
        return formatStringLiteral(text, NElementType.DOUBLE_QUOTED_STRING);
    }

    /**
     * Format string literal.
     *
     * @param text text
     * @param quoteType quote type
     * @return format string literal result
     */
    public static String formatStringLiteral(String text, NElementType quoteType) {
        /**
         * Format string literal.
         *
         * @param text text
         * @param quoteType quote type
         * @param NSupportMode.ALWAYS n support mode.always
         * @return format string literal result
         */
        return formatStringLiteral(text, quoteType, NSupportMode.ALWAYS);
    }

    /**
     * Format string literal.
     *
     * @param text text
     * @param quoteType quote type
     * @param condition condition
     * @return format string literal result
     */
    public static String formatStringLiteral(String text, NElementType quoteType, NSupportMode condition) {
        /**
         * Format string literal.
         *
         * @param text text
         * @param quoteType quote type
         * @param condition condition
         * @param false false
         * @param "" ""
         * @return format string literal result
         */
        return formatStringLiteral(text, quoteType, condition, false, "");
    }

    /**
     * Format string literal.
     *
     * @param text text
     * @param quoteType quote type
     * @param condition condition
     * @param skipBoundaries skip boundaries
     * @param extraEscapeChars extra escape chars
     * @return format string literal result
     */
    public static String formatStringLiteral(String text, NElementType quoteType, NSupportMode condition, boolean skipBoundaries, String extraEscapeChars) {
        if (text == null) {
            return "null";
        }
        NSupportMode effectiveCondition = skipBoundaries ? NSupportMode.NEVER : condition;

        // Build the char-escape set: start from the standard set and append extras if any
        NCharEscapeSet escapeSet = NCharEscapeSet.JAVA_WITH_SPACE;
        if (extraEscapeChars != null && !extraEscapeChars.isEmpty()) {
            escapeSet = NCharEscapeSet.combine(escapeSet,
                    NCharEscapeSet.of(
                            NCharEscapeSet.Entry.always(extraEscapeChars,
                                    NCharEscape.BACKSLASH)));
        }

        AbstractNStringLiteralFormat fmt;
        switch (quoteType) {
            case LINE_STRING:
                fmt = AbstractNStringLiteralFormat.ofPrefix("¶ ", "\n", effectiveCondition, escapeSet);
                break;
            case BLOCK_STRING:
                fmt = AbstractNStringLiteralFormat.ofPrefix("¶¶ ", "\n", effectiveCondition, escapeSet);
                break;
            default:
                fmt = AbstractNStringLiteralFormat.ofEscapeChar(
                        quoteType, effectiveCondition, escapeSet, NCharEscape.BACKSLASH);
                break;
        }

        return fmt.format(text);
    }

    /**
     * Parse property id list.
     *
     * @param s s
     * @return parse property id list result
     */
    public static NOptional<List<String>> parsePropertyIdList(String s) {
        return NReservedUtils.parseStringIdList(s);
    }

    /**
     * Parse property string list.
     *
     * @param s s
     * @return parse property string list result
     */
    public static List<String> parsePropertyStringList(String s) {
        return NReservedLangUtils.parseAndStripToDistinctList(s);
    }

    /**
     * Split.
     *
     * @param value value
     * @param chars chars
     * @return split result
     */
    public static List<String> split(String value, String chars) {
        /**
         * Split.
         *
         * @param value value
         * @param chars chars
         * @param true true
         * @param false false
         * @return split result
         */
        return split(value, chars, true, false);
    }

    /**
     * Repeat.
     *
     * @param c c
     * @param count count
     * @return repeat result
     */
    public static String repeat(char c, int count) {
        char[] e = new char[count];
        Arrays.fill(e, c);
        return new String(e);
    }

    /**
     * Repeat.
     *
     * @param str str
     * @param count count
     * @return repeat result
     */
    public static String repeat(String str, int count) {
        if (count < 0) {
            /**
             * Array index out of bounds exception.
             *
             * @param count count
             * @return array index out of bounds exception result
             */
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

    /**
     * Align left.
     *
     * @param s s
     * @param width width
     * @return align left result
     */
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

    /**
     * Align right.
     *
     * @param s s
     * @param width width
     * @return align right result
     */
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

    /**
     * Split.
     *
     * @param value value
     * @param chars chars
     * @param strip strip
     * @param ignoreEmpty ignore empty
     * @return split result
     */
    public static List<String> split(String value, String chars, boolean strip, boolean ignoreEmpty) {
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
                if (strip) {
                    s = NStringUtils.strip(s);
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

    /**
     * replace all placeholders in a text string with values from a given
     * mapper. Here is an example :
     * <pre>
     *     String result=NMsgVarTextParser.replacePlaceholder("a${b}ad","${(?&lt;var&gt;[a-z]+)}",x->x+x);
     *     // result is abbad
     * </pre>
     *
     * @param text   text to replace the placeholders in
     * @param regexp regular expression of the placeholder. The regexp MUST
     *               define the 'var' group
     * @param mapper mapper function that replaces each placeholder. When it
     *               returns null, no changes are made
     * @return text with all placeholders replaces with values from
     * <code>mapper</code>
     */
    public static String replacePlaceholder(String text, String regexp, Function<String, String> mapper) {
        /**
         * Replace placeholder.
         *
         * @param text text
         * @param Pattern.compile(regexp) pattern.compile(regexp)
         * @param null null
         * @param mapper mapper
         * @return replace placeholder result
         */
        return replacePlaceholder(text, Pattern.compile(regexp), null, mapper);
    }

    /**
     * replace all placeholders in a text string with values from a given
     * mapper. Here is an example :
     * <pre>
     *     String result=NMsgVarTextParser.replacePlaceholder("a${b}ad","${(?&lt;var&gt;[a-z]+)}","var",x->x+x);
     *     // result is abbad
     * </pre>
     *
     * @param text    text to replace the placeholders in
     * @param regexp  regular expression of the placeholder. The regexp MUST
     *                define the varName
     * @param varName the varName in the regex, defaults to
     *                <code>NMsgVarTextParser.DEFAULT_VAR_NAME</code> aka <code>"var"</code>
     * @param mapper  mapper function that replaces each placeholder. When it
     *                returns null, no changes are made
     * @return text with all placeholders replaces with values from
     * <code>mapper</code>
     */
    public static String replacePlaceholder(String text, String regexp, String varName, Function<String, String> mapper) {
        /**
         * Replace placeholder.
         *
         * @param text text
         * @param Pattern.compile(regexp) pattern.compile(regexp)
         * @param varName var name
         * @param mapper mapper
         * @return replace placeholder result
         */
        return replacePlaceholder(text, Pattern.compile(regexp), varName, mapper);
    }

    /**
     * replace all placeholders in a text string with values from a given
     * mapper. Here is an example :
     * <pre>
     *     String result=NMsgVarTextParser.replacePlaceholder("a${b}ad",Pattern.compile("${(?&lt;var&gt;[a-z]+)}"),"var",x->x+x);
     *     // result is abbad
     * </pre>
     *
     * @param text    text to replace the placeholders in
     * @param regexp  regular expression of the placeholder. The regexp MUST
     *                define the varName
     * @param varName the varName in the regex, defaults to
     *                <code>NMsgVarTextParser.DEFAULT_VAR_NAME</code> aka <code>"var"</code>
     * @param mapper  mapper function that replaces each placeholder. When it
     *                returns null, no changes are made
     * @return text with all placeholders replaces with values from
     * <code>mapper</code>
     */
    public static String replacePlaceholder(String text, Pattern regexp, String varName, Function<String, String> mapper) {
        if (text == null) {
            return "";
        }
        if (mapper == null) {
            return "";
        }
        return parsePlaceHolder(text, regexp, varName)
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

    /**
     * Parse place holder.
     *
     * @param text text
     * @param pattern pattern
     * @param patternVarName pattern var name
     * @return parse place holder result
     */
    public static Stream<NToken> parsePlaceHolder(String text, Pattern pattern, String patternVarName) {
        NAssert.requireNamedNonNull(pattern, "pattern");
        if (text == null) {
            return Stream.empty();
        }
        final String TT_DEFAULT_STR = NToken.typeString(NToken.TT_DEFAULT);
        final String TT_VAR_STR = NToken.typeString(NToken.TT_VAR);
        /**
         * Iter to stream.
         *
         * @param Iterator<NToken>( iterator<n token>(
         * @return iter to stream result
         */
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

            /**
             * Ready.
             *
             * @return ready result
             */
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
                /**
                 * Ready.
                 *
                 * @return ready result
                 */
                return ready();
            }

            @Override
            public NToken next() {
                NAssert.requireNamedTrue(ready(), "token ready");
                return buffer.remove(0);
            }
        });
    }

    /**
     * text replacing all $abc and ${abc} vars
     *
     * @param text   text to parse
     * @param mapper mapper function. when returns null, no replacement is performed
     * @return text replacing all $abc and ${abc} vars
     */
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
                                return t.image;
                            }
                            return x;
                        }
                    }
                    return t.sval;
                }).collect(Collectors.joining());
    }

    /**
     * Parse dollar place holder.
     *
     * @param text text
     * @return parse dollar place holder result
     */
    public static Stream<NToken> parseDollarPlaceHolder(String text) {
        final String TT_DEFAULT_STR = NToken.typeString(NToken.TT_DEFAULT);
        final String TT_DOLLAR_BRACE_STR = NToken.typeString(NToken.TT_DOLLAR_BRACE);
        final String TT_DOLLAR_STR = NToken.typeString(NToken.TT_DOLLAR);
        /**
         * Iter to stream.
         *
         * @param Iterator<NToken>( iterator<n token>(
         * @return iter to stream result
         */
        return iterToStream(new Iterator<NToken>() {
            final char[] t = (text == null ? new char[0] : text.toCharArray());
            int p = 0;
            final int length = t.length;
            final StringBuilder sb = new StringBuilder(length);
            final StringBuilder n = new StringBuilder(length);
            final StringBuilder ni = new StringBuilder(length);
            final List<NToken> buffer = new ArrayList<>(2);

            /**
             * Ready.
             *
             * @return ready result
             */
            private boolean ready() {
                return !buffer.isEmpty();
            }

            @Override
            public boolean hasNext() {
                if (ready()) {
                    return true;
                }
                while (p < length) {
                  /**
                   * Fill once.
                   */
                    fillOnce();
                    if (ready()) {
                        return true;
                    }
                }
                if (sb.length() > 0) {
                    buffer.add(NToken.of(NToken.TT_DEFAULT, sb.toString(), 0, 0, sb.toString(), TT_DEFAULT_STR));
                    sb.setLength(0);
                }
                /**
                 * Ready.
                 *
                 * @return ready result
                 */
                return ready();
            }

            /**
             * Fill once.
             *
             * @return fill once result
             */
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
                NAssert.requireNamedTrue(ready(), "token ready");
                return buffer.remove(0);
            }
        });

    }


    /**
     * text replacing all $abc and ${abc} vars
     *
     * @param text   text to parse
     * @param mapper mapper function. when returns null, no replacement is performed
     * @return text replacing all $abc and ${abc} vars
     */
    public static String replaceMoustachePlaceHolder(String text, Function<String, String> mapper) {
        if (mapper == null) {
            return "";
        }
        return parseMoustachePlaceHolder(text)
                .map(t -> {
                    switch (t.ttype) {
                        case NToken.TT_MOUSTACHE_START: {
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

    /**
     * Parse moustache place holder.
     *
     * @param text text
     * @return parse moustache place holder result
     */
    public static Stream<NToken> parseMoustachePlaceHolder(String text) {
        final String TT_DEFAULT_STR = NToken.typeString(NToken.TT_DEFAULT);
        final String TT_DOLLAR_BRACE_STR = NToken.typeString(NToken.TT_MOUSTACHE_START);
        /**
         * Iter to stream.
         *
         * @param Iterator<NToken>( iterator<n token>(
         * @return iter to stream result
         */
        return iterToStream(new Iterator<NToken>() {
            final char[] t = (text == null ? new char[0] : text.toCharArray());
            int p = 0;
            final int length = t.length;
            final StringBuilder sb = new StringBuilder(length);
            final StringBuilder n = new StringBuilder(length);
            final StringBuilder ni = new StringBuilder(length);
            final List<NToken> buffer = new ArrayList<>(2);

            /**
             * Ready.
             *
             * @return ready result
             */
            private boolean ready() {
                return !buffer.isEmpty();
            }

            @Override
            public boolean hasNext() {
                if (ready()) {
                    return true;
                }
                while (p < length) {
                  /**
                   * Fill once.
                   */
                    fillOnce();
                    if (ready()) {
                        return true;
                    }
                }
                if (sb.length() > 0) {
                    buffer.add(NToken.of(NToken.TT_DEFAULT, sb.toString(), 0, 0, sb.toString(), TT_DEFAULT_STR));
                    sb.setLength(0);
                }
                /**
                 * Ready.
                 *
                 * @return ready result
                 */
                return ready();
            }

            /**
             * Fill once.
             *
             * @return fill once result
             */
            private void fillOnce() {
                char c = t[p];
                if (c == '{' && p + 1 < length && t[p + 1] == '{') {
                    p += 2;
                    n.setLength(0);
                    ni.setLength(0);
                    ni.append("{{");

                    boolean closed = false;
                    while (p < length) {
                        c = t[p];
                        if (c == '}' && p + 1 < length && t[p + 1] == '}') {
                            ni.append("}}");
                            p += 2; // Advance past both closing braces
                            closed = true;
                            break;
                        } else {
                            // Strip trailing unmatched single '}' if we hit one at the end
                            if (c != '}' || p + 1 < length) {
                                n.append(c);
                            }
                            ni.append(c);
                            p++;
                        }
                    }

                    if (sb.length() > 0) {
                        buffer.add(NToken.of(NToken.TT_DEFAULT, sb.toString(), 0, 0, sb.toString(), TT_DEFAULT_STR));
                        sb.setLength(0);
                    }

                    // Variable name 'n' is cleanly extracted even if unclosed
                    String varName = n.toString();
                    // Clean up any trailing single '}' from unclosed '{{v}'
                    if (!closed && varName.endsWith("}")) {
                        varName = varName.substring(0, varName.length() - 1);
                    }

                    buffer.add(NToken.of(NToken.TT_MOUSTACHE_START, varName, 0, 0, ni.toString(), TT_DOLLAR_BRACE_STR));

                    // Return early since we consumed 'p' in the loop
                    return;
                } else {
                    sb.append(c);
                }
                p++;
            }

            @Override
            public NToken next() {
                NAssert.requireNamedTrue(ready(), "token ready");
                return buffer.remove(0);
            }
        });

    }


    /**
     * Checks if is valid var.
     *
     * @param c c
     * @return is valid var result
     */
    public static boolean isValidVar(String c) {
        if (c == null || c.isEmpty()) return false;
        char[] charArray = c.toCharArray();
        if (!isValidVarPart(charArray[0])) return false;
        for (int i = 0; i < charArray.length; i++) {
            if (!isValidVarPart(charArray[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if is valid var part.
     *
     * @param c c
     * @return is valid var part result
     */
    public static boolean isValidVarPart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
    }

    /**
     * Checks if is valid var start.
     *
     * @param c c
     * @return is valid var start result
     */
    public static boolean isValidVarStart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    /**
     * Iter to stream.
     *
     * @param it it
     * @return iter to stream result
     */
    private static <T> Stream<T> iterToStream(Iterator<T> it) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, 0), false);
    }

    /**
     * Converts to string or empty.
     *
     * @param any any
     * @return to string or empty result
     */
    public static String toStringOrEmpty(Object any) {
        if (any == null) {
            return "";
        }
        return any.toString();
    }

    /**
     * Stacktrace array.
     *
     * @param th th
     * @return stacktrace array result
     */
    public static String[] stacktraceArray(Throwable th) {
        try {
            StringWriter sw = new StringWriter();
          /**
           * Try.
           *
           * @param PrintWriter(sw) print writer(sw)
           */
            try (PrintWriter pw = new PrintWriter(sw)) {
                th.printStackTrace(pw);
            }
            BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
            List<String> s = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                s.add(line);
            }
            return s.toArray(new String[0]);
        } catch (Exception ex) {
            // ignore
        }
        return new String[0];
    }

    /**
     * Stacktrace.
     *
     * @param th th
     * @return stacktrace result
     */
    public static String stacktrace(Throwable th) {
        try {
            StringWriter sw = new StringWriter();
          /**
           * Try.
           *
           * @param PrintWriter(sw) print writer(sw)
           */
            try (PrintWriter pw = new PrintWriter(sw)) {
                th.printStackTrace(pw);
            }
            return sw.toString();
        } catch (Exception ex) {
            // ignore
        }
        return "";
    }

    /**
     * Last index of.
     *
     * @param string string
     * @param chars chars
     * @return last index of result
     */
    public static int lastIndexOf(String string, char[] chars) {
        if (string == null || chars == null || chars.length == 0) {
            return -1;
        }
        char[] value = string.toCharArray();
        for (int i = value.length - 1; i >= 0; i--) {
            for (char aChar : chars) {
                if (value[i] == aChar) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Index of.
     *
     * @param string string
     * @param chars chars
     * @return index of result
     */
    public static int indexOf(String string, char[] chars) {
        if (string == null || chars == null || chars.length == 0) {
            return -1;
        }
        char[] value = string.toCharArray();
        for (int i = 0; i < value.length; i++) {
            for (char aChar : chars) {
                if (value[i] == aChar) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Levenshtein closest.
     *
     * @param threshold threshold
     * @param str1 str1
     * @param dictionary dictionary
     * @return levenshtein closest result
     */
    public static String levenshteinClosest(double threshold, String str1, String... dictionary) {
        if (threshold > 1) {
            threshold = 1;
        }
        if (threshold < 0) {
            threshold = 0;
        }
        double bestRelativeDistance = -1;
        String bestResult = null;
        if (str1 == null) {
            str1 = "";
        }
        for (String s : dictionary) {
            if (s == null) {
                s = "";
            }
            int l = Math.max(s.length(), str1.length());
            int u = levenshteinDistance(str1, s);
            double relativeDistance = (l == 0) ? (u == 0 ? 0 : 1) : ((double) u) / l;
            if (relativeDistance >= threshold) {
                if (bestResult == null || relativeDistance < bestRelativeDistance) {
                    bestResult = s;
                    bestRelativeDistance = relativeDistance;
                }
            }
        }
        return bestResult;
    }

    /**
     * Levenshtein distance.
     *
     * @param str1 str1
     * @param str2 str2
     * @return levenshtein distance result
     */
    public static int levenshteinDistance(String str1, String str2) {
        if (str1 == null) {
            str1 = "";
        }
        if (str2 == null) {
            str2 = "";
        }
        if (str1.isEmpty()) {
            return str2.length();
        }
        if (str2.isEmpty()) {
            return str1.length();
        }
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
        for (int i = 0; i <= str1.length(); i++) {
            for (int j = 0; j <= str2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int v1 = dp[i - 1][j - 1] + (str1.charAt(i - 1) == str2.charAt(j - 1) ? 0 : 1);
                    int v2 = dp[i - 1][j] + 1;
                    int v3 = dp[i][j - 1] + 1;
                    if (v2 < v1) {
                        v1 = v2;
                    }
                    if (v3 < v1) {
                        v1 = v3;
                    }
                    dp[i][j] = v1;
                }
            }
        }
        return dp[str1.length()][str2.length()];
    }

    /**
     * Split lines.
     *
     * @param data data
     * @return split lines result
     */
    public static List<String> splitLines(String data) {
        if (data == null) {
            return new ArrayList<>();
        }
        /**
         * Read lines.
         *
         * @param StringBuilder(data) string builder(data)
         * @return read lines result
         */
        return readLines(new StringBuilder(data));
    }

    /**
     * Read lines.
     *
     * @param data data
     * @return read lines result
     */
    public static List<String> readLines(StringBuilder data) {
        if (data == null) {
            return new ArrayList<>();
        }
        List<String> all = new ArrayList<>();
        while (data.length() > 0) {
            all.add(readLine(data));
        }
        return all;
    }

    /**
     * Read line.
     *
     * @param data data
     * @return read line result
     */
    public static String readLine(StringBuilder data) {
        int i = 0;
        if (data == null) {
            return null;
        }
        while (i < data.length()) {
            char c = data.charAt(i);
            if (c == '\n') {
                if (i == 0) {
                    data.delete(0, i + 1);
                    return "";
                }
                String l = data.substring(0, i);
                data.delete(0, i + 1);
                return l;
            } else if (c == '\r') {
                if (i + 1 < data.length() && data.charAt(i + 1) == '\n') {
                    i++;
                    String l = data.substring(0, i - 1);
                    data.delete(0, i + 1);
                    return l;
                }
                if (i == 0) {
                    data.delete(0, i + 1);
                    return "";
                }
                String l = data.substring(0, i);
                data.delete(0, i + 1);
                return l;
            } else {
                i++;
            }
        }
        String l = data.toString();
        data.setLength(0);
        return l;
    }

    /**
     * Pjoin.
     *
     * @param delimiter delimiter
     * @param items items
     * @return pjoin result
     */
    public static String pjoin(String delimiter, String... items) {
        StringBuilder builder = new StringBuilder();
        if (delimiter == null) {
            delimiter = "";
        }
        if (delimiter.isEmpty()) {
            for (String item : items) {
                if (item != null && !item.isEmpty()) {
                    builder.append(item);
                }
            }
        } else {
            for (String item : items) {
                if (item != null && !item.isEmpty()) {
                    int length = builder.length();
                    if (length == 0) {
                        builder.append(item);
                    } else {
                        boolean o = length > 0 && builder.substring(length - delimiter.length(), length).equals(delimiter);
                        boolean n = item.startsWith(delimiter);
                        if (!o && !n) {
                            builder.append(delimiter);
                            builder.append(item);
                        } else if (o && n) {
                            builder.append(item.substring(delimiter.length()));
                        } else {
                            builder.append(item);
                        }
                    }
                }
            }
        }
        return builder.toString();
    }

    /**
     * Truncate.
     *
     * @param s s
     * @param maxLength max length
     * @return truncate result
     */
    public static String truncate(String s, int maxLength) {
        /**
         * Truncate.
         *
         * @param s s
         * @param maxLength max length
         * @param null null
         * @return truncate result
         */
        return truncate(s, maxLength, null);
    }

    /**
     * Truncate.
     *
     * @param s s
     * @param maxLength max length
     * @param suffix suffix
     * @return truncate result
     */
    public static String truncate(String s, int maxLength, String suffix) {
        if (s == null || maxLength < 0) {
            return s;
        }
        if (s.length() <= maxLength) {
            return s;
        }
        if (NBlankable.isBlank(suffix)) {
            return s.substring(0, maxLength);
        }
        int l2 = maxLength - suffix.length();
        if (l2 >= 0) {
            return s.substring(0, l2) + suffix;
        } else {
            return suffix.substring(0, maxLength);
        }
    }

    /**
     * Replace tail.
     *
     * @param s s
     * @param oldTail old tail
     * @param newTail new tail
     * @return replace tail result
     */
    public static String replaceTail(String s, String oldTail, String newTail) {
        NAssert.requireNamedNonNull(s, "string");
        NAssert.requireNamedNonNull(s, "oldTail");
        NAssert.requireNamedTrue(!oldTail.isEmpty(), "oldTail not empty");
        if (s.endsWith(oldTail)) {
            return s.substring(0, s.length() - oldTail.length()) + (newTail == null ? "" : newTail);
        } else {
            return s;
        }
    }

    /**
     * Replace head.
     *
     * @param s s
     * @param oldHead old head
     * @param newHead new head
     * @return replace head result
     */
    public static String replaceHead(String s, String oldHead, String newHead) {
        NAssert.requireNamedNonNull(s, "string");
        NAssert.requireNamedNonNull(s, "oldTail");
        NAssert.requireNamedTrue(!oldHead.isEmpty(), "oldHead not empty");
        if (s.startsWith(oldHead)) {
            return newHead + s.substring(oldHead.length());
        } else {
            return s;
        }
    }

    /**
     * Strip.
     *
     * @param sb sb
     * @return strip result
     */
    public static StringBuilder strip(StringBuilder sb) {
      /**
       * Strip left.
       *
       * @param sb sb
       */
        stripLeft(sb);
      /**
       * Strip right.
       *
       * @param sb sb
       */
        stripRight(sb);
        return sb;
    }

    /**
     * Strip left.
     *
     * @param sb sb
     * @return strip left result
     */
    public static StringBuilder stripLeft(StringBuilder sb) {
        int len = sb.length();
        int start = 0;
        while (start < len && Character.isWhitespace(sb.charAt(start))) {
            start++;
        }
        if (start > 0) {
            sb.delete(0, start);
        }
        return sb;
    }

    /**
     * Escape char.
     *
     * @param c c
     * @return escape char result
     */
    public static String escapeChar(char c) {
        switch (c) {
            case '\b':
              /**
               * Return.
               *
               * @param "\\b" "\\b"
               */
                return ("\\b");
            case '\t':
                return "\\t";
            case '\n':
              /**
               * Return.
               *
               * @param "\\n" "\\n"
               */
                return ("\\n");
            case '\f':
              /**
               * Return.
               *
               * @param "\\f" "\\f"
               */
                return ("\\f");
            case '\r':
              /**
               * Return.
               *
               * @param "\\r" "\\r"
               */
                return ("\\r");
            case '\\':
              /**
               * Return.
               *
               * @param "\\\\" "\\\\"
               */
                return ("\\\\");
            default: {
                if (c < 0x20 || c > 0x7e) {
                    String s = "0000" + Integer.toString(c, 16);
                  /**
                   * Return.
                   *
                   * @param 4) 4)
                   */
                    return ("\\u" + s.substring(s.length() - 4));
                } else {
                    return String.valueOf(c);
                }
            }
        }
    }

    /**
     * Strip right.
     *
     * @param sb sb
     * @return strip right result
     */
    public static StringBuilder stripRight(StringBuilder sb) {
        int end = sb.length() - 1;
        while (end >= 0 && Character.isWhitespace(sb.charAt(end))) {
            end--;
        }
        if (end < sb.length() - 1) {
            sb.delete(end + 1, sb.length());
        }
        return sb;
    }

    /**
     * Common prefix.
     *
     * @param all all
     * @return common prefix result
     */
    public static String commonPrefix(List<String> all) {
        /**
         * Common prefix.
         *
         * @param all all
         * @param null null
         * @return common prefix result
         */
        return commonPrefix(all, null);
    }

    /**
     * Checks if is java identifier.
     *
     * @param s s
     * @return is java identifier result
     */
    public static boolean isJavaIdentifier(String s) {
        if (s == null) {
            return false;
        }
        if (s.isEmpty()) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(s.charAt(0))) {
            return false;
        }
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * CommonPrefixFilter interface.
     *
     * @author thevpc
     * @since 0.8.0
     */
    public interface CommonPrefixFilter {
        /**
         * Accept.
         *
         * @param buffer buffer
         * @param c c
         * @return accept result
         */
        boolean accept(String buffer, char c);
    }

    /**
     * Common prefix.
     *
     * @param all all
     * @param prefixFilter prefix filter
     * @return common prefix result
     */
    public static String commonPrefix(List<String> all, CommonPrefixFilter prefixFilter) {
        if (all == null || all.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String pivot = all.get(0);
        if (pivot == null) {
            return "";
        }
        for (int i = 0; i < pivot.length(); i++) {
            if (prefixFilter != null && !prefixFilter.accept(sb.toString(), pivot.charAt(i))) {
                break;
            }
            boolean common = true;
            for (int j = 1; j < all.size(); j++) {
                String curr = all.get(j);
                if (curr == null || curr.isEmpty()) {
                    return "";
                }
                if (i >= curr.length() || curr.charAt(i) != curr.charAt(j)) {
                    common = false;
                    break;
                }
            }
            if (common) {
                sb.append(pivot.charAt(i));
            } else {
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Common whitespace prefix.
     *
     * @param all all
     * @return common whitespace prefix result
     */
    public static String commonWhitespacePrefix(List<String> all) {
        /**
         * Common prefix.
         *
         * @param all all
         * @param Character.isWhitespace(c) character.is whitespace(c)
         * @return common prefix result
         */
        return commonPrefix(all, (b, c) -> Character.isWhitespace(c));
    }

    /**
     * Convert char[] to UTF-8 bytes.
     * Caller MUST zero the returned byte array after use.
     */
    public static byte[] charsToUtf8Bytes(char[] chars) {
        if (chars == null || chars.length == 0) {
            return new byte[0];
        }
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer;
        try {
            byteBuffer = encoder.encode(charBuffer);
        } catch (CharacterCodingException e) {
            /**
             * Illegal argument exception.
             *
             * @param sequence" sequence"
             * @param e e
             * @return illegal argument exception result
             */
            throw new IllegalArgumentException("Invalid UTF-8 sequence", e);
        }
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return bytes;
        // WARNING: Returns NEW array — caller must zero it!
    }

    /**
     * Convert UTF-8 bytes to char[].
     * Caller MUST zero the returned char array after use.
     */
    public static char[] utf8BytesToChars(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return new char[0];
        }
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        CharBuffer charBuffer;
        try {
            charBuffer = decoder.decode(byteBuffer);
        } catch (CharacterCodingException e) {
            /**
             * Illegal argument exception.
             *
             * @param sequence" sequence"
             * @param e e
             * @return illegal argument exception result
             */
            throw new IllegalArgumentException("Invalid UTF-8 sequence", e);
        }
        char[] chars = new char[charBuffer.remaining()];
        charBuffer.get(chars);
        return chars;
    }
}
