package net.thevpc.nuts.runtime.standalone.format.text.bloc;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeFactory;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.format.text.parser.BlocTextFormatter;

public class CustomStyleBlocTextFormatter implements BlocTextFormatter {
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
}
