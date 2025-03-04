package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultNPrimitiveElementBuilder implements NPrimitiveElementBuilder, NLiteral {
    private Object value;
    private final List<NElementAnnotation> annotations = new ArrayList<>();

    private transient NWorkspace workspace;
    private NElementType type;

    public DefaultNPrimitiveElementBuilder(NWorkspace workspace) {
        this.workspace = workspace;
        this.type = NElementType.NULL;
    }

    @Override
    public NPrimitiveElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        if (annotations != null) {
            for (NElementAnnotation a : annotations) {
                if (a != null) {
                    this.annotations.add(a);
                }
            }
        }
        return this;
    }

    public Object get() {
        return value;
    }

    public NPrimitiveElementBuilder setValue(Object value) {
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
                    throw new NIllegalArgumentException(NMsg.ofC("Unsupported type: %s", value.getClass().getName()));
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
    public NPrimitiveElementBuilder addAnnotation(NElementAnnotation annotation) {
        if (annotation != null) {
            annotations.add(annotation);
        }
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        if (annotation != null) {
            annotations.add(index, annotation);
        }
        return this;
    }

    @Override
    public NPrimitiveElementBuilder removeAnnotationAt(int index) {
        annotations.remove(index);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder clearAnnotations() {
        annotations.clear();
        return this;
    }

    @Override
    public List<NElementAnnotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    @Override
    public NPrimitiveElement build() {
        return new DefaultNPrimitiveElement(type, value, annotations.toArray(new NElementAnnotation[0]), workspace);
    }

    @Override
    public NElementType type() {
        return type;
    }

    @Override
    public Object getRaw() {
        return value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NOptional<Instant> asInstant() {
        return NLiteral.of(value).asInstant();
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
}
