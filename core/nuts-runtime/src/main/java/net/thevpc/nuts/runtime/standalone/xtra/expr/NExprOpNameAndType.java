package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NFixity;

import java.util.Objects;

public final class NExprOpNameAndType {
    private final String name;
    private final NFixity type;

    public NExprOpNameAndType(String name, NFixity type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public NFixity getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NExprOpNameAndType that = (NExprOpNameAndType) o;
        return Objects.equals(name, that.name) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return type+"["+name+"]" ;
    }
}
