package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.util.*;

import java.util.Arrays;

public abstract class NutsExprDeclarationsBase implements NutsExprDeclarations {
    private NutsSession session;

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsExprDeclarationsBase setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public NutsOptional<Object> evalFunction(String fctName, Object... args) {
        return getFunction(fctName, args).flatMap(x->NutsOptional.ofNullable(x.eval(Arrays.asList(args), this)));
    }

    public NutsOptional<Object> evalConstruct(String constructName, NutsExprNode... args) {
        return getConstruct(constructName, args).flatMap(x->NutsOptional.ofNullable(x.eval(Arrays.asList(args), this)));
    }

    public NutsOptional<Object> evalOperator(String opName, NutsExprOpType type, NutsExprNode... args) {
        return getOperator(opName, type, args).flatMap(x->NutsOptional.ofNullable(x.eval(Arrays.asList(args), this)));
    }

    public NutsOptional<Object> evalSetVar(String varName, Object value) {
        return NutsOptional.of(getVar(varName).get(getSession()).set(value, this));
    }

    public NutsOptional<Object> evalGetVar(String varName) {
        NutsOptional<NutsExprVarDeclaration> var = getVar(varName);
        if(!var.isPresent()){
            return var.map(x->null);
        }
        return NutsOptional.ofNullable(var.get(getSession()).get(this));
    }

    @Override
    public NutsExprDeclarations newDeclarations(NutsExprEvaluator evaluator) {
        return new NutsExprEvaluatorAsContext(evaluator, this);
    }

    @Override
    public NutsExprMutableDeclarations newMutableDeclarations() {
        return new DefaultDeclarationMutableContext(this);
    }

    @Override
    public NutsOptional<NutsExprNode> parse(String expression) {
        return new SyntaxParser(expression, new NutsExprWithCache(this), getSession()).parse();
    }

}
