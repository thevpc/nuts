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
}
