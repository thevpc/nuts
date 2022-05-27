package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.util.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NutsExprEvaluatorAsContext extends NutsExprDeclarationsBase {
    private NutsExprEvaluator eval;
    private NutsExprDeclarations parent;

    public NutsExprEvaluatorAsContext(NutsExprEvaluator eval, NutsExprDeclarations parent) {
        this.eval = eval;
        this.parent = parent;
        setSession(parent.getSession());
    }


    @Override
    public NutsOptional<NutsExprFctDeclaration> getFunction(String fctName, Object... args) {
        return eval.getFunction(fctName, args, this)
                .<NutsExprFctDeclaration>map(x -> new DefaultNutsExprFctDeclaration(fctName, x))
                .orElseUse(() -> parent.getFunction(fctName, args))
                ;
    }

    @Override
    public NutsOptional<NutsExprConstructDeclaration> getConstruct(String constructName, NutsExprNode... args) {
        return eval.getConstruct(constructName, args, this)
                .<NutsExprConstructDeclaration>map(x -> new DefaultNutsExprConstructDeclaration(constructName, x))
                .orElseUse(() -> parent.getConstruct(constructName, args))
                ;
    }

    @Override
    public NutsOptional<NutsExprOpDeclaration> getOperator(String opName, NutsExprOpType type, NutsExprNode... args) {
        return eval.getOperator(opName, type, args, this)
                .<NutsExprOpDeclaration>map(x -> new DefaultNutsExprOpDeclaration(opName, x))
                .orElseUse(() -> parent.getOperator(opName, type, args))
                ;
    }

    @Override
    public NutsOptional<NutsExprVarDeclaration> getVar(String varName) {
        return eval.getVar(varName, this)
                .<NutsExprVarDeclaration>map(x -> new DefaultNutsExprVarDeclaration(varName, x))
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
    public List<NutsExprOpDeclaration> getOperators() {
        return parent.getOperators();
    }
}
