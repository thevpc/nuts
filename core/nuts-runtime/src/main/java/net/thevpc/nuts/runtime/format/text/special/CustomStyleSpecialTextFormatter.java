package net.thevpc.nuts.runtime.format.text.special;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeFactory;
import net.thevpc.nuts.NutsTextNodeStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.format.text.parser.SpecialTextFormatter;

public class CustomStyleSpecialTextFormatter implements SpecialTextFormatter {
    private NutsWorkspace ws;
    private NutsTextNodeStyle style;

    public CustomStyleSpecialTextFormatter(NutsTextNodeStyle style, NutsWorkspace ws) {
        this.ws = ws;
        this.style = style;
    }

    @Override
    public NutsTextNode toNode(String text) {
        NutsTextNodeFactory factory = ws.formats().text().factory();
        return factory.styled(factory.plain(text), style);
    }
}
