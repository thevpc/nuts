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
package net.thevpc.nuts.elem;

import net.thevpc.nuts.math.NBigComplex;
import net.thevpc.nuts.math.NDoubleComplex;
import net.thevpc.nuts.math.NFloatComplex;
import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.io.NReaderProvider;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

/**
 * Nuts Element types are generic JSON like parsable objects. elements are a superset of JSON actually
 * that support multiple structured elements including json, xml, etc...
 * Elements are used to provide a convenient way to manipulate structured elements regardless of the underlying
 * format. Hence It's used for converting from json to xml as an example among many other use cases in the NAF
 * (Nuts Application Framework)
 *
 * @author thevpc
 * @app.category Elements
 * @since 0.5.6
 */
public interface NElement extends NElementDescribable<NElement>, NBlankable/*, NLiteral*/ {

    /// ///////////////////////////////////////////////////////////////////////////////////

    static NPairElement ofPair(NElement key, NElement value) {
        return NElementFactory.of().ofPair(key, value);
    }

    static NPairElement ofPair(String key, NElement value) {
        return NElementFactory.of().ofPair(key, value);
    }

    static NPairElement ofPair(String key, Boolean value) {
        return NElementFactory.of().ofPair(key, value);
    }

    static NPairElement ofPair(String key, Byte value) {
        return NElementFactory.of().ofPair(key, value);
    }

    static NPairElement ofPair(String key, Short value) {
        return NElementFactory.of().ofPair(key, value);
    }

    static NPairElement ofPair(String key, Integer value) {
        return NElementFactory.of().ofPair(key, value);
    }

    static NPairElement ofPair(String key, Long value) {
        return NElementFactory.of().ofPair(key, value);
    }

    static NPairElement ofPair(String key, String value) {
        return NElementFactory.of().ofPair(key, value);
    }

    static NPairElement ofPair(String key, Double value) {
        return NElementFactory.of().ofPair(key, value);
    }

    static NPairElement ofPair(String key, Instant value) {
        return NElementFactory.of().ofPair(key, value);
    }

    static NPairElement ofPair(String key, LocalDate value) {
        return NElementFactory.of().ofPair(key, value);
    }

    static NPairElement ofPair(String key, LocalDateTime value) {
        return NElementFactory.of().ofPair(key, value);
    }

    static NPairElement ofPair(String key, LocalTime value) {
        return NElementFactory.of().ofPair(key, value);
    }

    static NPairElementBuilder ofPairBuilder(NElement key, NElement value) {
        return NElementFactory.of().ofPairBuilder(key, value);
    }

    static NPairElementBuilder ofPairBuilder() {
        return NElementFactory.of().ofPairBuilder();
    }


    static NOperatorElementBuilder ofOpBuilder() {
        return NElementFactory.of().ofOpBuilder();
    }

    static NOperatorElementBuilder ofOpBuilder(NElementType op) {
        return NElementFactory.of().ofOpBuilder().operator(op);
    }

    static NOperatorElement ofOp(NElementType op, NOperatorType operatorType, NElement first, NElement second) {
        return NElementFactory.of().ofOp(op, operatorType, first, second);
    }

    static NOperatorElement ofOp(NElementType op, NElement first, NElement second) {
        return NElementFactory.of().ofOp(op, first, second);
    }

    static NOperatorElement ofOp(NElementType op, NElement first) {
        return NElementFactory.of().ofOp(op, first);
    }

    static NOperatorElementBuilder ofOpBuilder(NElementType op, NOperatorType operatorType, NElement first, NElement second) {
        return ofOpBuilder().operator(op).operatorType(operatorType).first(first).second(second);
    }

    static NOperatorElementBuilder ofOpBuilder(NElementType op, NElement first, NElement second) {
        return ofOpBuilder(op, null, first, second);
    }

    static NOperatorElementBuilder ofOpBuilder(NElementType op, NElement first) {
        return ofOpBuilder(op, null, first, null);
    }


    /**
     * create object element builder (mutable)
     *
     * @return object element
     */
    static NObjectElementBuilder ofObjectBuilder() {
        return NElementFactory.of().ofObjectBuilder();
    }

    static NObjectElementBuilder ofObjectBuilder(String name) {
        return NElementFactory.of().ofObjectBuilder(name);
    }

