package net.thevpc.nuts.lib.doc.processor.base;

import java.io.IOException;

class StatementTagNode extends TagNode {
    private final String exprLang;
    private String expr;

    public StatementTagNode(String exprLang, String expr) {
        this.exprLang = exprLang;
        this.expr = expr;
    }

    public void run(ProcessStreamContext ctx) throws IOException {
        Object u = ctx.context.eval(expr, exprLang);
    }
    @Override
    public String toString() {
        return "Statement(" +expr+ ')';
    }
}
