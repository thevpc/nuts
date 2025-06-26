package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.text.MessageFormat;

public class NMsgJFormatHelper extends AbstractNMsgFormatHelper{
    int gParamIndex = 0;

    public NMsgJFormatHelper(NMsg m, NTexts txt) {
        super(m, txt);
    }

    protected NText formatPlain(String ss) {
        NFormattedTextParts r = NFormattedTextParts.parseJStyle(ss);
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
                Object a = params[paramIndex];
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
