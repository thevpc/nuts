package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.builders.TsonPrimitiveElementBuilderImpl;

import java.util.Objects;

public class TsonBooleanImpl extends AbstractPrimitiveTsonElement implements TsonBoolean {
    public static final TsonBoolean TRUE = new TsonBooleanImpl(true);
    public static final TsonBoolean FALSE = new TsonBooleanImpl(false);
    private boolean value;

    public static final TsonBoolean valueOf(boolean value) {
        return value ? TRUE : FALSE;
    }

    private TsonBooleanImpl(boolean value) {
        super(TsonElementType.BOOLEAN);
        this.value = value;
    }

    @Override
    public TsonString toStr() {
        return (TsonString) Tson.of(String.valueOf(value));
    }


    @Override
    public TsonBoolean toBoolean() {
        return this;
    }

    @Override
    public boolean value() {
        return value;
    }

    @Override
    public boolean booleanValue() {
        return value();
    }

    @Override
    public Boolean booleanObject() {
        return value();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonBooleanImpl that = (TsonBooleanImpl) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public TsonPrimitiveBuilder builder() {
        return new TsonPrimitiveElementBuilderImpl().copyFrom(this);
    }

    @Override
    protected int compareCore(TsonElement o) {
        return Boolean.compare(value, o.toBoolean().value());
    }
}
