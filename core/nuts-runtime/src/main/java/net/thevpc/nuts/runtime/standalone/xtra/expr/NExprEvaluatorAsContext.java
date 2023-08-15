package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.expr.*;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NExprEvaluatorAsContext extends NExprDeclarationsBase {
    private NExprEvaluator eval;
    private NExprDeclarations parent;

    public NExprEvaluatorAsContext(NExprEvaluator eval, NExprDeclarations parent) {
        this.eval = eval;
        this.parent = parent;
        setSession(parent.getSession());
    }


    @Override
    public NOptional<NExprFctDeclaration> getFunction(String fctName, Object... args) {
        return eval.getFunction(fctName, args, this)
                .<NExprFctDeclaration>map(x -> new DefaultNExprFctDeclaration(fctName, x))
                .orElseUse(() -> parent.getFunction(fctName, args))
                ;
    }

    @Override
    public NOptional<NExprConstructDeclaration> getConstruct(String constructName, NExprNode... args) {
        return eval.getConstruct(constructName, args, this)
                .<NExprConstructDeclaration>map(x -> new DefaultNExprConstructDeclaration(constructName, x))
                .orElseUse(() -> parent.getConstruct(constructName, args))
                ;
    }

    @Override
    public NOptional<NExprOpDeclaration> getOperator(String opName, NExprOpType type, NExprNode... args) {
        return eval.getOperator(opName, type, args, this)
                .<NExprOpDeclaration>map(x -> new DefaultNExprOpDeclaration(opName, x))
                .orElseUse(() -> parent.getOperator(opName, type, args))
                ;
    }

    @Override
    public NOptional<NExprVarDeclaration> getVar(String varName) {
        return eval.getVar(varName, this)
                .<NExprVarDeclaration>map(x -> new DefaultNExprVarDeclaration(varName, x))
                .orElseUse(() -> parent.getVar(varName))
                ;
    }

    public int[] getOperatorPrecedences() {
        return Stream.concat(
                IntStream.of(eval.getOperatorPrecedences(this)).boxed(),
                IntStream.of(parent.getOperatorPrecedences()).boxed()
        ).sorted().distinct().mapToInt(x -> x).toArray();
    }

    @Override
    public List<NExprOpDeclaration> getOperators() {
        return parent.getOperators();
    }
}
