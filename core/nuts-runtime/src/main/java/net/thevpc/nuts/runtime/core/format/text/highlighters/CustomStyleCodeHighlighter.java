package net.thevpc.nuts.runtime.core.format.text.highlighters;

import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.NutsTextStyle;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsCodeHighlighter;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextManager;
import net.thevpc.nuts.NutsText;

public class CustomStyleCodeHighlighter implements NutsCodeHighlighter {

    private NutsSession session;
    private NutsTextStyle style;
    private NutsTextManager factory;

    public CustomStyleCodeHighlighter(NutsTextStyle style, NutsSession session) {
        this.session = session;
        this.style = style;
        factory = session.text();
    }

    @Override
    public String getId() {
        return "styled("+style+")";
    }

    @Override
    public NutsText stringToText(String text, NutsSession session) {
        return factory.setSession(session).ofStyled(factory.setSession(session).ofPlain(text), style);
    }

    @Override
    public NutsText tokenToText(String text, String nodeType, NutsSession session) {
        return factory.setSession(session).ofPlain(text);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> context) {
        return NutsComponent.DEFAULT_SUPPORT;
    }

}
