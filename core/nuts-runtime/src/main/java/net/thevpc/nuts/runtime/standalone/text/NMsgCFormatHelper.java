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

    public NMsgCFormatHelper(NMsg m) {
        super(m);
    }

    /**
     * Render the message, falling back to the {@link NMsg#toString()} diagnostic when the
     * template is malformed. Message rendering must never throw: it happens on logging and
     * error paths, and the api (workspace free) renderer reports such templates as
     * {@code [ERROR] Invalid ...} rather than failing the caller.
     */
    @Override
    public NText format() {
        try {
            return super.format();
        } catch (RuntimeException e) {
            return NText.ofPlain(String.valueOf(m.toString()));
        }
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
                throw new UnknownFormatConversionException(String.valueOf(c) + " in " + s);
            }
        }
    }

    protected NText formatPlain(String ss) {
        NFormattedTextParts r = parseStyle(ss);
        NTextBuilder sb = NTextBuilder.of();
        for (NFormattedTextPart part : r.getParts()) {
            if (part.isFormat()) {
                String spec = part.getValue();
                if (spec.equals("%%")) {
                    sb.append("%");
                } else if (spec.equals("%n")) {
                    // same as java.util.Formatter: the platform line separator
                    sb.append(System.lineSeparator());
                } else {
                    // an explicit argument index (%1$s) selects its parameter and does not
                    // advance the sequential index, exactly like java.util.Formatter
                    int explicitIndex = explicitIndex(spec);
                    int currentIndex = explicitIndex >= 0 ? explicitIndex : paramIndex;
                    if (currentIndex < 0 || currentIndex >= params.length) {
                        throw new NIllegalArgumentException(NMsg.ofP("invalid index " + currentIndex + " in message (not in 0.." + (params.length - 1) + ")"));
                    }
                    Object a = resolvePlaceholder(params[currentIndex]);
                    if (explicitIndex < 0) {
                        paramIndex++;
                    }
                    if (a == null) {
                        // printf renders a null argument as "null"
                        sb.append("null");
                    } else if (NTextUtils.isSpecialLiteral(a)) {
                        sb.append(NText.ofStyled(formatWith(spec, a), NTextUtils.getSpecialLiteralType(a)));
                    } else if (a instanceof NText) {
                        NText u = (NText) a;
                        if (u.type() == NTextType.PLAIN) {
                            sb.append(NText.ofPlain(doFormatPlain(((NTextPlain) u).value(), spec)));
                        } else if (u.type() == NTextType.STYLED && ((NTextStyled) u).child().type() == NTextType.PLAIN) {
                            sb.append(NText.ofStyled(doFormatPlain(
                                    ((NTextPlain) ((NTextStyled) u).child()).value()
                                    , spec), ((NTextStyled) u).styles()));
                        } else {
                            //TODO : must support more complex NText tree
                            sb.append(u);
                        }
                    } else {
                        // any other value goes to Formatter untouched, so that conversions
                        // like %c, %b or %tF keep working instead of seeing a String
                        sb.append(NText.ofPlain(formatWith(spec, a)));
                    }
                }
            } else {
                sb.append(part.getValue());
            }
        }
        return sb.build();
    }

    /**
     * 1-based explicit argument index of a printf specifier ({@code %1$s}) as a 0-based
     * index, or -1 when the specifier carries none.
     */
    private static int explicitIndex(String spec) {
        int len = spec.length();
        if (len > 2 && spec.charAt(0) == '%') {
            int i = 1;
            int value = 0;
            boolean anyDigit = false;
            while (i < len && spec.charAt(i) >= '0' && spec.charAt(i) <= '9') {
                value = value * 10 + (spec.charAt(i) - '0');
                anyDigit = true;
                i++;
            }
            if (anyDigit && i < len && spec.charAt(i) == '$') {
                return value - 1;
            }
        }
        return -1;
    }

    private String formatWith(String pattern, Object value) {
        StringBuilder sb2 = new StringBuilder();
        new Formatter(sb2, locale).format(pattern, value);
        return sb2.toString();
    }

    private String doFormatPlain(String value, String pattern) {
        return formatWith(pattern, value == null ? "null" : value);
    }
}
