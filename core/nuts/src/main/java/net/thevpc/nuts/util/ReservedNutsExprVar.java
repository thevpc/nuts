package net.thevpc.nuts.util;

public class ReservedNutsExprVar implements NutsExprVar {
    private Object value;

    public ReservedNutsExprVar(Object value) {
        this.value = value;
    }

    @Override
    public Object get(String name, NutsExprDeclarations context) {
        return value;
    }

    @Override
    public Object set(String name, Object value, NutsExprDeclarations context) {
        Object old = this.value;
        this.value = value;
        return old;
    }
}
