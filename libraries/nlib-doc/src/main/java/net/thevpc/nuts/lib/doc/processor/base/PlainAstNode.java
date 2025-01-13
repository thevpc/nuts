package net.thevpc.nuts.lib.doc.processor.base;

import java.io.IOException;

class PlainAstNode extends TagNode {
    private String value;

    public PlainAstNode(String value) {
        this.value = value;
    }

    public void run(ProcessStreamContext ctx) throws IOException {
        ctx.out.write(value);
    }
}