    /**
     * create array element builder (mutable)
     *
     * @return array element
     */
    static NArrayElementBuilder ofArrayBuilder() {
        return NElementFactory.of().ofArrayBuilder();
    }

    static NArrayElementBuilder ofArrayBuilder(String name) {
        return NElementFactory.of().ofArrayBuilder(name);
    }

    static NArrayElement ofArray() {
        return NElementFactory.of().ofArray();
    }

    static NObjectElement ofObject() {
        return NElementFactory.of().ofObject();
    }

    static NPrimitiveElement ofBoolean(String value) {
        return NElementFactory.of().ofBoolean(value);
    }

    static NPrimitiveElement ofBoolean(boolean value) {
        return NElementFactory.of().ofBoolean(value);
    }

    static NPrimitiveElement ofRegex(String value) {
        return NElementFactory.of().ofRegex(value);
    }

    static NPrimitiveElement ofName(String value) {
        return NElementFactory.of().ofName(value);
    }

    static NPrimitiveElement ofNameOrString(String value) {
        return NElementFactory.of().ofNameOrString(value);
    }

    static NPrimitiveElement ofString(String value) {
        return NElementFactory.of().ofString(value);
    }

    static <T extends Enum<T>> NPrimitiveElement ofEnum(Enum<T> value) {
        return NElementFactory.of().ofEnum(value);
    }

    static NPrimitiveElement ofString(String value, NElementType stringLayout) {
        return NElementFactory.of().ofString(value, stringLayout);
    }

    static NCustomElement ofCustom(Object value) {
        return NElementFactory.of().ofCustom(value);
    }

    static NPrimitiveElement ofTrue() {
        return NElementFactory.of().ofTrue();
    }

    static NPrimitiveElement ofFalse() {
        return NElementFactory.of().ofFalse();
    }

    static NPrimitiveElement ofInstant(Instant value) {
        return NElementFactory.of().ofInstant(value);
    }

    static NPrimitiveElement ofLocalDate(LocalDate value) {
        return NElementFactory.of().ofLocalDate(value);
    }

    static NPrimitiveElement ofLocalDateTime(LocalDateTime value) {
        return NElementFactory.of().ofLocalDateTime(value);
    }

    static NPrimitiveElement ofLocalTime(LocalTime value) {
        return NElementFactory.of().ofLocalTime(value);
    }

    static NPrimitiveElement ofFloat(Float value) {
        return NElementFactory.of().ofFloat(value);
    }

    static NPrimitiveElement ofFloat(float value) {
        return NElementFactory.of().ofFloat(value);
    }

    static NPrimitiveElement ofFloat(Float value, String suffix) {
        return NElementFactory.of().ofFloat(value, suffix);
    }

    static NPrimitiveElement ofFloat(float value, String suffix) {
        return NElementFactory.of().ofFloat(value, suffix);
    }

    static NPrimitiveElement ofByte(Byte value) {
        return NElementFactory.of().ofByte(value);
    }

    static NPrimitiveElement ofByte(byte value) {
        return NElementFactory.of().ofByte(value);
    }

    static NPrimitiveElement ofByte(Byte value, NNumberLayout layout, String suffix) {
        return NElementFactory.of().ofByte(value, layout, suffix);
    }

    static NPrimitiveElement ofByte(byte value, NNumberLayout layout, String suffix) {
        return NElementFactory.of().ofByte(value, layout, suffix);
    }

    static NPrimitiveElement ofByte(Byte value, NNumberLayout layout) {
        return NElementFactory.of().ofByte(value, layout);
    }

    static NPrimitiveElement ofByte(byte value, NNumberLayout layout) {
        return NElementFactory.of().ofByte(value, layout);
    }

    static NPrimitiveElement ofByte(Byte value, String suffix) {
        return NElementFactory.of().ofByte(value, suffix);
    }

    static NPrimitiveElement ofByte(byte value, String suffix) {
        return NElementFactory.of().ofByte(value, suffix);
    }

    static NPrimitiveElement ofShort(Short value) {
        return NElementFactory.of().ofShort(value);
    }

    static NPrimitiveElement ofShort(short value) {
        return NElementFactory.of().ofShort(value);
    }

