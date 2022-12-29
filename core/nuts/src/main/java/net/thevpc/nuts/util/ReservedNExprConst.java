package net.thevpc.nuts.util;

public class ReservedNExprConst implements NExprVar {
    private final Object value;

    public ReservedNExprConst(Object value) {
        this.value = value;
    }

    @Override
    public Object get(String name, NExprDeclarations context) {
        return value;
    }

    @Override
    public Object set(String name, Object value, NExprDeclarations context) {
        return value;
    }
}
