package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.expr.*;

import java.util.Arrays;

public abstract class NExprDeclarationsBase implements NExprDeclarations {
    protected NWorkspace workspace;

    public NExprDeclarationsBase(NWorkspace workspace) {
        this.workspace = workspace;
    }

    public NWorkspace getWorkspace() {
        return workspace;
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
        NSession session = workspace.currentSession();
        return NOptional.of(getVar(varName).get().set(value, this));
    }

    public NOptional<Object> evalGetVar(String varName) {
        NOptional<NExprVarDeclaration> var = getVar(varName);
        if(!var.isPresent()){
            return var.map(x->null);
        }
        NSession session = workspace.currentSession();
        return NOptional.ofNullable(var.get().get(this));
    }

    @Override
    public NExprDeclarations newDeclarations(NExprEvaluator evaluator) {
        return new NExprEvaluatorAsContext(workspace,evaluator, this);
    }

    @Override
    public NExprMutableDeclarations newMutableDeclarations() {
        return new DefaultDeclarationMutableContext(workspace,this);
    }

    @Override
    public NOptional<NExprNode> parse(String expression) {
        return new SyntaxParser(expression, new NExprWithCache(this), workspace).parse();
    }

}
