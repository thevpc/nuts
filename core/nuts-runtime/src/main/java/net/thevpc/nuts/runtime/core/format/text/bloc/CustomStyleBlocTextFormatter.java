package net.thevpc.nuts.runtime.core.format.text.bloc;

import net.thevpc.nuts.NutsSupportLevelContext;
import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeFactory;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsCodeFormat;

public class CustomStyleBlocTextFormatter implements NutsCodeFormat {

    private NutsWorkspace ws;
    private NutsTextNodeStyle style;

    public CustomStyleBlocTextFormatter(NutsTextNodeStyle style, NutsWorkspace ws) {
        this.ws = ws;
        this.style = style;
    }

    @Override
    public NutsTextNode toNode(String text) {
        NutsTextNodeFactory factory = ws.formats().text().factory();
        return factory.styled(factory.plain(text), style);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> criteria) {
        return NutsComponent.DEFAULT_SUPPORT;
    }

}
