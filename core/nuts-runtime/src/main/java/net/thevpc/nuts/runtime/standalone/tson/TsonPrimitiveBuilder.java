package net.thevpc.nuts.runtime.standalone.tson;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

public interface TsonPrimitiveBuilder extends TsonElementBuilder {
    TsonElement value();


    TsonPrimitiveBuilder copyFrom(TsonElementBase element);

    TsonPrimitiveBuilder setNull();

    TsonPrimitiveBuilder set(String value);

    TsonPrimitiveBuilder set(LocalDate value);

    TsonPrimitiveBuilder set(java.sql.Date value);

    TsonPrimitiveBuilder set(Date value);

    TsonPrimitiveBuilder set(Instant value);

    TsonPrimitiveBuilder set(Time value);

    TsonPrimitiveBuilder set(LocalTime value);

    TsonPrimitiveBuilder set(Pattern value);

    TsonPrimitiveBuilder set(boolean value);

    TsonPrimitiveBuilder set(char value);

    TsonPrimitiveBuilder set(int value);

    TsonPrimitiveBuilder set(long value);

    TsonPrimitiveBuilder set(byte value);

    TsonPrimitiveBuilder set(short value);

    TsonPrimitiveBuilder set(float value);

    TsonPrimitiveBuilder set(double value);

    TsonPrimitiveBuilder setName(String value);

    TsonPrimitiveBuilder setAlias(String value);

    TsonPrimitiveBuilder setRegex(String value);

    TsonPrimitiveBuilder set(Boolean value);

    TsonPrimitiveBuilder set(Character value);

    TsonPrimitiveBuilder set(Integer value);

    TsonPrimitiveBuilder set(Long value);

    TsonPrimitiveBuilder set(Byte value);

    TsonPrimitiveBuilder set(Short value);

    TsonPrimitiveBuilder set(Float value);

    TsonPrimitiveBuilder set(Double value);

    TsonPrimitiveBuilder set(byte[] value);

    TsonPrimitiveBuilder set(InputStream value);

    boolean getBoolean();

    char getChar();

    byte getByte();

    short getShort();

    int getInt();

    long getLong();

    float getFloat();

    double getDouble();

    TsonPrimitiveBuilder setBinary(File value);

    TsonPrimitiveBuilder setBinary(Path value);

    TsonPrimitiveBuilder setCode(File value);

    TsonPrimitiveBuilder setCode(Path value);

    TsonPrimitiveBuilder setStopStream(File value, String stopWord);

    TsonPrimitiveBuilder setStopWordStream(Path value, String stopWord);

    Boolean getBooleanObject();

    Character getCharObject();

    Byte getByteObject();

    Short getShortObject();

    Integer getIntObject();

    Long getLongObject();

    Float getFloatObject();

    Double getDoubleObject();

    String getString();

    LocalDate getLocalDate();

    LocalDateTime getLocalDateTime();

    LocalTime getLocalTime();

    Pattern getRegex();

    TsonElement build();


    ////////////////////////////////////////////////

    TsonPrimitiveBuilder comments(TsonComments comments);

    TsonPrimitiveBuilder setComments(TsonComments comments);

    TsonPrimitiveBuilder setAnnotations(TsonAnnotation... annotations);

    TsonPrimitiveBuilder addAnnotations(TsonAnnotation... annotations);

    TsonPrimitiveBuilder addAnnotations(Collection<TsonAnnotation> annotations);

    TsonPrimitiveBuilder annotation(String name, TsonElementBase... elements);

    TsonPrimitiveBuilder addAnnotation(String name, TsonElementBase... elements);

    TsonPrimitiveBuilder addAnnotation(TsonAnnotation annotation);

    TsonPrimitiveBuilder removeAnnotationAt(int index);

    TsonPrimitiveBuilder clearAnnotations();
}
