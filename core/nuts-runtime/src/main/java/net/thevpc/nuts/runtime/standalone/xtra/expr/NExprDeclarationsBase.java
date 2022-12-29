package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.*;

import java.util.Arrays;

public abstract class NExprDeclarationsBase implements NExprDeclarations {
    private NSession session;

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NExprDeclarationsBase setSession(NSession session) {
        this.session = session;
        return this;
    }

    public NOptional<Object> evalFunction(String fctName, Object... args) {
        return getFunction(fctName, args).flatMap(x-> NOptional.ofNullable(x.eval(Arrays.asList(args), this)));
    }

    public NOptional<Object> evalConstruct(String constructName, NExprNode... args) {
        return getConstruct(constructName, args).flatMap(x-> NOptional.ofNullable(x.eval(Arrays.asList(args), this)));
    }

    public NOptional<Object> evalOperator(String opName, NExprOpType type, NExprNode... args) {
        return getOperator(opName, type, args).flatMap(x-> NOptional.ofNullable(x.eval(Arrays.asList(args), this)));
    }

    public NOptional<Object> evalSetVar(String varName, Object value) {
        return NOptional.of(getVar(varName).get(getSession()).set(value, this));
    }

    public NOptional<Object> evalGetVar(String varName) {
        NOptional<NExprVarDeclaration> var = getVar(varName);
        if(!var.isPresent()){
            return var.map(x->null);
        }
        return NOptional.ofNullable(var.get(getSession()).get(this));
    }

    @Override
    public NExprDeclarations newDeclarations(NExprEvaluator evaluator) {
        return new NExprEvaluatorAsContext(evaluator, this);
    }

    @Override
    public NExprMutableDeclarations newMutableDeclarations() {
        return new DefaultDeclarationMutableContext(this);
    }

    @Override
    public NOptional<NExprNode> parse(String expression) {
        return new SyntaxParser(expression, new NExprWithCache(this), getSession()).parse();
    }

}
