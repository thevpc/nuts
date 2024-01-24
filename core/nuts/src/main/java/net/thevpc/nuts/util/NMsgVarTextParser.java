package net.thevpc.nuts.util;

import net.thevpc.nuts.expr.NToken;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NMsgVarTextParser {

    public static String replaceDollarString(String text, Function<String, String> m) {
        return parseDollarString(text)
                .stream().map(t -> {
                    switch (t.ttype) {
                        case NToken.TT_DOLLAR:
                        case NToken.TT_DOLLAR_BRACE: {
                            String x = m.apply(t.sval);
                            if (x == null) {
                                throw new IllegalArgumentException("var not found " + t.sval);
                            }
                            return x;
                        }
                    }
                    return t.sval;
                }).collect(Collectors.joining());
    }

    public static List<NToken> parseDollarString(String text) {
        char[] t = (text == null ? new char[0] : text.toCharArray());
        int p = 0;
        int length = t.length;
        StringBuilder sb = new StringBuilder(length);
        StringBuilder n = new StringBuilder(length);
        StringBuilder ni = new StringBuilder(length);
        List<NToken> toks = new ArrayList<>();
        while (p < length) {
            char c = t[p];
            if (c == '$' && p + 1 < length && t[p + 1] == '{') {
                p += 2;
                n.setLength(0);
                ni.setLength(0);
                ni.append(c).append('{');
                while (p < length) {
                    c = t[p];
                    if (c != '}') {
                        n.append(c);
                        ni.append(c);
                        p++;
                    } else {
                        ni.append(c);
                        break;
                    }
                }
                if (sb.length() > 0) {
                    toks.add(NToken.of(NToken.TT_DEFAULT, sb.toString(),0,0, sb.toString(),"DEFAULT"));
                    sb.setLength(0);
                }
                toks.add(NToken.of(NToken.TT_DOLLAR_BRACE, n.toString(),0,0,ni.toString(),"DOLLAR_BRACE"));
            }else if (c == '$' && p + 1 < length && isValidMessageVar(t[p + 1])) {
                p++;
                n.setLength(0);
                ni.setLength(0);
                ni.append(c);
                while (p < length) {
                    c = t[p];
                    if (isValidMessageVar(c)) {
                        n.append(c);
                        ni.append(c);
                        p++;
                    } else {
                        p--;
                        break;
                    }
                }
                if (sb.length() > 0) {
                    toks.add(NToken.of(NToken.TT_DEFAULT, sb.toString(),0,0, sb.toString(),"DEFAULT"));
                    sb.setLength(0);
                }
                toks.add(NToken.of(NToken.TT_DOLLAR, n.toString(),0,0,ni.toString(),"DOLLAR"));
            } else {
                sb.append(c);
            }
            p++;
        }
        if (sb.length() > 0) {
            toks.add(NToken.of(NToken.TT_DEFAULT, sb.toString(),0,0, sb.toString(),"DEFAULT"));
            sb.setLength(0);
        }
        return toks;
    }

    static boolean isValidMessageVar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
    }
}
