package net.thevpc.nuts.lib.doc.processor.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;

class VarAstNode extends TagNode {
    private final String exprLang;
    private String expr;

    public VarAstNode(String exprLang, String expr) {
        this.exprLang = exprLang;
        this.expr = expr;
    }

    public void run(ProcessStreamContext ctx) throws IOException {
        ctx.out.write(ctx.context.executeStream(new ByteArrayInputStream(expr.getBytes()),exprLang));
    }
}
