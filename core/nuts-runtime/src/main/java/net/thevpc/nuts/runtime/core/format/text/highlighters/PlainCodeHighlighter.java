package net.thevpc.nuts.runtime.core.format.text.highlighters;

import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsCodeHighlighter;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextManager;
import net.thevpc.nuts.NutsText;

public class PlainCodeHighlighter implements NutsCodeHighlighter {

    NutsWorkspace ws;
    private NutsTextManager factory;

    public PlainCodeHighlighter(NutsWorkspace ws) {
        this.ws = ws;
        factory = NutsWorkspaceUtils.defaultSession(ws).text();
    }

    @Override
    public String getId() {
        return "plain";
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
