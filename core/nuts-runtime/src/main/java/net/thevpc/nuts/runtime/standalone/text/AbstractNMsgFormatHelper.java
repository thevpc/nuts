package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.util.NUnsupportedEnumException;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NMsgSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

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
        Object msg = m.message();
        smsg = (String) msg;
        nText = txt.of(smsg);
        session = NSession.of();
        String sLocale = session.locale().orDefault();
        locale = NBlankable.isBlank(sLocale) ? null : new Locale(sLocale);
        params = m.params();
        if (params == null) {
            params = new Object[0];
        }
    }

    public Object resolvePlaceholder(Object any) {
        if(any instanceof NMsg) {
            return ((NMsg) any).withPlaceholders(m.placeholders());
        }
        if(any instanceof NMsg.Placeholder) {
            String n = ((NMsg.Placeholder) any).name();
            Function<String, ?> h = m.placeholders();
            if(h!=null) {
                Object v = h.apply(n);
                if(v!=null){
                    return v;
                }
            }
        }
        if(any instanceof NMsgSupplier) {
            return ((NMsgSupplier)any).apply(m);
        }
        if(any instanceof Supplier && any.getClass().isSynthetic()) {
            return ((Supplier)any).get();
        }
        return any;
    }

    public NText format() {
        return convertCurrent(nText);
    }

    private String convertPlain(String t) {
        return convertCurrent(txt.ofPlain(t)).filteredText();
    }

    private NText convertCurrent(NText t) {
        switch (t.type()) {
            case PLAIN: {
                String ss = ((NTextPlain) t).value();
                return formatPlain(ss);
            }
            case CODE: {
                NTextCode c = (NTextCode) t;
                return txt.ofCode(convertPlain(c.value()), c.qualifier(), c.separator());
            }
            case LINK: {
                NTextLink c = (NTextLink) t;
                return txt.ofLink(convertPlain(c.value()), c.separator());
            }
            case ANCHOR: {
                NTextAnchor c = (NTextAnchor) t;
                return txt.ofAnchor(convertPlain(c.value()), c.separator());
            }
            case INCLUDE: {
                NTextInclude c = (NTextInclude) t;
                return txt.ofInclude(convertPlain(c.text()), c.separator());
            }
            case TITLE: {
                NTextTitle c = (NTextTitle) t;
                return txt.ofTitle(convertCurrent(c.child()), c.level());
            }
            case COMMAND: {
                NTextCmd c = (NTextCmd) t;
                NTerminalCmd tc = c.terminalCommand();
                List<String> newArgs = new ArrayList<>();
                for (String arg : tc.args()) {
                    newArgs.add(convertPlain(arg));
                }
                return txt.ofCommand(NTerminalCmd.of(tc.name(), newArgs.toArray(new String[0])));
            }
            case STYLED: {
                NTextStyled c = (NTextStyled) t;
                return txt.ofStyled(convertCurrent(c.child()), c.styles());
            }
            case LIST: {
                NTextList c = (NTextList) t;
                List<NText> rr = new ArrayList<>();
                for (NText child : c.children()) {
                    rr.add(convertCurrent(child));
                }
                return txt.ofList(rr);
            }
            case BUILDER: {
                NTextBuilder c = (NTextBuilder) t;
                return convertCurrent(c.build());
            }
        }
        throw new NUnsupportedEnumException(t.type());
    }

    protected abstract NText formatPlain(String ss);
}
