package net.thevpc.nuts.util;

import net.thevpc.nuts.io.NStringWriter;
import net.thevpc.nuts.pipeline.NStream;

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * A mutable sequence of characters with extensive utility methods,
 * similar to {@link StringBuilder} but with additional conveniences for
 * indentation, line handling, pattern replacement, and charset operations.
 *
 * <p>Implements {@link CharSequence} and {@link NBlankable}. All index‑based methods
 * support negative indexing (e.g., {@code -1} refers to the last character).
 *
 * <p>Many operations return the builder itself, allowing fluent chaining.
 *
 * <p>Examples:
 * <pre>{@code
 * NStringBuilder sb = NStringBuilder.of();
 * sb.println("Hello").append("world").indent("  ");
 * System.out.println(sb); // prints "  Hello\n  world"
 *
 * // Negative indexing
 * sb.setCharAt(-1, '!');  // changes last character to '!'
 * }</pre>
 *
 * @see StringBuilder
 * @see NBlankable
 */
public class NStringBuilder implements CharSequence, NBlankable {
    private StringBuilder data;

    /**
     * N string builder.
     *
     * @return n string builder result
     */
    public NStringBuilder() {
        data = new StringBuilder();
    }

    /**
     * N string builder.
     *
     * @param value value
     * @return n string builder result
     */
    public NStringBuilder(String value) {
        data = new StringBuilder(value == null ? "" : value);
    }

    /**
     * N string builder.
     *
     * @param value value
     * @return n string builder result
     */
    public NStringBuilder(CharSequence value) {
        data = value == null ? new StringBuilder() : new StringBuilder(value);
    }

    /**
     * N string builder.
     *
     * @param capacity capacity
     * @return n string builder result
     */
    public NStringBuilder(int capacity) {
        data = new StringBuilder(capacity);
    }

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    public static NStringBuilder of() {
        return new NStringBuilder();
    }

    /**
     * As string writer.
     *
     * @return as string writer result
     */
    public NStringWriter asStringWriter() {
        return new NStringWriter() {
            @Override
            public void write(char text) {
                NStringBuilder.this.append(text);
            }

            @Override
            public void write(String text) {
                NStringBuilder.this.append(text);
            }

            @Override
            public void write(char[] text, int offset, int len) {
                NStringBuilder.this.append(text, offset, len);
            }
        };
    }

    /**
     * Append.
     *
     * @param value value
     * @return append result
     */
    public NStringBuilder append(String value) {
        data.append(value);
        return this;
    }

    /**
     * Append.
     *
     * @param value value
     * @return append result
     */
    public NStringBuilder append(Object value) {
        data.append(value);
        return this;
    }

    /**
     * Append.
     *
     * @param value value
     * @return append result
     */
    public NStringBuilder append(int value) {
        data.append(value);
        return this;
    }

    /**
     * Append.
     *
     * @param value value
     * @return append result
     */
    public NStringBuilder append(boolean value) {
        data.append(value);
        return this;
    }

    /**
     * Append.
     *
     * @param value value
     * @return append result
     */
    public NStringBuilder append(byte value) {
        data.append(value);
        return this;
    }

    /**
     * Append.
     *
     * @param value value
     * @return append result
     */
    public NStringBuilder append(short value) {
        data.append(value);
        return this;
    }

    /**
     * Append.
     *
     * @param value value
     * @return append result
     */
    public NStringBuilder append(float value) {
        data.append(value);
        return this;
    }

    /**
     * Append.
     *
     * @param value value
     * @return append result
     */
    public NStringBuilder append(double value) {
        data.append(value);
        return this;
    }

    /**
     * Append.
     *
     * @param value value
     * @return append result
     */
    public NStringBuilder append(char value) {
        data.append(value);
        return this;
    }

    /**
     * Insert.
     *
     * @param offset offset
     * @param value value
     * @return insert result
     */
    public NStringBuilder insert(int offset, String value) {
        data.insert(offset, value);
        return this;
    }

    /**
     * Insert.
     *
     * @param offset offset
     * @param value value
     * @return insert result
     */
    public NStringBuilder insert(int offset, Object value) {
        data.insert(offset, value);
        return this;
    }

    /**
     * Insert.
     *
     * @param offset offset
     * @param value value
     * @return insert result
     */
    public NStringBuilder insert(int offset, int value) {
        data.insert(offset, value);
        return this;
    }

    /**
     * Insert.
     *
     * @param offset offset
     * @param value value
     * @return insert result
     */
    public NStringBuilder insert(int offset, boolean value) {
        data.insert(offset, value);
        return this;
    }

    /**
     * Insert.
     *
     * @param offset offset
     * @param value value
     * @return insert result
     */
    public NStringBuilder insert(int offset, byte value) {
        data.insert(offset, value);
        return this;
    }

