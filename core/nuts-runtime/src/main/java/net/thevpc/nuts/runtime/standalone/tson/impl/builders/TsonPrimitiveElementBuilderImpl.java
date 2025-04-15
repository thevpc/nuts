package net.thevpc.nuts.runtime.standalone.tson.impl.builders;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.elements.TsonElementDecorator;
import net.thevpc.nuts.runtime.standalone.tson.impl.elements.TsonNullImpl;
import net.thevpc.nuts.runtime.standalone.tson.impl.util.TsonUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.regex.Pattern;

public class TsonPrimitiveElementBuilderImpl extends AbstractTsonElementBuilder<TsonPrimitiveBuilder> implements TsonPrimitiveBuilder {
    private TsonElement value = TsonNullImpl.INSTANCE;

    @Override
    public TsonElementType type() {
        return value.type();
    }

    @Override
    public TsonPrimitiveBuilder copyFrom(TsonElementBase element0) {
        TsonElement element= Tson.of(element0);
        if (element == null) {
            element = TsonNullImpl.INSTANCE;
        }
        if (element instanceof TsonElementDecorator) {
            TsonElementDecorator d = (TsonElementDecorator) element;
            if(!d.getBase().isPrimitive()){
                throw new ClassCastException("Not a primitive type " + element.type());
            }
            this.value = d.getBase();
            setComments(d.comments());
            setAnnotations(d.annotations().toArray(new TsonAnnotation[0]));
        } else {
            if(!element.isPrimitive()){
                throw new ClassCastException("Not a primitive type " + element.type());
            }
            this.value = element;
        }
        return this;
    }

    @Override
    public TsonElement value() {
        return value == null ? TsonNullImpl.INSTANCE : value;
    }

    @Override
    public TsonPrimitiveBuilder setNull() {
        value = TsonNullImpl.INSTANCE;
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(boolean value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(String value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(Date value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(LocalDate value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(java.sql.Date value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(Time value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(Instant value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(LocalTime value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(Pattern value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(char value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(int value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(long value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(byte value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(short value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(float value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(double value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(Character value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(Integer value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(Long value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(Byte value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(Short value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(Float value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(Double value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(Boolean value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(byte[] value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder set(InputStream value) {
        this.value = Tson.of(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder setBinary(File value) {
        this.value = Tson.ofBinStream(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder setBinary(Path value) {
        this.value = Tson.ofBinStream(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder setCode(File value) {
        this.value = Tson.ofCharStream(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder setCode(Path value) {
        this.value = Tson.ofCharStream(value);
        return this;
    }


    @Override
    public TsonPrimitiveBuilder setStopStream(File value,String stopWord) {
        this.value = Tson.ofStopStream(value,stopWord);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder setStopWordStream(Path value,String stopWord) {
        this.value = Tson.ofStopStream(value,stopWord);
        return this;
    }

    @Override
    public Boolean getBooleanObject() {
        return value().booleanObject();
    }

    @Override
    public Character getCharObject() {
        return value().charObject();
    }

    @Override
    public Byte getByteObject() {
        return value().byteObject();
    }

    @Override
    public Short getShortObject() {
        return value().shortObject();
    }

    @Override
    public Integer getIntObject() {
        return value().intObject();
    }

    @Override
    public Long getLongObject() {
        return value().longObject();
    }

    @Override
    public Float getFloatObject() {
        return value().floatObject();
    }

    @Override
    public Double getDoubleObject() {
        return value().doubleObject();
    }

    @Override
    public float getFloat() {
        return value().floatValue();
    }

    @Override
    public double getDouble() {
        return value().doubleValue();
    }

    @Override
    public byte getByte() {
        return value().byteValue();
    }

    @Override
    public char getChar() {
        return value().charValue();
    }

    @Override
    public boolean getBoolean() {
        return value().booleanValue();
    }

    @Override
    public String getString() {
        return value().stringValue();
    }

    @Override
    public int getInt() {
        return value().intValue();
    }

    @Override
    public long getLong() {
        return value().longValue();
    }

    @Override
    public short getShort() {
        return value().shortValue();
    }

    @Override
    public LocalDate getLocalDate() {
        return value().localDateValue();
    }

    @Override
    public LocalDateTime getLocalDateTime() {
        return value().localDateTimeValue();
    }

    @Override
    public LocalTime getLocalTime() {
        return value().localTimeValue();
    }

    @Override
    public Pattern getRegex() {
        return value().regexValue();
    }

    @Override
    public TsonPrimitiveBuilder setName(String value) {
        this.value = Tson.ofName(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder setAlias(String value) {
        this.value = Tson.ofAlias(value);
        return this;
    }

    @Override
    public TsonPrimitiveBuilder setRegex(String value) {
        this.value = Tson.ofRegex(value);
        return this;
    }

    @Override
    public TsonElement build() {
        return TsonUtils.decorate(
                value
                , comments(), annotations())
                ;
    }
}
