package net.thevpc.nuts.runtime.core.format.text.bloc;

import net.thevpc.nuts.NutsSupportLevelContext;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsCodeFormat;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextManager;
import net.thevpc.nuts.NutsText;

public class PlainBlocTextFormatter implements NutsCodeFormat {

    NutsWorkspace ws;
    private NutsTextManager factory;

    public PlainBlocTextFormatter(NutsWorkspace ws) {
        this.ws = ws;
        factory = ws.text();
    }

    @Override
    public NutsText tokenToText(String text, String nodeType, NutsSession session) {
        factory.setSession(session);
        return factory.ofPlain(text);
    }

    @Override
    public NutsText stringToText(String text, NutsSession session) {
        factory.setSession(session);
        return factory.ofPlain(text);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> context) {
        return NutsComponent.DEFAULT_SUPPORT;
    }

}
