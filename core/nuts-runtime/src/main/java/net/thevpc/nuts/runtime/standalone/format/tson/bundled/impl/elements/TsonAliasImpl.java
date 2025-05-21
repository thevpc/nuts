package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders.TsonPrimitiveElementBuilderImpl;
import net.thevpc.nuts.util.NAssert;

import java.util.Objects;

public class TsonAliasImpl extends AbstractPrimitiveTsonElement implements TsonAlias {
    private String value;

    public TsonAliasImpl(String value) {
        super(TsonElementType.ALIAS);
        NAssert.requireNonNull(value, "value");
        this.value = value;
    }

    @Override
    public TsonAlias toAlias() {
        return this;
    }

    @Override
    public TsonString toStr() {
        return (TsonString) Tson.of(value);
    }

    @Override
    public String getName() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonAliasImpl tsonId = (TsonAliasImpl) o;
        return Objects.equals(value, tsonId.value);
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
        return value.compareTo(o.toName().value());
    }

    @Override
    public String stringValue() {
        return getName();
    }



}
