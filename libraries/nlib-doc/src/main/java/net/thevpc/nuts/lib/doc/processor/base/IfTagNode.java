package net.thevpc.nuts.lib.doc.processor.base;

import net.thevpc.nuts.util.NLiteral;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class IfTagNode extends TagNode {
    List<ElseIf> elseIfBranches = new ArrayList<>();
    TagNode elseExpr;
    String exprLang;

    public IfTagNode(String exprLang, String expr) {
        this.exprLang = exprLang;
        elseIfBranches.add(new ElseIf(expr,null));
    }

    public void run(ProcessStreamContext ctx) throws IOException {
        for (ElseIf e : elseIfBranches) {
            Object exprResult = ctx.context.eval(e.expr, exprLang);
            if (NLiteral.of(exprResult).asBoolean().orElse(false)) {
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
        String expr;
        TagNode body;

        public ElseIf(String expr, TagNode body) {
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
