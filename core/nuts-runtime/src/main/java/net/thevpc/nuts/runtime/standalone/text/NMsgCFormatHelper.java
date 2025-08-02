package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NMsg;

import java.util.Formatter;

public class NMsgCFormatHelper extends AbstractNMsgFormatHelper {
    int paramIndex = 0;

    public NMsgCFormatHelper(NMsg m, NTexts txt) {
        super(m, txt);
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
                        throw new NIllegalArgumentException(NMsg.ofPlain("invalid index "+paramIndex+" in message (not in 0.."+(params.length-1)+")"));
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
                        NText u = txt.of(a);
                        if (u.getType() == NTextType.PLAIN) {
                            sb.append(NText.ofPlain(doFormatPlain(((NTextPlain) u).getValue(), part.getValue())));
                        } else if (u.getType() == NTextType.STYLED && ((NTextStyled) u).getChild().getType() == NTextType.PLAIN) {
                            sb.append(NText.ofStyled(doFormatPlain(
                                    ((NTextPlain) ((NTextStyled) u).getChild()).getValue()
                                    , part.getValue()), ((NTextStyled) u).getStyles()));
                        } else {
                            //TODO : must support more complex NText tre
                            sb.append(u);
                        }
                    }
                    paramIndex++;
                }
            } else {
                sb.append(part.getValue());
            }
        }
        return sb.build();
    }

    private String doFormatPlain(String value, String pattern) {
        StringBuilder sb2 = new StringBuilder();
        new Formatter(sb2, locale).format(pattern, value == null ? "null" : value);
        return sb2.toString();
    }
}
