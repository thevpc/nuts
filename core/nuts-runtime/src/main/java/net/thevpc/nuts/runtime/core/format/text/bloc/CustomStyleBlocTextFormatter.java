package net.thevpc.nuts.runtime.core.format.text.bloc;

import net.thevpc.nuts.NutsSupportLevelContext;
import net.thevpc.nuts.NutsTextStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsCodeFormat;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextManager;
import net.thevpc.nuts.NutsText;

public class CustomStyleBlocTextFormatter implements NutsCodeFormat {

    private NutsWorkspace ws;
    private NutsTextStyle style;
    private NutsTextManager factory;

    public CustomStyleBlocTextFormatter(NutsTextStyle style, NutsWorkspace ws) {
        this.ws = ws;
        this.style = style;
        factory = ws.formats().text();
    }

    @Override
    public NutsText stringToText(String text, NutsSession session) {
        return factory.setSession(session).forStyled(factory.setSession(session).forPlain(text), style);
    }

    @Override
    public NutsText tokenToText(String text, String nodeType, NutsSession session) {
        return factory.setSession(session).forPlain(text);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> criteria) {
        return NutsComponent.DEFAULT_SUPPORT;
    }

}
