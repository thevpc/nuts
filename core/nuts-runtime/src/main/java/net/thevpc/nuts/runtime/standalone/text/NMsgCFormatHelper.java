package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.text.NMsg;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NMsgCFormatHelper extends AbstractNMsgFormatHelper {
    int paramIndex = 0;
    private static Pattern CFORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])");

    public NMsgCFormatHelper(NMsg m, NTexts txt) {
        super(m, txt);
    }


    protected NFormattedTextParts parseStyle(String msg) {
        if (msg == null) {
            return new NFormattedTextParts(NMsgType.CFORMAT, Collections.emptyList());
        }
        List<NFormattedTextPart> al = new ArrayList<>();
        Matcher m = CFORMAT_PATTERN.matcher(msg);
        int length = msg.length();
        for (int i = 0; i < length; ) {
            if (m.find(i)) {
                if (m.start() != i) {
                    checkCFormatText(msg, i, m.start());
                    al.add(new NFormattedTextPart(false, msg.substring(i, m.start())));
                }

                al.add(new NFormattedTextPart(true, m.group()));
                i = m.end();
            } else {
                checkCFormatText(msg, i, length);
                al.add(new NFormattedTextPart(false, msg.substring(i)));
                break;
            }
        }
        return new NFormattedTextParts(NMsgType.CFORMAT, al);
    }


    private static void checkCFormatText(String s, int start, int end) {
        for (int i = start; i < end; i++) {
            // Any '%' found in the region starts an invalid format specifier.
            if (s.charAt(i) == '%') {
                char c = (i == end - 1) ? '%' : s.charAt(i + 1);
                throw new UnknownFormatConversionException(String.valueOf(c)+" in "+s);
            }
        }
    }

    protected NText formatPlain(String ss) {
        NFormattedTextParts r = parseStyle(ss);
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
                    Object a = resolvePlaceholder(params[paramIndex]);
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
                        if (u.type() == NTextType.PLAIN) {
                            sb.append(NText.ofPlain(doFormatPlain(((NTextPlain) u).value(), part.getValue())));
                        } else if (u.type() == NTextType.STYLED && ((NTextStyled) u).child().type() == NTextType.PLAIN) {
                            sb.append(NText.ofStyled(doFormatPlain(
                                    ((NTextPlain) ((NTextStyled) u).child()).value()
                                    , part.getValue()), ((NTextStyled) u).styles()));
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
