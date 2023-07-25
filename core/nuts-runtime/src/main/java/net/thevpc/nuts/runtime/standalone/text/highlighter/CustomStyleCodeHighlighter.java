package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

public class CustomStyleCodeHighlighter implements NCodeHighlighter {

    private NSession session;
    private NTextStyle style;

    public CustomStyleCodeHighlighter(NTextStyle style, NSession session) {
        this.session = session;
        this.style = style;
    }

    @Override
    public String getId() {
        return "styled("+style+")";
    }

    @Override
    public NText stringToText(String text, NTexts txt, NSession session) {
        return txt.ofStyled(text, style);
    }

    @Override
    public NText tokenToText(String text, String nodeType, NTexts txt, NSession session) {
        return txt.ofPlain(text);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NCallableSupport.DEFAULT_SUPPORT;
    }

}
