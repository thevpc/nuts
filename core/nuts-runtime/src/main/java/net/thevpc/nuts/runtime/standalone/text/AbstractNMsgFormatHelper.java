package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NUnsupportedEnumException;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class AbstractNMsgFormatHelper {
    protected NMsg m;
    protected NTexts txt;
    protected NText nText;
    protected Locale locale;
    protected NSession session;
    protected String smsg;
    protected Object[] params;

    public AbstractNMsgFormatHelper(NMsg m, NTexts txt) {
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
                String ss = ((NTextPlain) t).getValue();
                return formatPlain(ss);
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

    protected abstract NText formatPlain(String ss) ;
}