    /**
     * Insert.
     *
     * @param offset offset
     * @param value value
     * @return insert result
     */
    public NStringBuilder insert(int offset, short value) {
        data.insert(offset, value);
        return this;
    }

    /**
     * Insert.
     *
     * @param offset offset
     * @param value value
     * @return insert result
     */
    public NStringBuilder insert(int offset, float value) {
        data.insert(offset, value);
        return this;
    }

    /**
     * Insert.
     *
     * @param offset offset
     * @param value value
     * @return insert result
     */
    public NStringBuilder insert(int offset, double value) {
        data.insert(offset, value);
        return this;
    }

    /**
     * Insert.
     *
     * @param offset offset
     * @param value value
     * @return insert result
     */
    public NStringBuilder insert(int offset, char value) {
        data.insert(offset, value);
        return this;
    }

    /**
     * Insert first.
     *
     * @param value value
     * @return insert first result
     */
    public NStringBuilder insertFirst(Object value) {
        data.insert(0, value);
        return this;
    }

    /**
     * Insert first.
     *
     * @param value value
     * @return insert first result
     */
    public NStringBuilder insertFirst(int value) {
        data.insert(0, value);
        return this;
    }

    /**
     * Insert first.
     *
     * @param value value
     * @return insert first result
     */
    public NStringBuilder insertFirst(boolean value) {
        data.insert(0, value);
        return this;
    }

    /**
     * Insert first.
     *
     * @param value value
     * @return insert first result
     */
    public NStringBuilder insertFirst(byte value) {
        data.insert(0, value);
        return this;
    }

    /**
     * Insert first.
     *
     * @param value value
     * @return insert first result
     */
    public NStringBuilder insertFirst(short value) {
        data.insert(0, value);
        return this;
    }

    /**
     * Insert first.
     *
     * @param value value
     * @return insert first result
     */
    public NStringBuilder insertFirst(float value) {
        data.insert(0, value);
        return this;
    }

    /**
     * Insert first.
     *
     * @param value value
     * @return insert first result
     */
    public NStringBuilder insertFirst(double value) {
        data.insert(0, value);
        return this;
    }

    /**
     * Insert first.
     *
     * @param value value
     * @return insert first result
     */
    public NStringBuilder insertFirst(char value) {
        data.insert(0, value);
        return this;
    }

    /**
     * Length.
     *
     * @return length result
     */
    public int length() {
        return data.length();
    }

    /**
     * Checks if is empty.
     *
     * @return is empty result
     */
    public boolean isEmpty() {
        return data.length() == 0;
    }

    /**
     * Wise index.
     *
     * @param index index
     * @param length length
     * @return wise index result
     */
    private int wiseIndex(int index, int length) {
        if (index < 0) {
            int rIndex = length + index;
            if (rIndex >= 0 && rIndex < length) {
                return rIndex;
            }
        }
        return index;
    }

    /**
     * Wise index.
     *
     * @param index index
     * @return wise index result
     */
    private int wiseIndex(int index) {
        /**
         * Wise index.
         *
         * @param index index
         * @param data.length() data.length()
         * @return wise index result
         */
        return wiseIndex(index, data.length());
    }

    /**
     * Wise index other.
     *
     * @param index index
     * @param other other
     * @return wise index other result
     */
    private int wiseIndexOther(int index, CharSequence other) {
        /**
         * Wise index.
         *
         * @param index index
         * @param other.length() other.length()
         * @return wise index result
         */
        return wiseIndex(index, other.length());
    }

    /**
     * Wise index other.
     *
     * @param index index
     * @param other other
     * @return wise index other result
     */
    private int wiseIndexOther(int index, char[] other) {
        /**
         * Wise index.
         *
         * @param index index
         * @param other.length other.length
         * @return wise index result
         */
        return wiseIndex(index, other.length);
    }

    /**
     * Wise index other.
     *
     * @param index index
     * @param other other
     * @return wise index other result
     */
    private int wiseIndexOther(int index, byte[] other) {
        /**
         * Wise index.
         *
         * @param index index
         * @param other.length other.length
         * @return wise index result
         */
        return wiseIndex(index, other.length);
    }

    /**
     * Char at.
     *
     * @param index index
     * @return char at result
     */
    public char charAt(int index) {
        return data.charAt(wiseIndex(index));
    }

    /**
     * Index of.
     *
     * @param str str
     * @return index of result
     */
    public int indexOf(String str) {
        return data.indexOf(str);
    }

    /**
     * Index of.
     *
     * @param str str
     * @param fromIndex from index
     * @return index of result
     */
    public int indexOf(String str, int fromIndex) {
        return data.indexOf(str, wiseIndex(fromIndex));
    }

    /**
     * Last index of.
     *
     * @param str str
     * @return last index of result
     */
    public int lastIndexOf(String str) {
        return data.lastIndexOf(str);
    }

