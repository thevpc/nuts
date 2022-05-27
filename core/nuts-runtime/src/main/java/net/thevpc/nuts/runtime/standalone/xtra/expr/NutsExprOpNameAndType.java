package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.NutsExprOpType;

import java.util.Objects;

public final class NutsExprOpNameAndType {
    private final String name;
    private final NutsExprOpType type;

    public NutsExprOpNameAndType(String name, NutsExprOpType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public NutsExprOpType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsExprOpNameAndType that = (NutsExprOpNameAndType) o;
        return Objects.equals(name, that.name) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}
