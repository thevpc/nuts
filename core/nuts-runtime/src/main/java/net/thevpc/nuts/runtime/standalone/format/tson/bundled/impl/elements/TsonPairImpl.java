package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders.TsonPairBuilderImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.TsonUtils;

import java.util.Objects;

public class TsonPairImpl extends AbstractNonPrimitiveTsonElement implements TsonPair {
    private TsonElement key;
    private TsonElement value;

    public TsonPairImpl(TsonElement key, TsonElement value) {
        super(TsonElementType.PAIR);
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null. Try to use NULL Tson element");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null. Try to use NULL Tson element");
        }
        if (key.type() == TsonElementType.PAIR) {
            throw new IllegalArgumentException("Key of Key Value cannot be a key value as well");
        }
        if (value.type() == TsonElementType.PAIR) {
            throw new IllegalArgumentException("value of Key Value cannot be a key value as well");
        }
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean isSimplePair() {
        return type() == TsonElementType.PAIR && key().isSimple();
    }

    @Override
    public TsonPair toPair() {
        return this;
    }

    @Override
    public TsonElement value() {
        return value;
    }

    @Override
    public TsonElement key() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonPairImpl that = (TsonPairImpl) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), key, value);
    }

    @Override
    public TsonPairBuilder builder() {
        return new TsonPairBuilderImpl().key(key()).value(value());
    }

    @Override
    public boolean visit(TsonDocumentVisitor visitor) {
        if (visitor.visit(this)) {
            if (!key.visit(visitor)) {
                return false;
            }
            if (!value.visit(visitor)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected int compareCore(TsonElement o) {
        TsonPair oo = o.toPair();
        return TsonUtils.compareElementsArray(
                new TsonElement[]{key(), value()},
                new TsonElement[]{oo.key(), oo.value()}
        );
    }

    @Override
    public void visit(TsonParserVisitor visitor) {
        visitor.visitInstructionStart();
        key().visit(visitor);
        value().visit(visitor);
        visitor.visitKeyValueEnd();
    }
}