    static NPrimitiveElement ofShort(Short value, NNumberLayout layout, String suffix) {
        return NElementFactory.of().ofShort(value, layout, suffix);
    }

    static NPrimitiveElement ofShort(short value, NNumberLayout layout, String suffix) {
        return NElementFactory.of().ofShort(value, layout, suffix);
    }

    static NPrimitiveElement ofShort(Short value, NNumberLayout layout) {
        return NElementFactory.of().ofShort(value, layout);
    }

    static NPrimitiveElement ofShort(short value, NNumberLayout layout) {
        return NElementFactory.of().ofShort(value, layout);
    }

    static NPrimitiveElement ofShort(Short value, String suffix) {
        return NElementFactory.of().ofShort(value, suffix);
    }

    static NPrimitiveElement ofShort(short value, String suffix) {
        return NElementFactory.of().ofShort(value, suffix);
    }

    static NPrimitiveElement ofInt(Integer value) {
        return NElementFactory.of().ofInt(value);
    }

    static NPrimitiveElement ofInt(int value) {
        return NElementFactory.of().ofInt(value);
    }

    static NPrimitiveElement ofInt(Integer value, String suffix) {
        return NElementFactory.of().ofInt(value, suffix);
    }

    static NPrimitiveElement ofInt(int value, String suffix) {
        return NElementFactory.of().ofInt(value, suffix);
    }

    static NPrimitiveElement ofInt(Integer value, NNumberLayout layout, String suffix) {
        return NElementFactory.of().ofInt(value, layout, suffix);
    }

    static NPrimitiveElement ofInt(int value, NNumberLayout layout, String suffix) {
        return NElementFactory.of().ofInt(value, layout, suffix);
    }

    static NPrimitiveElement ofInt(Integer value, NNumberLayout layout) {
        return NElementFactory.of().ofInt(value, layout);
    }

    static NPrimitiveElement ofInt(int value, NNumberLayout layout) {
        return NElementFactory.of().ofInt(value, layout);
    }

    static NPrimitiveElement ofLong(Long value) {
        return NElementFactory.of().ofLong(value);
    }

    static NPrimitiveElement ofLong(long value) {
        return NElementFactory.of().ofLong(value);
    }

    static NPrimitiveElement ofLong(Long value, String suffix) {
        return NElementFactory.of().ofLong(value, suffix);
    }

    static NPrimitiveElement ofLong(long value, String suffix) {
        return NElementFactory.of().ofLong(value, suffix);
    }

    static NPrimitiveElement ofLong(Long value, NNumberLayout layout, String suffix) {
        return NElementFactory.of().ofLong(value, layout, suffix);
    }

    static NPrimitiveElement ofLong(long value, NNumberLayout layout, String suffix) {
        return NElementFactory.of().ofLong(value, layout, suffix);
    }

    static NPrimitiveElement ofLong(Long value, NNumberLayout layout) {
        return NElementFactory.of().ofLong(value, layout);
    }

    static NPrimitiveElement ofLong(long value, NNumberLayout layout) {
        return NElementFactory.of().ofLong(value, layout);
    }

    static NPrimitiveElement ofNull() {
        return NElementFactory.of().ofNull();
    }

    static NPrimitiveElement ofNumber(String value) {
        return NElementFactory.of().ofNumber(value);
    }

    static NPrimitiveElement ofInstant(Date value) {
        return NElementFactory.of().ofInstant(value);
    }

    static NPrimitiveElement ofInstant(String value) {
        return NElementFactory.of().ofInstant(value);
    }

    static NPrimitiveElement ofChar(Character value) {
        return NElementFactory.of().ofChar(value);
    }

    static NPrimitiveElement ofDouble(Double value) {
        return NElementFactory.of().ofDouble(value);
    }

    static NPrimitiveElement ofDouble(double value) {
        return NElementFactory.of().ofDouble(value);
    }

    static NPrimitiveElement ofDouble(Double value, String suffix) {
        return NElementFactory.of().ofDouble(value, suffix);
    }

    static NPrimitiveElement ofDouble(double value, String suffix) {
        return NElementFactory.of().ofDouble(value, suffix);
    }

    static NPrimitiveElement ofDoubleComplex(double real) {
        return NElementFactory.of().ofDoubleComplex(real);
    }

