/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nlog.filter;

import net.thevpc.nuts.toolbox.nlog.model.LineFilter;
import net.thevpc.nuts.toolbox.nlog.util.StringUtils;
import net.thevpc.nuts.toolbox.nlog.model.Line;

import java.util.regex.Pattern;

/**
 *
 * @author vpc
 */
public class GlobLineFilter implements LineFilter {

    private final Pattern match;
    private final boolean caseSensitive;
    private final String matchString;

    public GlobLineFilter(String match, boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        if (StringUtils.isBlank(match)) {
            throw new IllegalArgumentException("invalid empty filter");
        }
        this.matchString = match;
        StringBuilder sb = new StringBuilder();
        //\.[]{}()<>*+-=!?^$|
        for (char c : match.toCharArray()) {
            switch (c) {
                case '*': {
                    sb.append(".*");
                    break;
                }
                case '\\':
                case '.':
                case '[':
                case ']':
                case '(':
                case ')':
                case '<':
                case '>':
                case '^':
                case '$':
                case '{':
                case '}':
                case '+':
                case '-':
                case '=':
                case '!':
                case '?':
                case '|': {
                    sb.append('\\').append(c);
                    break;
                }
                default: {
                    if (caseSensitive) {
                        sb.append(c);
                    } else {
                        sb.append(Character.toLowerCase(c));
                    }
                }
            }
        }
        this.match = Pattern.compile(sb.toString());
    }

    @Override
    public boolean accept(Line line) {
        String text = line.getText();
        if (!caseSensitive) {
            text = text.toLowerCase();
        }
        return match.matcher(text).matches();
    }

    @Override
    public LineFilter copy() {
        //immutable
        return this;
    }

    @Override
    public String toString() {
        if (caseSensitive) {
            return "Like(" + StringUtils.quote(matchString) + ')';
        } else {
            return "LikeInsensitive(" + StringUtils.quote(matchString) + ')';
        }
    }
}
