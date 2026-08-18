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


    static NElementRPI of() {
        return NExtensions.of(NElementRPI.class);
    }

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

    NElementType commonNumberType(NElementType aa, NElementType bb);

    NExprElementReshaper createExprElementReshaper(NExprElementReshaperType type);

    NExprElementReshaperBuilder createExprElementReshaperBuilder(NExprElementReshaperType type);

    NElementFormatterBuilder createElementFormatterBuilder();

    NElementFormatter createElementFormatter(NElementFormatterStyle style);

    NElementPath createRootPath();

    NElementMetadata createElementMetadata();

    NElementMetadata createElementMetadata(Object key, Object value);

    NElementMetadata createElementMetadata(Map<Object, Object> any);

    NElementStep createStepChild(String name);

    NElementStep createStepChild(int index);

    NElementStep createStepParam(String name);

    NElementStep createStepParam(int index);

    NElementStep createStepAnnotationParam(int paramIndex, String name);

    NElementStep createStepAnnotationParam(int paramIndex, int index);

    NElementStep createStepSubList(int index);

    NElementNavigator createRootNavigator(NElement element);


    ////

    NPairElement createPair(NElement key, NElement value);

    NPairElement createPair(String key, NElement value);

    NPairElement createPair(String key, Boolean value);

    NPairElement createPair(String key, Number value);

    NPairElement createPair(String key, Byte value);

    NPairElement createPair(String key, Short value);

    NPairElement createPair(String key, Integer value);

    NPairElement createPair(String key, Long value);

    NPairElement createPair(String key, String value);

    NPairElement createPair(String key, Double value);

    NPairElement createPair(String key, Instant value);

    NPairElement createPair(String key, LocalDate value);

    NPairElement createPair(String key, LocalDateTime value);

    NPairElement createPair(String key, LocalTime value);

    NPairElementBuilder createPairBuilder(NElement key, NElement value);

    NOperatorElementBuilder createOpBuilder();

    NOperatorElement createBinaryInfixOperator(NOperatorSymbol op, NElement first, NElement second);

    NOperatorElement createUnaryPrefixOperator(NOperatorSymbol op, NElement first);

    NOperatorSymbolElement createOp(NOperatorSymbol op);

    NPairElementBuilder createPairBuilder();

    /**
     * create object element builder (mutable)
     *
     * @return object element
     */
    NObjectElementBuilder createObjectBuilder();

    NObjectElementBuilder createObjectBuilder(String name);

    /**
     * create array element builder (mutable)
     *
     * @return array element
     */
    NArrayElementBuilder createArrayBuilder();

    NFragmentElementBuilder createFragmentBuilder();

    NArrayElementBuilder createArrayBuilder(String name);

    NArrayElement createArray();

    NObjectElement createObject();

    //    public NutsPrimitiveElement forNutsString(NutsString str) {
    //        return str == null ? DefaultNPrimitiveElementBuilder.NULL : new DefaultNPrimitiveElement(NutsElementType.NUTS_STRING, str);
    //    }
    NPrimitiveElement createBoolean(String value);

    NPrimitiveElement createBoolean(boolean value);

    NPrimitiveElement createName(String value);

    NPrimitiveElement createNameOrString(String value);

    <T extends Enum<T>> NPrimitiveElement createEnum(Enum<T> value);

    NPrimitiveElement createString(String str);

    NPrimitiveElement createString(String str, NElementType stringLayout);

    NCustomElement createCustom(Object object);

    NPrimitiveElement createTrue();

    NPrimitiveElement createFalse();

    NPrimitiveElement createInstant(Instant instant);

    NPrimitiveElement createLocalDate(LocalDate localDate);

    NPrimitiveElement createLocalDateTime(LocalDateTime localDateTime);

    NPrimitiveElement createLocalTime(LocalTime localTime);

    NPrimitiveElement createFloat(Float value);

    NPrimitiveElement createFloat(float value);

    NPrimitiveElement createFloat(Float value, String suffix);

    NPrimitiveElement createFloat(float value, String suffix);

    NPrimitiveElement createByte(Byte value);

    NPrimitiveElement createByte(byte value);

    NPrimitiveElement createByte(Byte value, NNumberLayout layout, String suffix);

    NPrimitiveElement createByte(byte value, NNumberLayout layout, String suffix);

    NPrimitiveElement createByte(Byte value, NNumberLayout layout);

    NPrimitiveElement createByte(byte value, NNumberLayout layout);

    NPrimitiveElement createByte(Byte value, String suffix);

    NPrimitiveElement createByte(byte value, String suffix);

    NPrimitiveElement createShort(Short value);

    NPrimitiveElement createShort(short value);

    NPrimitiveElement createShort(Short value, NNumberLayout layout, String suffix);

    NPrimitiveElement createShort(short value, NNumberLayout layout, String suffix);

    NPrimitiveElement createShort(Short value, NNumberLayout layout);

    NPrimitiveElement createShort(short value, NNumberLayout layout);

    NPrimitiveElement createShort(Short value, String suffix);

    NPrimitiveElement createShort(short value, String suffix);


    NPrimitiveElement createInt(Integer value);

    NPrimitiveElement createInt(int value);

    NPrimitiveElement createInt(Integer value, String suffix);

    NPrimitiveElement createInt(int value, String suffix);

    NPrimitiveElement createInt(Integer value, NNumberLayout layout, String suffix);

    NPrimitiveElement createInt(int value, NNumberLayout layout, String suffix);

    NPrimitiveElement createInt(Integer value, NNumberLayout layout);

    NPrimitiveElement createInt(int value, NNumberLayout layout);

    NPrimitiveElement createLong(Long value);

    NPrimitiveElement createLong(long value);

    NPrimitiveElement createLong(Long value, String suffix);

    NPrimitiveElement createLong(long value, String suffix);

    NPrimitiveElement createLong(Long value, NNumberLayout layout, String suffix);

    NPrimitiveElement createLong(long value, NNumberLayout layout, String suffix);

    NPrimitiveElement createLong(Long value, NNumberLayout layout);

    NPrimitiveElement createLong(long value, NNumberLayout layout);


    NPrimitiveElement createNull();

    NPrimitiveElement createNumber(String value);

    NPrimitiveElement createInstant(Date value);

    NPrimitiveElement createInstant(String value);

    NPrimitiveElement createChar(Character value);

    NPrimitiveElement createDouble(Double value);

    NPrimitiveElement createDouble(double value);

    NPrimitiveElement createDouble(Double value, String suffix);

    NPrimitiveElement createDouble(double value, String suffix);

    NPrimitiveElement createDoubleComplex(double real);

    NPrimitiveElement createDoubleComplex(double real, double imag);

    NPrimitiveElement createDoubleComplex(double real, double imag, String suffix);

    NPrimitiveElement createFloatComplex(float real);

    NPrimitiveElement createFloatComplex(float real, float imag);

    NPrimitiveElement createFloatComplex(float real, float imag, String suffix);

    NPrimitiveElement createBigComplex(BigDecimal real);

    NPrimitiveElement createBigComplex(BigDecimal real, BigDecimal imag);

    NPrimitiveElement createBigComplex(BigDecimal real, BigDecimal imag, String suffix);


    NPrimitiveElement createNumber(Number value);

    NPrimitiveElement createBigDecimal(BigDecimal value);

    NPrimitiveElement createNumber(Number value, NNumberLayout layout, String suffix);

    NPrimitiveElement createBigDecimal(BigDecimal value, String suffix);

    NPrimitiveElement createBigInt(BigInteger value);

    NPrimitiveElement createBigInt(BigInteger value, NNumberLayout layout, String suffix);

    NPrimitiveElement createBigInt(BigInteger value, NNumberLayout layout);

    NPrimitiveElement createBigInt(BigInteger value, String suffix);

    NTupleElementBuilder createTupleBuilder();

    NTupleElementBuilder createTupleBuilder(String name);

    NTupleElement createTuple();

    NTupleElement createTuple(NElement... items);

    NTupleElement createTuple(String name, NElement... items);

    NTupleElement createNamedTuple(String name, NElement... items);

    NArrayElement createIntArray(int... items);

    NArrayElement createIntArray(Integer... items);

    NArrayElement createLongArray(long... items);

    NArrayElement createLongArray(Long... items);

    NArrayElement createNumberArray(Number... items);

    NArrayElement createBooleanArray(boolean... items);

    NArrayElement createBooleanArray(Boolean... items);

    NArrayElement createArray(NElement... items);

    NArrayElement createArray(String name, NElement... items);

    NArrayElement createNamedArray(String name, NElement... items);

    NArrayElement createFullArray(String name, NElement[] params, NElement... items);

    NArrayElement createArray(String name, NElement[] params, NElement... items);

    NArrayElement createParamArray(NElement[] params, NElement... items);

    NArrayElement createParamArray(NElement... params);

    NArrayElement createParamArray(String name, NElement[] params, NElement... items);

    NArrayElement createParamArray(String name, NElement... params);

    NArrayElement createStringArray(String... items);

    NArrayElement createDoubleArray(double... items);

    NArrayElement createDoubleArray(Double... items);

    NArrayElement createFloatArray(float... items);

    NArrayElement createFloatArray(Float... items);

    NArrayElement createByteArray(byte... items);

    NArrayElement createCharArray(char... items);
    NArrayElement createCharArray(Character... items);

    NArrayElement createByteArray(Byte... items);

    NArrayElement createShortArray(short... items);

    NArrayElement createShortArray(Short... items);

    NObjectElement createObject(NElement... items);

    NObjectElement createObject(String name, NElement... items);

    NObjectElement createNamedObject(String name, NElement... items);

    NObjectElement createFullObject(String name, NElement[] params, NElement... items);

    NObjectElement createParamObject(NElement[] params, NElement... items);

    NObjectElement createParamObject(NElement... params);

    NObjectElement createParamObject(String name, NElement[] params, NElement... items);

    NObjectElement createObject(String name, NElement[] params, NElement... items);

    NObjectElement createParamObject(String name, NElement... params);

    NElementComment createBlocComment(String text);

    NElementComment createLineComment(String text);

    NElementComment createBlocComment(NElementLine... text);

    NElementComment createLineComment(NElementLine... text);

    NElement createBinaryStream(NInputStreamProvider value);

    NElement createBinaryStream(NInputStreamProvider value, String blockIdentifier);

    NElement createCharStream(NReaderProvider value, String blockIdentifier);

    NBinaryStreamElementBuilder createBinaryStreamBuilder();

    NCharStreamElementBuilder createCharStreamBuilder();

    NElementAnnotation createAnnotation(String name, NElement... values);

    NElementAnnotation createAnnotation(String name);

    NPrimitiveElementBuilder createPrimitiveBuilder();

    NFlatExprElementBuilder createFlatExprBuilder();

    NEmptyElementBuilder createErrorBuilder();

    NElementDiagnosticBuilder createDiagnosticBuilder();

    NElementDiagnostic createDiagnostic(NMsg msg);

    NElementSeparator createSeparator(String value);

    NElementSeparator createSeparator(char value);

    NElementSpace createSpace(String value);

    NElementNewLine createNewline(String value);

    NBoundAffix createBoundAffix(NAffix affix, NAffixAnchor anchor);

    NFragmentElement createFragment(NElement... elements);

    NElementLine createElementLine(String prefix, String startMarker, String startPadding, String content, String endPadding, String endMarker, NNewLineMode newline);

}
