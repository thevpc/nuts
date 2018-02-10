/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 5/23/17.
 */
public class NutsTextParser {

    public static final NutsTextParser INSTANCE = new NutsTextParser();
    private static final char[][] startPattern = new char[][]{
        "<<<".toCharArray(),
        "<<".toCharArray(),
        "{{{".toCharArray(),
        "{{".toCharArray(),
        "(((".toCharArray(),
        "((".toCharArray(),
        "[[[".toCharArray(),
        "[[".toCharArray(),
        "===".toCharArray(),
        "==".toCharArray(),
        "***".toCharArray(),
        "**".toCharArray(),
        "@@@".toCharArray(),
        "@@".toCharArray(),
        "\"\"\"".toCharArray(),
        "\"\"".toCharArray(),
        "\"".toCharArray(),
        "\'".toCharArray(),};
    private static final char[][] endPattern = new char[][]{
        ">>>".toCharArray(),
        ">>".toCharArray(),
        "}}}".toCharArray(),
        "}}".toCharArray(),
        ")))".toCharArray(),
        "))".toCharArray(),
        "]]]".toCharArray(),
        "]]".toCharArray(),
        "===".toCharArray(),
        "==".toCharArray(),
        "***".toCharArray(),
        "**".toCharArray(),
        "@@@".toCharArray(),
        "@@".toCharArray(),
        "\"\"\"".toCharArray(),
        "\"\"".toCharArray(),
        "\"".toCharArray(),
        "\'".toCharArray(),};

    private static int[] findNext2(char[][] start, char[][] end, char[] source, int index) {
        int expectedIndex = 0;
        while (expectedIndex < start.length) {
            int[] s = findNext(start, expectedIndex, source, index);
            if (s != null) {
                int[] e = findNext(new char[][]{end[s[0]]}, 0, source, s[1] + start[s[0]].length);
                if (e != null) {
                    return new int[]{s[0], s[1], e[1]};
                }
                expectedIndex = s[0] + 1;
            } else {
                break;
            }
        }
        return null;
    }

    private static int[] findNext(char[][] expected, int expectedIndex, char[] source, int index) {
        int[] pos = new int[expected.length];
        int best = -1;
        for (int i = expectedIndex; i < expected.length; i++) {
            pos[i] = findNext(expected[i], source, index);
            if (pos[i] >= 0) {
                if (best == -1 || pos[i] < pos[best]) {
                    best = i;
                }
            }
        }
        if (best >= 0) {
            return new int[]{best, pos[best]};
        }
        return null;
    }

    private static int findNext(char[] expected, char[] source, int index) {
        for (int i = index; i <= source.length - expected.length; i++) {
            if (source[i] == '\\') {
                i++;
            } else {
                if (expect(expected, source, i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static boolean expect(char[] expected, char[] source, int index) {
        if (index + expected.length < source.length) {
            for (int i = 0; i < expected.length; i++) {
                if (expected[i] != source[index + i]) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String unescape(String text) {
        StringBuilder sb = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '\\') {
                i++;
                if (i < chars.length) {
                    sb.append(chars[i]);
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    public static void main(String[] args) {
        List<NutsTextChunck> sss = new NutsTextParser().parse("==identities== : NONE");
        System.out.println(sss);
    }

    public List<NutsTextChunck> parse(String text) {
        List<NutsTextChunck> found = new ArrayList<>();
        if (text == null) {
            text = "";
        }
        char[] chars = text.toCharArray();
        int index = 0;
        while (index < chars.length) {
            int[] startLocation = findNext2(startPattern, endPattern, chars, index);
            if (startLocation == null) {
                //not found
                found.add(new NutsTextChunck(null, unescape(new String(chars, index, chars.length - index))));
                index = chars.length;
            } else {
                String start = new String(startPattern[startLocation[0]]);
                String end = new String(endPattern[startLocation[0]]);
                int startPos = startLocation[1];
                int endPos = startLocation[2];
                if (startPos > index) {
                    found.add(new NutsTextChunck(null, unescape(new String(chars, index, startPos - index))));
                }
                found.add(new NutsTextChunck(start, unescape(new String(chars, startPos + start.length(), endPos - (startPos + start.length())))));
                index = endPos + end.length();
            }
        }
        return found;
    }
}
