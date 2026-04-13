package net.thevpc.nuts.expr;

import java.util.Map;

public interface NExprVar {
    static NExprVar ofVar(String name) {
        return NExprs.of().newVar(name);
    }

    static NExprVar ofVar(String name,Object value) {
        return NExprs.of().newVar(name,value);
    }

    static NExprVar ofConst(String name, Object value) {
        return NExprs.of().newConst(name, value);
    }

    static NExprVar ofMap(Map<String,Object> vars){
        return new NExprVar() {
            @Override
            public Object get(String s, NExprDeclarations ctx) { return vars.get(s); }
            @Override
            public Object set(String s, Object o, NExprDeclarations ctx) {
                vars.put(s, o); return o;
            }
        };
    }
    Object get(String name, NExprDeclarations context);

    Object set(String name, Object value, NExprDeclarations context);

}
