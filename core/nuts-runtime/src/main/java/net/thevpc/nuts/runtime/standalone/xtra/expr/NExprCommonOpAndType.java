package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NExprCommonOp;
import net.thevpc.nuts.expr.NFixity;

import java.util.Objects;

class NExprCommonOpAndType {
    private NExprCommonOp op;
    private NFixity type;

    public NExprCommonOpAndType(NExprCommonOp op, NFixity type) {
        this.op = op;
        this.type = type;
    }

    public NExprCommonOp getOp() {
        return op;
    }

    public NFixity getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NExprCommonOpAndType that = (NExprCommonOpAndType) o;
        return op == that.op && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, type);
    }

    @Override
    public String toString() {
        return op.name()+"-"+type.name();
    }
}
