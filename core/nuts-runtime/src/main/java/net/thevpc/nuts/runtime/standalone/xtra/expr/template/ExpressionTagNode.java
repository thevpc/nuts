package net.thevpc.nuts.runtime.standalone.xtra.expr.template;

import net.thevpc.nuts.expr.NExprNode;

import java.io.IOException;

class ExpressionTagNode extends TagNode {
    private NExprNode expr;

    public ExpressionTagNode(NExprNode expr) {
        this.expr = expr;
    }

    public void run(ProcessStreamContext ctx) throws IOException {
        Object s = ctx.eval(expr);
        ctx.out.write(s == null ? "" : s.toString());
    }

    @Override
    public String toString() {
        return "Expression(" + expr + ')';
    }
}
