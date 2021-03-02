package net.thevpc.nuts.runtime.core.format.text.bloc;

import net.thevpc.nuts.NutsSupportLevelContext;
import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsCodeFormat;
import net.thevpc.nuts.NutsTextManager;

public class CustomStyleBlocTextFormatter implements NutsCodeFormat {

    private NutsWorkspace ws;
    private NutsTextNodeStyle style;
    private NutsTextManager factory;

    public CustomStyleBlocTextFormatter(NutsTextNodeStyle style, NutsWorkspace ws) {
        this.ws = ws;
        this.style = style;
        factory = ws.formats().text();
    }

    @Override
    public NutsTextNode textToNode(String text) {
        return factory.styled(factory.plain(text), style);
    }

    @Override
    public NutsTextNode tokenToNode(String text, String nodeType) {
        return factory.plain(text);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> criteria) {
        return NutsComponent.DEFAULT_SUPPORT;
    }

}
