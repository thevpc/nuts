package net.thevpc.nuts.lib.doc.processor.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ListAstNode extends TagNode {
    List<TagNode> children;

    public ListAstNode(TagNode[] children) {
        this.children = new ArrayList<>(Arrays.asList(children));
    }

    public ListAstNode(List<TagNode> children) {
        this.children = new ArrayList<>(children);
    }

    public static final TagNode of(List<TagNode> all) {
        if (all.size() == 0) {
            return null;
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new ListAstNode(all);
    }

    public static final TagNode of(TagNode[] all) {
        if (all.length == 0) {
            return null;
        }
        if (all.length == 1) {
            return all[0];
        }
        return new ListAstNode(all);
    }

    @Override
    public void run(ProcessStreamContext ctx) throws IOException {
        for (TagNode child : children) {
            child.run(ctx);
        }
    }
}
