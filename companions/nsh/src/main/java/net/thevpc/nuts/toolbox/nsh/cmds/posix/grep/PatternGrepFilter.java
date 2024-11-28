package net.thevpc.nuts.toolbox.nsh.cmds.posix.grep;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.text.NTexts;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternGrepFilter implements GrepFilter {
    public Pattern p;
    public PatternGrepFilter(String expression, boolean word, boolean lineRegexp, boolean ignoreCase) {
        String baseExpr = simpexpToRegexp(expression, true);
        if (word) {
            baseExpr = "\\b" + baseExpr + "\\b";
        }
        if (lineRegexp) {
            baseExpr = "^" + baseExpr + "$";
        }
        if (ignoreCase) {
            baseExpr = "(?i)" + baseExpr;
        }
        p = Pattern.compile(baseExpr);
    }

    public void processNonPivot(String line, NTextBuilder coloredLine, NTextStyles selectionStyle, NSession session) {

    }
        @Override
    public boolean processPivot(String line, NTextBuilder coloredLine, NTextStyles selectionStyle, NSession session) {
        Matcher matcher = p.matcher(line);
        boolean anyMatch=false;
        while (matcher.find()) {
            anyMatch = true;
            int pos = matcher.start();
            int end = matcher.end();
            coloredLine.replace(pos, end,
                    NText.ofStyled(
                            coloredLine.substring(pos, end)
                            , selectionStyle
                    )
            );
        }
        return anyMatch;
    }

    public static String simpexpToRegexp(String pattern, boolean contains) {
        if (pattern == null) {
            pattern = "*";
        }
        int i = 0;
        char[] cc = pattern.toCharArray();
        StringBuilder sb = new StringBuilder();
        while (i < cc.length) {
            char c = cc[i];
            switch (c) {
                case '.':
                case '!':
                case '$':
                case '[':
                case ']':
                case '(':
                case ')':
                case '?':
                case '^':
                case '|':
                case '\\': {
                    sb.append('\\').append(c);
                    break;
                }
                case '*': {
//                    if (i + 1 < cc.length && cc[i + 1] == '*') {
//                        i++;
//                        sb.append("[a-zA-Z_0-9_$.-]*");
//                    } else {
//                        sb.append("[a-zA-Z_0-9_$-]*");
//                    }
                    sb.append(".*");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
            i++;
        }
        if (!contains) {
            sb.insert(0, '^');
            sb.append('$');
        }
        return sb.toString();
    }
}
