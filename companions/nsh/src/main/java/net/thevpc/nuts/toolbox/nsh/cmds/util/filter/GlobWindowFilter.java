/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.toolbox.nsh.cmds.util.filter;

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.toolbox.nsh.cmds.util.WindowFilter;
import net.thevpc.nuts.toolbox.nsh.cmds.util.NNumberedObject;
import net.thevpc.nuts.util.NStringUtils;

import java.util.regex.Pattern;

/**
 *
 * @author vpc
 */
public class GlobWindowFilter implements WindowFilter<NNumberedObject<String>> {

    private final Pattern match;
    private final boolean caseSensitive;
    private final String matchString;

    public GlobWindowFilter(String match, boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        if (NBlankable.isBlank(match)) {
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
    public boolean accept(NNumberedObject<String> line) {
        String text = line.getObject();
        if (!caseSensitive) {
            text = text.toLowerCase();
        }
        return match.matcher(text).matches();
    }

    @Override
    public WindowFilter<NNumberedObject<String>> copy() {
        //immutable
        return this;
    }

    @Override
    public String toString() {
        if (caseSensitive) {
            return "Like(" + NStringUtils.formatStringLiteral(matchString) + ')';
        } else {
            return "LikeInsensitive(" + NStringUtils.formatStringLiteral(matchString) + ')';
        }
    }
}
