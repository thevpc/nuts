package net.thevpc.nuts.runtime.core.format.text.parser;

import net.thevpc.nuts.NutsString;
import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsWorkspace;

public abstract class AbstractNutsTextNode implements NutsTextNode {
    private NutsWorkspace ws;
    public AbstractNutsTextNode(NutsWorkspace ws) {
        this.ws=ws;
    }

    @Override
    public String toString() {
        return immutable().toString();
    }

    @Override
    public NutsString immutable() {
        return ws.formats().text().builder().append(this).immutable();
    }

    @Override
    public String filteredText() {
        return immutable().filteredText();
    }

    @Override
    public int textLength() {
        return immutable().textLength();
    }

    @Override
    public NutsTextNode toNode() {
        return this;
    }

    @Override
    public boolean isEmpty() {
        return immutable().isEmpty();
    }

}
