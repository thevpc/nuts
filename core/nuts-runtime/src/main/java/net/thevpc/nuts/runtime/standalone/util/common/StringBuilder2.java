/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.util.common;

import java.util.stream.IntStream;

/**
 *
 * @author thevpc
 */
public class StringBuilder2 {

    private StringBuilder sb = new StringBuilder();

    public StringBuilder2 clear() {
        sb.delete(0, sb.length());
        return this;
    }

    public StringBuilder2 trim() {
        String v = sb.toString().trim();
        clear();
        sb.append(v);
        return this;
    }

    public StringBuilder2 write(String s) {
        sb.append(s);
        return this;
    }

    public StringBuilder2 newLine() {
        sb.append("\n");
        return this;
    }

    public StringBuilder2 append(Object obj) {
        sb.append(obj);
        return this;
    }

    public StringBuilder2 append(String str) {
        sb.append(str);
        return this;
    }

    public StringBuilder2 append(StringBuffer sb) {
        this.sb.append(sb);
        return this;
    }

    public StringBuilder2 append(CharSequence s) {
        sb.append(s);
        return this;
    }

    public StringBuilder2 append(CharSequence s, int start, int end) {
        sb.append(s, start, end);
        return this;
    }

    public StringBuilder2 append(char[] str) {
        sb.append(str);
        return this;
    }

    public StringBuilder2 append(char[] str, int offset, int len) {
        sb.append(str, offset, len);
        return this;
    }

    public StringBuilder2 append(boolean b) {
        sb.append(b);
        return this;
    }

    public StringBuilder2 append(char c) {
        sb.append(c);
        return this;
    }

    public StringBuilder2 append(int i) {
        sb.append(i);
        return this;
    }

    public StringBuilder2 append(long lng) {
        sb.append(lng);
        return this;
    }

    public StringBuilder2 append(float f) {
        sb.append(f);
        return this;
    }

    public StringBuilder2 append(double d) {
        sb.append(d);
        return this;
    }

    public StringBuilder2 appendCodePoint(int codePoint) {
        sb.appendCodePoint(codePoint);
        return this;
    }

    public StringBuilder2 delete(int start, int end) {
        sb.delete(start, end);
        return this;
    }

    public StringBuilder2 deleteCharAt(int index) {
        sb.deleteCharAt(index);
        return this;
    }

    public StringBuilder2 replace(int start, int end, String str) {
        sb.replace(start, end, str);
        return this;
    }

    public StringBuilder2 insert(int index, char[] str, int offset, int len) {
        sb.insert(index, str, offset, len);
        return this;
    }

    public StringBuilder2 insert(int offset, Object obj) {
        sb.insert(offset, obj);
        return this;
    }

    public StringBuilder2 insert(int offset, String str) {
        sb.insert(offset, str);
        return this;
    }

    public StringBuilder2 insert(int offset, char[] str) {
        sb.insert(offset, str);
        return this;
    }

    public StringBuilder2 insert(int dstOffset, CharSequence s) {
        sb.insert(dstOffset, s);
        return this;
    }

    public StringBuilder2 insert(int dstOffset, CharSequence s, int start, int end) {
        sb.insert(dstOffset, s, start, end);
        return this;
    }

    public StringBuilder2 insert(int offset, boolean b) {
        sb.insert(offset, b);
        return this;
    }

    public StringBuilder2 insert(int offset, char c) {
        sb.insert(offset, c);
        return this;
    }

    public StringBuilder2 insert(int offset, int i) {
        sb.insert(offset, i);
        return this;
    }

    public StringBuilder2 insert(int offset, long l) {
        sb.insert(offset, l);
        return this;
    }

    public StringBuilder2 insert(int offset, float f) {
        sb.insert(offset, f);
        return this;
    }

    public StringBuilder2 insert(int offset, double d) {
        sb.insert(offset, d);
        return this;
    }

    public int indexOf(String str) {
        return sb.indexOf(str);
    }

    public int indexOf(String str, int fromIndex) {
        return sb.indexOf(str, fromIndex);
    }

    public int lastIndexOf(String str) {
        return sb.lastIndexOf(str);
    }

    public int lastIndexOf(String str, int fromIndex) {
        return sb.lastIndexOf(str, fromIndex);
    }

    public StringBuilder2 reverse() {
        sb.reverse();
        return this;
    }

    public String toString() {
        return sb.toString();
    }

    public int length() {
        return sb.length();
    }

    public int capacity() {
        return sb.capacity();
    }

    public void ensureCapacity(int minimumCapacity) {
        sb.ensureCapacity(minimumCapacity);
    }

    public void trimToSize() {
        sb.trimToSize();
    }

    public void setLength(int newLength) {
        sb.setLength(newLength);
    }

    public char charAt(int index) {
        return sb.charAt(index);
    }

    public int codePointAt(int index) {
        return sb.codePointAt(index);
    }

    public int codePointBefore(int index) {
        return sb.codePointBefore(index);
    }

    public int codePointCount(int beginIndex, int endIndex) {
        return sb.codePointCount(beginIndex, endIndex);
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        return sb.offsetByCodePoints(index, codePointOffset);
    }

    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        sb.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public void setCharAt(int index, char ch) {
        sb.setCharAt(index, ch);
    }

    public String substring(int start) {
        return sb.substring(start);
    }

    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }

    public String substring(int start, int end) {
        return sb.substring(start, end);
    }

    public IntStream chars() {
        return sb.chars();
    }

    public IntStream codePoints() {
        return sb.codePoints();
    }

    @Override
    public int hashCode() {
        return sb.hashCode();
    }

}
