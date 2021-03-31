/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.service.search.strsearch;

/**
 *
 * @author vpc
 */
public class StringToPatternPortionImpl<T> extends AbstractStringToPatternPortion<T> {

    private String key;
    private String text;
    private T object;
    private Object part;

    public StringToPatternPortionImpl(String key, String text, T object, Object part, String stringValue) {
        super(stringValue);
        this.key = key;
        this.text = text;
        this.object = object;
        this.part = part;
    }

    @Override
    public StringSearchResult resolvePosition(int index, String value) {
        int line = 1;
        int col = 1;
        int p = 0;
        char[] chars = text.toCharArray();
        for (char c : chars) {
            if (p == index) {
                return new StringSearchResult(object, key, null, key, index, line, col, value,
                        extractLine(chars, index, index + value.length(), 80)
                );
            }
            if (c == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
            p++;
        }
        return new StringSearchResult(object, key, part, key, index, -1, -1, value, value);
    }

    protected String extractLine(char[] chars, int from, int to, int max) {
        StringBuilder sb = new StringBuilder();
        sb.append(new String(chars, from, to - from));
        boolean left = true;
        boolean right = true;
        boolean currLeft = true;
        int i = from - 1;
        int j = to;
        while (left || right) {
            if (currLeft) {
                if (left) {
                    if (i >= 0 && chars[i] != '\n') {
                        sb.insert(0, chars[i]);
                        i--;
                    } else {
                        left = false;
                    }
                }
                if (right) {
                    currLeft = false;
                }
            } else {
                if (right) {
                    if (j < chars.length && chars[j] != '\n') {
                        sb.append(chars[j]);
                        j++;
                    } else {
                        right = false;
                    }
                }
                if (left) {
                    currLeft = true;
                }
            }
        }
        return sb.toString();
    }

}
