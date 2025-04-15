package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders.TsonPrimitiveElementBuilderImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.Objects;

public class TsonLocalTimeImpl extends AbstractTemporalTsonElement implements TsonLocalTime {
    private LocalTime value;

    public TsonLocalTimeImpl(LocalTime value) {
        super(TsonElementType.LOCAL_TIME);
        this.value = value;
    }

    public TsonLocalTimeImpl(String value) {
        super(TsonElementType.LOCAL_DATE);
        this.value = LocalTime.parse(value);
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
    public TsonLocalDate toLocalDate() {
        return throwPrimitive(TsonElementType.LOCAL_DATE);
    }

    @Override
    public TsonLocalTime toLocalTime() {
        return this;
    }

    @Override public LocalTime value() {
        return value;
    }

    @Override
    public LocalDateTime localDateTimeValue() {
        return LocalDateTime.from(this.localTimeValue());
    }

    @Override
    public LocalDate localDateValue() {
        return LocalDate.from(this.localTimeValue());
    }

    @Override
    public LocalTime localTimeValue() {
        return value();
    }

    @Override
    public TsonLocalDateTime toLocalDateTime() {
        return (TsonLocalDateTime) Tson.of(this.localDateTimeValue());
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonLocalTimeImpl tsonTime = (TsonLocalTimeImpl) o;
        return Objects.equals(value, tsonTime.value);
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
        return value.compareTo(o.toLocalTime().value());
    }

    @Override
    public int compareTo(TsonElement o) {
        if (o.type().isTemporal()) {
            switch (o.type()) {
                case LOCAL_DATETIME: {
                    int i = this.localDateTimeValue().compareTo(o.localDateTimeValue());
                    return i == 0 ? type().compareTo(o.type()) : i;
                }
                case LOCAL_DATE: {
                    int i = this.localDateValue().compareTo(o.localDateValue());
                    return i == 0 ? type().compareTo(o.type()) : i;
                }
                case LOCAL_TIME: {
                    int i = value().compareTo(o.localTimeValue());
                    return i == 0 ? type().compareTo(o.type()) : i;
                }
            }
        }
        return super.compareTo(o);
    }
}
