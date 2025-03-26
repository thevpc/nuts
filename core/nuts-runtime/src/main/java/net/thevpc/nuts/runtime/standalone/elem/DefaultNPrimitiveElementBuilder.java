package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class DefaultNPrimitiveElementBuilder extends AbstractNElementBuilder implements NPrimitiveElementBuilder, NLiteral {
    private Object value;
    private NNumberLayout numberLayout;
    private NStringLayout stringLayout;
    private String numberSuffix;

    private NElementType type;

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

    public NStringLayout stringLayout() {
        return stringLayout;
    }

    public NPrimitiveElementBuilder stringLayout(NStringLayout stringLayout) {
        this.stringLayout = stringLayout;
        return this;
    }

    public String numberSuffix() {
        return numberSuffix;
    }

    public NPrimitiveElementBuilder numberSuffix(String numberSuffix) {
        this.numberSuffix = numberSuffix;
        return this;
    }

    public NPrimitiveElementBuilder value(Object value) {
        if (value == null) {
            this.value = null;
            this.type = NElementType.NULL;
        } else {
            switch (value.getClass().getName()) {
                case "java.lang.String": {
                    this.value = value;
                    this.type = NElementType.STRING;
                    break;
                }
                case "java.lang.Boolean":
                case "boolean": {
                    this.value = value;
                    this.type = NElementType.BOOLEAN;
                    break;
                }
                case "java.lang.Byte":
                case "byte": {
                    this.value = value;
                    this.type = NElementType.BYTE;
                    break;
                }
                case "java.lang.Short":
                case "short": {
                    this.value = value;
                    this.type = NElementType.SHORT;
                    break;
                }
                case "java.lang.Character":
                case "char": {
                    this.value = value;
                    this.type = NElementType.CHAR;
                    break;
                }
                case "java.lang.Integer":
                case "int": {
                    this.value = value;
                    this.type = NElementType.INTEGER;
                    break;
                }
                case "java.lang.Long":
                case "long": {
                    this.value = value;
                    this.type = NElementType.LONG;
                    break;
                }
                case "java.lang.Float":
                case "float": {
                    this.value = value;
                    this.type = NElementType.FLOAT;
                    break;
                }
                case "java.lang.Double":
                case "double": {
                    this.value = value;
                    this.type = NElementType.DOUBLE;
                    break;
                }
                case "java.time.Instant": {
                    this.value = value;
                    this.type = NElementType.INSTANT;
                    break;
                }
                case "java.math.BigInteger": {
                    this.value = value;
                    this.type = NElementType.BIG_INTEGER;
                    break;
                }
                case "java.math.BigDecimal": {
                    this.value = value;
                    this.type = NElementType.BIG_DECIMAL;
                    break;
                }
                default: {
                    if (value instanceof NDoubleComplex) {
                        this.value = value;
                        this.type = NElementType.DOUBLE_COMPLEX;
                    } else if (value instanceof NFloatComplex) {
                        this.value = value;
                        this.type = NElementType.FLOAT_COMPLEX;
                    } else if (value instanceof NBigComplex) {
                        this.value = value;
                        this.type = NElementType.BIG_COMPLEX;
                    } else {
                        throw new NIllegalArgumentException(NMsg.ofC("Unsupported type: %s", value.getClass().getName()));
                    }
                }
            }
        }
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
            this.value = null;
            this.type = NElementType.NULL;
        } else {
            this.value = value;
            this.type = NElementType.DOUBLE_COMPLEX;
        }
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setFloatComplex(NFloatComplex value) {
        if (value == null) {
            this.value = null;
            this.type = NElementType.NULL;
        } else {
            this.value = value;
            this.type = NElementType.FLOAT_COMPLEX;
        }
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setBigComplex(NBigComplex value) {
        if (value == null) {
            this.value = null;
            this.type = NElementType.NULL;
        } else {
            this.value = value;
            this.type = NElementType.BIG_COMPLEX;
        }
        return this;
    }

    @Override
    public NPrimitiveElement build() {
        if (type().isNumber()) {
            return new DefaultNNumberElement(type, (Number) value, numberLayout(), numberSuffix(), annotations().toArray(new NElementAnnotation[0]), comments());
        }
        if (type() == NElementType.STRING) {
            return new DefaultNStringElement(type, (String) value, stringLayout(), annotations().toArray(new NElementAnnotation[0]), comments());
        }
        return new DefaultNPrimitiveElement(type, value, annotations().toArray(new NElementAnnotation[0]), comments());
    }

    @Override
    public NElementType type() {
        return type;
    }

    @Override
    public Object asObjectValue() {
        return value;
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public NOptional<Instant> asInstantValue() {
        return NLiteral.of(value).asInstantValue();
    }

    @Override
    public NOptional<LocalDate> asLocalDateValue() {
        return NLiteral.of(value).asLocalDateValue();
    }

    @Override
    public NOptional<LocalDateTime> asLocalDateTimeValue() {
        return NLiteral.of(value).asLocalDateTimeValue();
    }

    @Override
    public NOptional<LocalTime> asLocalTimeValue() {
        return NLiteral.of(value).asLocalTimeValue();
    }

    @Override
    public NOptional<NBigComplex> asBigComplexValue() {
        return NLiteral.of(value).asBigComplexValue();
    }

    @Override
    public NOptional<NDoubleComplex> asDoubleComplexValue() {
        return NLiteral.of(value).asDoubleComplexValue();
    }

    @Override
    public NOptional<NFloatComplex> asFloatComplexValue() {
        return NLiteral.of(value).asFloatComplexValue();
    }

    @Override
    public NOptional<Number> asNumberValue() {
        return NLiteral.of(value).asNumberValue();
    }

    @Override
    public NOptional<Boolean> asBooleanValue() {
        return NLiteral.of(value).asBooleanValue();
    }

    @Override
    public NOptional<Long> asLongValue() {
        return NLiteral.of(value).asLongValue();
    }

    @Override
    public NOptional<Double> asDoubleValue() {
        return NLiteral.of(value).asDoubleValue();
    }

    @Override
    public NOptional<Float> asFloatValue() {
        return NLiteral.of(value).asFloatValue();
    }

    @Override
    public NOptional<Byte> asByteValue() {
        return NLiteral.of(value).asByteValue();
    }

    @Override
    public NOptional<Short> asShortValue() {
        return NLiteral.of(value).asShortValue();
    }

    @Override
    public NOptional<Character> asCharValue() {
        return NLiteral.of(value).asCharValue();
    }

    @Override
    public NOptional<Integer> asIntValue() {
        return NLiteral.of(value).asIntValue();
    }

    @Override
    public NOptional<String> asStringValue() {
        return NLiteral.of(value).asStringValue();
    }

    @Override
    public NOptional<BigInteger> asBigIntValue() {
        return NLiteral.of(value).asBigIntValue();
    }

    @Override
    public NOptional<BigDecimal> asBigDecimalValue() {
        return NLiteral.of(value).asBigDecimalValue();
    }

    @Override
    public boolean isBoolean() {
        return NLiteral.of(value).isBoolean();
    }

    @Override
    public boolean isDecimalNumber() {
        return NLiteral.of(value).isDecimalNumber();
    }

    @Override
    public boolean isBigNumber() {
        return NLiteral.of(value).isBigNumber();
    }

    @Override
    public boolean isBigDecimal() {
        return NLiteral.of(value).isBigDecimal();
    }

    @Override
    public boolean isBigInt() {
        return NLiteral.of(value).isBigInt();
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    public boolean isString() {
        return NLiteral.of(value).isString();
    }

    @Override
    public boolean isByte() {
        return NLiteral.of(value).isByte();
    }

    @Override
    public boolean isInt() {
        return NLiteral.of(value).isInt();
    }

    @Override
    public boolean isLong() {
        return NLiteral.of(value).isLong();
    }

    @Override
    public boolean isShort() {
        return NLiteral.of(value).isShort();
    }

    @Override
    public boolean isFloat() {
        return NLiteral.of(value).isFloat();
    }

    @Override
    public boolean isDouble() {
        return NLiteral.of(value).isDouble();
    }

    @Override
    public boolean isInstant() {
        return NLiteral.of(value).isInstant();
    }

    @Override
    public String toStringLiteral() {
        return NLiteral.of(value).toStringLiteral();
    }

    @Override
    public boolean isEmpty() {
        return NLiteral.of(value).isEmpty();
    }

    @Override
    public NOptional<String> asStringValueAt(int index) {
        return NLiteral.of(value).asStringValueAt(index);
    }

    @Override
    public NOptional<Long> asLongValueAt(int index) {
        return NLiteral.of(value).asLongValueAt(index);
    }

    @Override
    public NOptional<Integer> asIntValueAt(int index) {
        return NLiteral.of(value).asIntValueAt(index);
    }

    @Override
    public NOptional<Double> asDoubleValueAt(int index) {
        return NLiteral.of(value).asDoubleValueAt(index);
    }

    @Override
    public boolean isNullAt(int index) {
        return NLiteral.of(value).isNullAt(index);
    }

    @Override
    public NLiteral asLiteralAt(int index) {
        return NLiteral.of(value).asLiteralAt(index);
    }

    @Override
    public NOptional<Object> asObjectValueAt(int index) {
        return NLiteral.of(value).asObjectValueAt(index);
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(value);
    }

    @Override
    public boolean isNumber() {
        return NLiteral.of(value).isNumber();
    }

    @Override
    public boolean isSupportedType(Class<?> type) {
        if (type == null) {
            return false;
        }
        switch (type.getName()) {
            case "java.lang.String":
            case "java.lang.Boolean":
            case "boolean":
            case "java.lang.Byte":
            case "byte":
            case "java.lang.Short":
            case "short":
            case "java.lang.Character":
            case "char":
            case "java.lang.Integer":
            case "int":
            case "java.lang.Long":
            case "long":
            case "java.lang.Float":
            case "float":
            case "java.lang.Double":
            case "double":
            case "java.time.Instant":
            case "java.lang.Number":
                return true;
        }
        if (Number.class.isAssignableFrom(type)) {
            return true;
        }
        return false;
    }

    @Override
    public <ET> NOptional<ET> asType(Class<ET> expectedType) {
        return NLiteral.of(value).asType(expectedType);
    }

    @Override
    public <ET> NOptional<ET> asType(Type expectedType) {
        return NLiteral.of(value).asType(expectedType);
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

    @Override
    public boolean isStream() {
        return type().isStream();
    }

    @Override
    public boolean isComplexNumber() {
        return type().isStream();
    }

    @Override
    public boolean isTemporal() {
        return type().isTemporal();
    }

    @Override
    public boolean isLocalTemporal() {
        return type().isLocalTemporal();
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
            if (element instanceof NStringElement) {
                NStringElement ne = (NStringElement) element;
                stringLayout(ne.stringLayout());
            }
        }
        return this;
    }

    public NPrimitiveElementBuilder copyFrom(NPrimitiveElementBuilder element) {
        if (element != null) {
            addAnnotations(element.annotations());
            addComments(element.comments());
            value(element.value());
            numberLayout(element.numberLayout());
            numberSuffix(element.numberSuffix());
            stringLayout(element.stringLayout());
        }
        return this;
    }
}
