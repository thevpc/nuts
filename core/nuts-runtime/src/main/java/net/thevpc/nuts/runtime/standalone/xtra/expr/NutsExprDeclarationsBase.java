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

    public Object evalFunction(String fctName, Object... args) {
        return getFunction(fctName, args).get(getSession()).eval(Arrays.asList(args), this);
    }

    public Object evalConstruct(String constructName, NutsExprNode... args) {
        return getConstruct(constructName, args).get(getSession()).eval(Arrays.asList(args), this);
    }

    public Object evalOperator(String opName, NutsExprOpType type, NutsExprNode... args) {
        return getOperator(opName, type, args).get(getSession()).eval(Arrays.asList(args), this);
    }

    public Object evalSetVar(String varName, Object value) {
        return getVar(varName).get(getSession()).set(value, this);
    }

    public Object evalGetVar(String varName) {
        return getVar(varName).get(getSession()).get(this);
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
