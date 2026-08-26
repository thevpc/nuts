/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.io.NReaderProvider;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NNewLineMode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;

/**
 * Class responsible for manipulating {@link NElement} type. It help parsing
 * from, converting to and formatting such types.
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
public interface NElementRPI extends NComponent {


    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NElementRPI of() {
        return NExtensions.of(NElementRPI.class);
    }

    /**
     * Returns the shared elements.
     *
     * @return get shared elements result
     */
    NElements getSharedElements();


    /**
     * compile pathExpression into a valid NutsElementPath that helps filtering
     * elements tree. JSONPath expressions refer to a JSON structure the same
     * way as XPath expression are used with XML documents. JSONPath expressions
     * can use the dot notation and/or bracket notations .store.book[0].title
     * The trailing root is not necessary : .store.book[0].title You can also
     * use bracket notation store['book'][0].title for input paths.
     *
     * @param pathExpression element path expression
     * @return Element Path filter
     */
    NElementSelector compileSelector(String pathExpression);

    /**
     * Common number type.
     *
     * @param aa aa
     * @param bb bb
     * @return common number type result
     */
    NElementType commonNumberType(NElementType aa, NElementType bb);

    /**
     * Creates a new instance of create expr element reshaper.
     *
     * @param type type
     * @return create expr element reshaper result
     */
    NExprElementReshaper createExprElementReshaper(NExprElementReshaperType type);

    /**
     * Creates a new instance of create expr element reshaper builder.
     *
     * @param type type
     * @return create expr element reshaper builder result
     */
    NExprElementReshaperBuilder createExprElementReshaperBuilder(NExprElementReshaperType type);

    /**
     * Creates a new instance of create element formatter builder.
     *
     * @return create element formatter builder result
     */
    NElementFormatterBuilder createElementFormatterBuilder();

    /**
     * Creates a new instance of create element formatter.
     *
     * @param style style
     * @return create element formatter result
     */
    NElementFormatter createElementFormatter(NElementFormatterStyle style);

    /**
     * Creates a new instance of create root path.
     *
     * @return create root path result
     */
    NElementPath createRootPath();

    /**
     * Creates a new instance of create element metadata.
     *
     * @return create element metadata result
     */
    NElementMetadata createElementMetadata();

    /**
     * Creates a new instance of create element metadata.
     *
     * @param key key
     * @param value value
     * @return create element metadata result
     */
    NElementMetadata createElementMetadata(Object key, Object value);

    /**
     * Creates a new instance of create element metadata.
     *
     * @param any any
     * @return create element metadata result
     */
    NElementMetadata createElementMetadata(Map<Object, Object> any);

    /**
     * Creates a new instance of create step child.
     *
     * @param name name
     * @return create step child result
     */
    NElementStep createStepChild(String name);

    /**
     * Creates a new instance of create step child.
     *
     * @param index index
     * @return create step child result
     */
    NElementStep createStepChild(int index);

    /**
     * Creates a new instance of create step param.
     *
     * @param name name
     * @return create step param result
     */
    NElementStep createStepParam(String name);

    /**
     * Creates a new instance of create step param.
     *
     * @param index index
     * @return create step param result
     */
    NElementStep createStepParam(int index);

    /**
     * Creates a new instance of create step annotation param.
     *
     * @param paramIndex param index
     * @param name name
     * @return create step annotation param result
     */
    NElementStep createStepAnnotationParam(int paramIndex, String name);

    /**
     * Creates a new instance of create step annotation param.
     *
     * @param paramIndex param index
     * @param index index
     * @return create step annotation param result
     */
    NElementStep createStepAnnotationParam(int paramIndex, int index);

    /**
     * Creates a new instance of create step sub list.
     *
     * @param index index
     * @return create step sub list result
     */
    NElementStep createStepSubList(int index);

    /**
     * Creates a new instance of create root navigator.
     *
     * @param element element
     * @return create root navigator result
     */
    NElementNavigator createRootNavigator(NElement element);


    ////

    /**
     * Creates a new instance of create pair.
     *
     * @param key key
     * @param value value
     * @return create pair result
     */
    NPairElement createPair(NElement key, NElement value);

    /**
     * Creates a new instance of create pair.
     *
     * @param key key
     * @param value value
     * @return create pair result
     */
    NPairElement createPair(String key, NElement value);

    /**
     * Creates a new instance of create pair.
     *
     * @param key key
     * @param value value
     * @return create pair result
     */
    NPairElement createPair(String key, Boolean value);

    /**
     * Creates a new instance of create pair.
     *
     * @param key key
     * @param value value
     * @return create pair result
     */
    NPairElement createPair(String key, Number value);

    /**
     * Creates a new instance of create pair.
     *
     * @param key key
     * @param value value
     * @return create pair result
     */
    NPairElement createPair(String key, Byte value);

    /**
     * Creates a new instance of create pair.
     *
     * @param key key
     * @param value value
     * @return create pair result
     */
    NPairElement createPair(String key, Short value);

    /**
     * Creates a new instance of create pair.
     *
     * @param key key
     * @param value value
     * @return create pair result
     */
    NPairElement createPair(String key, Integer value);

    /**
     * Creates a new instance of create pair.
     *
     * @param key key
     * @param value value
     * @return create pair result
     */
    NPairElement createPair(String key, Long value);

    /**
     * Creates a new instance of create pair.
     *
     * @param key key
     * @param value value
     * @return create pair result
     */
    NPairElement createPair(String key, String value);

    /**
     * Creates a new instance of create pair.
     *
     * @param key key
     * @param value value
     * @return create pair result
     */
    NPairElement createPair(String key, Double value);

    /**
     * Creates a new instance of create pair.
     *
     * @param key key
     * @param value value
     * @return create pair result
     */
    NPairElement createPair(String key, Instant value);

    /**
     * Creates a new instance of create pair.
     *
     * @param key key
     * @param value value
     * @return create pair result
     */
    NPairElement createPair(String key, LocalDate value);

    /**
     * Creates a new instance of create pair.
     *
     * @param key key
     * @param value value
     * @return create pair result
     */
    NPairElement createPair(String key, LocalDateTime value);

    /**
     * Creates a new instance of create pair.
     *
     * @param key key
     * @param value value
     * @return create pair result
     */
    NPairElement createPair(String key, LocalTime value);

    /**
     * Creates a new instance of create pair builder.
     *
     * @param key key
     * @param value value
     * @return create pair builder result
     */
    NPairElementBuilder createPairBuilder(NElement key, NElement value);

    /**
     * Creates a new instance of create op builder.
     *
     * @return create op builder result
     */
    NOperatorElementBuilder createOpBuilder();

    /**
     * Creates a new instance of create binary infix operator.
     *
     * @param op op
     * @param first first
     * @param second second
     * @return create binary infix operator result
     */
    NOperatorElement createBinaryInfixOperator(NOperatorSymbol op, NElement first, NElement second);

    /**
     * Creates a new instance of create unary prefix operator.
     *
     * @param op op
     * @param first first
     * @return create unary prefix operator result
     */
    NOperatorElement createUnaryPrefixOperator(NOperatorSymbol op, NElement first);

    /**
     * Creates a new instance of create op.
     *
     * @param op op
     * @return create op result
     */
    NOperatorSymbolElement createOp(NOperatorSymbol op);

    /**
     * Creates a new instance of create pair builder.
     *
     * @return create pair builder result
     */
    NPairElementBuilder createPairBuilder();

    /**
     * create object element builder (mutable)
     *
     * @return object element
     */
    NObjectElementBuilder createObjectBuilder();

    /**
     * Creates a new instance of create object builder.
     *
     * @param name name
     * @return create object builder result
     */
    NObjectElementBuilder createObjectBuilder(String name);

    /**
     * create array element builder (mutable)
     *
     * @return array element
     */
    NArrayElementBuilder createArrayBuilder();

    /**
     * Creates a new instance of create fragment builder.
     *
     * @return create fragment builder result
     */
    NFragmentElementBuilder createFragmentBuilder();

    /**
     * Creates a new instance of create array builder.
     *
     * @param name name
     * @return create array builder result
     */
    NArrayElementBuilder createArrayBuilder(String name);

    /**
     * Creates a new instance of create array.
     *
     * @return create array result
     */
    NArrayElement createArray();

    /**
     * Creates a new instance of create object.
     *
     * @return create object result
     */
    NObjectElement createObject();

    //    public NutsPrimitiveElement forNutsString(NutsString str) {
    //        return str == null ? DefaultNPrimitiveElementBuilder.NULL : new DefaultNPrimitiveElement(NutsElementType.NUTS_STRING, str);
    //    }
    /**
     * Creates a new instance of create boolean.
     *
     * @param value value
     * @return create boolean result
     */
    NPrimitiveElement createBoolean(String value);

    /**
     * Creates a new instance of create boolean.
     *
     * @param value value
     * @return create boolean result
     */
    NPrimitiveElement createBoolean(boolean value);

    /**
     * Creates a new instance of create name.
     *
     * @param value value
     * @return create name result
     */
    NPrimitiveElement createName(String value);

    /**
     * Creates a new instance of create name or string.
     *
     * @param value value
     * @return create name or string result
     */
    NPrimitiveElement createNameOrString(String value);

    /**
     * Creates a new instance of create enum.
     *
     * @param value value
     * @return create enum result
     */
    <T extends Enum<T>> NPrimitiveElement createEnum(Enum<T> value);

    /**
     * Creates a new instance of create string.
     *
     * @param str str
     * @return create string result
     */
    NPrimitiveElement createString(String str);

    /**
     * Creates a new instance of create string.
     *
     * @param str str
     * @param stringLayout string layout
     * @return create string result
     */
    NPrimitiveElement createString(String str, NElementType stringLayout);

    /**
     * Creates a new instance of create custom.
     *
     * @param object object
     * @return create custom result
     */
    NCustomElement createCustom(Object object);

    /**
     * Creates a new instance of create true.
     *
     * @return create true result
     */
    NPrimitiveElement createTrue();

    /**
     * Creates a new instance of create false.
     *
     * @return create false result
     */
    NPrimitiveElement createFalse();

    /**
     * Creates a new instance of create instant.
     *
     * @param instant instant
     * @return create instant result
     */
    NPrimitiveElement createInstant(Instant instant);

    /**
     * Creates a new instance of create local date.
     *
     * @param localDate local date
     * @return create local date result
     */
    NPrimitiveElement createLocalDate(LocalDate localDate);

    /**
     * Creates a new instance of create local date time.
     *
     * @param localDateTime local date time
     * @return create local date time result
     */
    NPrimitiveElement createLocalDateTime(LocalDateTime localDateTime);

    /**
     * Creates a new instance of create local time.
     *
     * @param localTime local time
     * @return create local time result
     */
    NPrimitiveElement createLocalTime(LocalTime localTime);

    /**
     * Creates a new instance of create float.
     *
     * @param value value
     * @return create float result
     */
    NPrimitiveElement createFloat(Float value);

    /**
     * Creates a new instance of create float.
     *
     * @param value value
     * @return create float result
     */
    NPrimitiveElement createFloat(float value);

    /**
     * Creates a new instance of create float.
     *
     * @param value value
     * @param suffix suffix
     * @return create float result
     */
    NPrimitiveElement createFloat(Float value, String suffix);

    /**
     * Creates a new instance of create float.
     *
     * @param value value
     * @param suffix suffix
     * @return create float result
     */
    NPrimitiveElement createFloat(float value, String suffix);

    /**
     * Creates a new instance of create byte.
     *
     * @param value value
     * @return create byte result
     */
    NPrimitiveElement createByte(Byte value);

    /**
     * Creates a new instance of create byte.
     *
     * @param value value
     * @return create byte result
     */
    NPrimitiveElement createByte(byte value);

    /**
     * Creates a new instance of create byte.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return create byte result
     */
    NPrimitiveElement createByte(Byte value, NNumberLayout layout, String suffix);

    /**
     * Creates a new instance of create byte.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return create byte result
     */
    NPrimitiveElement createByte(byte value, NNumberLayout layout, String suffix);

    /**
     * Creates a new instance of create byte.
     *
     * @param value value
     * @param layout layout
     * @return create byte result
     */
    NPrimitiveElement createByte(Byte value, NNumberLayout layout);

    /**
     * Creates a new instance of create byte.
     *
     * @param value value
     * @param layout layout
     * @return create byte result
     */
    NPrimitiveElement createByte(byte value, NNumberLayout layout);

    /**
     * Creates a new instance of create byte.
     *
     * @param value value
     * @param suffix suffix
     * @return create byte result
     */
    NPrimitiveElement createByte(Byte value, String suffix);

    /**
     * Creates a new instance of create byte.
     *
     * @param value value
     * @param suffix suffix
     * @return create byte result
     */
    NPrimitiveElement createByte(byte value, String suffix);

    /**
     * Creates a new instance of create short.
     *
     * @param value value
     * @return create short result
     */
    NPrimitiveElement createShort(Short value);

    /**
     * Creates a new instance of create short.
     *
     * @param value value
     * @return create short result
     */
    NPrimitiveElement createShort(short value);

    /**
     * Creates a new instance of create short.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return create short result
     */
    NPrimitiveElement createShort(Short value, NNumberLayout layout, String suffix);

    /**
     * Creates a new instance of create short.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return create short result
     */
    NPrimitiveElement createShort(short value, NNumberLayout layout, String suffix);

    /**
     * Creates a new instance of create short.
     *
     * @param value value
     * @param layout layout
     * @return create short result
     */
    NPrimitiveElement createShort(Short value, NNumberLayout layout);

    /**
     * Creates a new instance of create short.
     *
     * @param value value
     * @param layout layout
     * @return create short result
     */
    NPrimitiveElement createShort(short value, NNumberLayout layout);

    /**
     * Creates a new instance of create short.
     *
     * @param value value
     * @param suffix suffix
     * @return create short result
     */
    NPrimitiveElement createShort(Short value, String suffix);

    /**
     * Creates a new instance of create short.
     *
     * @param value value
     * @param suffix suffix
     * @return create short result
     */
    NPrimitiveElement createShort(short value, String suffix);


    /**
     * Creates a new instance of create int.
     *
     * @param value value
     * @return create int result
     */
    NPrimitiveElement createInt(Integer value);

    /**
     * Creates a new instance of create int.
     *
     * @param value value
     * @return create int result
     */
    NPrimitiveElement createInt(int value);

    /**
     * Creates a new instance of create int.
     *
     * @param value value
     * @param suffix suffix
     * @return create int result
     */
    NPrimitiveElement createInt(Integer value, String suffix);

    /**
     * Creates a new instance of create int.
     *
     * @param value value
     * @param suffix suffix
     * @return create int result
     */
    NPrimitiveElement createInt(int value, String suffix);

    /**
     * Creates a new instance of create int.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return create int result
     */
    NPrimitiveElement createInt(Integer value, NNumberLayout layout, String suffix);

    /**
     * Creates a new instance of create int.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return create int result
     */
    NPrimitiveElement createInt(int value, NNumberLayout layout, String suffix);

    /**
     * Creates a new instance of create int.
     *
     * @param value value
     * @param layout layout
     * @return create int result
     */
    NPrimitiveElement createInt(Integer value, NNumberLayout layout);

    /**
     * Creates a new instance of create int.
     *
     * @param value value
     * @param layout layout
     * @return create int result
     */
    NPrimitiveElement createInt(int value, NNumberLayout layout);

    /**
     * Creates a new instance of create long.
     *
     * @param value value
     * @return create long result
     */
    NPrimitiveElement createLong(Long value);

    /**
     * Creates a new instance of create long.
     *
     * @param value value
     * @return create long result
     */
    NPrimitiveElement createLong(long value);

    /**
     * Creates a new instance of create long.
     *
     * @param value value
     * @param suffix suffix
     * @return create long result
     */
    NPrimitiveElement createLong(Long value, String suffix);

    /**
     * Creates a new instance of create long.
     *
     * @param value value
     * @param suffix suffix
     * @return create long result
     */
    NPrimitiveElement createLong(long value, String suffix);

    /**
     * Creates a new instance of create long.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return create long result
     */
    NPrimitiveElement createLong(Long value, NNumberLayout layout, String suffix);

    /**
     * Creates a new instance of create long.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return create long result
     */
    NPrimitiveElement createLong(long value, NNumberLayout layout, String suffix);

    /**
     * Creates a new instance of create long.
     *
     * @param value value
     * @param layout layout
     * @return create long result
     */
    NPrimitiveElement createLong(Long value, NNumberLayout layout);

    /**
     * Creates a new instance of create long.
     *
     * @param value value
     * @param layout layout
     * @return create long result
     */
    NPrimitiveElement createLong(long value, NNumberLayout layout);


    /**
     * Creates a new instance of create null.
     *
     * @return create null result
     */
    NPrimitiveElement createNull();

    /**
     * Creates a new instance of create number.
     *
     * @param value value
     * @return create number result
     */
    NPrimitiveElement createNumber(String value);

    /**
     * Creates a new instance of create instant.
     *
     * @param value value
     * @return create instant result
     */
    NPrimitiveElement createInstant(Date value);

    /**
     * Creates a new instance of create instant.
     *
     * @param value value
     * @return create instant result
     */
    NPrimitiveElement createInstant(String value);

    /**
     * Creates a new instance of create char.
     *
     * @param value value
     * @return create char result
     */
    NPrimitiveElement createChar(Character value);

    /**
     * Creates a new instance of create double.
     *
     * @param value value
     * @return create double result
     */
    NPrimitiveElement createDouble(Double value);

    /**
     * Creates a new instance of create double.
     *
     * @param value value
     * @return create double result
     */
    NPrimitiveElement createDouble(double value);

    /**
     * Creates a new instance of create double.
     *
     * @param value value
     * @param suffix suffix
     * @return create double result
     */
    NPrimitiveElement createDouble(Double value, String suffix);

    /**
     * Creates a new instance of create double.
     *
     * @param value value
     * @param suffix suffix
     * @return create double result
     */
    NPrimitiveElement createDouble(double value, String suffix);

    /**
     * Creates a new instance of create double complex.
     *
     * @param real real
     * @return create double complex result
     */
    NPrimitiveElement createDoubleComplex(double real);

    /**
     * Creates a new instance of create double complex.
     *
     * @param real real
     * @param imag imag
     * @return create double complex result
     */
    NPrimitiveElement createDoubleComplex(double real, double imag);

    /**
     * Creates a new instance of create double complex.
     *
     * @param real real
     * @param imag imag
     * @param suffix suffix
     * @return create double complex result
     */
    NPrimitiveElement createDoubleComplex(double real, double imag, String suffix);

    /**
     * Creates a new instance of create float complex.
     *
     * @param real real
     * @return create float complex result
     */
    NPrimitiveElement createFloatComplex(float real);

    /**
     * Creates a new instance of create float complex.
     *
     * @param real real
     * @param imag imag
     * @return create float complex result
     */
    NPrimitiveElement createFloatComplex(float real, float imag);

    /**
     * Creates a new instance of create float complex.
     *
     * @param real real
     * @param imag imag
     * @param suffix suffix
     * @return create float complex result
     */
    NPrimitiveElement createFloatComplex(float real, float imag, String suffix);

    /**
     * Creates a new instance of create big complex.
     *
     * @param real real
     * @return create big complex result
     */
    NPrimitiveElement createBigComplex(BigDecimal real);

    /**
     * Creates a new instance of create big complex.
     *
     * @param real real
     * @param imag imag
     * @return create big complex result
     */
    NPrimitiveElement createBigComplex(BigDecimal real, BigDecimal imag);

    /**
     * Creates a new instance of create big complex.
     *
     * @param real real
     * @param imag imag
     * @param suffix suffix
     * @return create big complex result
     */
    NPrimitiveElement createBigComplex(BigDecimal real, BigDecimal imag, String suffix);


    /**
     * Creates a new instance of create number.
     *
     * @param value value
     * @return create number result
     */
    NPrimitiveElement createNumber(Number value);

    /**
     * Creates a new instance of create big decimal.
     *
     * @param value value
     * @return create big decimal result
     */
    NPrimitiveElement createBigDecimal(BigDecimal value);

    /**
     * Creates a new instance of create number.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return create number result
     */
    NPrimitiveElement createNumber(Number value, NNumberLayout layout, String suffix);

    /**
     * Creates a new instance of create big decimal.
     *
     * @param value value
     * @param suffix suffix
     * @return create big decimal result
     */
    NPrimitiveElement createBigDecimal(BigDecimal value, String suffix);

    /**
     * Creates a new instance of create big int.
     *
     * @param value value
     * @return create big int result
     */
    NPrimitiveElement createBigInt(BigInteger value);

    /**
     * Creates a new instance of create big int.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return create big int result
     */
    NPrimitiveElement createBigInt(BigInteger value, NNumberLayout layout, String suffix);

    /**
     * Creates a new instance of create big int.
     *
     * @param value value
     * @param layout layout
     * @return create big int result
     */
    NPrimitiveElement createBigInt(BigInteger value, NNumberLayout layout);

    /**
     * Creates a new instance of create big int.
     *
     * @param value value
     * @param suffix suffix
     * @return create big int result
     */
    NPrimitiveElement createBigInt(BigInteger value, String suffix);

    /**
     * Creates a new instance of create tuple builder.
     *
     * @return create tuple builder result
     */
    NTupleElementBuilder createTupleBuilder();

    /**
     * Creates a new instance of create tuple builder.
     *
     * @param name name
     * @return create tuple builder result
     */
    NTupleElementBuilder createTupleBuilder(String name);

    /**
     * Creates a new instance of create tuple.
     *
     * @return create tuple result
     */
    NTupleElement createTuple();

    /**
     * Creates a new instance of create tuple.
     *
     * @param items items
     * @return create tuple result
     */
    NTupleElement createTuple(NElement... items);

    /**
     * Creates a new instance of create tuple.
     *
     * @param name name
     * @param items items
     * @return create tuple result
     */
    NTupleElement createTuple(String name, NElement... items);

    /**
     * Creates a new instance of create named tuple.
     *
     * @param name name
     * @param items items
     * @return create named tuple result
     */
    NTupleElement createNamedTuple(String name, NElement... items);

    /**
     * Creates a new instance of create int array.
     *
     * @param items items
     * @return create int array result
     */
    NArrayElement createIntArray(int... items);

    /**
     * Creates a new instance of create int array.
     *
     * @param items items
     * @return create int array result
     */
    NArrayElement createIntArray(Integer... items);

    /**
     * Creates a new instance of create long array.
     *
     * @param items items
     * @return create long array result
     */
    NArrayElement createLongArray(long... items);

    /**
     * Creates a new instance of create long array.
     *
     * @param items items
     * @return create long array result
     */
    NArrayElement createLongArray(Long... items);

    /**
     * Creates a new instance of create number array.
     *
     * @param items items
     * @return create number array result
     */
    NArrayElement createNumberArray(Number... items);

    /**
     * Creates a new instance of create boolean array.
     *
     * @param items items
     * @return create boolean array result
     */
    NArrayElement createBooleanArray(boolean... items);

    /**
     * Creates a new instance of create boolean array.
     *
     * @param items items
     * @return create boolean array result
     */
    NArrayElement createBooleanArray(Boolean... items);

    /**
     * Creates a new instance of create array.
     *
     * @param items items
     * @return create array result
     */
    NArrayElement createArray(NElement... items);

    /**
     * Creates a new instance of create array.
     *
     * @param name name
     * @param items items
     * @return create array result
     */
    NArrayElement createArray(String name, NElement... items);

    /**
     * Creates a new instance of create named array.
     *
     * @param name name
     * @param items items
     * @return create named array result
     */
    NArrayElement createNamedArray(String name, NElement... items);

    /**
     * Creates a new instance of create full array.
     *
     * @param name name
     * @param params params
     * @param items items
     * @return create full array result
     */
    NArrayElement createFullArray(String name, NElement[] params, NElement... items);

    /**
     * Creates a new instance of create array.
     *
     * @param name name
     * @param params params
     * @param items items
     * @return create array result
     */
    NArrayElement createArray(String name, NElement[] params, NElement... items);

    /**
     * Creates a new instance of create param array.
     *
     * @param params params
     * @param items items
     * @return create param array result
     */
    NArrayElement createParamArray(NElement[] params, NElement... items);

    /**
     * Creates a new instance of create param array.
     *
     * @param params params
     * @return create param array result
     */
    NArrayElement createParamArray(NElement... params);

    /**
     * Creates a new instance of create param array.
     *
     * @param name name
     * @param params params
     * @param items items
     * @return create param array result
     */
    NArrayElement createParamArray(String name, NElement[] params, NElement... items);

    /**
     * Creates a new instance of create param array.
     *
     * @param name name
     * @param params params
     * @return create param array result
     */
    NArrayElement createParamArray(String name, NElement... params);

    /**
     * Creates a new instance of create string array.
     *
     * @param items items
     * @return create string array result
     */
    NArrayElement createStringArray(String... items);

    /**
     * Creates a new instance of create double array.
     *
     * @param items items
     * @return create double array result
     */
    NArrayElement createDoubleArray(double... items);

    /**
     * Creates a new instance of create double array.
     *
     * @param items items
     * @return create double array result
     */
    NArrayElement createDoubleArray(Double... items);

    /**
     * Creates a new instance of create float array.
     *
     * @param items items
     * @return create float array result
     */
    NArrayElement createFloatArray(float... items);

    /**
     * Creates a new instance of create float array.
     *
     * @param items items
     * @return create float array result
     */
    NArrayElement createFloatArray(Float... items);

    /**
     * Creates a new instance of create byte array.
     *
     * @param items items
     * @return create byte array result
     */
    NArrayElement createByteArray(byte... items);

    /**
     * Creates a new instance of create char array.
     *
     * @param items items
     * @return create char array result
     */
    NArrayElement createCharArray(char... items);
    /**
     * Creates a new instance of create char array.
     *
     * @param items items
     * @return create char array result
     */
    NArrayElement createCharArray(Character... items);

    /**
     * Creates a new instance of create byte array.
     *
     * @param items items
     * @return create byte array result
     */
    NArrayElement createByteArray(Byte... items);

    /**
     * Creates a new instance of create short array.
     *
     * @param items items
     * @return create short array result
     */
    NArrayElement createShortArray(short... items);

    /**
     * Creates a new instance of create short array.
     *
     * @param items items
     * @return create short array result
     */
    NArrayElement createShortArray(Short... items);

    /**
     * Creates a new instance of create object.
     *
     * @param items items
     * @return create object result
     */
    NObjectElement createObject(NElement... items);

    /**
     * Creates a new instance of create object.
     *
     * @param name name
     * @param items items
     * @return create object result
     */
    NObjectElement createObject(String name, NElement... items);

    /**
     * Creates a new instance of create named object.
     *
     * @param name name
     * @param items items
     * @return create named object result
     */
    NObjectElement createNamedObject(String name, NElement... items);

    /**
     * Creates a new instance of create full object.
     *
     * @param name name
     * @param params params
     * @param items items
     * @return create full object result
     */
    NObjectElement createFullObject(String name, NElement[] params, NElement... items);

    /**
     * Creates a new instance of create param object.
     *
     * @param params params
     * @param items items
     * @return create param object result
     */
    NObjectElement createParamObject(NElement[] params, NElement... items);

    /**
     * Creates a new instance of create param object.
     *
     * @param params params
     * @return create param object result
     */
    NObjectElement createParamObject(NElement... params);

    /**
     * Creates a new instance of create param object.
     *
     * @param name name
     * @param params params
     * @param items items
     * @return create param object result
     */
    NObjectElement createParamObject(String name, NElement[] params, NElement... items);

    /**
     * Creates a new instance of create object.
     *
     * @param name name
     * @param params params
     * @param items items
     * @return create object result
     */
    NObjectElement createObject(String name, NElement[] params, NElement... items);

    /**
     * Creates a new instance of create param object.
     *
     * @param name name
     * @param params params
     * @return create param object result
     */
    NObjectElement createParamObject(String name, NElement... params);

    /**
     * Creates a new instance of create bloc comment.
     *
     * @param text text
     * @return create bloc comment result
     */
    NElementComment createBlocComment(String text);

    /**
     * Creates a new instance of create line comment.
     *
     * @param text text
     * @return create line comment result
     */
    NElementComment createLineComment(String text);

    /**
     * Creates a new instance of create bloc comment.
     *
     * @param text text
     * @return create bloc comment result
     */
    NElementComment createBlocComment(NElementLine... text);

    /**
     * Creates a new instance of create line comment.
     *
     * @param text text
     * @return create line comment result
     */
    NElementComment createLineComment(NElementLine... text);

    /**
     * Creates a new instance of create binary stream.
     *
     * @param value value
     * @return create binary stream result
     */
    NElement createBinaryStream(NInputStreamProvider value);

    /**
     * Creates a new instance of create binary stream.
     *
     * @param value value
     * @param blockIdentifier block identifier
     * @return create binary stream result
     */
    NElement createBinaryStream(NInputStreamProvider value, String blockIdentifier);

    /**
     * Creates a new instance of create char stream.
     *
     * @param value value
     * @param blockIdentifier block identifier
     * @return create char stream result
     */
    NElement createCharStream(NReaderProvider value, String blockIdentifier);

    /**
     * Creates a new instance of create binary stream builder.
     *
     * @return create binary stream builder result
     */
    NBinaryStreamElementBuilder createBinaryStreamBuilder();

    /**
     * Creates a new instance of create char stream builder.
     *
     * @return create char stream builder result
     */
    NCharStreamElementBuilder createCharStreamBuilder();

    /**
     * Creates a new instance of create annotation.
     *
     * @param name name
     * @param values values
     * @return create annotation result
     */
    NElementAnnotation createAnnotation(String name, NElement... values);

    /**
     * Creates a new instance of create annotation.
     *
     * @param name name
     * @return create annotation result
     */
    NElementAnnotation createAnnotation(String name);

    /**
     * Creates a new instance of create primitive builder.
     *
     * @return create primitive builder result
     */
    NPrimitiveElementBuilder createPrimitiveBuilder();

    /**
     * Creates a new instance of create flat expr builder.
     *
     * @return create flat expr builder result
     */
    NFlatExprElementBuilder createFlatExprBuilder();

    /**
     * Creates a new instance of create error builder.
     *
     * @return create error builder result
     */
    NEmptyElementBuilder createErrorBuilder();

    /**
     * Creates a new instance of create diagnostic builder.
     *
     * @return create diagnostic builder result
     */
    NElementDiagnosticBuilder createDiagnosticBuilder();

    /**
     * Creates a new instance of create diagnostic.
     *
     * @param msg msg
     * @return create diagnostic result
     */
    NElementDiagnostic createDiagnostic(NMsg msg);

    /**
     * Creates a new instance of create separator.
     *
     * @param value value
     * @return create separator result
     */
    NElementSeparator createSeparator(String value);

    /**
     * Creates a new instance of create separator.
     *
     * @param value value
     * @return create separator result
     */
    NElementSeparator createSeparator(char value);

    /**
     * Creates a new instance of create space.
     *
     * @param value value
     * @return create space result
     */
    NElementSpace createSpace(String value);

    /**
     * Creates a new instance of create newline.
     *
     * @param value value
     * @return create newline result
     */
    NElementNewLine createNewline(String value);

    /**
     * Creates a new instance of create bound affix.
     *
     * @param affix affix
     * @param anchor anchor
     * @return create bound affix result
     */
    NBoundAffix createBoundAffix(NAffix affix, NAffixAnchor anchor);

    /**
     * Creates a new instance of create fragment.
     *
     * @param elements elements
     * @return create fragment result
     */
    NFragmentElement createFragment(NElement... elements);

    /**
     * Creates a new instance of create element line.
     *
     * @param prefix prefix
     * @param startMarker start marker
     * @param startPadding start padding
     * @param content content
     * @param endPadding end padding
     * @param endMarker end marker
     * @param newline newline
     * @return create element line result
     */
    NElementLine createElementLine(String prefix, String startMarker, String startPadding, String content, String endPadding, String endMarker, NNewLineMode newline);

}
