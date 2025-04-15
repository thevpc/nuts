package net.thevpc.nuts.runtime.standalone.format.elem;

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
import java.util.function.Consumer;

public class DefaultNPrimitiveElementBuilder extends AbstractNElementBuilder implements NPrimitiveElementBuilder, NLiteral {
    private Object value;
    private NNumberLayout numberLayout;
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
                    this.type = NElementType.DOUBLE_QUOTED_STRING;
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
        if (type().isString()) {
            return new DefaultNStringElement(type, (String) value, annotations().toArray(new NElementAnnotation[0]), comments());
        }
        return new DefaultNPrimitiveElement(type, value, annotations().toArray(new NElementAnnotation[0]), comments());
    }

    @Override
    public NElementType type() {
        return type;
    }

    @Override
    public Object asRawObject() {
        return value;
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public NOptional<Instant> asInstant() {
        return NLiteral.of(value).asInstant();
    }

    @Override
    public NOptional<LocalDate> asLocalDate() {
        return NLiteral.of(value).asLocalDate();
    }

    @Override
    public NOptional<LocalDateTime> asLocalDateTime() {
        return NLiteral.of(value).asLocalDateTime();
    }

    @Override
    public NOptional<LocalTime> asLocalTime() {
        return NLiteral.of(value).asLocalTime();
    }

    @Override
    public NOptional<NBigComplex> asBigComplex() {
        return NLiteral.of(value).asBigComplex();
    }

    @Override
    public NOptional<NDoubleComplex> asDoubleComplex() {
        return NLiteral.of(value).asDoubleComplex();
    }

    @Override
    public NOptional<NFloatComplex> asFloatComplex() {
        return NLiteral.of(value).asFloatComplex();
    }

    @Override
    public NOptional<Number> asNumber() {
        return NLiteral.of(value).asNumber();
    }

    @Override
    public NOptional<Boolean> asBoolean() {
        return NLiteral.of(value).asBoolean();
    }

    @Override
    public NOptional<Long> asLong() {
        return NLiteral.of(value).asLong();
    }

    @Override
    public NOptional<Double> asDouble() {
        return NLiteral.of(value).asDouble();
    }

    @Override
    public NOptional<Float> asFloat() {
        return NLiteral.of(value).asFloat();
    }

    @Override
    public NOptional<Byte> asByte() {
        return NLiteral.of(value).asByte();
    }

    @Override
    public NOptional<Short> asShort() {
        return NLiteral.of(value).asShort();
    }

    @Override
    public NOptional<Character> asChar() {
        return NLiteral.of(value).asChar();
    }

    @Override
    public NOptional<Integer> asInt() {
        return NLiteral.of(value).asInt();
    }

    @Override
    public NOptional<String> asString() {
        return NLiteral.of(value).asString();
    }

    @Override
    public NOptional<BigInteger> asBigInt() {
        return NLiteral.of(value).asBigInt();
    }

    @Override
    public NOptional<BigDecimal> asBigDecimal() {
        return NLiteral.of(value).asBigDecimal();
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
    public NOptional<String> asStringAt(int index) {
        return NLiteral.of(value).asStringAt(index);
    }

    @Override
    public NOptional<Long> asLongAt(int index) {
        return NLiteral.of(value).asLongAt(index);
    }

    @Override
    public NOptional<Integer> asIntAt(int index) {
        return NLiteral.of(value).asIntAt(index);
    }

    @Override
    public NOptional<Double> asDoubleAt(int index) {
        return NLiteral.of(value).asDoubleAt(index);
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
    public NOptional<Object> asObjectAt(int index) {
        return NLiteral.of(value).asObjectAt(index);
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
        }
        return this;
    }

    @Override
    public boolean isOrdinalNumber() {
        return type().isOrdinalNumber();
    }

    @Override
    public boolean isFloatingNumber() {
        return type().isFloatingNumber();
    }

    @Override
    public NPrimitiveElementBuilder doWith(Consumer<NPrimitiveElementBuilder> con) {
        if(con!=null){
            con.accept(this);
        }
        return this;
    }
}
