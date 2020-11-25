package net.thevpc.nuts.runtime.util.fprint;

import net.thevpc.nuts.runtime.util.fprint.parser.TextNode;
import net.thevpc.nuts.runtime.util.fprint.parser.TextNodeList;

import java.util.ArrayList;
import java.util.List;

public class TextNodeCollector implements TextNodeVisitor {
    private List<TextNode> all = new ArrayList<>();

    @Override
    public void visit(TextNode node) {
        all.add(node);
    }

    public TextNode getRootOrNull() {
        if (all.isEmpty()) {
            return null;
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new TextNodeList(all.toArray(new TextNode[0]));
    }

    public TextNode getRootOrEmpty() {
        if (all.isEmpty()) {
            return new TextNodeList();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new TextNodeList(all.toArray(new TextNode[0]));
    }
}