    static NPrimitiveElement ofDoubleComplex(double real, double imag) {
        return NElementFactory.of().ofDoubleComplex(real, imag);
    }

    static NPrimitiveElement ofFloatComplex(float real) {
        return NElementFactory.of().ofFloatComplex(real);
    }

    static NPrimitiveElement ofFloatComplex(float real, float imag) {
        return NElementFactory.of().ofFloatComplex(real, imag);
    }

    static NPrimitiveElement ofBigComplex(BigDecimal real) {
        return NElementFactory.of().ofBigComplex(real);
    }

    static NPrimitiveElement ofBigComplex(BigDecimal real, BigDecimal imag) {
        return NElementFactory.of().ofBigComplex(real, imag);
    }

    static NPrimitiveElement ofNumber(Number value) {
        return NElementFactory.of().ofNumber(value);
    }

    static NPrimitiveElement ofBigDecimal(BigDecimal value) {
        return NElementFactory.of().ofBigDecimal(value);
    }

    static NPrimitiveElement ofBigDecimal(BigDecimal value, String suffix) {
        return NElementFactory.of().ofBigDecimal(value, suffix);
    }

    static NPrimitiveElement ofBigInt(BigInteger value) {
        return NElementFactory.of().ofBigInt(value);
    }

    static NPrimitiveElement ofBigInt(BigInteger value, NNumberLayout layout, String suffix) {
        return NElementFactory.of().ofBigInt(value, layout, suffix);
    }

    static NPrimitiveElement ofBigInt(BigInteger value, NNumberLayout layout) {
        return NElementFactory.of().ofBigInt(value, layout);
    }

    static NPrimitiveElement ofBigInt(BigInteger value, String suffix) {
        return NElementFactory.of().ofBigInt(value, suffix);
    }

    static NUpletElementBuilder ofUpletBuilder() {
        return NElementFactory.of().ofUpletBuilder();
    }

    static NUpletElementBuilder ofUpletBuilder(String name) {
        return NElementFactory.of().ofUpletBuilder(name);
    }

    static NUpletElement ofUplet() {
        return NElementFactory.of().ofUplet();
    }

    static NUpletElement ofUplet(NElement... items) {
        return NElementFactory.of().ofUplet(items);
    }

    static NUpletElement ofUplet(String name, NElement... items) {
        return NElementFactory.of().ofUplet(name, items);
    }

    static NUpletElement ofNamedUplet(String name, NElement... items) {
        return NElementFactory.of().ofNamedUplet(name, items);
    }

    static NMatrixElementBuilder ofMatrixBuilder() {
        return NElementFactory.of().ofMatrixBuilder();
    }

    static NArrayElement ofIntArray(int... items) {
        return NElementFactory.of().ofIntArray(items);
    }

    static NArrayElement ofIntArray(Integer... items) {
        return NElementFactory.of().ofIntArray(items);
    }

    static NArrayElement ofLongArray(long... items) {
        return NElementFactory.of().ofLongArray(items);
    }

    static NArrayElement ofLongArray(Long... items) {
        return NElementFactory.of().ofLongArray(items);
    }

    static NArrayElement ofNumberArray(Number... items) {
        return NElementFactory.of().ofNumberArray(items);
    }

    static NArrayElement ofBooleanArray(boolean... items) {
        return NElementFactory.of().ofBooleanArray(items);
    }

    static NArrayElement ofBooleanArray(Boolean... items) {
        return NElementFactory.of().ofBooleanArray(items);
    }

    static NArrayElement ofArray(NElement... items) {
        return NElementFactory.of().ofArray(items);
    }

    static NArrayElement ofArray(String name, NElement... items) {
        return NElementFactory.of().ofArray(name, items);
    }

    static NArrayElement ofNamedArray(String name, NElement... items) {
        return NElementFactory.of().ofNamedArray(name, items);
    }

    static NArrayElement ofNamedParametrizedArray(String name, NElement[] params, NElement... items) {
        return NElementFactory.of().ofNamedParametrizedArray(name, items);
    }

    static NArrayElement ofArray(String name, NElement[] params, NElement... items) {
        return NElementFactory.of().ofArray(name, params, items);
    }

