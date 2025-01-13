package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.expr.*;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NExprEvaluatorAsContext extends NExprDeclarationsBase {
    private NExprEvaluator eval;
    private NExprDeclarations parent;

    public NExprEvaluatorAsContext(NExprs exprs,NWorkspace workspace,NExprEvaluator eval, NExprDeclarations parent) {
        super(exprs,workspace);
        this.eval = eval;
        this.parent = parent;
    }


    @Override
    public NOptional<NExprFctDeclaration> getFunction(String fctName, NExprNodeValue... args) {
        return eval.getFunction(fctName, args, this)
                .<NExprFctDeclaration>map(x -> new DefaultNExprFctDeclaration(fctName, x))
                .orElseUse(() -> parent.getFunction(fctName, args))
                ;
    }

    @Override
    public NOptional<NExprConstructDeclaration> getConstruct(String constructName, NExprNodeValue... args) {
        return eval.getConstruct(constructName, args, this)
                .<NExprConstructDeclaration>map(x -> new DefaultNExprConstructDeclaration(constructName, x))
                .orElseUse(() -> parent.getConstruct(constructName, args))
                ;
    }

    @Override
    public NOptional<NExprOpDeclaration> getOperator(String opName, NExprOpType type, NExprNodeValue... args) {
        return eval.getOperator(opName, type, args, this)
                .<NExprOpDeclaration>map(x -> new DefaultNExprOpDeclaration(opName, x))
                .orElseUse(() -> parent.getOperator(opName, type, args))
                ;
    }

    @Override
    public NOptional<NExprVarDeclaration> getVar(String varName) {
        return eval.getVar(varName, this)
                .<NExprVarDeclaration>map(x -> new DefaultNExprVarDeclaration(getWorkspace(),varName, x))
                .orElseUse(() -> parent.getVar(varName))
                ;
    }

    @Override
    public List<NExprOpDeclaration> getOperators() {
        return parent.getOperators();
    }
}
