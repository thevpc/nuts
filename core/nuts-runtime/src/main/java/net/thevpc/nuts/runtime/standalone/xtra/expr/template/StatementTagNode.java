package net.thevpc.nuts.runtime.standalone.xtra.expr.template;

import net.thevpc.nuts.expr.NExprNode;

import java.io.IOException;

class StatementTagNode extends TagNode {
    private NExprNode expr;

    public StatementTagNode(NExprNode expr) {
        this.expr = expr;
    }

    public void run(ProcessStreamContext ctx) throws IOException {
        Object u = ctx.eval(expr);
    }
    @Override
    public String toString() {
        return "Statement(" +expr+ ')';
    }
}
