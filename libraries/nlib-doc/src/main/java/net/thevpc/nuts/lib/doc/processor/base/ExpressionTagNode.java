package net.thevpc.nuts.lib.doc.processor.base;

import java.io.IOException;

class ExpressionTagNode extends TagNode {
    private final String exprLang;
    private String expr;

    public ExpressionTagNode(String exprLang, String expr) {
        this.exprLang = exprLang;
        this.expr = expr;
    }

    public void run(ProcessStreamContext ctx) throws IOException {
        String s = ctx.context.executeString(expr, exprLang);
        ctx.out.write(s);
    }

    @Override
    public String toString() {
        return "Expression(" +expr+ ')';
    }
}
