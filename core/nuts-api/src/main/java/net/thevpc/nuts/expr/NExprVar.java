package net.thevpc.nuts.expr;

import java.util.Map;
import java.util.function.Function;

public interface NExprVar {
    static NExprVar ofVar(String name) {
        return NExprs.of().newVar(name);
    }

    static NExprVar ofVar(String name, Object value) {
        return NExprs.of().newVar(name, value);
    }

    static NExprVar ofConst(String name, Object value) {
        return NExprs.of().newConst(name, value);
    }

    static NExprVar ofConsts(Function<String, Object> vars) {
        return new NExprVar() {
            @Override
            public Object get(String s, NExprDeclarations ctx) {
                return vars == null ? null : vars.apply(s);
            }

            @Override
            public Object set(String s, Object o, NExprDeclarations ctx) {
                return vars == null ? null : vars.apply(s);
            }
        };
    }

    static NExprVar ofMap(Map<String, Object> vars) {
        return new NExprVar() {
            @Override
            public Object get(String s, NExprDeclarations ctx) {
                return vars == null ? null : vars.get(s);
            }

            @Override
            public Object set(String s, Object o, NExprDeclarations ctx) {
                if (vars != null) {
                    vars.put(s, o);
                }
                return o;
            }
        };
    }

    Object get(String name, NExprDeclarations context);

    Object set(String name, Object value, NExprDeclarations context);

}
