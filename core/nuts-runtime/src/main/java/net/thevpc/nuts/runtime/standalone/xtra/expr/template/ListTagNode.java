package net.thevpc.nuts.runtime.standalone.xtra.expr.template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ListTagNode extends TagNode {
    List<TagNode> children;

    public ListTagNode(TagNode[] children) {
        this.children = new ArrayList<>(Arrays.asList(children));
    }

    public ListTagNode(List<TagNode> children) {
        this.children = new ArrayList<>(children);
    }

    public static final TagNode of(List<TagNode> all) {
        if (all.size() == 0) {
            return null;
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new ListTagNode(all);
    }

    public static final TagNode of(TagNode[] all) {
        if (all.length == 0) {
            return null;
        }
        if (all.length == 1) {
            return all[0];
        }
        return new ListTagNode(all);
    }

    @Override
    public void run(ProcessStreamContext ctx) throws IOException {
        for (TagNode child : children) {
            child.run(ctx);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (TagNode child : children) {
            builder.append(child).append("\n");
        }
        return builder.toString();
    }
}
