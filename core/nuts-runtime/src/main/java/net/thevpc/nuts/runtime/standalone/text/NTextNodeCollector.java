package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextVisitor;

import java.util.ArrayList;
import java.util.List;

public class NTextNodeCollector implements NTextVisitor {
    private List<NText> all = new ArrayList<>();
    public NTextNodeCollector() {
    }

    @Override
    public void visit(NText node) {
        all.add(node);
    }

    public NText getRootOrNull() {
        if (all.isEmpty()) {
            return null;
        }
        return NText.ofList(all).simplify();
    }

    public NText getRootOrEmpty() {
        return NText.ofList(all).simplify();
    }
}
