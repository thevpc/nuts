package net.thevpc.nuts.runtime.standalone.format.elem.builder;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.format.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.format.elem.item.DefaultNNumberElement;
import net.thevpc.nuts.runtime.standalone.format.elem.item.DefaultNPrimitiveElement;
import net.thevpc.nuts.runtime.standalone.format.elem.item.DefaultNStringElement;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMapStrategy;
import net.thevpc.nuts.util.NMsg;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;

public class DefaultNPrimitiveElementBuilder extends AbstractNElementBuilder implements NPrimitiveElementBuilder {
    private Object value;
    private NNumberLayout numberLayout;
    private String numberSuffix;

    private NElementType type;

    public DefaultNPrimitiveElementBuilder() {
        this.type = NElementType.NULL;
    }

    @Override
    public NPrimitiveElementBuilder copyFrom(NElementBuilder other) {
        copyFrom(other,NMapStrategy.ANY);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder copyFrom(NElement other) {
        copyFrom(other,NMapStrategy.ANY);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder copyFrom(NElementBuilder other, NMapStrategy strategy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, strategy);
        if (other instanceof NPrimitiveElementBuilder) {
            NPrimitiveElementBuilder from = (NPrimitiveElementBuilder) other;
            this.type = from.type();
            this.value = from.value();
            this.numberLayout = from.numberLayout();
            this.numberSuffix = from.numberSuffix();
        }
        return this;
    }

    @Override
    public NPrimitiveElementBuilder copyFrom(NElement other, NMapStrategy strategy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, strategy);
        if (other instanceof NPrimitiveElement) {
            NPrimitiveElement from = (NPrimitiveElement) other;
            this.type = from.type();
            this.value = from.value();
            if(other instanceof NNumberElement) {
                NNumberElement nfrom = (NNumberElement) other;
                this.numberLayout = nfrom.numberLayout();
                this.numberSuffix = nfrom.numberSuffix();
            }
        }
        return this;
    }

    public NNumberLayout numberLayout() {
        return numberLayout;
    }

    public NPrimitiveElementBuilder numberLayout(NNumberLayout numberLayout) {
        this.numberLayout = numberLayout;
        return this;
    }

    public String numberSuffix() {
        return numberSuffix;
    }