    /**
     * Last index of.
     *
     * @param str str
     * @param fromIndex from index
     * @return last index of result
     */
    public int lastIndexOf(String str, int fromIndex) {
        return data.lastIndexOf(str, wiseIndex(fromIndex));
    }

    /**
     * Index of.
     *
     * @param str str
     * @return index of result
     */
    public int indexOf(CharSequence str) {
        return data.indexOf(str.toString());
    }

    /**
     * Index of.
     *
     * @param str str
     * @param fromIndex from index
     * @return index of result
     */
    public int indexOf(CharSequence str, int fromIndex) {
        return data.indexOf(str.toString(), wiseIndex(fromIndex));
    }

    /**
     * Last index of.
     *
     * @param str str
     * @return last index of result
     */
    public int lastIndexOf(CharSequence str) {
        return data.lastIndexOf(str.toString());
    }

    /**
     * Last index of.
     *
     * @param str str
     * @param fromIndex from index
     * @return last index of result
     */
    public int lastIndexOf(CharSequence str, int fromIndex) {
        return data.lastIndexOf(str.toString(), fromIndex);
    }

    /**
     * Index of.
     *
     * @param str str
     * @return index of result
     */
    public int indexOf(char[] str) {
        return data.indexOf(new String(str));
    }

    /**
     * Index of.
     *
     * @param str str
     * @param fromIndex from index
     * @return index of result
     */
    public int indexOf(char[] str, int fromIndex) {
        return data.indexOf(new String(str), wiseIndex(fromIndex));
    }

    /**
     * Last index of.
     *
     * @param str str
     * @return last index of result
     */
    public int lastIndexOf(char[] str) {
        return data.lastIndexOf(new String(str));
    }

    /**
     * Last index of.
     *
     * @param str str
     * @param fromIndex from index
     * @return last index of result
     */
    public int lastIndexOf(char[] str, int fromIndex) {
        return data.lastIndexOf(new String(str), wiseIndex(fromIndex));
    }

    /**
     * Println.
     *
     * @param str str
     * @return println result
     */
    public NStringBuilder println(Object str) {
        /**
         * Println.
         *
         * @param String.valueOf(str) string.value of(str)
         * @return println result
         */
        return println(String.valueOf(str));
    }

    /**
     * Println.
     *
     * @return println result
     */
    public NStringBuilder println() {
      /**
       * New line.
       */
        newLine();
        return this;
    }

    /**
     * Println.
     *
     * @param str str
     * @return println result
     */
    public NStringBuilder println(String str) {
      /**
       * Append.
       *
       * @param str str
       */
        append(str);
      /**
       * New line.
       */
        newLine();
        return this;
    }

    /**
     * Replace.
     *
     * @param start start
     * @param end end
     * @param str str
     * @return replace result
     */
    public NStringBuilder replace(int start, int end, String str) {
        data.replace(wiseIndex(start), wiseIndex(end), str);
        return this;
    }

    /**
     * Checks if is blank.
     *
     * @return is blank result
     */
    public boolean isBlank() {
        return NBlankable.isBlank(data.toString());
    }

    /**
     * Reverse.
     *
     * @return reverse result
     */
    public NStringBuilder reverse() {
        data.reverse();
        return this;
    }

    /**
     * Clear.
     *
     * @return clear result
     */
    public NStringBuilder clear() {
        data.setLength(0);
        return this;
    }

    /**
     * Delete char at.
     *
     * @param index index
     * @return delete char at result
     */
    public NStringBuilder deleteCharAt(int index) {
        data.deleteCharAt(wiseIndex(index));
        return this;
    }

    /**
     * Delete.
     *
     * @param start start
     * @param end end
     * @return delete result
     */
    public NStringBuilder delete(int start, int end) {
        data.delete(wiseIndex(start), wiseIndex(end));
        return this;
    }

    /**
     * Returns the get.
     *
     * @param index index
     * @return get result
     */
    public char get(int index) {
        return data.charAt(wiseIndex(index));
    }

    /**
     * First.
     *
     * @return first result
     */
    public char first() {
        return data.charAt(0);
    }

    /**
     * Last.
     *
     * @return last result
     */
    public char last() {
        return data.charAt(data.length());
    }

    /**
     * Removes the specified first.
     *
     * @return remove first result
     */
    public char removeFirst() {
        char c = data.charAt(0);
        data.deleteCharAt(0);
        return c;
    }

    /**
     * Removes the specified last.
     *
     * @return remove last result
     */
    public char removeLast() {
        int len = data.length();
        char c = data.charAt(len - 1);
        data.deleteCharAt(len - 1);
        return c;
    }

    /**
     * Build.
     *
     * @return build result
     */
    public String build() {
        return data.toString();
    }

    @Override
    public String toString() {
        return data.toString();
    }

    /**
     * Converts to char array.
     *
     * @return to char array result
     */
    public char[] toCharArray() {
        int length = length();
        char[] c = new char[length];
        data.getChars(
                0, length, c, 0
        );
        return c;
    }

