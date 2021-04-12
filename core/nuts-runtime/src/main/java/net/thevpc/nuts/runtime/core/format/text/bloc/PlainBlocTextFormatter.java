package net.thevpc.nuts.runtime.core.format.text.bloc;

import net.thevpc.nuts.NutsSupportLevelContext;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsCodeFormat;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextManager;

public class PlainBlocTextFormatter implements NutsCodeFormat {

    NutsWorkspace ws;
    private NutsTextManager factory;

    public PlainBlocTextFormatter(NutsWorkspace ws) {
        this.ws = ws;
        factory = ws.formats().text();
    }

    @Override
    public NutsTextNode tokenToNode(String text, String nodeType, NutsSession session) {
        factory.setSession(session);
        return factory.plain(text);
    }

    @Override
    public NutsTextNode textToNode(String text, NutsSession session) {
        factory.setSession(session);
        return factory.plain(text);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> criteria) {
        return NutsComponent.DEFAULT_SUPPORT;
    }

}
