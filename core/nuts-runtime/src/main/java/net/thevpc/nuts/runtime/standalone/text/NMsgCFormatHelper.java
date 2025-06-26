package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NMsg;

import java.util.Formatter;

public class NMsgCFormatHelper extends AbstractNMsgFormatHelper{
    int paramIndex = 0;

    public NMsgCFormatHelper(NMsg m, NTexts txt) {
        super(m,txt);
    }


    protected NText formatPlain(String ss) {
        NFormattedTextParts r = NFormattedTextParts.parseCFormat(ss);
        NTextBuilder sb = NTextBuilder.of();
        for (NFormattedTextPart part : r.getParts()) {
            if (part.isFormat()) {
                if (part.getValue().equals("%%")) {
                    sb.append("%");
                } else if (part.getValue().equals("%n")) {
                    sb.append("\n");
                } else {
                    if (paramIndex < 0 || paramIndex >= params.length) {
                        throw new NIllegalArgumentException(NMsg.ofPlain("invalid index in message"));
                    }
                    Object a = params[paramIndex];
                    if (a == null) {
                        sb.append((String) null);
                    } else if (NTextUtils.isSpecialLiteral(a)) {
                        StringBuilder sb2 = new StringBuilder();
                        new Formatter(sb2, locale).format(part.getValue(), a);
                        sb.append(txt.ofStyled(sb2.toString(), NTextUtils.getSpecialLiteralType(a)));
                    } else {
//                                StringBuilder sb2 = new StringBuilder();
//                                new Formatter(sb2, locale).format(part.getValue(), txt.ofText(a));
//                                sb.append(sb2);
                        sb.append(txt.of(a));
                    }
                    paramIndex++;
                }
            } else {
                sb.append(part.getValue());
            }
        }
        return sb.build();
    }
}