    static NArrayElement ofParametrizedArray(NElement[] params, NElement... items) {
        return NElementFactory.of().ofParametrizedArray(params, items);
    }

    static NArrayElement ofParametrizedArray(NElement... params) {
        return NElementFactory.of().ofParametrizedArray(params);
    }

    static NArrayElement ofParametrizedArray(String name, NElement[] params, NElement... items) {
        return NElementFactory.of().ofParametrizedArray(name, params, items);
    }

    static NArrayElement ofParametrizedArray(String name, NElement... params) {
        return NElementFactory.of().ofParametrizedArray(name, params);
    }

    static NArrayElement ofStringArray(String... items) {
        return NElementFactory.of().ofStringArray(items);
    }

    static NArrayElement ofDoubleArray(double... items) {
        return NElementFactory.of().ofDoubleArray(items);
    }

    static NArrayElement ofDoubleArray(Double... items) {
        return NElementFactory.of().ofDoubleArray(items);
    }

    static NObjectElement ofObject(NElement... items) {
        return NElementFactory.of().ofObject(items);
    }

    static NObjectElement ofObject(String name, NElement... items) {
        return NElementFactory.of().ofObject(name, items);
    }

    static NObjectElement ofNamedObject(String name, NElement... items) {
        return NElementFactory.of().ofNamedObject(name, items);
    }

    static NObjectElement ofNamedParametrizedObject(String name, NElement[] params, NElement... items) {
        return NElementFactory.of().ofNamedParametrizedObject(name, params, items);
    }

    static NObjectElement ofParametrizedObject(NElement[] params, NElement... items) {
        return NElementFactory.of().ofParametrizedObject(params, items);
    }

    static NObjectElement ofParametrizedObject(NElement... params) {
        return NElementFactory.of().ofParametrizedObject(params);
    }

    static NObjectElement ofParametrizedObject(String name, NElement[] params, NElement... items) {
        return NElementFactory.of().ofParametrizedObject(name, params, items);
    }

    static NObjectElement ofObject(String name, NElement[] params, NElement... items) {
        return NElementFactory.of().ofObject(name, params, items);
    }

    static NObjectElement ofParametrizedObject(String name, NElement... params) {
        return NElementFactory.of().ofParametrizedObject(name, params);
    }

    static NElementComments ofMultiLineComments(String... comments) {
        return NElementFactory.of().ofMultiLineComments();
    }

    static NElementComments ofSingleLineComments(String... comments) {
        return NElementFactory.of().ofSingleLineComments(comments);
    }

    static NElementComments ofComments(NElementComment[] leading, NElementComment[] trailing) {
        return NElementFactory.of().ofComments(leading, trailing);
    }

    static NElementComment ofMultiLineComment(String... comments) {
        return NElementFactory.of().ofMultiLineComment(comments);
    }

    static NElementComment ofSingleLineComment(String... lines) {
        return NElementFactory.of().ofSingleLineComment(lines);
    }

    static NElement ofBinaryStream(NInputStreamProvider value) {
        return NElementFactory.of().ofBinaryStream(value);
    }

    static NElement ofCharStream(NReaderProvider value) {
        return NElementFactory.of().ofCharStream(value);
    }

    static NBinaryStreamElementBuilder ofBinaryStreamBuilder() {
        return NElementFactory.of().ofBinaryStreamBuilder();
    }

    static NCharStreamElementBuilder ofCharStreamBuilder() {
        return NElementFactory.of().ofCharStreamBuilder();
    }

    static NElementAnnotation ofAnnotation(String name, NElement... values) {
        return NElementFactory.of().ofAnnotation(name, values);
    }

    static NElementAnnotation ofAnnotation(String name) {
        return NElementFactory.of().ofAnnotation(name);
    }

    static NPrimitiveElementBuilder ofPrimitiveBuilder() {
        return NElementFactory.of().ofPrimitiveBuilder();
    }

    /**
     * element type
     *
     * @return element type
     */
    NElementType type();

    String toString(boolean compact);

    boolean isCustomTree();

    boolean isStream();

    boolean isNumber();

    boolean isFloatingNumber();

    boolean isOrdinalNumber();

    boolean isNull();

    boolean isString();

    boolean isByte();

    boolean isInt();

    boolean isLong();

