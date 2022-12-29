package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextVisitor;
import net.thevpc.nuts.text.NTexts;

import java.util.ArrayList;
import java.util.List;

public class NTextNodeCollector implements NTextVisitor {
    private List<NText> all = new ArrayList<>();
    private NSession ws;
    public NTextNodeCollector(NSession ws) {
        this.ws=ws;
    }

    @Override
    public void visit(NText node) {
        all.add(node);
    }

    public NText getRootOrNull() {
        if (all.isEmpty()) {
            return null;
        }
        return NTexts.of(ws).ofList(all).simplify();
    }

    public NText getRootOrEmpty() {
        return NTexts.of(ws).ofList(all).simplify();
    }
}
