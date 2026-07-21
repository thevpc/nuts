package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.text.NMsg;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NMsgJFormatHelper extends AbstractNMsgFormatHelper{
    int gParamIndex = 0;

    public NMsgJFormatHelper(NMsg m, NTexts txt) {
        super(m, txt);
    }

    public static NFormattedTextParts parseStyle(String msg) {
        if (msg == null) {
            return new NFormattedTextParts(NMsgType.JFORMAT, Collections.emptyList());
        }
        List<NFormattedTextPart> al = new ArrayList<>();
        int length = msg.length();
        char[] chars = msg.toCharArray();
        boolean inText = true;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (inText) {
                if (chars[i] == '{') {
                    if (sb.length() > 0) {
                        al.add(new NFormattedTextPart(false, sb.toString()));
                        sb.setLength(0);
                    }
                    inText = false;
                } else if (chars[i] == '\\') {
                    i++;
                    sb.append(chars[i]);
                } else {
                    sb.append(chars[i]);
                }
            } else {
                if (chars[i] == '}') {
                    al.add(new NFormattedTextPart(true, sb.toString()));
                    if (sb.length() > 0) {
                        sb.setLength(0);
                    }
                    inText = true;
                } else if (chars[i] == '\\') {
                    i++;
                    sb.append(chars[i]);
                } else {
                    sb.append(chars[i]);
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
                int paramIndex = -1;
                int commaPos = formatPart.indexOf(',');
                if (commaPos >= 0) {
                    String paramIndexStr = formatPart.substring(0, commaPos).trim();
                    if (paramIndexStr.isEmpty()) {
                        paramIndex = gParamIndex;
                    } else {
                        paramIndex = NLiteral.of(paramIndexStr).asInt().get();
                    }
                    formatExt = formatPart.substring(commaPos + 1);
                } else {
                    String paramIndexStr = formatPart.trim();
                    if (paramIndexStr.isEmpty()) {
                        paramIndex = gParamIndex;
                    } else {
                        paramIndex = NLiteral.of(paramIndexStr).asInt().get();
                    }
                }
                Object a = resolvePlaceholder(params[paramIndex]);
                if (a == null) {
                    sb.append((String) null);
                } else if (NTextUtils.isSpecialLiteral(a)) {
                    String sb2 = MessageFormat.format("{0" + formatExt + "}", a);
                    sb.append(txt.ofStyled(sb2, NTextUtils.getSpecialLiteralType(a)));
                } else {
                    sb.append(MessageFormat.format("{0" + formatExt + "}", txt.of(a)));
                }
                gParamIndex++;
            } else {
                sb.append(part.getValue());
            }
        }
        return txt.of(sb.toString());
    }
}