    boolean isShort();

    boolean isFloat();

    boolean isDouble();

    boolean isBoolean();

    boolean isDecimalNumber();

    boolean isBigNumber();

    boolean isBigDecimal();

    boolean isBigInt();

    boolean isInstant();

    boolean isComplexNumber();

    boolean isTemporal();

    boolean isLocalTemporal();

    boolean isNamed();

    boolean isNamed(Predicate<String> nameCondition);

    boolean isParametrized();

    boolean isUplet();

    boolean isNamedUplet();

    boolean isNamedUplet(Predicate<String> nameCondition);

    boolean isNamedUplet(String name);

    boolean isNamedObject();

    boolean isNamedObject(String name);

    boolean isNamedObject(Predicate<String> nameCondition);

    boolean isAnyNamedObject();

    boolean isAnyNamedObject(String name);

    boolean isParametrizedObject();

    boolean isNamedParametrizedObject();

    boolean isNamedParametrizedObject(Predicate<String> nameCondition);

    boolean isNamedParametrizedMatrix();

    boolean isNamedParametrizedMatrix(Predicate<String> nameCondition);

    boolean isNamedParametrizedMatrix(String name);

    boolean isNamedParametrizedObject(String name);

    boolean isNamedArray();

    boolean isAnyArray();

    boolean isListContainer();

    boolean isParametrizedContainer();

    boolean isAnyObject();

    boolean isAnyMatrix();

    boolean isAnyUplet();

    boolean isAnyNamedArray();

    boolean isAnyNamedArray(String name);

    boolean isParametrizedArray();

    boolean isNamedParametrizedArray();

    boolean isNamedParametrizedArray(String name);

    boolean isNamedMatrix();

    boolean isAnyNamedMatrix();

    boolean isAnyNamedMatrix(String name);

    boolean isParametrizedMatrix();

    boolean isAnyParametrizedMatrix();

    boolean isName(String name);

    boolean isName(Predicate<String> nameCondition);

    boolean isNamed(String name);

    boolean isAnyParametrizedMatrix(String name);

    List<NElementAnnotation> annotations();

    List<NElementAnnotation> findAnnotations(String name);

    boolean isAnnotated(String name);

    /**
     * convert this element to {@link NPrimitiveElement} or throw
     * ClassCastException
     *
     * @return {@link NPrimitiveElement}
     */
    NOptional<NPrimitiveElement> asPrimitive();

    NOptional<NElement> resolve(String pattern);

    List<NElement> resolveAll(String pattern);

    /**
     * cast this element to {@link NObjectElement} or throw
     * ClassCastException
     *
     * @return {@link NObjectElement}
     */
    NOptional<NObjectElement> asObject();

    NOptional<NElement> asElementAt(int index);

    NOptional<NUpletElement> asUplet();

    NOptional<NMatrixElement> asMatrix();

    NOptional<NPairElement> asPair();

    NOptional<NNumberElement> asInt();

    /**
     * cast this element to {@link NObjectElement} or throw
     * ClassCastException
     *
     * @return {@link NObjectElement}
     */
    NOptional<NListContainerElement> asListContainer();

    NOptional<NParametrizedContainerElement> asParametrizedContainer();

    NOptional<NObjectElement> asParametrizedObject();

    NOptional<NObjectElement> asNamedParametrizedObject(String name);

    NOptional<NNamedElement> asNamed();

    /**
     * cast this element to {@link NCustomElement} or throw
     * ClassCastException
     *
     * @return {@link NObjectElement}
     */
    NOptional<NCustomElement> asCustom();

    /**
     * true if can be cast to a custom element
     *
     * @return true if can be cast to a custom element
     */
    boolean isCustom();

    /**
     * convert this element to {@link NArrayElement} or throw
     * ClassCastException
     *
     * @return {@link NArrayElement}
     */
    NOptional<NArrayElement> asArray();

    /**
     * return true if this element can be cast to {@link NPrimitiveElement}
     *
     * @return true if this element can be cast to {@link NPrimitiveElement}
     */
    boolean isPrimitive();

    boolean isAnyString();


    /**
     * return true if this element can be cast to {@link NObjectElement} and is a simple object
     *
     * @return true if this element can be cast to {@link NObjectElement} and is a simple object
     */
    boolean isObject();

