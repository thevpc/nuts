package net.thevpc.nuts.runtime.core.format.text.highlighters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsTexts;

public class CustomStyleCodeHighlighter implements NutsCodeHighlighter {

    private NutsSession session;
    private NutsTextStyle style;
    private NutsTexts factory;

    public CustomStyleCodeHighlighter(NutsTextStyle style, NutsSession session) {
        this.session = session;
        this.style = style;
        factory = NutsTexts.of(session);
    }

    @Override
    public String getId() {
        return "styled("+style+")";
    }

    @Override
    public NutsText stringToText(String text, NutsSession session) {
        return factory.setSession(session).applyStyles(factory.setSession(session).ofPlain(text), style);
    }

    @Override
    public NutsText tokenToText(String text, String nodeType, NutsSession session) {
        return factory.setSession(session).ofPlain(text);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return NutsComponent.DEFAULT_SUPPORT;
    }

}
