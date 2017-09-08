package net.vpc.app.nuts.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 5/23/17.
 */
public class EnhancedTextParser {
    public static final EnhancedTextParser INSTANCE = new EnhancedTextParser();
    private static final char[][] startPattern = new char[][]{
            "<<<".toCharArray(),
            "<<".toCharArray(),
            "(((".toCharArray(),
            "((".toCharArray(),
            "[[[".toCharArray(),
            "[[".toCharArray(),
            "===".toCharArray(),
            "==".toCharArray(),
            "@@".toCharArray(),
            "\"\"".toCharArray(),
            "\"".toCharArray(),
            "\'".toCharArray(),
    };
    private static final char[][] endPattern = new char[][]{
            ">>>".toCharArray(),
            ">>".toCharArray(),
            ")))".toCharArray(),
            "))".toCharArray(),
            "]]]".toCharArray(),
            "]]".toCharArray(),
            "===".toCharArray(),
            "==".toCharArray(),
            "@@".toCharArray(),
            "\"\"".toCharArray(),
            "\"".toCharArray(),
            "\'".toCharArray(),
    };

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

    public List<EnhancedTextChunck> parse(String text) {
        List<EnhancedTextChunck> found = new ArrayList<>();
        if (text == null) {
            text = "";
        }
        char[] chars = text.toCharArray();
        int index = 0;
        while (index < chars.length) {
            int[] startLocation = findNext2(startPattern, endPattern, chars, index);
            if (startLocation == null) {
                //not found
                found.add(new EnhancedTextChunck(null, unescape(new String(chars, index, chars.length - index))));
                index = chars.length;
            } else {
                String start = new String(startPattern[startLocation[0]]);
                String end = new String(endPattern[startLocation[0]]);
                int startPos = startLocation[1];
                int endPos = startLocation[2];
                if (startPos > index) {
                    found.add(new EnhancedTextChunck(null, unescape(new String(chars, index, startPos - index))));
                }
                found.add(new EnhancedTextChunck(start, unescape(new String(chars, startPos + start.length(), endPos - (startPos + start.length())))));
                index = endPos + end.length();
            }
        }
        return found;
    }
}
