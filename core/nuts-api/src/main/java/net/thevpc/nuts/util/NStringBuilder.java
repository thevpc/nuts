package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.io.NStringWriter;
import net.thevpc.nuts.pipeline.NStream;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.stream.IntStream;

public interface NStringBuilder extends CharSequence, NBlankable {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NStringBuilder of() {
        return NUtilsRPI.of().createStringBuilder(null);
    }

    static NStringBuilder of(String any) {
        return NUtilsRPI.of().createStringBuilder(any);
    }

    NStringWriter asStringWriter();

    NStringBuilder append(String value);

    NStringBuilder append(Object value);

    NStringBuilder append(int value);

    NStringBuilder append(boolean value);

    NStringBuilder append(byte value);

    NStringBuilder append(short value);

    NStringBuilder append(float value);

    NStringBuilder append(double value);

    NStringBuilder append(char value);

    NStringBuilder insert(int offset, String value);

    NStringBuilder insert(int offset, Object value);

    NStringBuilder insert(int offset, int value);

    NStringBuilder insert(int offset, boolean value);

    NStringBuilder insert(int offset, byte value);

    NStringBuilder insert(int offset, short value);

    NStringBuilder insert(int offset, float value);

    NStringBuilder insert(int offset, double value);

    NStringBuilder insert(int offset, char value);

    NStringBuilder insertFirst(Object value);

    NStringBuilder insertFirst(int value);

    NStringBuilder insertFirst(boolean value);

    NStringBuilder insertFirst(byte value);

    NStringBuilder insertFirst(short value);

    NStringBuilder insertFirst(float value);

    NStringBuilder insertFirst(double value);

    NStringBuilder insertFirst(char value);

    int length();

    boolean isEmpty();

    char charAt(int index);

    int indexOf(String str);

    int indexOf(String str, int fromIndex);

    int lastIndexOf(String str);

    int lastIndexOf(String str, int fromIndex);

    int indexOf(CharSequence str);

    int indexOf(CharSequence str, int fromIndex);

    int lastIndexOf(CharSequence str);

    int lastIndexOf(CharSequence str, int fromIndex);

    int indexOf(char[] str);

    int indexOf(char[] str, int fromIndex);

    int lastIndexOf(char[] str);

    int lastIndexOf(char[] str, int fromIndex);

    NStringBuilder println(Object str);

    NStringBuilder println();

    NStringBuilder println(String str);

    NStringBuilder replace(int start, int end, String str);

    boolean isBlank();

    NStringBuilder reverse();

    NStringBuilder clear();

    NStringBuilder deleteCharAt(int index);

    NStringBuilder delete(int start, int end);

    char get(int index);

    char first();

    char last();

    char removeFirst();

    char removeLast();

    String build();

    @Override
    String toString();

    char[] toCharArray();

    NStringBuilder copy();

    NStringBuilder strip();

    String substring(int start, int end);

    String head(int size);

    String tail(int size);

    boolean startsWith(String other);

    boolean startsWith(String other, int toffset);

    boolean startWith(char other);

    boolean contains(char other);

    int indexOf(char other);

    boolean endsWith(char other);

    boolean endsWith(String other);

    boolean regionMatches(boolean ignoreCase, int toffset,
                          String other, int ooffset, int len);

    boolean regionMatches(int toffset, String other, int ooffset,
                          int len);

    NStringBuilder setUpperCaseAt(int index);

    NStringBuilder setLowerCaseAt(int index);

    @Override
    CharSequence subSequence(int start, int end);

    @Override
    IntStream chars();

    @Override
    IntStream codePoints();

    NStringBuilder append(CharSequence value);

    NStringBuilder insert(int index, CharSequence value);

    NStringBuilder insertFirst(CharSequence value);

    NStringBuilder append(CharSequence value, int from, int to);

    NStringBuilder insert(int index, CharSequence value, int from, int to);

    NStringBuilder insertFirst(CharSequence value, int from, int to);

    NStringBuilder append(char[] value, int from, int to);

    NStringBuilder insert(int index, char[] value, int from, int to);

    NStringBuilder insertFirst(char[] value, int from, int to);

    NStringBuilder append(StringBuilder value, int from, int to);

    NStringBuilder insert(int index, StringBuilder value, int from, int to);

    NStringBuilder insertFirst(StringBuilder value, int from, int to);

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    int indexOf(int ch, int fromIndex);

    int lastIndexOf(int ch, int fromIndex);

    int codePointAt(int index);

    int codePointBefore(int index);

    int codePointCount(int begin, int end);

    int offsetByCodePoints(int index, int codePointOffset);

    void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin);

    void setCharAt(int index, char ch);

    String substring(int start);

    void getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin);

    byte[] getBytes(String charsetName);

    byte[] getBytes();

    byte[] getBytes(Charset charset);

    boolean contentEquals(StringBuffer sb);

    boolean contentEquals(CharSequence sb);

    boolean contentEquals(String sb);

    boolean contentEquals(char[] chars);

    boolean equalsIgnoreCase(String anotherString);

    boolean equalsIgnoreCase(CharSequence anotherString);

    boolean equalsIgnoreCase(char[] anotherString);

    int compareTo(String anotherString);

    int compareTo(char[] anotherString);

    int compareTo(CharSequence anotherString);

    int compareToIgnoreCase(String anotherString);

    int compareToIgnoreCase(char[] anotherString);

    int compareToIgnoreCase(CharSequence anotherString);

    NStringBuilder replace(char oldChar, char newChar);

    boolean matches(String regex);

    boolean contains(CharSequence s);

    boolean contains(char[] s);

    NStringBuilder replaceFirst(String regex, String replacement);

    NStringBuilder setContent(String s);

    NStringBuilder replaceAll(String regex, String replacement);

    NStringBuilder replace(CharSequence target, CharSequence replacement);

    String[] split(String regex, int limit);

    String[] split(String regex);

    NStringBuilder toLowerCase(Locale locale);

    NStringBuilder toLowerCase();

    NStringBuilder toUpperCase(Locale locale);

    NStringBuilder toUpperCase();

    NStringBuilder toNameFormat(NNameFormat format);

    String removeAll();

    NStringBuilder ensureCapacity(int minimumCapacity);

    NStringBuilder trimToSize();

    NStringBuilder newLine();

    NStringBuilder appendRandom(int count, String patternChars);

    NStringBuilder indent(String prefix);

    NStringBuilder indent(String prefix, boolean skipFirstLine);

    NStream<String> lines();

    String readUntil(CharPredicate predicate);

    String readWhile(CharPredicate predicate);

    String readLine();

    String readCount(int count);

    boolean isMultiLine();

    NStringBuilder print(String s);

    NStringBuilder print(Object value);

    NStringBuilder print(int value);

    NStringBuilder print(boolean value);

    NStringBuilder print(byte value);

    NStringBuilder print(short value);

    NStringBuilder print(long value);

    NStringBuilder print(float value);

    NStringBuilder print(double value);

    NStringBuilder print(char value);

    NStringBuilder clearSecurely();
}
