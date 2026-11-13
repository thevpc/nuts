package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.text.NMsg;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NMsgJFormatHelper extends AbstractNMsgFormatHelper {
    int gParamIndex = 0;

    public NMsgJFormatHelper(NMsg m) {
        super(m);
    }

    /**
     * Render the message, falling back to the {@link NMsg#toString()} diagnostic when the
     * template is malformed, so that message rendering never throws (see
     * {@link NMsgCFormatHelper#format()}).
     */
    @Override
    public NText format() {
        try {
            return super.format();
        } catch (RuntimeException e) {
            return NText.ofPlain(String.valueOf(m.toString()));
        }
    }

    public static NFormattedTextParts parseStyle(String msg) {
        if (msg == null) {
            return new NFormattedTextParts(NMsgType.JFORMAT, Collections.emptyList());
        }
        List<NFormattedTextPart> al = new ArrayList<>();
        int length = msg.length();
        char[] chars = msg.toCharArray();
        boolean inText = true;
        // MessageFormat quoting: a single quote starts a quoted literal in which '{' and '}'
        // are plain text, '' is one literal quote, and the quotes themselves are removed
        boolean inQuote = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = chars[i];
            if (inQuote) {
                if (c == '\'') {
                    if (i + 1 < length && chars[i + 1] == '\'') {
                        sb.append('\'');
                        i++;
                    } else {
                        inQuote = false;
                    }
                } else {
                    sb.append(c);
                }
            } else if (c == '\'') {
                inQuote = true;
            } else if (inText) {
                if (c == '{') {
                    if (sb.length() > 0) {
                        al.add(new NFormattedTextPart(false, sb.toString()));
                        sb.setLength(0);
                    }
                    inText = false;
                } else if (c == '\\') {
                    i++;
                    sb.append(chars[i]);
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '}') {
                    al.add(new NFormattedTextPart(true, sb.toString()));
                    if (sb.length() > 0) {
                        sb.setLength(0);
                    }
                    inText = true;
                } else if (c == '\\') {
                    i++;
                    sb.append(chars[i]);
                } else {
                    sb.append(c);
                }
            }
        }
        if (sb.length() > 0) {
            al.add(new NFormattedTextPart(!inText, sb.toString()));
            sb.setLength(0);
        }
        return new NFormattedTextParts(NMsgType.JFORMAT, al);
    }

    protected NText formatPlain(String ss) {
        NFormattedTextParts r = parseStyle(ss);
        StringBuilder sb = new StringBuilder();
        for (NFormattedTextPart part : r.getParts()) {
            if (part.isFormat()) {
                String formatExt = "";
                String formatPart = part.getValue();
                int paramIndex;
                int commaPos = formatPart.indexOf(',');
                if (commaPos >= 0) {
                    String paramIndexStr = formatPart.substring(0, commaPos).trim();
                    if (paramIndexStr.isEmpty()) {
                        paramIndex = gParamIndex;
                    } else {
                        paramIndex = NLiteral.of(paramIndexStr).asInt().get();
                    }
                    // keep the comma: MessageFormat expects "{0,number,00000}"
                    formatExt = formatPart.substring(commaPos);
                } else {
                    String paramIndexStr = formatPart.trim();
                    if (paramIndexStr.isEmpty()) {
                        paramIndex = gParamIndex;
                    } else {
                        paramIndex = NLiteral.of(paramIndexStr).asInt().get();
                    }
                }
                if (paramIndex < 0 || paramIndex >= params.length) {
                    throw new IllegalArgumentException("invalid index " + paramIndex + " in message (not in 0.." + (params.length - 1) + ")");
                }
                Object a = resolvePlaceholder(params[paramIndex]);
                if (a == null) {
                    // MessageFormat renders a null argument as "null"
                    sb.append("null");
                } else if (NTextUtils.isSpecialLiteral(a)) {
                    String sb2 = MessageFormat.format("{0" + formatExt + "}", a);
                    sb.append(NText.ofStyled(sb2, NTextUtils.getSpecialLiteralType(a)));
                } else {
                    sb.append(MessageFormat.format("{0" + formatExt + "}", NText.of(a)));
                }
                gParamIndex++;
            } else {
                sb.append(part.getValue());
            }
        }
        return NText.of(sb.toString());
    }
}
