package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NMsgType;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NIllegalArgumentException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class NMsgSFormatHelper extends AbstractNMsgFormatHelper {
    int gParamIndex = 0;

    public NMsgSFormatHelper(NMsg m, NTexts txt) {
        super(m, txt);
    }

    /**
     * Splits a SQL-like string into plain-text parts and placeholder parts.
     * Recognizes:
     *  - '?'      -> positional placeholder (format value = "")
     *  - ':name'  -> named placeholder (format value = "name")
     *  - '::'     -> passthrough literal (postgres cast), never a placeholder
     *  - '\?' '\:' -> escaped literal char
     *  - single/double-quoted string literals (with doubled-quote escaping) are
     *    copied verbatim; '?' and ':' inside them are never treated as placeholders
     */
    public static NFormattedTextParts parseStyle(String msg) {
        if (msg == null) {
            return new NFormattedTextParts(NMsgType.SFORMAT, Collections.emptyList());
        }
        List<NFormattedTextPart> al = new ArrayList<>();
        int length = msg.length();
        char[] chars = msg.toCharArray();
        StringBuilder sb = new StringBuilder();
        char quote = 0; // 0 = not inside a string literal

        for (int i = 0; i < length; i++) {
            char c = chars[i];

            if (quote != 0) {
                sb.append(c);
                if (c == quote) {
                    if (i + 1 < length && chars[i + 1] == quote) {
                        // doubled-quote escape: 'it''s'
                        sb.append(chars[++i]);
                    } else {
                        quote = 0;
                    }
                }
                continue;
            }

            if (c == '\'' || c == '"') {
                quote = c;
                sb.append(c);
                continue;
            }

            if (c == '\\' && i + 1 < length && (chars[i + 1] == '?' || chars[i + 1] == ':')) {
                sb.append(chars[++i]);
                continue;
            }

            if (c == ':' && i + 1 < length && chars[i + 1] == ':') {
                sb.append("::");
                i++;
                continue;
            }

            if (c == '?') {
                if (sb.length() > 0) {
                    al.add(new NFormattedTextPart(false, sb.toString()));
                    sb.setLength(0);
                }
                al.add(new NFormattedTextPart(true, ""));
                continue;
            }

            if (c == ':' && i + 1 < length && (Character.isLetter(chars[i + 1]) || chars[i + 1] == '_')) {
                if (sb.length() > 0) {
                    al.add(new NFormattedTextPart(false, sb.toString()));
                    sb.setLength(0);
                }
                int j = i + 1;
                while (j < length && (Character.isLetterOrDigit(chars[j]) || chars[j] == '_')) {
                    j++;
                }
                al.add(new NFormattedTextPart(true, msg.substring(i + 1, j)));
                i = j - 1;
                continue;
            }

            sb.append(c);
        }
        if (sb.length() > 0) {
            al.add(new NFormattedTextPart(false, sb.toString()));
            sb.setLength(0);
        }
        return new NFormattedTextParts(NMsgType.SFORMAT, al);
    }

    protected NText formatPlain(String ss) {
        NFormattedTextParts r = parseStyle(ss);
        StringBuilder sb = new StringBuilder();
        for (NFormattedTextPart part : r.getParts()) {
            if (part.isFormat()) {
                String name = part.getValue();
                Object a;
                if (name.isEmpty()) {
                    // positional '?'
                    if (gParamIndex >= params.length) {
                        throw new NIllegalArgumentException(
                                NMsg.ofC("missing sql parameter at position %s", gParamIndex));
                    }
                    a = resolvePlaceholder(params[gParamIndex]);
                    gParamIndex++;
                } else {
                    // named ':name'
                    Function<String, ?> h = m.placeholders();
                    Object v = h != null ? h.apply(name) : null;
                    a = resolvePlaceholder(v);
                }
                if (a == null) {
                    sb.append((String) null);
                } else if (NTextUtils.isSpecialLiteral(a)) {
                    sb.append(txt.ofStyled(String.valueOf(a), NTextUtils.getSpecialLiteralType(a)));
                } else {
                    sb.append(txt.of(a));
                }
            } else {
                sb.append(part.getValue());
            }
        }
        return txt.of(sb.toString());
    }
}