    /**
     * Copy.
     *
     * @return copy result
     */
    public NStringBuilder copy() {
        return new NStringBuilder().append(data);
    }

    /**
     * Strip.
     *
     * @return strip result
     */
    public NStringBuilder strip() {
        int len0 = length();
        int len = len0;
        int st = 0;
        char[] val = toString().toCharArray();    /* avoid getfield opcode */

        while ((st < len) && Character.isWhitespace(val[st])) {
            st++;
        }
        while ((st < len) && Character.isWhitespace(val[len - 1])) {
            len--;
        }
        if (((st > 0) || (len < len0))) {
            String v = data.substring(st, len);
          /**
           * Sets the content.
           *
           * @param v v
           */
            setContent(v);
        }
        return this;
    }

    /**
     * Substring.
     *
     * @param start start
     * @param end end
     * @return substring result
     */
    public String substring(int start, int end) {
        return data.substring(wiseIndex(start), wiseIndex(end));
    }

    /**
     * Head.
     *
     * @param size size
     * @return head result
     */
    public String head(int size) {
        return data.substring(0, size);
    }

    /**
     * Tail.
     *
     * @param size size
     * @return tail result
     */
    public String tail(int size) {
        int length = data.length();
        return data.substring(length - size, length);
    }

    /**
     * Starts with.
     *
     * @param other other
     * @return starts with result
     */
    public boolean startsWith(String other) {
        int olength = other.length();
        /**
         * Length.
         *
         * @param olength).equals(other olength).equals(other
         * @return length result
         */
        return length() > olength && subSequence(0, olength).equals(other);
    }

    /**
     * Starts with.
     *
     * @param other other
     * @param toffset toffset
     * @return starts with result
     */
    public boolean startsWith(String other, int toffset) {
        toffset = wiseIndex(toffset);
        int olength = other.length();
        int length = length() - toffset;
        return length >= olength && subSequence(toffset, toffset + olength).equals(other);
    }

    /**
     * Start with.
     *
     * @param other other
     * @return start with result
     */
    public boolean startWith(char other) {
        int length = length();
      /**
       * Return.
       *
       * @param other other
       */
        return (length > 0 && charAt(0) == other);
    }

    /**
     * Contains.
     *
     * @param other other
     * @return contains result
     */
    public boolean contains(char other) {
        return indexOf(other) >= 0;
    }

    /**
     * Index of.
     *
     * @param other other
     * @return index of result
     */
    public int indexOf(char other) {
        return data.indexOf(String.valueOf(other));
    }

    /**
     * Ends with.
     *
     * @param other other
     * @return ends with result
     */
    public boolean endsWith(char other) {
        int length = length();
      /**
       * Return.
       *
       * @param other other
       */
        return (length > 0 && charAt(length - 1) == other);
    }

    /**
     * Ends with.
     *
     * @param other other
     * @return ends with result
     */
    public boolean endsWith(String other) {
        int length = length();
        int olength = other.length();
        return length >= olength && subSequence(length - olength, length).equals(other);
    }

    public boolean regionMatches(boolean ignoreCase, int toffset,
                                 String other, int ooffset, int len) {
        return data.toString().regionMatches(ignoreCase, wiseIndex(toffset), other, wiseIndexOther(ooffset, other), len);
    }

    public boolean regionMatches(int toffset, String other, int ooffset,
                                 int len) {
        return data.toString().regionMatches(wiseIndex(toffset), other, wiseIndexOther(ooffset, other), len);
    }

    /**
     * Sets the upper case at.
     *
     * @param index index
     * @return set upper case at result
     */
    public NStringBuilder setUpperCaseAt(int index) {
        index = wiseIndex(index);
        data.setCharAt(index,
                Character.toUpperCase(data.charAt(index))
        );
        return this;
    }

    /**
     * Sets the lower case at.
     *
     * @param index index
     * @return set lower case at result
     */
    public NStringBuilder setLowerCaseAt(int index) {
        index = wiseIndex(index);
        data.setCharAt(index,
                Character.toLowerCase(data.charAt(index))
        );
        return this;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return data.subSequence(wiseIndex(start), wiseIndex(end));
    }

    @Override
    public IntStream chars() {
        return data.chars();
    }

    @Override
    public IntStream codePoints() {
        return data.codePoints();
    }

    /**
     * Append.
     *
     * @param value value
     * @return append result
     */
    public NStringBuilder append(CharSequence value) {
        data.append(value);
        return this;
    }

    /**
     * Insert.
     *
     * @param index index
     * @param value value
     * @return insert result
     */
    public NStringBuilder insert(int index, CharSequence value) {
        data.insert(wiseIndex(index), value);
        return this;
    }

    /**
     * Insert first.
     *
     * @param value value
     * @return insert first result
     */
    public NStringBuilder insertFirst(CharSequence value) {
        data.insert(0, value);
        return this;
    }

