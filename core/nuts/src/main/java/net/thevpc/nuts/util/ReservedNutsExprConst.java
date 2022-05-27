package net.thevpc.nuts.util;

public class ReservedNutsExprConst implements NutsExprVar {
    private final Object value;

    public ReservedNutsExprConst(Object value) {
        this.value = value;
    }

    @Override
    public Object get(String name, NutsExprDeclarations context) {
        return value;
    }

    @Override
    public Object set(String name, Object value, NutsExprDeclarations context) {
        return value;
    }
}