    public NPrimitiveElementBuilder numberSuffix(String numberSuffix) {
        this.numberSuffix = numberSuffix;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setValue(Object value) {
        return value(value);
    }

    public NPrimitiveElementBuilder value(Object value) {
        if (value == null) {
            this.value = null;
            this.type = NElementType.NULL;
        } else {
            switch (value.getClass().getName()) {
                case "java.lang.String": {
                    setDoubleQuotedString((String) value);
                    break;
                }
                case "java.lang.Boolean":
                case "boolean": {
                    setBoolean(((Boolean) value).booleanValue());
                    break;
                }
                case "java.lang.Byte":
                case "byte": {
                    setByte(((Byte) value).byteValue());
                    break;
                }
                case "java.lang.Short":
                case "short": {
                    setShort(((Short) value).shortValue());
                    break;
                }
                case "java.lang.Character":
                case "char": {
                    setChar(((Character) value).charValue());
                    break;
                }
                case "java.lang.Integer":
                case "int": {
                    setInt(((Integer) value).intValue());
                    break;
                }
                case "java.lang.Long":
                case "long": {
                    setLong(((Long) value).longValue());
                    break;
                }
                case "java.lang.Float":
                case "float": {
                    setFloat(((Float) value).floatValue());
                    break;
                }
                case "java.lang.Double":
                case "double": {
                    setDouble(((Double) value).doubleValue());
                    break;
                }
                case "java.math.BigInteger": {
                    setBigInteger(((BigInteger) value));
                    break;
                }
                case "java.math.BigDecimal": {
                    setBigDecimal(((BigDecimal) value));
                    break;
                }
                case "java.time.Instant": {
                    setInstant(((Instant) value));
                    break;
                }
                case "java.time.LocalDate": {
                    setLocalDate(((LocalDate) value));
                    break;
                }
                case "java.time.LocalTime": {
                    setLocalTime(((LocalTime) value));
                    break;
                }
                case "java.time.LocalDateTime": {
                    setLocalDateTime(((LocalDateTime) value));
                    break;
                }
                default: {
                    if (value instanceof NDoubleComplex) {
                        setDoubleComplex(((NDoubleComplex) value));
                    } else if (value instanceof NFloatComplex) {
                        setFloatComplex(((NFloatComplex) value));
                    } else if (value instanceof NBigComplex) {
                        setBigComplex(((NBigComplex) value));
                    } else {
                        throw new NIllegalArgumentException(NMsg.ofC("Unsupported type: %s", value.getClass().getName()));
                    }
                }
            }
        }
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setInstant(Instant value) {
        if (value == null) {
            return setNull();
        }
        this.value = value;
        this.type = NElementType.INSTANT;
        this.numberLayout = null;
        this.numberSuffix = null;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setLocalDate(LocalDate value) {
        if (value == null) {
            return setNull();
        }
        this.value = value;
        this.type = NElementType.LOCAL_DATE;
        this.numberLayout = null;
        this.numberSuffix = null;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setLocalDateTime(LocalDateTime value) {
        if (value == null) {
            return setNull();
        }
        this.value = value;
        this.type = NElementType.LOCAL_DATETIME;
        this.numberLayout = null;
        this.numberSuffix = null;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setLocalTime(LocalTime value) {
        if (value == null) {
            return setNull();
        }
        this.value = value;
        this.type = NElementType.LOCAL_TIME;
        this.numberLayout = null;
        this.numberSuffix = null;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setString(String value) {
        if (value == null) {
            return setNull();
        }
        this.value = value;
        this.type = NElementType.DOUBLE_QUOTED_STRING;
        this.numberLayout = null;
        this.numberSuffix = null;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setString(String value, NElementType stringLayout) {
        if (stringLayout == null) {
            stringLayout = NElementType.DOUBLE_QUOTED_STRING;
        }
        NAssert.requireTrue(stringLayout.isString(), "string : " + stringLayout.id());
        if (value == null) {
            setNull();
        } else {
            this.value = value;
            this.type = stringLayout;
            this.numberLayout = null;
            this.numberSuffix = null;
        }
        return this;
    }


    @Override
    public NPrimitiveElementBuilder setBoolean(Boolean value) {
        if (value == null) {
            return setNull();
        }
        this.value = value;
        this.type = NElementType.BOOLEAN;
        this.numberLayout = null;
        this.numberSuffix = null;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setBoolean(boolean value) {
        this.value = value;
        this.type = NElementType.BOOLEAN;
        this.numberLayout = null;
        this.numberSuffix = null;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setSingleQuotedString(String value) {
        return setString(value, NElementType.SINGLE_QUOTED_STRING);
    }

    @Override
    public NPrimitiveElementBuilder setDoubleQuotedString(String value) {
        return setString(value, NElementType.DOUBLE_QUOTED_STRING);
    }

    @Override
    public NPrimitiveElementBuilder setAntiQuotedString(String value) {
        return setString(value, NElementType.ANTI_QUOTED_STRING);
    }

    @Override
    public NPrimitiveElementBuilder setTripleSingleQuotedString(String value) {
        return setString(value, NElementType.TRIPLE_SINGLE_QUOTED_STRING);
    }

    @Override
    public NPrimitiveElementBuilder setTripleDoubleQuotedString(String value) {
        return setString(value, NElementType.TRIPLE_DOUBLE_QUOTED_STRING);
    }

    @Override
    public NPrimitiveElementBuilder setTripleAntiQuotedString(String value) {
        return setString(value, NElementType.TRIPLE_ANTI_QUOTED_STRING);
    }

    @Override
    public NPrimitiveElementBuilder setLineString(String value) {
        return setString(value, NElementType.LINE_STRING);
    }

    @Override
    public NPrimitiveElementBuilder setInt(Integer value) {
        if (value == null) {
            return setNull();
        }
        this.type = NElementType.INTEGER;
        this.value = value;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setLong(Long value) {
        if (value == null) {
            return setNull();
        }
        this.type = NElementType.LONG;
        this.value = value;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setNull() {
        this.value = null;
        this.type = NElementType.NULL;
        this.numberLayout = null;
        this.numberSuffix = null;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setByte(Byte value) {
        if (value == null) {
            return setNull();
        }
        this.type = NElementType.BYTE;
        this.value = value;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setShort(Short value) {
        if (value == null) {
            return setNull();
        }
        this.type = NElementType.SHORT;
        this.value = value;
        this.numberLayout = NNumberLayout.DECIMAL;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setChar(char value) {
        this.type = NElementType.CHAR;
        this.value = value;
        this.numberLayout = null;
        this.numberSuffix = null;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setChar(Character value) {
        if (value == null) {
            return setNull();
        }
        this.type = NElementType.CHAR;
        this.value = value;
        this.numberLayout = null;
        this.numberSuffix = null;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setShort(short value) {
        this.type = NElementType.SHORT;
        this.value = value;
        this.numberLayout = NNumberLayout.DECIMAL;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setDouble(double value) {
        this.type = NElementType.DOUBLE;
        this.value = value;
        this.numberLayout = NNumberLayout.DECIMAL;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setFloat(Float value) {
        if (value == null) {
            return setNull();
        }
        this.type = NElementType.FLOAT;
        this.value = value;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setDouble(Double value) {
        if (value == null) {
            return setNull();
        }
        this.type = NElementType.DOUBLE;
        this.value = value;
        this.numberLayout = NNumberLayout.DECIMAL;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setBigInteger(BigInteger value) {
        if (value == null) {
            return setNull();
        }
        this.type = NElementType.BIG_INTEGER;
        this.value = value;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setBigDecimal(BigDecimal value) {
        if (value == null) {
            return setNull();
        }
        this.type = NElementType.BIG_DECIMAL;
        this.value = value;
        this.numberLayout = NNumberLayout.DECIMAL;
        return this;
    }

    public NPrimitiveElementBuilder setInt(int value) {
        this.type = NElementType.INTEGER;
        this.value = value;
        return this;
    }

    public NPrimitiveElementBuilder setByte(byte value) {
        this.type = NElementType.BYTE;
        this.value = value;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setDoubleComplex(NDoubleComplex value) {
        if (value == null) {
            return setNull();
        }
        this.value = value;
        this.type = NElementType.DOUBLE_COMPLEX;
        this.numberLayout = NNumberLayout.DECIMAL;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setFloatComplex(NFloatComplex value) {
        if (value == null) {
            return setNull();
        }
        this.value = value;
        this.type = NElementType.FLOAT_COMPLEX;
        this.numberLayout = NNumberLayout.DECIMAL;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setBigComplex(NBigComplex value) {
        if (value == null) {
            return setNull();
        }
        this.value = value;
        this.type = NElementType.BIG_COMPLEX;
        this.numberLayout = NNumberLayout.DECIMAL;
        return this;
    }

    @Override
    public NPrimitiveElement build() {
        if (type().isNumber()) {
            return new DefaultNNumberElement(type, (Number) value, numberLayout(), numberSuffix(), annotations().toArray(new NElementAnnotation[0]), comments());
        }
        if (type().isAnyString()) {
            return new DefaultNStringElement(type, (String) value, annotations().toArray(new NElementAnnotation[0]), comments());
        }
        return new DefaultNPrimitiveElement(type, value, annotations().toArray(new NElementAnnotation[0]), comments());
    }

    @Override
    public NElementType type() {
        return type;
    }

    @Override
    public Object value() {
        return value;
    }


    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NPrimitiveElementBuilder addLeadingComment(NElementCommentType type, String text) {
        super.addLeadingComment(type, text);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addTrailingComment(NElementCommentType type, String text) {
        super.addTrailingComment(type, text);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder removeTrailingCommentAt(int index) {
        super.removeTrailingCommentAt(index);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder removeLeadingCommentAt(int index) {
        super.removeLeadingCommentAt(index);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder removeTrailingComment(NElementComment comment) {
        super.removeTrailingComment(comment);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder removeLeadingComment(NElementComment comment) {
        super.removeLeadingComment(comment);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addComments(NElementComments comments) {
        super.addComments(comments);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        super.addAnnotationAt(index, annotation);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder removeAnnotationAt(int index) {
        super.removeAnnotationAt(index);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NPrimitiveElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    public NPrimitiveElementBuilder copyFrom(NPrimitiveElement element) {
        if (element != null) {
            addAnnotations(element.annotations());
            addComments(element.comments());
            value(element.value());
            if (element instanceof NNumberElement) {
                NNumberElement ne = (NNumberElement) element;
                numberLayout(ne.numberLayout());
                numberSuffix(ne.numberSuffix());
            }
        }
        return this;
    }


    @Override
    public NPrimitiveElementBuilder doWith(Consumer<NPrimitiveElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }
}
