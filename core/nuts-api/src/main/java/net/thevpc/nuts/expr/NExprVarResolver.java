package net.thevpc.nuts.expr;

import net.thevpc.nuts.internal.expr.NExprRPI;
import net.thevpc.nuts.util.NOptional;

import java.util.Map;
import java.util.function.Function;

@FunctionalInterface
public interface NExprVarResolver {
    static NExprVarResolver ofConst(Function<String, Object> vars) {
        return NExprRPI.of().createLazyConstResolver(vars);
    }

    static NExprVarResolver ofReadOnly(Function<String, Object> vars) {
        return NExprRPI.of().createReadOnlyVarResolver(vars);
    }

    static NExprVarResolver ofMap(Map<String, Object> variables) {
        return NExprRPI.of().createMapVarResolver(variables);
    }

    NOptional<NExprVar> getVar(String varName, NExprContext context);
}
