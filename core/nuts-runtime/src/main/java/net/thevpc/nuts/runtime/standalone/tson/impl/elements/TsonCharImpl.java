package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.TsonChar;
import net.thevpc.nuts.runtime.standalone.tson.TsonElement;
import net.thevpc.nuts.runtime.standalone.tson.TsonElementType;
import net.thevpc.nuts.runtime.standalone.tson.TsonPrimitiveBuilder;
import net.thevpc.nuts.runtime.standalone.tson.impl.builders.TsonPrimitiveElementBuilderImpl;

import java.util.Objects;

public class TsonCharImpl extends AbstractPrimitiveTsonElement implements TsonChar {
    private char value;

    public TsonCharImpl(char value) {
        super(TsonElementType.CHAR);
        this.value = value;
    }

    @Override
    public TsonChar toChar() {
        return this;
    }

    @Override
    public char value() {
        return value;
    }

    @Override
    public char charValue() {
        return value();
    }

    @Override
    public Character charObject() {
        return value();
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonCharImpl tsonChar = (TsonCharImpl) o;
        return value == tsonChar.value;
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
        return Character.compare(value,o.toChar().value());
    }
}
