package net.thevpc.nuts.runtime.core.format.text.highlighters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsTexts;

public class PlainCodeHighlighter implements NutsCodeHighlighter {

    NutsWorkspace ws;
    private NutsTexts factory;

    public PlainCodeHighlighter(NutsWorkspace ws) {
        this.ws = ws;
        factory = NutsTexts.of(NutsWorkspaceUtils.defaultSession(ws));
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
