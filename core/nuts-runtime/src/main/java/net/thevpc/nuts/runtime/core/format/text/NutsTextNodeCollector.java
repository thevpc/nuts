package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsWorkspace;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NutsText;
import net.thevpc.nuts.NutsTextVisitor;

public class NutsTextNodeCollector implements NutsTextVisitor {
    private List<NutsText> all = new ArrayList<>();
    private NutsWorkspace ws;
    public NutsTextNodeCollector(NutsWorkspace ws) {
        this.ws=ws;
    }

    @Override
    public void visit(NutsText node) {
        all.add(node);
    }

    public NutsText getRootOrNull() {
        if (all.isEmpty()) {
            return null;
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return ws.formats().text().forList(all);
    }

    public NutsText getRootOrEmpty() {
        return ws.formats().text().forList(all);
    }
}