    /**
     * return true if this element can be cast to {@link NArrayElement}
     *
     * @return true if this element can be cast to {@link NArrayElement}
     */
    boolean isArray();


    /**
     * return true if this element is empty:
     * <ul>
     *     <li>primitives are empty only if they are null or an empty string</li>
     *     <li>objects are empty if they do not have any field</li>
     *     <li>arrays are empty if they do not have any item</li>
     *     <li>customs are NEVER empty</li>
     * </ul>
     *
     * @return return true if this element is empty
     */
    boolean isEmpty();

    /**
     * return true if this element is blank:
     * <ul>
     *     <li>primitives are blank only if they are null or a blank string</li>
     *     <li>objects are blank if they do not have any field</li>
     *     <li>arrays are blank if they do not have any item</li>
     *     <li>customs are NEVER blank</li>
     * </ul>
     *
     * @return return true if this element is blank
     */
    @Override
    boolean isBlank();

    NElementBuilder builder();

    boolean isPair();

    /**
     * true if pair with primitive key
     *
     * @return true if pair with primitive key
     */
    boolean isSimplePair();

    /**
     * true if pair with string like key
     *
     * @return true if pair with string like key
     */
    boolean isNamedPair();

    boolean isNamedPair(String name);

    boolean isNamedPair(Predicate<String> nameCondition);

    /**
     * best effort to convert to NListContainerElement
     *
     * @return NListContainerElement
     */
    NOptional<NListContainerElement> toListContainer();

    NElementComments comments();

    boolean isName();

    /**
     * converts the current element to a named uplet is applicable without information loss
     *
     * @return
     */
    NOptional<NUpletElement> toNamedUplet();

    /**
     * converts the current element to a named uplet
     *
     * @return
     */
    NOptional<NPairElement> toNamedPair();

    NOptional<NObjectElement> toNamedObject();

    NOptional<NObjectElement> toObject();

    NOptional<NArrayElement> toNamedArray();

    NOptional<NNamedElement> toNamed();

    NOptional<NArrayElement> toArray();

    NArrayElement wrapIntoArray();

    NObjectElement wrapIntoObject();

    NUpletElement wrapIntoUplet();

    NArrayElement wrapIntoNamedArray(String name);

    NObjectElement wrapIntoNamedObject(String name);

    NUpletElement wrapIntoNamedUplet(String name);

    NPairElement wrapIntoNamedPair(String name);

    NLiteral asLiteral();

    NOptional<NStringElement> asString();

    NOptional<String> asStringValue();

    NOptional<String> asNameValue();

    NOptional<LocalTime> asLocalTimeValue();

    NOptional<BigInteger> asBigIntValue();

    NOptional<BigDecimal> asBigDecimalValue();

    NOptional<Boolean> asBooleanValue();

    NOptional<Number> asNumberValue();

    NOptional<Temporal> asTemporalValue();

    NOptional<Character> asCharValue();

    NOptional<Instant> asInstantValue();

    NOptional<NBigComplex> asBigComplexValue();

    NOptional<NDoubleComplex> asDoubleComplexValue();

    NOptional<NFloatComplex> asFloatComplexValue();

    NOptional<LocalDate> asLocalDateValue();

    NOptional<LocalDateTime> asLocalDateTimeValue();

    NOptional<Byte> asByteValue();

    NOptional<Short> asShortValue();

    NOptional<Integer> asIntValue();

    NOptional<Long> asLongValue();

    NOptional<Float> asFloatValue();

    NOptional<Double> asDoubleValue();

    NOptional<NNumberElement> asNumber();

    boolean isAnyDate();

    NOptional<NOperatorElement> asOperator();

    boolean isBinaryOperator();

    boolean isBinaryOperator(NElementType type);

    boolean isLeftNamedBinaryOperator(NElementType type);

    boolean isLeftNamedBinaryOperator(NElementType type, String name);

    boolean isAnyOperator();

    boolean isBinaryInfixOperator();

    boolean isUnaryOperator();

    boolean isUnaryPrefixOperator();

    NOptional<NElement> asNumberType(NElementType elemType);

    NElement[] transform(NElementTransform transform);

    String snippet();

    String snippet(int size);
}
