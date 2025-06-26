package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NUnsupportedEnumException;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NMsgJFormatHelper {
    private NMsg m;
    private NTexts txt;
    private NText nText;
    private Locale locale;
    private NSession session;
    private String smsg;
    int gParamIndex = 0;
    Object[] params;

    public NMsgJFormatHelper(NMsg m, NTexts txt) {
        this.m = m;
        this.txt = txt;
        Object msg = m.getMessage();
        smsg = (String) msg;
        nText = txt.of(smsg);
        session = NSession.of();
        String sLocale = session.getLocale().orDefault();
        locale = NBlankable.isBlank(sLocale) ? null : new Locale(sLocale);
        params = m.getParams();
        if (params == null) {
            params = new Object[0];
        }
    }

    public NText format() {
        return convertCurrent(nText);
    }

    private String convertPlain(String t) {
        return convertCurrent(txt.ofPlain(t)).filteredText();
    }

    private NText convertCurrent(NText t) {
        switch (t.getType()) {
            case PLAIN: {
                String ss = ((NTextPlain) t).getText();
                return _txt(ss);
            }
            case CODE: {
                NTextCode c = (NTextCode) t;
                return txt.ofCode(convertPlain(c.getValue()), c.getQualifier(), c.getSeparator());
            }
            case LINK: {
                NTextLink c = (NTextLink) t;
                return txt.ofLink(convertPlain(c.getValue()), c.getSeparator());
            }
            case ANCHOR: {
                NTextAnchor c = (NTextAnchor) t;
                return txt.ofAnchor(convertPlain(c.getValue()), c.getSeparator());
            }
            case INCLUDE: {
                NTextInclude c = (NTextInclude) t;
                return txt.ofInclude(convertPlain(c.getText()), c.getSeparator());
            }
            case TITLE: {
                NTextTitle c = (NTextTitle) t;
                return txt.ofTitle(convertCurrent(c.getChild()), c.getLevel());
            }
            case COMMAND: {
                NTextCmd c = (NTextCmd) t;
                NTerminalCmd tc = c.getCommand();
                List<String> newArgs = new ArrayList<>();
                for (String arg : tc.getArgs()) {
                    newArgs.add(convertPlain(arg));
                }
                return txt.ofCommand(NTerminalCmd.of(tc.getName(), newArgs.toArray(new String[0])));
            }
            case STYLED: {
                NTextStyled c = (NTextStyled) t;
                return txt.ofStyled(convertCurrent(c.getChild()), c.getStyles());
            }
            case LIST: {
                NTextList c = (NTextList) t;
                List<NText> rr = new ArrayList<>();
                for (NText child : c.getChildren()) {
                    rr.add(convertCurrent(child));
                }
                return txt.ofList(rr);
            }
            case BUILDER: {
                NTextBuilder c = (NTextBuilder) t;
                return convertCurrent(c.build());
            }
        }
        throw new NUnsupportedEnumException(t.getType());
    }

    private NText _txt(String ss) {
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
                } else if (DefaultNTexts.isSpecialLiteral(a)) {
                    String sb2 = MessageFormat.format("{0" + formatExt + "}", a);
                    sb.append(txt.ofStyled(sb2, DefaultNTexts.getSpecialLiteralType(a)));
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
