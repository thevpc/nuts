package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders.TsonPrimitiveElementBuilderImpl;

import java.time.*;
import java.time.temporal.Temporal;
import java.util.Objects;

public class TsonInstantImpl extends AbstractTemporalTsonElement implements TsonInstant {
    private Instant value;

    public TsonInstantImpl(Instant value) {
        super(TsonElementType.INSTANT);
        this.value = value;
    }

    @Override
    public TsonString toStr() {
        return (TsonString) Tson.of(String.valueOf(value));
    }

    @Override
    public Temporal temporalValue() {
        return value;
    }


    @Override
    public TsonInstant toInstant() {
        return this;
    }

    @Override
    public LocalDateTime localDateTimeValue() {
        return value().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public TsonLocalTime toLocalTime() {
        return (TsonLocalTime) Tson.ofLocalTime(localTimeValue());
    }

    @Override
    public LocalDate localDateValue() {
        return value().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public LocalTime localTimeValue() {
        return value().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    @Override
    public Instant instantValue() {
        return value;
    }

    @Override
    public TsonLocalDate toLocalDate() {
        return (TsonLocalDate) Tson.ofLocalDate(localDateValue());
    }

    @Override
    public Instant value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonInstantImpl tsonDate = (TsonInstantImpl) o;
        return Objects.equals(value, tsonDate.value);
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
        return value.compareTo(o.toInstant().value());
    }


}
