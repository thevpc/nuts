package net.thevpc.nuts.runtime.standalone.xtra.expr.template;

import net.thevpc.nuts.expr.NExprNode;
import net.thevpc.nuts.util.NLiteral;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class IfTagNode extends TagNode {
    List<ElseIf> elseIfBranches = new ArrayList<>();
    TagNode elseExpr;

    public IfTagNode(NExprNode expr) {
        elseIfBranches.add(new ElseIf(expr,null));
    }

    public void run(ProcessStreamContext ctx) throws IOException {
        for (ElseIf e : elseIfBranches) {
            Object exprResult = ctx.eval(e.expr);
            if (NLiteral.of(exprResult).asBooleanValue().orElse(false)) {
                if(e.body!=null){
                    e.body.run(ctx);
                }
                return;
            }
        }
        if (elseExpr != null) {
            elseExpr.run(ctx);
        }
    }

    static class ElseIf {
        NExprNode expr;
        TagNode body;

        public ElseIf(NExprNode expr, TagNode body) {
            this.expr = expr;
            this.body = body;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < elseIfBranches.size(); i++) {
            ElseIf elseIfBranch = elseIfBranches.get(i);
            if(i>0){
                sb.append("\n else if ");
            }else{
                sb.append("if ");
            }
            sb.append(elseIfBranch.expr);
        }
        if(elseExpr!=null){
            sb.append("\nelse ").append(elseExpr);
        }
        sb.append("\nend");
        return sb.toString();
    }
}
