package net.thevpc.nuts.runtime.util.fprint;

import net.thevpc.nuts.runtime.util.fprint.parser.TextNode;

public interface TextNodeVisitor {
    void visit(TextNode node);
}
