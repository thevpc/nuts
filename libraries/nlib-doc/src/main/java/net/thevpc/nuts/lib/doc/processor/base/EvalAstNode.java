package net.thevpc.nuts.lib.doc.processor.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;

class EvalAstNode extends TagNode {
    private final String exprLang;
    private String expr;

    public EvalAstNode(String exprLang, String expr) {
        this.exprLang = exprLang;
        this.expr = expr;
    }

    public void run(ProcessStreamContext ctx) throws IOException {
        ctx.context.executeString(expr,exprLang);
    }
}
