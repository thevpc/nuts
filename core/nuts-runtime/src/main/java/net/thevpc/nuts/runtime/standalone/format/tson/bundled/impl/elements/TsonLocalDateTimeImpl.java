package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders.TsonPrimitiveElementBuilderImpl;

import java.time.*;
import java.time.temporal.Temporal;
import java.util.Objects;

public class TsonLocalDateTimeImpl extends AbstractTemporalTsonElement implements TsonLocalDateTime {
    private LocalDateTime value;

    public TsonLocalDateTimeImpl(LocalDateTime value) {
        super(TsonElementType.LOCAL_DATETIME);
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
    public TsonLocalDateTime toLocalDateTime() {
        return this;
    }

    @Override
    public LocalDateTime localDateTimeValue() {
        return value();
    }

    @Override
    public TsonLocalTime toLocalTime() {
        return (TsonLocalTime) Tson.ofLocalTime(localTimeValue());
    }

    @Override
    public LocalDate localDateValue() {
        return value().toLocalDate();
    }

    @Override
    public LocalTime localTimeValue() {
        return LocalTime.from(value());
    }

    @Override
    public TsonLocalDate toLocalDate() {
        return (TsonLocalDate) Tson.ofLocalDate(localDateValue());
    }

    @Override
    public LocalDateTime value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonLocalDateTimeImpl tsonDate = (TsonLocalDateTimeImpl) o;
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
        return value.compareTo(o.toLocalDateTime().value());
    }

    @Override
    public int compareTo(TsonElement o) {
        if (o.type().isTemporal()) {
            switch (o.type()) {
                case LOCAL_DATETIME: {
                    int i = value().compareTo(o.localDateTimeValue());
                    return i == 0 ? type().compareTo(o.type()) : i;
                }
                case LOCAL_DATE: {
                    int i = value().compareTo(o.localDateTimeValue());
                    return i == 0 ? type().compareTo(o.type()) : i;
                }
                case LOCAL_TIME: {
                    int i = value().compareTo(o.localDateTimeValue());
                    return i == 0 ? type().compareTo(o.type()) : i;
                }
            }
        }
        return super.compareTo(o);
    }
}
