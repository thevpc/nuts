package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.builders.TsonPrimitiveElementBuilderImpl;

import java.util.Objects;
import java.util.regex.Pattern;

public class TsonRegexImpl extends AbstractPrimitiveTsonElement implements TsonRegex {
    private Pattern value;

    public TsonRegexImpl(Pattern value) {
        super(TsonElementType.REGEX);
        this.value = value;
    }

    public TsonRegexImpl(String value) {
        super(TsonElementType.LOCAL_DATE);
        this.value = Pattern.compile(value);
    }

    @Override
    public TsonString toStr() {
        return (TsonString) Tson.of(value.toString());
    }

    @Override
    public TsonRegex toRegex() {
        return this;
    }

    @Override
    public Pattern value() {
        return value;
    }

    @Override
    public Pattern regexValue() {
        return value();
    }

    @Override
    public String stringValue() {
        return value().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonRegexImpl tsonRegex = (TsonRegexImpl) o;
        return Objects.equals(value.toString(), tsonRegex.value.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value.toString());
    }
    @Override
    public TsonPrimitiveBuilder builder() {
        return new TsonPrimitiveElementBuilderImpl().copyFrom(this);
    }

    @Override
    protected int compareCore(TsonElement o) {
        return value.toString().compareTo(o.toRegex().value().toString());
    }
}
