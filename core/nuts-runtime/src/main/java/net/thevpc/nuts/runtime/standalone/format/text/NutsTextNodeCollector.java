package net.thevpc.nuts.runtime.standalone.format.text;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeVisitor;
import net.thevpc.nuts.NutsWorkspace;

import java.util.ArrayList;
import java.util.List;

public class NutsTextNodeCollector implements NutsTextNodeVisitor {
    private List<NutsTextNode> all = new ArrayList<>();
    private NutsWorkspace ws;
    public NutsTextNodeCollector(NutsWorkspace ws) {
        this.ws=ws;
    }

    @Override
    public void visit(NutsTextNode node) {
        all.add(node);
    }

    public NutsTextNode getRootOrNull() {
        if (all.isEmpty()) {
            return null;
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return ws.formats().text().factory().list(all);
    }

    public NutsTextNode getRootOrEmpty() {
        return ws.formats().text().factory().list(all);
    }
}
