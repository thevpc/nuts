package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextVisitor;
import net.thevpc.nuts.text.NTexts;

import java.util.ArrayList;
import java.util.List;

public class NTextNodeCollector implements NTextVisitor {
    private List<NText> all = new ArrayList<>();
    private NSession session;
    public NTextNodeCollector(NSession session) {
        this.session =session;
    }

    @Override
    public void visit(NText node) {
        all.add(node);
    }

    public NText getRootOrNull() {
        if (all.isEmpty()) {
            return null;
        }
        return NTexts.of(session).ofList(all).simplify();
    }

    public NText getRootOrEmpty() {
        return NTexts.of(session).ofList(all).simplify();
    }
}
