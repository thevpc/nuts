package net.thevpc.nuts.runtime.format.text.special;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.runtime.format.text.parser.SpecialTextFormatter;

public class PlainSpecialTextFormatter implements SpecialTextFormatter {
    NutsWorkspace ws;

    public PlainSpecialTextFormatter(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsTextNode toNode(String text) {
        return ws.formats().text().factory().plain(text);
    }
}
