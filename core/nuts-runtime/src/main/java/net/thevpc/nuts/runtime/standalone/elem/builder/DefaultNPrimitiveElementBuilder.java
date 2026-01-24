package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.math.NBigComplex;
import net.thevpc.nuts.math.NDoubleComplex;
import net.thevpc.nuts.math.NFloatComplex;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNNumberElement;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNPrimitiveElement;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNStringElement;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.text.NMsg;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class DefaultNPrimitiveElementBuilder extends AbstractNElementBuilder implements NPrimitiveElementBuilder {
    private Object value;
    private NNumberLayout numberLayout;
    private String numberSuffix;

    private NElementType type;
    private String image;

    public DefaultNPrimitiveElementBuilder() {
        this.type = NElementType.NULL;
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
            setNull();
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
                    setBigInt(((BigInteger) value));
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
        this.image = String.valueOf(value);
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
        this.image = String.valueOf(value);
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
        this.image = String.valueOf(value);
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
        this.image = String.valueOf(value);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setString(String value) {
        if (value == null) {
            return setNull();
        }
        NElementType newType = this.type;
        if (newType != null && newType.isAnyString()) {
            //okkay
        } else {
            newType = NElementType.DOUBLE_QUOTED_STRING;
        }
        this.value = value;
        this.type = newType;
        this.numberLayout = null;
        this.numberSuffix = null;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setString(String value, NElementType stringLayout) {
        if (stringLayout == null) {
            NElementType newType = this.type;
            if (newType != null && newType.isAnyString()) {
                //okkay
            } else {
                newType = NElementType.DOUBLE_QUOTED_STRING;
            }
            stringLayout = newType;
        }
        NAssert.requireTrue(stringLayout.isAnyString(), "string : " + stringLayout.id());
        if (value == null) {
            setNull();
        } else {
            this.value = value;
            this.image = null;
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
        this.image = String.valueOf(value);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setBoolean(boolean value) {
        this.value = value;
        this.type = NElementType.BOOLEAN;
        this.image = String.valueOf(value);
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
        return setString(value, NElementType.BACKTICK_STRING);
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
        return setString(value, NElementType.TRIPLE_BACKTICK_STRING);
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
        this.type = NElementType.INT;
        this.value = value;
        this.image = String.valueOf(value);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setLong(Long value) {
        if (value == null) {
            return setNull();
        }
        this.type = NElementType.LONG;
        this.value = value;
        this.image = String.valueOf(value);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setNull() {
        this.value = null;
        this.image = "null";
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
        this.image = String.valueOf(value);
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
        this.image = String.valueOf(value);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setChar(char value) {
        this.type = NElementType.CHAR;
        this.value = value;
        this.numberLayout = null;
        this.numberSuffix = null;
        this.image = String.valueOf(value);
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
        this.image = String.valueOf(value);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setShort(short value) {
        this.type = NElementType.SHORT;
        this.value = value;
        this.numberLayout = NNumberLayout.DECIMAL;
        this.image = String.valueOf(value);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setDouble(double value) {
        this.type = NElementType.DOUBLE;
        this.value = value;
        this.numberLayout = NNumberLayout.DECIMAL;
        this.image = String.valueOf(value);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setFloat(Float value) {
        if (value == null) {
            return setNull();
        }
        this.type = NElementType.FLOAT;
        this.value = value;
        this.image = String.valueOf(value);
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
        this.image = String.valueOf(value);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setBigInt(BigInteger value) {
        if (value == null) {
            return setNull();
        }
        this.type = NElementType.BIG_INT;
        this.value = value;
        this.image = String.valueOf(value);
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
        this.image = String.valueOf(value);
        return this;
    }

    public NPrimitiveElementBuilder setInt(int value) {
        this.type = NElementType.INT;
        this.value = value;
        this.image = String.valueOf(value);
        return this;
    }

    public NPrimitiveElementBuilder setByte(byte value) {
        this.type = NElementType.BYTE;
        this.value = value;
        this.image = String.valueOf(value);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setDoubleComplex(NDoubleComplex value) {
        if (value == null) {
            return setNull();
        }
        this.value = value;
        this.image = String.valueOf(value);
        this.type = NElementType.DOUBLE_COMPLEX;
        this.numberLayout = NNumberLayout.DECIMAL;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setFloatComplex(NFloatComplex value) {
        if (value == null) {
            return setNull();
        }
        this.image = String.valueOf(value);
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
        this.image = String.valueOf(value);
        this.value = value;
        this.type = NElementType.BIG_COMPLEX;
        this.numberLayout = NNumberLayout.DECIMAL;
        return this;
    }

    @Override
    public NPrimitiveElement build() {
        if (type().isAnyNumber()) {
            return new DefaultNNumberElement(type, (Number) value, numberLayout(), numberSuffix(), image, affixes(), diagnostics());
        }
        if (type().isAnyStringOrName()) {
            return new DefaultNStringElement(type, (String) value, image, affixes(), diagnostics());
        }
        return new DefaultNPrimitiveElement(type, value, affixes(), diagnostics());
    }

    @Override
    public NElementType type() {
        return type;
    }

    @Override
    public Object value() {
        return value;
    }

    public NPrimitiveElementBuilder copyFrom(NPrimitiveElement element) {
        return copyFrom(element, NAssignmentPolicy.ANY);
    }

    @Override
    public NPrimitiveElementBuilder doWith(Consumer<NPrimitiveElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }

    // ------------------------------------------

    @Override
    public NPrimitiveElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NPrimitiveElementBuilder) {
            NPrimitiveElementBuilder b = (NPrimitiveElementBuilder) other;
            this.type = b.type();
            this.value = b.value();
            this.numberLayout = b.numberLayout();
            this.numberSuffix = b.numberSuffix();
            this.image = b.image();
        }
        return this;
    }

    public String image() {
        return image;
    }

    @Override
    public NPrimitiveElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NPrimitiveElement) {
            NPrimitiveElement b = (NPrimitiveElement) other;
            this.type = b.type();
            this.value = b.value();
            if (other instanceof NNumberElement) {
                NNumberElement nfrom = (NNumberElement) other;
                this.numberLayout = nfrom.numberLayout();
                this.numberSuffix = nfrom.numberSuffix();
                this.image = nfrom.image();
            }
        }
        return this;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

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
    public NPrimitiveElementBuilder addAffix(int index, NBoundAffix affix) {
        super.addAffix(index, affix);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder removeAffix(int index) {
        super.removeAffix(index);
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

    @Override
    public NPrimitiveElementBuilder setAffix(int index, NBoundAffix affix) {
        super.setAffix(index, affix);
        return this;
    }

    public NPrimitiveElementBuilder addAffix(NBoundAffix affix){
        super.addAffix(affix);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder removeDiagnostic(NElementDiagnostic error) {
        super.removeDiagnostic(error);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addAffixes(List<NBoundAffix> affixes) {
        super.addAffixes(affixes);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addDiagnostic(NElementDiagnostic error) {
        super.addDiagnostic(error);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        super.addAffixes(affixes, anchor);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        super.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        super.removeAffixes(type, anchor);
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
    public NPrimitiveElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder copyFrom(NElementBuilder other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder copyFrom(NElement other) {
        super.copyFrom(other);
        return this;
    }

}
