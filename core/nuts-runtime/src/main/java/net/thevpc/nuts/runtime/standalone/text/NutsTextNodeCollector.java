package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;

import java.util.ArrayList;
import java.util.List;

public class NutsTextNodeCollector implements NutsTextVisitor {
    private List<NutsText> all = new ArrayList<>();
    private NutsSession ws;
    public NutsTextNodeCollector(NutsSession ws) {
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
        return NutsTexts.of(ws).ofList(all).simplify();
    }

    public NutsText getRootOrEmpty() {
        return NutsTexts.of(ws).ofList(all).simplify();
    }
}
