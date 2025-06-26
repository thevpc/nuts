package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NUnsupportedEnumException;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NMsgVFormatHelper {
    private NMsg m;
    private NTexts txt;
    private NText nText;
    private Locale locale;
    private NSession session;
    private String smsg;
    Object[] params;

    Function<String, NText> mapper = null;
    public NMsgVFormatHelper(NMsg m, NTexts txt) {
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
        Object param = params == null ? (Collections.emptyMap()) : params[0];
        if (param instanceof Map) {
            mapper = x -> {
                Object u = ((Map<String, ?>) param).get(x);
                if (u == null) {
                    return null;
                }
                return txt.of(u);
            };
        } else {
            Function<String, ?> f = (Function<String, ?>) param;
            mapper = x -> {
                Object u = f.apply(x);
                if (u == null) {
                    return null;
                }
                return txt.of(u);
            };
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
        if (ss == null) {
            return txt.of("");
        }
        List<NText> dd = NStringUtils.parseDollarPlaceHolder(ss)
                .map(t -> {
                    switch (t.ttype) {
                        case NToken.TT_DOLLAR:
                        case NToken.TT_DOLLAR_BRACE: {
                            NText x = mapper.apply(t.sval);
                            if (x == null) {
                                throw new IllegalArgumentException("var not found " + t.sval);
                            }
                            return x;
                        }
                    }
                    return txt.ofPlain(t.sval);
                }).collect(Collectors.toList());
        NTextBuilder sb = NTextBuilder.of();
        sb.appendAll(dd);
        return sb.build();
    }
}
