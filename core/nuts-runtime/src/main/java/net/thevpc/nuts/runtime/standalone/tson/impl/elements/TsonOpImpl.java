package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.builders.TsonOpBuilderImpl;
import net.thevpc.nuts.runtime.standalone.tson.impl.util.TsonUtils;

import java.util.Objects;

public class TsonOpImpl extends AbstractNonPrimitiveTsonElement implements TsonOp {
    private String opName;
    private TsonElement first;
    private TsonElement second;
    private TsonOpType opType;

    public TsonOpImpl(String opName, TsonOpType opType, TsonElement first, TsonElement second) {
        super(TsonElementType.OP);
        if (opName == null) {
            throw new IllegalArgumentException("op cannot be null. Try to use NULL Tson element");
        }
        if (first == null) {
            throw new IllegalArgumentException("Key cannot be null. Try to use NULL Tson element");
        }
        if (second == null) {
            throw new IllegalArgumentException("Value cannot be null. Try to use NULL Tson element");
        }
        if (opType == null) {
            throw new IllegalArgumentException("opType cannot be null. Try to use NULL Tson element");
        }
        if (first.type() == TsonElementType.PAIR) {
            throw new IllegalArgumentException("Key of Key Value cannot be a key value as well");
        }
        if (second.type() == TsonElementType.PAIR) {
            throw new IllegalArgumentException("Key of Key Value cannot be a key value as well");
        }
        this.opName = opName;
        this.opType = opType;
        this.first = first;
        this.second = second;
    }

    @Override
    public TsonOpType opType() {
        return opType;
    }

    @Override
    public String opName() {
        return opName;
    }

    @Override
    public TsonOp toOp() {
        return this;
    }

    @Override
    public TsonElement second() {
        return second;
    }

    @Override
    public TsonElement first() {
        return first;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonOpImpl that = (TsonOpImpl) o;
        return Objects.equals(first, that.first) &&
                Objects.equals(second, that.second) &&
                Objects.equals(opType, that.opType)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), first, second, opType);
    }

    @Override
    public TsonOpBuilder builder() {
        return new TsonOpBuilderImpl().first(first()).second(second());
    }

    @Override
    public boolean visit(TsonDocumentVisitor visitor) {
        if (visitor.visit(this)) {
            if (!first.visit(visitor)) {
                return false;
            }
            if (!second.visit(visitor)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected int compareCore(TsonElement o) {
        TsonPair oo = o.toPair();
        return TsonUtils.compareElementsArray(
                new TsonElement[]{first(), second()},
                new TsonElement[]{oo.key(), oo.value()}
        );
    }

    @Override
    public void visit(TsonParserVisitor visitor) {
        visitor.visitInstructionStart();
        first().visit(visitor);
        second().visit(visitor);
        visitor.visitKeyValueEnd();
    }
}