    /**
     * Append.
     *
     * @param value value
     * @param from from
     * @param to to
     * @return append result
     */
    public NStringBuilder append(CharSequence value, int from, int to) {
        data.append(value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

    /**
     * Insert.
     *
     * @param index index
     * @param value value
     * @param from from
     * @param to to
     * @return insert result
     */
    public NStringBuilder insert(int index, CharSequence value, int from, int to) {
        data.insert(index, value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

    /**
     * Insert first.
     *
     * @param value value
     * @param from from
     * @param to to
     * @return insert first result
     */
    public NStringBuilder insertFirst(CharSequence value, int from, int to) {
        data.insert(0, value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

    /**
     * Append.
     *
     * @param value value
     * @param from from
     * @param to to
     * @return append result
     */
    public NStringBuilder append(char[] value, int from, int to) {
        data.append(value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

    /**
     * Insert.
     *
     * @param index index
     * @param value value
     * @param from from
     * @param to to
     * @return insert result
     */
    public NStringBuilder insert(int index, char[] value, int from, int to) {
        data.insert(wiseIndex(index), value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

    /**
     * Insert first.
     *
     * @param value value
     * @param from from
     * @param to to
     * @return insert first result
     */
    public NStringBuilder insertFirst(char[] value, int from, int to) {
        data.insert(0, value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

    /**
     * Append.
     *
     * @param value value
     * @param from from
     * @param to to
     * @return append result
     */
    public NStringBuilder append(StringBuilder value, int from, int to) {
        data.append(value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

    /**
     * Insert.
     *
     * @param index index
     * @param value value
     * @param from from
     * @param to to
     * @return insert result
     */
    public NStringBuilder insert(int index, StringBuilder value, int from, int to) {
        data.insert(wiseIndex(index), value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

    /**
     * Insert first.
     *
     * @param value value
     * @param from from
     * @param to to
     * @return insert first result
     */
    public NStringBuilder insertFirst(StringBuilder value, int from, int to) {
        data.insert(0, value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NStringBuilder that = (NStringBuilder) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    /**
     * Index of.
     *
     * @param ch ch
     * @param fromIndex from index
     * @return index of result
     */
    public int indexOf(int ch, int fromIndex) {
        return data.indexOf(String.valueOf(ch), wiseIndex(fromIndex));
    }

    /**
     * Last index of.
     *
     * @param ch ch
     * @param fromIndex from index
     * @return last index of result
     */
    public int lastIndexOf(int ch, int fromIndex) {
        return data.lastIndexOf(String.valueOf(ch), wiseIndex(fromIndex));
    }

    /**
     * Code point at.
     *
     * @param index index
     * @return code point at result
     */
    public int codePointAt(int index) {
        return data.codePointAt(wiseIndex(index));
    }

    /**
     * Code point before.
     *
     * @param index index
     * @return code point before result
     */
    public int codePointBefore(int index) {
        return data.codePointBefore(wiseIndex(index));
    }

    /**
     * Code point count.
     *
     * @param begin begin
     * @param end end
     * @return code point count result
     */
    public int codePointCount(int begin, int end) {
        return data.codePointCount(wiseIndex(begin), wiseIndex(end));
    }

    /**
     * Creates a new instance of offset by code points.
     *
     * @param index index
     * @param codePointOffset code point offset
     * @return offset by code points result
     */
    public int offsetByCodePoints(int index, int codePointOffset) {
        return data.offsetByCodePoints(wiseIndex(index), codePointOffset);
    }

    /**
     * Returns the chars.
     *
     * @param srcBegin src begin
     * @param srcEnd src end
     * @param dst dst
     * @param dstBegin dst begin
     */
    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        data.getChars(wiseIndex(srcBegin), wiseIndex(srcEnd), dst, wiseIndexOther(dstBegin, dst));
    }

    /**
     * Sets the char at.
     *
     * @param index index
     * @param ch ch
     */
    public void setCharAt(int index, char ch) {
        data.setCharAt(wiseIndex(index), ch);
    }

    /**
     * Substring.
     *
     * @param start start
     * @return substring result
     */
    public String substring(int start) {
        return data.substring(wiseIndex(start));
    }

    /**
     * Returns the bytes.
     *
     * @param srcBegin src begin
     * @param srcEnd src end
     * @param dst dst
     * @param dstBegin dst begin
     */
    public void getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin) {
        data.toString().getBytes(wiseIndex(srcBegin), wiseIndex(srcEnd), dst, wiseIndexOther(dstBegin, dst));
    }

    /**
     * Returns the bytes.
     *
     * @param charsetName charset name
     * @return get bytes result
     */
    public byte[] getBytes(String charsetName) {
        try {
            return data.toString().getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            /**
             * Unchecked io exception.
             *
             * @param e e
             * @return unchecked io exception result
             */
            throw new UncheckedIOException(e);
        }
    }

    /**
     * remain getBytes() (not byte()) to stay compatible with java StringBuilder
     * @return
     */
    public byte[] getBytes() {
        return data.toString().getBytes();
    }

    /**
     * Returns the bytes.
     *
     * @param charset charset
     * @return get bytes result
     */
    public byte[] getBytes(Charset charset) {
        return data.toString().getBytes(charset);
    }

    /**
     * Content equals.
     *
     * @param sb sb
     * @return content equals result
     */
    public boolean contentEquals(StringBuffer sb) {
        return sb != null && toString().equals(sb.toString());
    }

    /**
     * Content equals.
     *
     * @param sb sb
     * @return content equals result
     */
    public boolean contentEquals(CharSequence sb) {
        return sb != null && toString().equals(sb.toString());
    }

    /**
     * Content equals.
     *
     * @param sb sb
     * @return content equals result
     */
    public boolean contentEquals(String sb) {
        return sb != null && toString().equals(sb.toString());
    }

    /**
     * Content equals.
     *
     * @param chars chars
     * @return content equals result
     */
    public boolean contentEquals(char[] chars) {
        return chars != null && toString().equals(new String(chars));
    }

    /**
     * Equals ignore case.
     *
     * @param anotherString another string
     * @return equals ignore case result
     */
    public boolean equalsIgnoreCase(String anotherString) {
        return anotherString != null && toString().equals(anotherString);
    }

    /**
     * Equals ignore case.
     *
     * @param anotherString another string
     * @return equals ignore case result
     */
    public boolean equalsIgnoreCase(CharSequence anotherString) {
        return anotherString != null && toString().equals(anotherString.toString());
    }

    /**
     * Equals ignore case.
     *
     * @param anotherString another string
     * @return equals ignore case result
     */
    public boolean equalsIgnoreCase(char[] anotherString) {
        return anotherString != null && toString().equals(new String(anotherString));
    }

    /**
     * Compare to.
     *
     * @param anotherString another string
     * @return compare to result
     */
    public int compareTo(String anotherString) {
        return anotherString == null ? 1 : toString().compareTo(anotherString);
    }

    /**
     * Compare to.
     *
     * @param anotherString another string
     * @return compare to result
     */
    public int compareTo(char[] anotherString) {
        return anotherString == null ? 1 : toString().compareTo(new String(anotherString));
    }

    /**
     * Compare to.
     *
     * @param anotherString another string
     * @return compare to result
     */
    public int compareTo(CharSequence anotherString) {
        return anotherString == null ? 1 : toString().compareTo(anotherString.toString());
    }

    /**
     * Compare to ignore case.
     *
     * @param anotherString another string
     * @return compare to ignore case result
     */
    public int compareToIgnoreCase(String anotherString) {
        return anotherString == null ? 1 : toString().compareToIgnoreCase(anotherString);
    }

    /**
     * Compare to ignore case.
     *
     * @param anotherString another string
     * @return compare to ignore case result
     */
    public int compareToIgnoreCase(char[] anotherString) {
        return anotherString == null ? 1 : toString().compareToIgnoreCase(new String(anotherString));
    }

    /**
     * Compare to ignore case.
     *
     * @param anotherString another string
     * @return compare to ignore case result
     */
    public int compareToIgnoreCase(CharSequence anotherString) {
        return anotherString == null ? 1 : toString().compareToIgnoreCase(anotherString.toString());
    }

    /**
     * Replace.
     *
     * @param oldChar old char
     * @param newChar new char
     * @return replace result
     */
    public NStringBuilder replace(char oldChar, char newChar) {
        char[] chars = toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == oldChar) {
                data.setCharAt(i, newChar);
            }
        }
        return this;
    }

    /**
     * Matches.
     *
     * @param regex regex
     * @return matches result
     */
    public boolean matches(String regex) {
        return toString().matches(regex);
    }

    /**
     * Contains.
     *
     * @param s s
     * @return contains result
     */
    public boolean contains(CharSequence s) {
        return indexOf(s) >= 0;
    }

    /**
     * Contains.
     *
     * @param s s
     * @return contains result
     */
    public boolean contains(char[] s) {
        return indexOf(s) >= 0;
    }

    /**
     * Replace first.
     *
     * @param regex regex
     * @param replacement replacement
     * @return replace first result
     */
    public NStringBuilder replaceFirst(String regex, String replacement) {
        String s = Pattern.compile(regex).matcher(this).replaceFirst(replacement);
        if (!s.equals(data.toString())) {
          /**
           * Sets the content.
           *
           * @param s s
           */
            setContent(s);
        }
        return this;
    }

    /**
     * Sets the content.
     *
     * @param s s
     * @return set content result
     */
    public NStringBuilder setContent(String s) {
        data.setLength(0);
        data.append(s);
        return this;
    }

    /**
     * Replace all.
     *
     * @param regex regex
     * @param replacement replacement
     * @return replace all result
     */
    public NStringBuilder replaceAll(String regex, String replacement) {
        /**
         * Sets the content.
         *
         * @param Pattern.compile(regex).matcher(this).replaceAll(replacement) pattern.compile(regex).matcher(this).replace all(replacement)
         * @return set content result
         */
        return setContent(Pattern.compile(regex).matcher(this).replaceAll(replacement));
    }

    /**
     * Replace.
     *
     * @param target target
     * @param replacement replacement
     * @return replace result
     */
    public NStringBuilder replace(CharSequence target, CharSequence replacement) {
        /**
         * Sets the content.
         *
         * @param replacement) replacement)
         * @return set content result
         */
        return setContent(toString().replace(target, replacement));
    }

    /**
     * Split.
     *
     * @param regex regex
     * @param limit limit
     * @return split result
     */
    public String[] split(String regex, int limit) {
        return toString().split(regex, limit);
    }

    /**
     * Split.
     *
     * @param regex regex
     * @return split result
     */
    public String[] split(String regex) {
        return toString().split(regex);
    }

    /**
     * Converts to lower case.
     *
     * @param locale locale
     * @return to lower case result
     */
    public NStringBuilder toLowerCase(Locale locale) {
        /**
         * Sets the content.
         *
         * @param toString().toLowerCase(locale) to string().to lower case(locale)
         * @return set content result
         */
        return setContent(toString().toLowerCase(locale));
    }

    /**
     * Converts to lower case.
     *
     * @return to lower case result
     */
    public NStringBuilder toLowerCase() {
        /**
         * Sets the content.
         *
         * @param toString().toLowerCase() to string().to lower case()
         * @return set content result
         */
        return setContent(toString().toLowerCase());
    }

    /**
     * Converts to upper case.
     *
     * @param locale locale
     * @return to upper case result
     */
    public NStringBuilder toUpperCase(Locale locale) {
        /**
         * Sets the content.
         *
         * @param toString().toUpperCase(locale) to string().to upper case(locale)
         * @return set content result
         */
        return setContent(toString().toUpperCase(locale));
    }

    /**
     * Converts to upper case.
     *
     * @return to upper case result
     */
    public NStringBuilder toUpperCase() {
        /**
         * Sets the content.
         *
         * @param toString().toUpperCase() to string().to upper case()
         * @return set content result
         */
        return setContent(toString().toUpperCase());
    }

    /**
     * Converts to name format.
     *
     * @param format format
     * @return to name format result
     */
    public NStringBuilder toNameFormat(NNameFormat format) {
        NAssert.requireNamedNonNull(format, "format");
        /**
         * Sets the content.
         *
         * @param format.format(toString()) format.format(to string())
         * @return set content result
         */
        return setContent(format.format(toString()));
    }

    /**
     * Removes the specified all.
     *
     * @return remove all result
     */
    public String removeAll() {
        String s = data.toString();
        data.setLength(0);
        return s;
    }

    /**
     * Ensure capacity.
     *
     * @param minimumCapacity minimum capacity
     * @return ensure capacity result
     */
    public NStringBuilder ensureCapacity(int minimumCapacity) {
        data.ensureCapacity(minimumCapacity);
        return this;
    }

    /**
     * Trim to size.
     *
     * @return trim to size result
     */
    public NStringBuilder trimToSize() {
        data.trimToSize();
        return this;
    }

    /**
     * New line.
     *
     * @return new line result
     */
    public NStringBuilder newLine() {
        data.append("\n");
        return this;
    }

    /**
     * Append random.
     *
     * @param count count
     * @param patternChars pattern chars
     * @return append random result
     */
    public NStringBuilder appendRandom(int count, String patternChars) {
        if (count > 0) {
            NAssert.requireNamedNonNull(patternChars, "patternChars");
            NAssert.requireNamedTrue(!patternChars.isEmpty(), "patternChars.length>0");
            SecureRandom random = new SecureRandom();
            for (int i = 0; i < count; i++) {
                int randomIndex = random.nextInt(patternChars.length());
                char randomChar = patternChars.charAt(randomIndex);
                this.append(randomChar);
            }
        }
        return this;
    }

    /**
     * Indent.
     *
     * @param prefix prefix
     * @return indent result
     */
    public NStringBuilder indent(String prefix) {
        /**
         * Indent.
         *
         * @param prefix prefix
         * @param false false
         * @return indent result
         */
        return indent(prefix, false);
    }

    /**
     * Indent.
     *
     * @param prefix prefix
     * @param skipFirstLine skip first line
     * @return indent result
     */
    public NStringBuilder indent(String prefix, boolean skipFirstLine) {
        if (prefix == null || prefix.isEmpty()) {
            return this;
        }
        char[] charArray = data.toString().toCharArray();
        boolean wasNewLine = true;
        data.setLength(0);
        boolean firstLine = true;
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '\r') {
                if (i + 1 < charArray.length && charArray[i + 1] == '\n') {
                    i++;
                    data.append('\r');
                    data.append('\n');
                } else {
                    data.append('\r');
                }
                wasNewLine = true;
                firstLine = false;
            } else if (c == '\n') {
                data.append('\n');
                wasNewLine = true;
                firstLine = false;
            } else {
                if (wasNewLine) {
                    if (!firstLine || !skipFirstLine) {
                        data.append(prefix);
                    }
                }
                data.append(c);
                wasNewLine = false;
            }
        }
        return this;
    }

    /**
     * Lines.
     *
     * @return lines result
     */
    public NStream<String> lines() {
        StringBuilder data2 = new StringBuilder(data);
        return NStream.ofIterator(new Iterator<String>() {
            String nextLine = null;

            @Override
            public boolean hasNext() {
                if (data2.length() == 0) {
                    return false;
                }
                nextLine = NStringUtils.readLine(data2);
                return nextLine != null;
            }

            @Override
            public String next() {
                return nextLine;
            }
        });
    }

    /**
     * Read until.
     *
     * @param predicate predicate
     * @return read until result
     */
    public String readUntil(CharPredicate predicate) {
        int i = 0;
        while (i < data.length()) {
            char c = data.charAt(i);
            if (predicate.test(c)) {
                String l = data.substring(0, i);
                data.delete(0, i);
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
     * Read while.
     *
     * @param predicate predicate
     * @return read while result
     */
    public String readWhile(CharPredicate predicate) {
        int i = 0;
        while (i < data.length()) {
            char c = data.charAt(i);
            if (!predicate.test(c)) {
                String l = data.substring(0, i);
                data.delete(0, i);
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
     * Read line.
     *
     * @return read line result
     */
    public String readLine() {
        return NStringUtils.readLine(data);
    }

    /**
     * Read count.
     *
     * @param count count
     * @return read count result
     */
    public String readCount(int count) {
        if (count <= 0) {
            return "";
        }
        if (data.length() <= count) {
            String s = data.toString();
            data.setLength(0);
            return s;
        }
        String s = data.substring(0, count);
        data.delete(0, count);
        return s;
    }


    /**
     * Checks if is multi line.
     *
     * @return is multi line result
     */
    public boolean isMultiLine() {
        return lines().count() > 1;
    }

    /**
     * Print.
     *
     * @param s s
     * @return print result
     */
    public NStringBuilder print(String s) {
        /**
         * Append.
         *
         * @param s s
         * @return append result
         */
        return append(s);
    }

    /**
     * Print.
     *
     * @param value value
     * @return print result
     */
    public NStringBuilder print(Object value) {
      /**
       * Append.
       *
       * @param value value
       */
        append(value);
        return this;
    }

    /**
     * Print.
     *
     * @param value value
     * @return print result
     */
    public NStringBuilder print(int value) {
      /**
       * Append.
       *
       * @param value value
       */
        append(value);
        return this;
    }

    /**
     * Print.
     *
     * @param value value
     * @return print result
     */
    public NStringBuilder print(boolean value) {
      /**
       * Append.
       *
       * @param value value
       */
        append(value);
        return this;
    }

    /**
     * Print.
     *
     * @param value value
     * @return print result
     */
    public NStringBuilder print(byte value) {
      /**
       * Append.
       *
       * @param value value
       */
        append(value);
        return this;
    }

    /**
     * Print.
     *
     * @param value value
     * @return print result
     */
    public NStringBuilder print(short value) {
      /**
       * Append.
       *
       * @param value value
       */
        append(value);
        return this;
    }

    /**
     * Print.
     *
     * @param value value
     * @return print result
     */
    public NStringBuilder print(long value) {
      /**
       * Append.
       *
       * @param value value
       */
        append(value);
        return this;
    }

    /**
     * Print.
     *
     * @param value value
     * @return print result
     */
    public NStringBuilder print(float value) {
      /**
       * Append.
       *
       * @param value value
       */
        append(value);
        return this;
    }

    /**
     * Print.
     *
     * @param value value
     * @return print result
     */
    public NStringBuilder print(double value) {
      /**
       * Append.
       *
       * @param value value
       */
        append(value);
        return this;
    }

    /**
     * Print.
     *
     * @param value value
     * @return print result
     */
    public NStringBuilder print(char value) {
      /**
       * Append.
       *
       * @param value value
       */
        append(value);
        return this;
    }

    /**
     * Clear securely.
     *
     * @return clear securely result
     */
    public NStringBuilder clearSecurely() {
        int c = data.capacity();
        for (int i = 0; i < c; i++) {
            if (i < data.length()) {
                data.setCharAt(i, '\0');
            } else {
                data.append('\0');
            }
        }
        data.delete(0, data.length());
        return this;
    }
}
