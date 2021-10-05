package net.thevpc.nuts.runtime.core.format.text.bloc;

import net.thevpc.nuts.NutsSupportLevelContext;
import net.thevpc.nuts.NutsTextStyle;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsCodeFormat;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextManager;
import net.thevpc.nuts.NutsText;

public class CustomStyleBlocTextFormatter implements NutsCodeFormat {

    private NutsSession session;
    private NutsTextStyle style;
    private NutsTextManager factory;

    public CustomStyleBlocTextFormatter(NutsTextStyle style, NutsSession session) {
        this.session = session;
        this.style = style;
        factory = session.text();
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
