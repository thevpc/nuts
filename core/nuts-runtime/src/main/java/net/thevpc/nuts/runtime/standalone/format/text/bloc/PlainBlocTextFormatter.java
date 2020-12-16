package net.thevpc.nuts.runtime.standalone.format.text.bloc;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.runtime.standalone.format.text.parser.BlocTextFormatter;

public class PlainBlocTextFormatter implements BlocTextFormatter {
    NutsWorkspace ws;

    public PlainBlocTextFormatter(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsTextNode toNode(String text) {
        return ws.formats().text().factory().plain(text);
    }
}
