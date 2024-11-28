package net.thevpc.nuts.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class NStringBuilder implements CharSequence, NBlankable {
    private StringBuilder data;

    public NStringBuilder() {
        data = new StringBuilder();
    }

    public NStringBuilder(String value) {
        data = new StringBuilder(value);
    }

    public NStringBuilder(CharSequence value) {
        data = new StringBuilder(value);
    }

    public NStringBuilder(int capacity) {
        data = new StringBuilder(capacity);
    }

    public NStringBuilder append(String value) {
        data.append(value);
        return this;
    }

    public NStringBuilder append(Object value) {
        data.append(value);
        return this;
    }

    public NStringBuilder append(int value) {
        data.append(value);
        return this;
    }

    public NStringBuilder append(boolean value) {
        data.append(value);
        return this;
    }

    public NStringBuilder append(byte value) {
        data.append(value);
        return this;
    }

    public NStringBuilder append(short value) {
        data.append(value);
        return this;
    }

    public NStringBuilder append(float value) {
        data.append(value);
        return this;
    }

    public NStringBuilder append(double value) {
        data.append(value);
        return this;
    }

    public NStringBuilder append(char value) {
        data.append(value);
        return this;
    }

    public NStringBuilder insert(int offset, String value) {
        data.insert(offset, value);
        return this;
    }

    public NStringBuilder insert(int offset, Object value) {
        data.insert(offset, value);
        return this;
    }

    public NStringBuilder insert(int offset, int value) {
        data.insert(offset, value);
        return this;
    }

    public NStringBuilder insert(int offset, boolean value) {
        data.insert(offset, value);
        return this;
    }

    public NStringBuilder insert(int offset, byte value) {
        data.insert(offset, value);
        return this;
    }

    public NStringBuilder insert(int offset, short value) {
        data.insert(offset, value);
        return this;
    }

    public NStringBuilder insert(int offset, float value) {
        data.insert(offset, value);
        return this;
    }

    public NStringBuilder insert(int offset, double value) {
        data.insert(offset, value);
        return this;
    }

    public NStringBuilder insert(int offset, char value) {
        data.insert(offset, value);
        return this;
    }

    public NStringBuilder insertFirst(Object value) {
        data.insert(0, value);
        return this;
    }

    public NStringBuilder insertFirst(int value) {
        data.insert(0, value);
        return this;
    }

    public NStringBuilder insertFirst(boolean value) {
        data.insert(0, value);
        return this;
    }

    public NStringBuilder insertFirst(byte value) {
        data.insert(0, value);
        return this;
    }

    public NStringBuilder insertFirst(short value) {
        data.insert(0, value);
        return this;
    }

    public NStringBuilder insertFirst(float value) {
        data.insert(0, value);
        return this;
    }

    public NStringBuilder insertFirst(double value) {
        data.insert(0, value);
        return this;
    }

    public NStringBuilder insertFirst(char value) {
        data.insert(0, value);
        return this;
    }

    public int length() {
        return data.length();
    }

    public boolean isEmpty() {
        return data.length() == 0;
    }

    private int wiseIndex(int index, int length) {
        if (index < 0) {
            int rIndex = length + index;
            if (rIndex >= 0 && rIndex < length) {
                return rIndex;
            }
        }
        return index;
    }

    private int wiseIndex(int index) {
        return wiseIndex(index, data.length());
    }

    private int wiseIndexOther(int index, CharSequence other) {
        return wiseIndex(index, other.length());
    }

    private int wiseIndexOther(int index, char[] other) {
        return wiseIndex(index, other.length);
    }

    private int wiseIndexOther(int index, byte[] other) {
        return wiseIndex(index, other.length);
    }

    public char charAt(int index) {
        return data.charAt(wiseIndex(index));
    }

    public int indexOf(String str) {
        return data.indexOf(str);
    }

    public int indexOf(String str, int fromIndex) {
        return data.indexOf(str, wiseIndex(fromIndex));
    }

    public int lastIndexOf(String str) {
        return data.lastIndexOf(str);
    }

    public int lastIndexOf(String str, int fromIndex) {
        return data.lastIndexOf(str, wiseIndex(fromIndex));
    }

    public int indexOf(CharSequence str) {
        return data.indexOf(str.toString());
    }

    public int indexOf(CharSequence str, int fromIndex) {
        return data.indexOf(str.toString(), wiseIndex(fromIndex));
    }

    public int lastIndexOf(CharSequence str) {
        return data.lastIndexOf(str.toString());
    }

    public int lastIndexOf(CharSequence str, int fromIndex) {
        return data.lastIndexOf(str.toString(), fromIndex);
    }

    public int indexOf(char[] str) {
        return data.indexOf(new String(str));
    }

    public int indexOf(char[] str, int fromIndex) {
        return data.indexOf(new String(str), wiseIndex(fromIndex));
    }

    public int lastIndexOf(char[] str) {
        return data.lastIndexOf(new String(str));
    }

    public int lastIndexOf(char[] str, int fromIndex) {
        return data.lastIndexOf(new String(str), wiseIndex(fromIndex));
    }

    public NStringBuilder println(Object str) {
        return println(String.valueOf(str));
    }

    public NStringBuilder println(String str) {
        append(str);
        newLine();
        return this;
    }

    public NStringBuilder replace(int start, int end, String str) {
        data.replace(wiseIndex(start), wiseIndex(end), str);
        return this;
    }

    public boolean isBlank() {
        return NBlankable.isBlank(data.toString());
    }

    public NStringBuilder reverse() {
        data.reverse();
        return this;
    }

    public NStringBuilder clear() {
        data.setLength(0);
        return this;
    }

    public NStringBuilder deleteCharAt(int index) {
        data.deleteCharAt(wiseIndex(index));
        return this;
    }

    public NStringBuilder delete(int start, int end) {
        data.delete(wiseIndex(start), wiseIndex(end));
        return this;
    }

    public char get(int index) {
        return data.charAt(wiseIndex(index));
    }

    public char getFirst() {
        return data.charAt(0);
    }

    public char getLast() {
        return data.charAt(data.length());
    }

    public char removeFirst() {
        char c = data.charAt(0);
        data.deleteCharAt(0);
        return c;
    }

    public char removeLast() {
        int len = data.length();
        char c = data.charAt(len - 1);
        data.deleteCharAt(len - 1);
        return c;
    }

    @Override
    public String toString() {
        return data.toString();
    }

    public char[] toCharArray() {
        int length = length();
        char[] c = new char[length];
        data.getChars(
                0, length, c, 0
        );
        return c;
    }

    public NStringBuilder copy() {
        return new NStringBuilder().append(data);
    }

    public NStringBuilder trim() {
        int len0 = length();
        int len = len0;
        int st = 0;
        char[] val = toString().toCharArray();    /* avoid getfield opcode */

        while ((st < len) && (val[st] <= ' ')) {
            st++;
        }
        while ((st < len) && (val[len - 1] <= ' ')) {
            len--;
        }
        if (((st > 0) || (len < len0))) {
            String v = data.substring(st, len);
            setContent(v);
        }
        return this;
    }

    public String substring(int start, int end) {
        return data.substring(wiseIndex(start), wiseIndex(end));
    }

    public String head(int size) {
        return data.substring(0, size);
    }

    public String tail(int size) {
        int length = data.length();
        return data.substring(length - size, length);
    }

    public boolean startsWith(String other) {
        int olength = other.length();
        return length() > olength && subSequence(0, olength).equals(other);
    }

    public boolean startsWith(String other, int toffset) {
        toffset = wiseIndex(toffset);
        int olength = other.length();
        int length = length() - toffset;
        return length >= olength && subSequence(toffset, toffset + olength).equals(other);
    }

    public boolean startWith(char other) {
        int length = length();
        return (length > 0 && charAt(0) == other);
    }

    public boolean contains(char other) {
        return indexOf(other) >= 0;
    }

    public int indexOf(char other) {
        return data.indexOf(String.valueOf(other));
    }

    public boolean endsWith(char other) {
        int length = length();
        return (length > 0 && charAt(length - 1) == other);
    }

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

    public NStringBuilder setUpperCaseAt(int index) {
        index = wiseIndex(index);
        data.setCharAt(index,
                Character.toUpperCase(data.charAt(index))
        );
        return this;
    }

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

    public NStringBuilder append(CharSequence value) {
        data.append(value);
        return this;
    }

    public NStringBuilder insert(int index, CharSequence value) {
        data.insert(wiseIndex(index), value);
        return this;
    }

    public NStringBuilder insertFirst(CharSequence value) {
        data.insert(0, value);
        return this;
    }

    public NStringBuilder append(CharSequence value, int from, int to) {
        data.append(value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

    public NStringBuilder insert(int index, CharSequence value, int from, int to) {
        data.insert(index, value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

    public NStringBuilder insertFirst(CharSequence value, int from, int to) {
        data.insert(0, value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

    public NStringBuilder append(char[] value, int from, int to) {
        data.append(value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

    public NStringBuilder insert(int index, char[] value, int from, int to) {
        data.insert(wiseIndex(index), value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

    public NStringBuilder insertFirst(char[] value, int from, int to) {
        data.insert(0, value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

    public NStringBuilder append(StringBuilder value, int from, int to) {
        data.append(value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

    public NStringBuilder insert(int index, StringBuilder value, int from, int to) {
        data.insert(wiseIndex(index), value, wiseIndexOther(from, value), wiseIndexOther(to, value));
        return this;
    }

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

    public int indexOf(int ch, int fromIndex) {
        return data.indexOf(String.valueOf(ch), wiseIndex(fromIndex));
    }

    public int lastIndexOf(int ch, int fromIndex) {
        return data.lastIndexOf(String.valueOf(ch), wiseIndex(fromIndex));
    }

    public int codePointAt(int index) {
        return data.codePointAt(wiseIndex(index));
    }

    public int codePointBefore(int index) {
        return data.codePointBefore(wiseIndex(index));
    }

    public int codePointCount(int begin, int end) {
        return data.codePointCount(wiseIndex(begin), wiseIndex(end));
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        return data.offsetByCodePoints(wiseIndex(index), codePointOffset);
    }

    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        data.getChars(wiseIndex(srcBegin), wiseIndex(srcEnd), dst, wiseIndexOther(dstBegin, dst));
    }

    public void setCharAt(int index, char ch) {
        data.setCharAt(wiseIndex(index), ch);
    }

    public String substring(int start) {
        return data.substring(wiseIndex(start));
    }

    public void getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin) {
        data.toString().getBytes(wiseIndex(srcBegin), wiseIndex(srcEnd), dst, wiseIndexOther(dstBegin, dst));
    }

    public byte[] getBytes(String charsetName) {
        try {
            return data.toString().getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedException(e);
        }
    }

    public byte[] getBytes() {
        return data.toString().getBytes();
    }

    public byte[] getBytes(Charset charset) {
        return data.toString().getBytes(charset);
    }

    public boolean contentEquals(StringBuffer sb) {
        return sb != null && toString().equals(sb.toString());
    }

    public boolean contentEquals(CharSequence sb) {
        return sb != null && toString().equals(sb.toString());
    }

    public boolean contentEquals(String sb) {
        return sb != null && toString().equals(sb.toString());
    }

    public boolean contentEquals(char[] chars) {
        return chars != null && toString().equals(new String(chars));
    }

    public boolean equalsIgnoreCase(String anotherString) {
        return anotherString != null && toString().equals(anotherString);
    }

    public boolean equalsIgnoreCase(CharSequence anotherString) {
        return anotherString != null && toString().equals(anotherString.toString());
    }

    public boolean equalsIgnoreCase(char[] anotherString) {
        return anotherString != null && toString().equals(new String(anotherString));
    }

    public int compareTo(String anotherString) {
        return anotherString == null ? 1 : toString().compareTo(anotherString);
    }

    public int compareTo(char[] anotherString) {
        return anotherString == null ? 1 : toString().compareTo(new String(anotherString));
    }

    public int compareTo(CharSequence anotherString) {
        return anotherString == null ? 1 : toString().compareTo(anotherString.toString());
    }

    public int compareToIgnoreCase(String anotherString) {
        return anotherString == null ? 1 : toString().compareToIgnoreCase(anotherString);
    }

    public int compareToIgnoreCase(char[] anotherString) {
        return anotherString == null ? 1 : toString().compareToIgnoreCase(new String(anotherString));
    }

    public int compareToIgnoreCase(CharSequence anotherString) {
        return anotherString == null ? 1 : toString().compareToIgnoreCase(anotherString.toString());
    }

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

    public boolean matches(String regex) {
        return toString().matches(regex);
    }

    public boolean contains(CharSequence s) {
        return indexOf(s) >= 0;
    }

    public boolean contains(char[] s) {
        return indexOf(s) >= 0;
    }

    public NStringBuilder replaceFirst(String regex, String replacement) {
        String s = Pattern.compile(regex).matcher(this).replaceFirst(replacement);
        if (!s.equals(data.toString())) {
            setContent(s);
        }
        return this;
    }

    public NStringBuilder setContent(String s) {
        data.setLength(0);
        data.append(s);
        return this;
    }

    public NStringBuilder replaceAll(String regex, String replacement) {
        return setContent(Pattern.compile(regex).matcher(this).replaceAll(replacement));
    }

    public NStringBuilder replace(CharSequence target, CharSequence replacement) {
        return setContent(toString().replace(target, replacement));
    }

    public String[] split(String regex, int limit) {
        return toString().split(regex, limit);
    }

    public String[] split(String regex) {
        return toString().split(regex);
    }

    public NStringBuilder toLowerCase(Locale locale) {
        return setContent(toString().toLowerCase(locale));
    }

    public NStringBuilder toLowerCase() {
        return setContent(toString().toLowerCase());
    }

    public NStringBuilder toUpperCase(Locale locale) {
        return setContent(toString().toUpperCase(locale));
    }

    public NStringBuilder toUpperCase() {
        return setContent(toString().toUpperCase());
    }

    public NStringBuilder toNameFormat(NNameFormat format) {
        NAssert.requireNonNull(format, "format");
        return setContent(format.format(toString()));
    }

    public String removeAll() {
        String s = data.toString();
        data.setLength(0);
        return s;
    }

    public NStringBuilder ensureCapacity(int minimumCapacity) {
        data.ensureCapacity(minimumCapacity);
        return this;
    }

    public NStringBuilder trimToSize() {
        data.trimToSize();
        return this;
    }

    public NStringBuilder newLine() {
        data.append("\n");
        return this;
    }

    public NStringBuilder appendRandom(int count, String patternChars) {
        if (count > 0) {
            NAssert.requireNonNull(patternChars, "patternChars");
            NAssert.requireTrue(!patternChars.isEmpty(), "patternChars.length>0");
            SecureRandom random = new SecureRandom();
            for (int i = 0; i < count; i++) {
                int randomIndex = random.nextInt(patternChars.length());
                char randomChar = patternChars.charAt(randomIndex);
                this.append(randomChar);
            }
        }
        return this;
    }

    public NStringBuilder indent(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return this;
        }
        char[] charArray = data.toString().toCharArray();
        boolean wasNewLine = true;
        data.setLength(0);
        for (int i = 0; i < charArray.length; i++) {
            if (wasNewLine) {
                data.append(prefix);
            }
            char c = charArray[i];
            if (c == '\r') {
                if (i + 1 < charArray.length && charArray[i + 1] == '\n') {
                    i++;
                    data.append('\r');
                    data.append('\n');
                } else {
                    data.append('\r');
                    data.append(prefix);
                }
                wasNewLine = true;
            } else if (c == '\n') {
                data.append('\r');
                data.append(prefix);
                wasNewLine = true;
            } else {
                data.append(c);
                wasNewLine = false;
            }
        }
        return this;
    }

    public NStream<String> lines() {
        return null;
    }

    public String readLine() {
        return readLine(data);
    }

    private String readLine(StringBuilder data) {
        int i=0;
        while(i<data.length()) {
            char c = data.charAt(i);
            if(c=='\n'){
                if(i+1<data.length() && data.charAt(i+1)=='\r'){
                    i++;
                    String l=data.substring(0,i-2);
                    data.delete(0,i);
                    return l;
                }
                String l=data.substring(0,i-1);
                data.delete(0,i);
                return l;
            }
        }
        String l=data.toString();
        data.setLength(0);
        return l;
    }

}
