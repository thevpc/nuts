package net.thevpc.nuts.lib.doc.executor.expr;

import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.executor.expr.fct.*;
import net.thevpc.nuts.util.NOptional;

public class NDocExprEvaluator implements net.thevpc.nuts.lib.doc.executor.NDocExprEvaluator {
    public static final String NODC_CONTEXT_VAR_NAME = "ndoc";
    private NExprMutableDeclarations rootDecls;
    private NExprs nExprs;

    public NDocExprEvaluator() {

        nExprs = NExprs.of();
        NDocNExprVar v = new NDocNExprVar();
        rootDecls = nExprs.newMutableDeclarations(true, new NExprEvaluator() {
            @Override
            public NOptional<NExprVar> getVar(String varName, NExprDeclarations context2) {
                return NOptional.of(v);
            }
        });
        declareFunction(new ExecFct());
        declareFunction(new PrintlnFct());
        declareFunction(new PrintFct());
        declareFunction(new StringFct());
        declareFunction(new ProcessFileFct());
        declareFunction(new LoadFileFct());
        declareFunction(new IncludeFct());
        declareFunction(new LoadPagesFct());
        declareFunction(new PageToHtmlFct());
        declareFunction(new PageContentToHtmlFct());
        declareFunction(new FormatDateFct());
        declareFunction(new FileContentLengthString());
    }

    @Override
    public Object eval(String content, NDocContext fcontext) {
        content = content.trim();
        NExprMutableDeclarations decl = rootDecls.newMutableDeclarations();
        decl.declareConstant(NODC_CONTEXT_VAR_NAME, fcontext);
        decl.declareConstant("cwd", System.getProperty("user.dir"));
        decl.declareConstant("projectRoot", fcontext.getProjectRoot());
        decl.declareConstant("dir", fcontext.getWorkingDir().orNull());
        NExprDeclarations decl2 = decl.newDeclarations(new NExprEvaluator() {
            @Override
            public NOptional<NExprVar> getVar(String varName, NExprDeclarations context) {
                NOptional<Object> var = fcontext.getVar(varName);
                if (var.isPresent()) {
                    return NOptional.of(new NExprVar() {
                        @Override
                        public Object get(String name, NExprDeclarations context) {
                            return var.get();
                        }

                        @Override
                        public Object set(String name, Object value, NExprDeclarations context) {
                            return fcontext.setVar(name, value);
                        }
                    });
                }
                return NExprEvaluator.super.getVar(varName, context);
            }
        });
        NExprNode nExprNode = decl2.parse(content).get();
        NOptional<Object> eval = nExprNode.eval(decl2);
        if (!eval.isPresent()) {
            eval = nExprNode.eval(decl2);
        }
        return eval.get();
    }

    @Override
    public String toString() {
        return "NExpr";
    }

    protected void declareFunction(BaseNexprNExprFct d) {
        rootDecls.declareFunction(d.getName(), d);
    }

    private static NDocContext fcontext(NExprDeclarations context) {
        NExprVarDeclaration vd = context.getVar(NODC_CONTEXT_VAR_NAME).get();
        return (NDocContext) vd.get(context);
    }

    private static class NDocNExprVar implements NExprVar {
        public NDocNExprVar() {
        }

        @Override
        public Object get(String name, NExprDeclarations context) {
            return fcontext(context).getVar(name).orNull();
        }

        @Override
        public Object set(String name, Object value, NExprDeclarations context) {
            return fcontext(context).setVar(name, value);
        }
    }
}
