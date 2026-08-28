package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.expr.NFixity;
import net.thevpc.nuts.expr.NOperatorAssociativity;
import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.io.NReaderProvider;
import net.thevpc.nuts.math.NBigComplex;
import net.thevpc.nuts.math.NDoubleComplex;
import net.thevpc.nuts.math.NFloatComplex;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.runtime.standalone.elem.builder.*;
import net.thevpc.nuts.runtime.standalone.elem.item.*;
import net.thevpc.nuts.runtime.standalone.elem.path.NElementSelectorFilters;
import net.thevpc.nuts.runtime.standalone.elem.steps.NElementStepAnnotationParam;
import net.thevpc.nuts.runtime.standalone.elem.steps.NElementStepChild;
import net.thevpc.nuts.runtime.standalone.elem.steps.NElementStepParam;
import net.thevpc.nuts.runtime.standalone.elem.steps.NElementStepSubList;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementLineImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.custom.TsonCustomLexer;
import net.thevpc.nuts.runtime.standalone.util.DefaultNLiteral;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNElementRPI implements NElementRPI {


    public DefaultNElementRPI() {
    }

    @Override
    public NElements getSharedElements() {
        return NWorkspaceExt.of().getModel().defaultElements;
    }

    @Override
    public NElementFormatterBuilder createElementFormatterBuilder() {
        return new DefaultNElementFormatterBuilder();
    }

    @Override
    public NElementPath createRootPath() {
        return DefaultNElementPath.ROOT;
    }


    @Override
    public NElementSelector compileSelector(String pathExpression) {
        return NElementSelectorFilters.compile(pathExpression);
    }

    public NElementType commonNumberType(NElementType aa, NElementType bb) {
        if (aa != null) {
            NAssert.requireNamedEquals(NElementTypeGroup.NUMBER, aa.group(), "aa typeGroup");
        }
        if (bb != null) {
            NAssert.requireNamedEquals(NElementTypeGroup.NUMBER, bb.group(), "bb typeGroup");
        }

        if (aa == null && bb == null) {
            return null;
        }
        if (aa == null) {
            return bb;
        }
        if (bb == null) {
            return aa;
        }
        if (NElementType.BIG_COMPLEX == aa || NElementType.BIG_COMPLEX.equals(bb)) {
            return NElementType.BIG_COMPLEX;
        }

        if (NElementType.DOUBLE_COMPLEX == aa || NElementType.DOUBLE_COMPLEX.equals(bb)) {
            if (
                    NElementType.BIG_DECIMAL == aa || NElementType.BIG_DECIMAL.equals(bb)
                            || NElementType.BIG_INT == aa || NElementType.BIG_INT.equals(bb)
            ) {
                return NElementType.BIG_COMPLEX;
            }
            return NElementType.DOUBLE_COMPLEX;
        }

        if (NElementType.FLOAT_COMPLEX == aa || NElementType.FLOAT_COMPLEX.equals(bb)) {
            if (
                    NElementType.BIG_DECIMAL == aa || NElementType.BIG_DECIMAL.equals(bb)
                            || NElementType.BIG_INT == aa || NElementType.BIG_INT.equals(bb)
            ) {
                return NElementType.BIG_COMPLEX;
            }
            if (
                    NElementType.DOUBLE == aa || NElementType.DOUBLE == bb
            ) {
                return NElementType.DOUBLE_COMPLEX;
            }
            return NElementType.FLOAT_COMPLEX;
        }


        if (NElementType.BIG_DECIMAL == aa || NElementType.BIG_DECIMAL.equals(bb)) {
            return NElementType.BIG_DECIMAL;
        }
        if (NElementType.BIG_INT.equals(aa) || NElementType.BIG_INT.equals(bb)) {
            if (NElementType.DOUBLE.equals(aa) || NElementType.DOUBLE.equals(bb) || NElementType.FLOAT.equals(aa) || NElementType.FLOAT.equals(bb)) {
                return NElementType.BIG_DECIMAL;
            }
            return NElementType.BIG_INT;
        }
        if (NElementType.DOUBLE.equals(aa) || NElementType.DOUBLE.equals(bb)) {
            return NElementType.DOUBLE;
        }
        if (NElementType.FLOAT.equals(aa) || NElementType.FLOAT.equals(bb)) {
            if (NElementType.LONG.equals(aa) || NElementType.LONG.equals(bb)) {
                return NElementType.DOUBLE;
            }
            return NElementType.FLOAT;
        }
        if (NElementType.LONG.equals(aa) || NElementType.LONG.equals(bb)) {
            return NElementType.LONG;
        }
        if (NElementType.INT.equals(aa) || NElementType.INT.equals(bb)) {
            return NElementType.INT;
        }
        if (NElementType.SHORT.equals(aa) || NElementType.SHORT.equals(bb)) {
            return NElementType.SHORT;
        }
        if (NElementType.BYTE.equals(aa) || NElementType.BYTE.equals(bb)) {
            return NElementType.BYTE;
        }
        return aa;
    }

    @Override
    public NExprElementReshaperBuilder createExprElementReshaperBuilder(NExprElementReshaperType type) {
        switch (type == null ? NExprElementReshaperType.DEFAULT : type) {
            case EMPTY:
                return new DefaultNExprElementReshaperBuilder();
            case JAVA:
            case DEFAULT: {
                return new DefaultNExprElementReshaperBuilder()
                        // Unary operators (high precedence)
                        .addUnaryOperator(NOperatorSymbol.NOT)       // !
                        .addUnaryOperator(NOperatorSymbol.TILDE)     // ~
                        .addUnaryOperator(NOperatorSymbol.MINUS)     // -x
                        .addUnaryOperator(NOperatorSymbol.PLUS)      // +x
                        .addBinaryOperator(NOperatorSymbol.EQ, 0, NOperatorAssociativity.RIGHT) // lowest precedence

                        // Multiplicative
                        .addBinaryOperator(NOperatorSymbol.MUL, 30, NOperatorAssociativity.LEFT)      // *
                        .addBinaryOperator(NOperatorSymbol.DIV, 30, NOperatorAssociativity.LEFT)      // /
                        .addBinaryOperator(NOperatorSymbol.REM, 30, NOperatorAssociativity.LEFT)      // %

                        // Additive
                        .addBinaryOperator(NOperatorSymbol.PLUS, 20, NOperatorAssociativity.LEFT)     // a + b
                        .addBinaryOperator(NOperatorSymbol.MINUS, 20, NOperatorAssociativity.LEFT)    // a - b

                        // Relational
                        .addBinaryOperator(NOperatorSymbol.LT, 10, NOperatorAssociativity.LEFT)
                        .addBinaryOperator(NOperatorSymbol.GT, 10, NOperatorAssociativity.LEFT)
                        .addBinaryOperator(NOperatorSymbol.LTE, 10, NOperatorAssociativity.LEFT)
                        .addBinaryOperator(NOperatorSymbol.GTE, 10, NOperatorAssociativity.LEFT)

                        // Equality
                        .addBinaryOperator(NOperatorSymbol.EQ2, 5, NOperatorAssociativity.LEFT)        // ==
                        .addBinaryOperator(NOperatorSymbol.NOT_EQ, 5, NOperatorAssociativity.LEFT) // !=

                        // Logical AND
                        .addBinaryOperator(NOperatorSymbol.AND2, 3, NOperatorAssociativity.LEFT)       // &&

                        // Logical OR
                        .addBinaryOperator(NOperatorSymbol.PIPE2, 1, NOperatorAssociativity.LEFT)      // ||
                        ;
            }
            case LEFT_ASSOCIATIVE: {
                DefaultNExprElementReshaperBuilder r = new DefaultNExprElementReshaperBuilder();
                // Add all known operators with same precedence
                for (NOperatorSymbol op : NOperatorSymbol.values()) {
                    if (op == NOperatorSymbol.NOT || op == NOperatorSymbol.TILDE || op == NOperatorSymbol.MINUS || op == NOperatorSymbol.PLUS) {
                        r.addUnaryOperator(op);
                    } else {
                        r.addBinaryOperator(op, 1, NOperatorAssociativity.LEFT);
                    }
                }
                return r;
            }
            case LOGICAL: {
                DefaultNExprElementReshaperBuilder r = new DefaultNExprElementReshaperBuilder();
                r.addUnaryOperator(NOperatorSymbol.NOT);
                r.addBinaryOperator(NOperatorSymbol.AND2, 2, NOperatorAssociativity.LEFT);
                r.addBinaryOperator(NOperatorSymbol.PIPE2, 1, NOperatorAssociativity.LEFT);
                r.addBinaryOperator(NOperatorSymbol.EQ2, 0, NOperatorAssociativity.LEFT);
                r.addBinaryOperator(NOperatorSymbol.NOT_EQ, 0, NOperatorAssociativity.LEFT);
                return r;
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("never happens"));
    }

    @Override
    public NExprElementReshaper createExprElementReshaper(NExprElementReshaperType type) {
        NExprElementReshaperType vtype = type == null ? NExprElementReshaperType.DEFAULT : type;
        return NWorkspace.of().getOrComputeProperty(
                NExprElementReshaper.class.getName() + "::" + vtype,
                () -> createExprElementReshaperBuilder(vtype).build()
        );
    }

    @Override
    public NElementFormatter createElementFormatter(NElementFormatterStyle style) {
        if (style == null) {
            style = NElementFormatterStyle.PRETTY;
        }
        switch (style) {
            case CUSTOM:
            case PRETTY:
                return DefaultNElementFormatter.PRETTY;
            case COMPACT: {
                return DefaultNElementFormatter.COMPACT;
            }
            case STABLE: {
                return DefaultNElementFormatter.STABLE;
            }
            case SIMPLE: {
                return DefaultNElementFormatter.SIMPLE;
            }
            case VERBATIM: {
                return DefaultNElementFormatter.VERBATIM;
            }
        }
        return DefaultNElementFormatter.PRETTY;
    }


    @Override
    public NElementMetadata createElementMetadata() {
        return DefaultNElementMetadata.EMPTY;
    }

    @Override
    public NElementMetadata createElementMetadata(Object key, Object value) {
        return NElementMetadata.of(key, value);
    }

    @Override
    public NElementMetadata createElementMetadata(Map<Object, Object> any) {
        return NElementMetadata.of(any);
    }

    @Override
    public NElementStep createStepChild(String name) {
        return new NElementStepChild(name);
    }

    @Override
    public NElementStep createStepChild(int index) {
        return new NElementStepChild(index);
    }

    @Override
    public NElementStep createStepParam(String name) {
        return new NElementStepParam(name);
    }

    @Override
    public NElementStep createStepParam(int index) {
        return new NElementStepParam(index);
    }

    @Override
    public NElementStep createStepAnnotationParam(int paramIndex, String name) {
        return new NElementStepAnnotationParam(paramIndex, name);
    }

    @Override
    public NElementStep createStepAnnotationParam(int paramIndex, int index) {
        return new NElementStepAnnotationParam(paramIndex, index);
    }

    @Override
    public NElementStep createStepSubList(int index) {
        return new NElementStepSubList(index);
    }

    @Override
    public NElementNavigator createRootNavigator(NElement element) {
        return new DefaultNElementNavigator(
                null, element, NElementPath.ofRoot()
        );
    }

    @Override
    public NPairElement createPair(NElement key, NElement value) {
        return new DefaultNPairElement(
                key == null ? createNull() : key,
                value == null ? createNull() : value
        );
    }

    @Override
    public NPairElement createPair(String key, NElement value) {
        return createPair(createNameOrString(key), value);
    }

    @Override
    public NPairElement createPair(String key, Boolean value) {
        return createPair(createNameOrString(key), createBoolean(value));
    }

    @Override
    public NPairElement createPair(String key, Number value) {
        return createPair(createNameOrString(key), createNumber(value));
    }

    @Override
    public NPairElement createPair(String key, Short value) {
        return createPair(createNameOrString(key), createShort(value));
    }

    @Override
    public NPairElement createPair(String key, Byte value) {
        return createPair(createNameOrString(key), createByte(value));
    }

    @Override
    public NPairElement createPair(String key, Integer value) {
        return createPair(createNameOrString(key), createInt(value));
    }

    @Override
    public NPairElement createPair(String key, Long value) {
        return createPair(createNameOrString(key), createLong(value));
    }

    @Override
    public NPairElement createPair(String key, String value) {
        return createPair(createNameOrString(key), createString(value));
    }

    @Override
    public NPairElement createPair(String key, Double value) {
        return createPair(createNameOrString(key), createDouble(value));
    }

    @Override
    public NPairElement createPair(String key, Instant value) {
        return createPair(createNameOrString(key), createInstant(value));
    }

    @Override
    public NPairElement createPair(String key, LocalDate value) {
        return createPair(createNameOrString(key), createLocalDate(value));
    }

    @Override
    public NPairElement createPair(String key, LocalDateTime value) {
        return createPair(createNameOrString(key), createLocalDateTime(value));
    }

    @Override
    public NOperatorSymbolElement createOp(NOperatorSymbol op) {
        return new DefaultNOperatorSymbolElement(op);
    }

    @Override
    public NPairElement createPair(String key, LocalTime value) {
        return createPair(createNameOrString(key), createLocalTime(value));
    }

    @Override
    public NPairElementBuilder createPairBuilder(NElement key, NElement value) {
        return new DefaultNPairElementBuilder(
                key == null ? createNull() : key,
                value == null ? createNull() : value
        );
    }

    @Override
    public NOperatorElementBuilder createOpBuilder() {
        return new DefaultNOperatorElementBuilder();
    }


    @Override
    public NOperatorElement createBinaryInfixOperator(NOperatorSymbol op, NElement first, NElement second) {
        NAssert.requireNamedNonNull(op, "operator");
        NAssert.requireNamedNonNull(first, "first operand");
        NAssert.requireNamedNonNull(second, "second operand");
        return createOpBuilder().operator(op).fixity(NFixity.INFIX).first(first).second(second).build();
    }

    @Override
    public NEmptyElementBuilder createErrorBuilder() {
        return new DefaultNEmptyElementBuilder();
    }

    @Override
    public NElementDiagnosticBuilder createDiagnosticBuilder() {
        return new DefaultNElementDiagnosticBuilder();
    }

    @Override
    public NElementDiagnostic createDiagnostic(NMsg msg) {
        return new DefaultNElementDiagnosticBuilder().message(msg).build();
    }

    @Override
    public NElementSeparator createSeparator(String value) {
        return DefaultNElementSeparator.of(value);
    }

    @Override
    public NElementSeparator createSeparator(char value) {
        return DefaultNElementSeparator.of(value);
    }

    @Override
    public NElementSpace createSpace(String value) {
        return DefaultNElementSpace.of(value);
    }

    @Override
    public NElementNewLine createNewline(String value) {
        return DefaultNElementNewLine.of(value);
    }

    @Override
    public NBoundAffix createBoundAffix(NAffix affix, NAffixAnchor anchor) {
        NAssert.requireNamedNonNull(affix, "affix");
        NAssert.requireNamedNonNull(anchor, "anchor");
        return DefaultNBoundAffix.of(affix, anchor);
    }

    @Override
    public NOperatorElement createUnaryPrefixOperator(NOperatorSymbol op, NElement operand) {
        NAssert.requireNamedNonNull(op, "operator");
        NAssert.requireNamedNonNull(operand, "operand");
        return createOpBuilder().operator(op).fixity(NFixity.PREFIX).first(operand).build();
    }

    @Override
    public NPairElementBuilder createPairBuilder() {
        return new DefaultNPairElementBuilder();
    }

    @Override
    public NObjectElementBuilder createObjectBuilder() {
        return new DefaultNObjectElementBuilder();
    }

    @Override
    public NObjectElementBuilder createObjectBuilder(String name) {
        return createObjectBuilder().name(name);
    }

    @Override
    public NArrayElementBuilder createArrayBuilder() {
        return new DefaultNArrayElementBuilder();
    }

    @Override
    public NArrayElementBuilder createArrayBuilder(String name) {
        return createArrayBuilder().name(name);
    }

    @Override
    public NArrayElement createArray() {
        return createArrayBuilder().build();
    }


    @Override
    public NArrayElement createStringArray(String... items) {
        return createArrayBuilder().addAll(Arrays.stream(items).map(this::createString).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createDoubleArray(double... items) {
        return createArrayBuilder().addAll(Arrays.stream(items).mapToObj(this::createDouble).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createDoubleArray(Double... items) {
        return createArrayBuilder().addAll(Arrays.stream(items).map(this::createDouble).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createFloatArray(float... items) {
        return createArrayBuilder().addAll(IntStream.range(0, items.length).mapToObj(i -> createFloat(items[i])).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createFloatArray(Float... items) {
        return createArrayBuilder().addAll(Arrays.stream(items).map(this::createFloat).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createByteArray(byte... items) {
        return createArrayBuilder().addAll(IntStream.range(0, items.length).mapToObj(i -> createByte(items[i])).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createCharArray(char... items) {
        return createArrayBuilder().addAll(IntStream.range(0, items.length).mapToObj(i -> createChar(items[i])).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createCharArray(Character... items) {
        return createArrayBuilder().addAll(IntStream.range(0, items.length).mapToObj(i -> createChar(items[i])).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createByteArray(Byte... items) {
        return createArrayBuilder().addAll(Arrays.stream(items).map(this::createByte).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createShortArray(short... items) {
        return createArrayBuilder().addAll(IntStream.range(0, items.length).mapToObj(i -> createShort(items[i])).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createShortArray(Short... items) {
        return createArrayBuilder().addAll(Arrays.stream(items).map(this::createShort).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createIntArray(int... items) {
        return createArrayBuilder().addAll(Arrays.stream(items).mapToObj(this::createInt).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createIntArray(Integer... items) {
        return createArrayBuilder().addAll(Arrays.stream(items).map(this::createInt).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createLongArray(long... items) {
        return createArrayBuilder().addAll(Arrays.stream(items).mapToObj(this::createLong).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createLongArray(Long... items) {
        return createArrayBuilder().addAll(Arrays.stream(items).map(this::createLong).collect(Collectors.toList())).build();
    }


    @Override
    public NArrayElement createNumberArray(Number... items) {
        return createArrayBuilder().addAll(Arrays.stream(items).map(this::createNumber).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createBooleanArray(boolean... items) {
        NArrayElementBuilder b = createArrayBuilder();
        for (boolean item : items) {
            b.add(item);
        }
        return b.build();
    }

    @Override
    public NArrayElement createBooleanArray(Boolean... items) {
        return createArrayBuilder().addAll(Arrays.stream(items).map(this::createBoolean).collect(Collectors.toList())).build();
    }

    @Override
    public NArrayElement createArray(NElement... items) {
        return createArrayBuilder().addAll(items).build();
    }

    @Override
    public NArrayElement createArray(String name, NElement... items) {
        return createArrayBuilder().name(name).addAll(items).build();
    }

    @Override
    public NArrayElement createNamedArray(String name, NElement... items) {
        return createArrayBuilder().name(name).addAll(items).build();
    }

    @Override
    public NArrayElement createFullArray(String name, NElement[] params, NElement... items) {
        return createArrayBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NArrayElement createArray(String name, NElement[] params, NElement... items) {
        return createArrayBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NArrayElement createParamArray(NElement... params) {
        return createArrayBuilder().addParams(params == null ? null : Arrays.asList(params)).build();
    }


    @Override
    public NArrayElement createParamArray(NElement[] params, NElement... items) {
        return createArrayBuilder().addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NArrayElement createParamArray(String name, NElement[] params, NElement... items) {
        return createArrayBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NObjectElement createObject(NElement... items) {
        return createObjectBuilder().addAll(items).build();
    }

    @Override
    public NArrayElement createParamArray(String name, NElement... params) {
        return createArrayBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).build();
    }

    @Override
    public NObjectElement createObject(String name, NElement... items) {
        return createObjectBuilder().name(name).addAll(items).build();
    }

    @Override
    public NObjectElement createParamObject(NElement[] params, NElement... items) {
        return createObjectBuilder().addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NObjectElement createParamObject(String name, NElement[] params, NElement... items) {
        return createObjectBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NObjectElement createObject(String name, NElement[] params, NElement... items) {
        return createObjectBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NObjectElement createNamedObject(String name, NElement... items) {
        return createObjectBuilder().name(name).addAll(items).build();
    }

    @Override
    public NObjectElement createFullObject(String name, NElement[] params, NElement... items) {
        return createObjectBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).addAll(items).build();
    }

    @Override
    public NObjectElement createParamObject(NElement... params) {
        return createObjectBuilder().addParams(params == null ? null : Arrays.asList(params)).build();
    }

    @Override
    public NObjectElement createParamObject(String name, NElement... params) {
        return createObjectBuilder().name(name).addParams(params == null ? null : Arrays.asList(params)).build();
    }

    @Override
    public NObjectElement createObject() {
        return createObjectBuilder().build();
    }

    @Override
    public NPrimitiveElement createBoolean(String value) {
        NOptional<Boolean> o = NLiteral.of(value).asBoolean();
        if (o.isEmpty()) {
            return createNull();
        }
        return createBoolean(o.get());
    }

    @Override
    public NPrimitiveElement createBoolean(boolean value) {
        //TODO: perhaps we can optimize this
        if (value) {
            return new DefaultNPrimitiveElement(NElementType.BOOLEAN, true);
        } else {
            return new DefaultNPrimitiveElement(NElementType.BOOLEAN, false);
        }
    }

    @Override
    public <T extends Enum<T>> NPrimitiveElement createEnum(Enum<T> value) {
        if (value == null) {
            return createNull();
        }
        if (value instanceof NEnum) {
            return createName(((NEnum) value).id());
        }
        return createName(value.name());
    }

    public NPrimitiveElement createString(String str) {
        return createString(str, null);
    }

    public NPrimitiveElement createString(String str, NElementType stringLayout) {
        if (str == null) {
            return createNull();
        }
        if (stringLayout == null) {
            stringLayout = NElementType.DOUBLE_QUOTED_STRING;
        }
        if (stringLayout.isAnyStringOrName()) {
            return DefaultNStringElement.ofValue(stringLayout, str);
        }
        throw new NUnsupportedEnumException(stringLayout);
    }

    public NPrimitiveElement createName(String str) {
        return str == null ? createNull() : DefaultNStringElement.ofValue(NElementType.NAME, str);
    }

    @Override
    public NPrimitiveElement createNameOrString(String value) {
        if (value == null) {
            return createNull();
        }
        return NElementUtils.isElementName(value) ? DefaultNStringElement.ofValue(NElementType.NAME, value)
                : DefaultNStringElement.ofValue(NElementType.DOUBLE_QUOTED_STRING, value)
                ;
    }

    @Override
    public NCustomElement createCustom(Object object) {
        NAssert.requireNamedNonNull(object, "custom element");
        return new DefaultNCustomElement(object);
    }

    @Override
    public NPrimitiveElement createTrue() {
        return createBoolean(true);
    }

    @Override
    public NPrimitiveElement createFalse() {
        return createBoolean(false);
    }

    @Override
    public NPrimitiveElement createInstant(Instant instant) {
        return instant == null ? createNull() : new DefaultNPrimitiveElement(NElementType.INSTANT, instant);
    }

    @Override
    public NPrimitiveElement createLocalDate(LocalDate localDate) {
        return localDate == null ? createNull() : new DefaultNPrimitiveElement(NElementType.LOCAL_DATE, localDate);
    }

    @Override
    public NPrimitiveElement createLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime == null ? createNull() : new DefaultNPrimitiveElement(NElementType.LOCAL_DATE, localDateTime);
    }

    @Override
    public NPrimitiveElement createLocalTime(LocalTime localTime) {
        return localTime == null ? createNull() : new DefaultNPrimitiveElement(NElementType.LOCAL_TIME, localTime);
    }

    @Override
    public NPrimitiveElement createFloat(Float value) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.FLOAT, value);
    }


    @Override
    public NPrimitiveElement createFloat(float value) {
        return new DefaultNNumberElement(NElementType.FLOAT, value);
    }

    @Override
    public NPrimitiveElement createFloat(Float value, String suffix) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.FLOAT, value, null, suffix);
    }

    @Override
    public NPrimitiveElement createFloat(float value, String suffix) {
        return new DefaultNNumberElement(NElementType.FLOAT, value, null, suffix);
    }

    @Override
    public NPrimitiveElement createInt(Integer value) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.INT, value);
    }

    @Override
    public NPrimitiveElement createInt(int value) {
        return new DefaultNNumberElement(NElementType.INT, value, null, null);
    }

    @Override
    public NPrimitiveElement createInt(Integer value, String suffix) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.INT, value, null, suffix);
    }

    @Override
    public NPrimitiveElement createInt(int value, String suffix) {
        return new DefaultNNumberElement(NElementType.INT, value, null, suffix);
    }

    @Override
    public NPrimitiveElement createInt(Integer value, NNumberLayout layout, String suffix) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.INT, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement createInt(int value, NNumberLayout layout, String suffix) {
        return new DefaultNNumberElement(NElementType.INT, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement createInt(Integer value, NNumberLayout layout) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.INT, value, layout, null);
    }

    @Override
    public NPrimitiveElement createInt(int value, NNumberLayout layout) {
        return new DefaultNNumberElement(NElementType.INT, value, layout, null);
    }

    @Override
    public NElement createBinaryStream(NInputStreamProvider value) {
        return value == null ? createNull() : new DefaultNBinaryStreamElement(value, null);
    }

    @Override
    public NElement createBinaryStream(NInputStreamProvider value, String blockIdentifier) {
        return value == null ? createNull() : new DefaultNBinaryStreamElement(value, blockIdentifier);
    }

    @Override
    public NBinaryStreamElementBuilder createBinaryStreamBuilder() {
        return new DefaultNBinaryStreamElementBuilder();
    }

    @Override
    public NElement createCharStream(NReaderProvider value, String blockIdentifier) {
        return value == null ? createNull() : new DefaultNCharStreamElement(NStringUtils.strip(blockIdentifier), value);
    }

    @Override
    public NCharStreamElementBuilder createCharStreamBuilder() {
        return new DefaultNCharStreamElementBuilder();
    }

    @Override
    public NElementAnnotation createAnnotation(String name, NElement... values) {
        return new NElementAnnotationImpl(name, values == null ? null : Arrays.asList(values), null);
    }

    @Override
    public NElementAnnotation createAnnotation(String name) {
        return new NElementAnnotationImpl(name, null, null);
    }

    @Override
    public NPrimitiveElement createLong(Long value) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.LONG, value);
    }

    @Override
    public NPrimitiveElement createLong(long value, NNumberLayout layout) {
        return new DefaultNNumberElement(NElementType.LONG, value, layout, null);
    }

    @Override
    public NPrimitiveElement createLong(Long value, NNumberLayout layout) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.LONG, value, layout, null);
    }

    @Override
    public NPrimitiveElement createLong(long value, NNumberLayout layout, String suffix) {
        return new DefaultNNumberElement(NElementType.LONG, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement createLong(Long value, NNumberLayout layout, String suffix) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.LONG, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement createLong(long value, String suffix) {
        return new DefaultNNumberElement(NElementType.LONG, value, null, suffix);
    }

    @Override
    public NPrimitiveElement createLong(Long value, String suffix) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.LONG, value, null, suffix);
    }

    @Override
    public NPrimitiveElement createLong(long value) {
        return new DefaultNNumberElement(NElementType.LONG, value, null, null);
    }

    @Override
    public NPrimitiveElement createNull() {
        return DefaultNPrimitiveElement.NULL;
    }

    @Override
    public NPrimitiveElement createNumber(String value) {
        if (NBlankable.isBlank(value)) {
            return createNull();
        }
        NElementTokenImpl next = new TsonCustomLexer(value).next();
        if (next != null) {
            Object v = next.value();
            if (v instanceof NPrimitiveElement) {
                if (((NPrimitiveElement) v).isNumber()) {
                    return (NPrimitiveElement) v;
                }
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("not a number %s", value));
//        TsonNumberHelper parse;
//        try {
//            parse = TsonNumberHelper.parse(value);
//        } catch (RuntimeException ex) {
//            throw ex;
//        }
//        return (NPrimitiveElement) parse.toTson();
//        if (value.indexOf('.') >= 0) {
//            try {
//                return ofNumber(Double.parseDouble(value));
//            } catch (Exception ex) {
//
//            }
//            try {
//                return ofNumber(new BigDecimal(value));
//            } catch (Exception ex) {
//
//            }
//        } else {
//            try {
//                return ofNumber(Integer.parseInt(value));
//            } catch (Exception ex) {
//
//            }
//            try {
//                return ofNumber(Long.parseLong(value));
//            } catch (Exception ex) {
//
//            }
//            try {
//                return ofNumber(new BigInteger(value));
//            } catch (Exception ex) {
//
//            }
//        }
//        throw new NParseException(NMsg.ofC("unable to parse number %s", value));
    }

    @Override
    public NPrimitiveElement createInstant(Date value) {
        if (value == null) {
            return createNull();
        }
        return new DefaultNPrimitiveElement(NElementType.INSTANT, value.toInstant());
    }

    @Override
    public NPrimitiveElement createInstant(String value) {
        if (value == null) {
            return createNull();
        }
        return new DefaultNPrimitiveElement(NElementType.INSTANT, DefaultNLiteral.parseInstant(value).get());
    }

    @Override
    public NPrimitiveElement createByte(Byte value) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.BYTE, value);
    }

    @Override
    public NPrimitiveElement createByte(Byte value, String suffix) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.BYTE, value, null, suffix);
    }

    @Override
    public NPrimitiveElement createByte(byte value) {
        return new DefaultNNumberElement(NElementType.BYTE, value);
    }

    @Override
    public NPrimitiveElement createByte(Byte value, NNumberLayout layout, String suffix) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.BYTE, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement createByte(byte value, NNumberLayout layout, String suffix) {
        return new DefaultNNumberElement(NElementType.BYTE, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement createByte(Byte value, NNumberLayout layout) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.BYTE, value, layout, null);
    }

    @Override
    public NPrimitiveElement createByte(byte value, NNumberLayout layout) {
        return new DefaultNNumberElement(NElementType.BYTE, value, layout, null);
    }

    @Override
    public NPrimitiveElement createByte(byte value, String suffix) {
        return new DefaultNNumberElement(NElementType.BYTE, value, null, suffix);
    }

    @Override
    public NPrimitiveElement createShort(Short value) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.SHORT, value);
    }


    @Override
    public NPrimitiveElement createShort(short value) {
        return new DefaultNNumberElement(NElementType.SHORT, value, null, null);
    }

    @Override
    public NPrimitiveElement createShort(Short value, NNumberLayout layout, String suffix) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.SHORT, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement createShort(short value, NNumberLayout layout, String suffix) {
        return new DefaultNNumberElement(NElementType.SHORT, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement createShort(Short value, NNumberLayout layout) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.SHORT, value, layout, null);
    }

    @Override
    public NPrimitiveElement createShort(short value, NNumberLayout layout) {
        return new DefaultNNumberElement(NElementType.SHORT, value, layout, null);
    }

    @Override
    public NPrimitiveElement createShort(Short value, String suffix) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.SHORT, value, null, suffix);
    }

    @Override
    public NPrimitiveElement createShort(short value, String suffix) {
        return new DefaultNNumberElement(NElementType.SHORT, value, null, suffix);
    }

    @Override
    public NPrimitiveElement createChar(Character value) {
        return value == null ? createNull() : new DefaultNStringElement(NElementType.CHAR, value);
    }

    @Override
    public NPrimitiveElement createDouble(Double value) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.DOUBLE, value);
    }

    @Override
    public NPrimitiveElement createDouble(double value) {
        return new DefaultNNumberElement(NElementType.DOUBLE, value);
    }

    @Override
    public NPrimitiveElement createDouble(Double value, String suffix) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.DOUBLE, value, NNumberLayout.DECIMAL, suffix);
    }

    @Override
    public NPrimitiveElement createDouble(double value, String suffix) {
        return new DefaultNNumberElement(NElementType.DOUBLE, value, NNumberLayout.DECIMAL, suffix);
    }


    @Override
    public NPrimitiveElement createBigDecimal(BigDecimal value) {
        if (value == null) {
            return createNull();
        }
        return new DefaultNNumberElement(NElementType.BIG_DECIMAL, value);
    }

    @Override
    public NPrimitiveElement createBigDecimal(BigDecimal value, String suffix) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.BIG_DECIMAL, value, NNumberLayout.DECIMAL, suffix);
    }

    @Override
    public NPrimitiveElement createBigInt(BigInteger value) {
        if (value == null) {
            return createNull();
        }
        return new DefaultNNumberElement(NElementType.BIG_INT, value);
    }

    @Override
    public NPrimitiveElement createBigInt(BigInteger value, NNumberLayout layout, String suffix) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.BIG_INT, value, layout, suffix);
    }

    @Override
    public NPrimitiveElement createBigInt(BigInteger value, NNumberLayout layout) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.BIG_INT, value, layout, null);
    }

    @Override
    public NPrimitiveElement createBigInt(BigInteger value, String suffix) {
        return value == null ? createNull() : new DefaultNNumberElement(NElementType.BIG_INT, value, null, suffix);
    }

    @Override
    public NTupleElementBuilder createTupleBuilder() {
        return new DefaultNTupleElementBuilder();
    }

    @Override
    public NTupleElementBuilder createTupleBuilder(String name) {
        return createTupleBuilder().name(name);
    }

    @Override
    public NTupleElement createTuple() {
        return createTupleBuilder().build();
    }

    @Override
    public NTupleElement createTuple(String name, NElement... items) {
        return createTupleBuilder().name(name).addAll(items).build();
    }

    @Override
    public NTupleElement createNamedTuple(String name, NElement... items) {
        return createTupleBuilder().name(name).addAll(items).build();
    }

    @Override
    public NTupleElement createTuple(NElement... items) {
        return createTupleBuilder().addAll(items).build();
    }

    @Override
    public NPrimitiveElement createDoubleComplex(double real) {
        return createDoubleComplex(real, 0);
    }

    @Override
    public NPrimitiveElement createDoubleComplex(double real, double imag) {
        return new DefaultNNumberElement(NElementType.DOUBLE_COMPLEX, NDoubleComplex.of(real, imag));
    }

    @Override
    public NPrimitiveElement createDoubleComplex(double real, double imag, String suffix) {
        return new DefaultNNumberElement(NElementType.DOUBLE_COMPLEX, NDoubleComplex.of(real, imag), NNumberLayout.DECIMAL, suffix);
    }

    @Override
    public NPrimitiveElement createFloatComplex(float real) {
        return createFloatComplex(real, 0);
    }

    @Override
    public NPrimitiveElement createFloatComplex(float real, float imag) {
        return new DefaultNNumberElement(NElementType.FLOAT_COMPLEX, NFloatComplex.of(real, imag));
    }

    @Override
    public NPrimitiveElement createFloatComplex(float real, float imag, String suffix) {
        return new DefaultNNumberElement(NElementType.FLOAT_COMPLEX, NFloatComplex.of(real, imag), NNumberLayout.DECIMAL, suffix);
    }

    @Override
    public NPrimitiveElement createBigComplex(BigDecimal real) {
        return createBigComplex(real, BigDecimal.ZERO);
    }

    @Override
    public NPrimitiveElement createBigComplex(BigDecimal real, BigDecimal imag) {
        if (real == null && imag == null) {
            return createNull();
        }
        return new DefaultNNumberElement(NElementType.BIG_COMPLEX, NBigComplex.of(real, imag));
    }

    @Override
    public NPrimitiveElement createBigComplex(BigDecimal real, BigDecimal imag, String suffix) {
        if (real == null && imag == null) {
            return createNull();
        }
        return new DefaultNNumberElement(NElementType.BIG_COMPLEX, NBigComplex.of(real, imag), NNumberLayout.DECIMAL, suffix);
    }

    @Override
    public NPrimitiveElement createNumber(Number value) {
        return createNumber(value, null, null);
    }

    @Override
    public NPrimitiveElement createNumber(Number value, NNumberLayout layout, String suffix) {
        if (value == null) {
            return createNull();
        }
        switch (value.getClass().getName()) {
            case "java.lang.Byte":
                return new DefaultNNumberElement(NElementType.BYTE, value, layout, suffix);
            case "java.lang.Short":
                return new DefaultNNumberElement(NElementType.SHORT, value, layout, suffix);
            case "java.lang.Integer":
                return new DefaultNNumberElement(NElementType.INT, value, layout, suffix);
            case "java.lang.Long":
                return new DefaultNNumberElement(NElementType.LONG, value, layout, suffix);
            case "java.math.BigInteger":
                return new DefaultNNumberElement(NElementType.BIG_INT, value, layout, suffix);
            case "java.lang.float":
                return new DefaultNNumberElement(NElementType.FLOAT, value, layout, suffix);
            case "java.lang.Double":
                return new DefaultNNumberElement(NElementType.DOUBLE, value, layout, suffix);
            case "java.math.BigDecimal":
                return new DefaultNNumberElement(NElementType.BIG_DECIMAL, value, layout, suffix);
            case "net.thevpc.nuts.math.NDoubleComplex":
                return new DefaultNNumberElement(NElementType.DOUBLE_COMPLEX, value, layout, suffix);
            case "net.thevpc.nuts.math.NFloatComplex":
                return new DefaultNNumberElement(NElementType.FLOAT_COMPLEX, value, layout, suffix);
            case "net.thevpc.nuts.math.NBigComplex":
                return new DefaultNNumberElement(NElementType.BIG_COMPLEX, value, layout, suffix);
        }
        //this is for when someone implements a custom NDoubleComplex, etc
        if (value instanceof NDoubleComplex) {
            return new DefaultNNumberElement(NElementType.DOUBLE_COMPLEX, value, layout, suffix);
        }
        if (value instanceof NBigComplex) {
            return new DefaultNNumberElement(NElementType.BIG_COMPLEX, value, layout, suffix);
        }
        if (value instanceof NFloatComplex) {
            return new DefaultNNumberElement(NElementType.FLOAT_COMPLEX, value, layout, suffix);
        }
        // ???
        return new DefaultNNumberElement(NElementType.DOUBLE, value, layout, suffix);
    }

    public NElementComment createBlocComment(String lines) {
        return NElementCommentImpl.ofBloc(lines);
    }

    public NElementComment createLineComment(String lines) {
        return NElementCommentImpl.ofLine(lines);
    }

    public NElementComment createBlocComment(NElementLine... lines) {
        return NElementCommentImpl.ofBloc(lines);
    }

    public NElementComment createLineComment(NElementLine... lines) {
        return NElementCommentImpl.ofLine(lines);
    }

    @Override
    public NPrimitiveElementBuilder createPrimitiveBuilder() {
        return new DefaultNPrimitiveElementBuilder();
    }

    @Override
    public NFlatExprElementBuilder createFlatExprBuilder() {
        return new DefaultNFlatExprElementBuilder();
    }

    @Override
    public NFragmentElementBuilder createFragmentBuilder() {
        return new DefaultNFragmentElementBuilder();
    }

    @Override
    public NFragmentElement createFragment(NElement... elements) {
        return createFragmentBuilder().addAll(elements).build();
    }

    @Override
    public NElementLine createElementLine(String prefix, String startMarker, String startPadding, String content, String endPadding, String endMarker, NNewLineMode newline) {
        return new NElementLineImpl(prefix, startMarker, startPadding, content, endPadding, endMarker, newline);
    }

}
