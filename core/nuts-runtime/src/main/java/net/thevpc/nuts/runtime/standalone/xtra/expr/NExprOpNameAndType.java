package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.NExprOpType;

import java.util.Objects;

public final class NExprOpNameAndType {
    private final String name;
    private final NExprOpType type;

    public NExprOpNameAndType(String name, NExprOpType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public NExprOpType getType() {
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
}
