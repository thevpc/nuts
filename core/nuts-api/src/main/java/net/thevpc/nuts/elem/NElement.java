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

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.math.NBigComplex;
import net.thevpc.nuts.math.NDoubleComplex;
import net.thevpc.nuts.math.NFloatComplex;
import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.io.NReaderProvider;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NTreeVisitResult;
import net.thevpc.nuts.util.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
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
public interface NElement extends NDescribable, NBlankable, NElementSimple {

    static NElement of(Object any) {
        return NElementRPI.of().getSharedElements().toElement(any);
    }

    static Object simpleOf(Object any) {
        return NElementRPI.of().getSharedElements().toSimple(any);
    }

    static <T> T convertAny(Object any, Class<T> to) {
        return NElementRPI.of().getSharedElements().convert(any, to);
    }
    static NElements doWithMapperStore(Consumer<NElementMapperStore> doWith){
        return NElementRPI.of().getSharedElements().doWithMapperStore(doWith);
    }


    /// ///////////////////////////////////////////////////////////////////////////////////

    static NPairElement ofPair(NElement key, NElement value) {
        return NElementRPI.of().createPair(key, value);
    }

    static NOperatorSymbolElement ofOperatorSymbol(NOperatorSymbol kind) {
        return NElementRPI.of().createOp(kind);
    }

    static NPairElement ofPair(String key, NElement value) {
        return NElementRPI.of().createPair(key, value);
    }

    static NPairElement ofPair(String key, Boolean value) {
        return NElementRPI.of().createPair(key, value);
    }

    static NPairElement ofPair(String key, Number value) {
        return NElementRPI.of().createPair(key, value);
    }

    static NPairElement ofPair(String key, Byte value) {
        return NElementRPI.of().createPair(key, value);
    }

    static NPairElement ofPair(String key, Short value) {
        return NElementRPI.of().createPair(key, value);
    }

    static NPairElement ofPair(String key, Integer value) {
        return NElementRPI.of().createPair(key, value);
    }

    static NPairElement ofPair(String key, Long value) {
        return NElementRPI.of().createPair(key, value);
    }

    static NPairElement ofPair(String key, String value) {
        return NElementRPI.of().createPair(key, value);
    }

    static NPairElement ofPair(String key, Double value) {
        return NElementRPI.of().createPair(key, value);
    }

    static NPairElement ofPair(String key, Instant value) {
        return NElementRPI.of().createPair(key, value);
    }

    static NPairElement ofPair(String key, LocalDate value) {
        return NElementRPI.of().createPair(key, value);
    }

    static NPairElement ofPair(String key, LocalDateTime value) {
        return NElementRPI.of().createPair(key, value);
    }

    static NPairElement ofPair(String key, LocalTime value) {
        return NElementRPI.of().createPair(key, value);
    }

    static NPairElementBuilder ofPairBuilder(NElement key, NElement value) {
        return NElementRPI.of().createPairBuilder(key, value);
    }

    static NPairElementBuilder ofPairBuilder() {
        return NElementRPI.of().createPairBuilder();
    }


    static NOperatorElementBuilder ofExprBuilder() {
        return NElementRPI.of().createOpBuilder();
    }

    static NOperatorElementBuilder ofExprBuilder(NOperatorSymbol op) {
        return NElementRPI.of().createOpBuilder().operator(op);
    }

    static NOperatorElement ofBinaryInfixOperator(NOperatorSymbol op, NElement first, NElement second) {
        return NElementRPI.of().createBinaryInfixOperator(op, first, second);
    }

    static NOperatorElement ofUnaryPrefixOperator(NOperatorSymbol op, NElement first) {
        return NElementRPI.of().createUnaryPrefixOperator(op, first);
    }

    static NOperatorElementBuilder ofExprBuilder(NOperatorSymbol op, NOperatorPosition operatorType, NElement first, NElement second) {
        return ofExprBuilder().operator(op).position(operatorType).first(first).second(second);
    }

    static NOperatorElementBuilder ofExprBuilder(NOperatorSymbol op, NElement first, NElement second) {
        return ofExprBuilder(op, null, first, second);
    }

    static NOperatorElementBuilder ofExprBuilder(NOperatorSymbol op, NElement first) {
        return ofExprBuilder(op, null, first, null);
    }


    /**
     * create object element builder (mutable)
     *
     * @return object element
     */
    static NObjectElementBuilder ofObjectBuilder() {
        return NElementRPI.of().createObjectBuilder();
    }

    static NObjectElementBuilder ofObjectBuilder(String name) {
        return NElementRPI.of().createObjectBuilder(name);
    }

    /**
     * create array element builder (mutable)
     *
     * @return array element
     */
    static NArrayElementBuilder ofArrayBuilder() {
        return NElementRPI.of().createArrayBuilder();
    }

    static NFragmentElementBuilder ofFragmentBuilder() {
        return NElementRPI.of().createFragmentBuilder();
    }

    static NArrayElementBuilder ofArrayBuilder(String name) {
        return NElementRPI.of().createArrayBuilder(name);
    }

    static NArrayElement ofArray() {
        return NElementRPI.of().createArray();
    }

    static NFragmentElement ofFragment() {
        return NElementRPI.of().createFragment();
    }

    static NObjectElement ofObject() {
        return NElementRPI.of().createObject();
    }

    static NPrimitiveElement ofBoolean(String value) {
        return NElementRPI.of().createBoolean(value);
    }

    static NPrimitiveElement ofBoolean(boolean value) {
        return NElementRPI.of().createBoolean(value);
    }

    static NPrimitiveElement ofName(String value) {
        return NElementRPI.of().createName(value);
    }

    static NPrimitiveElement ofNameOrString(String value) {
        return NElementRPI.of().createNameOrString(value);
    }

    static NPrimitiveElement ofString(String value) {
        return NElementRPI.of().createString(value);
    }

    static <T extends Enum<T>> NPrimitiveElement ofEnum(Enum<T> value) {
        return NElementRPI.of().createEnum(value);
    }

    static NPrimitiveElement ofString(String value, NElementType stringLayout) {
        return NElementRPI.of().createString(value, stringLayout);
    }

    static NCustomElement ofCustom(Object value) {
        return NElementRPI.of().createCustom(value);
    }

    static NPrimitiveElement ofTrue() {
        return NElementRPI.of().createTrue();
    }

    static NPrimitiveElement ofFalse() {
        return NElementRPI.of().createFalse();
    }

    static NPrimitiveElement ofInstant(Instant value) {
        return NElementRPI.of().createInstant(value);
    }

    static NPrimitiveElement ofLocalDate(LocalDate value) {
        return NElementRPI.of().createLocalDate(value);
    }

    static NPrimitiveElement ofLocalDateTime(LocalDateTime value) {
        return NElementRPI.of().createLocalDateTime(value);
    }

    static NPrimitiveElement ofLocalTime(LocalTime value) {
        return NElementRPI.of().createLocalTime(value);
    }

    static NPrimitiveElement ofFloat(Float value) {
        return NElementRPI.of().createFloat(value);
    }

    static NPrimitiveElement ofFloat(float value) {
        return NElementRPI.of().createFloat(value);
    }

    static NPrimitiveElement ofFloat(Float value, String suffix) {
        return NElementRPI.of().createFloat(value, suffix);
    }

    static NPrimitiveElement ofFloat(float value, String suffix) {
        return NElementRPI.of().createFloat(value, suffix);
    }

    static NPrimitiveElement ofByte(Byte value) {
        return NElementRPI.of().createByte(value);
    }

    static NPrimitiveElement ofByte(byte value) {
        return NElementRPI.of().createByte(value);
    }

    static NPrimitiveElement ofByte(Byte value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createByte(value, layout, suffix);
    }

    static NPrimitiveElement ofByte(byte value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createByte(value, layout, suffix);
    }

    static NPrimitiveElement ofByte(Byte value, NNumberLayout layout) {
        return NElementRPI.of().createByte(value, layout);
    }

    static NPrimitiveElement ofByte(byte value, NNumberLayout layout) {
        return NElementRPI.of().createByte(value, layout);
    }

    static NPrimitiveElement ofByte(Byte value, String suffix) {
        return NElementRPI.of().createByte(value, suffix);
    }

    static NPrimitiveElement ofByte(byte value, String suffix) {
        return NElementRPI.of().createByte(value, suffix);
    }

    static NPrimitiveElement ofShort(Short value) {
        return NElementRPI.of().createShort(value);
    }

    static NPrimitiveElement ofShort(short value) {
        return NElementRPI.of().createShort(value);
    }

    static NPrimitiveElement ofShort(Short value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createShort(value, layout, suffix);
    }

    static NPrimitiveElement ofShort(short value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createShort(value, layout, suffix);
    }

    static NPrimitiveElement ofShort(Short value, NNumberLayout layout) {
        return NElementRPI.of().createShort(value, layout);
    }

    static NPrimitiveElement ofShort(short value, NNumberLayout layout) {
        return NElementRPI.of().createShort(value, layout);
    }

    static NPrimitiveElement ofShort(Short value, String suffix) {
        return NElementRPI.of().createShort(value, suffix);
    }

    static NPrimitiveElement ofShort(short value, String suffix) {
        return NElementRPI.of().createShort(value, suffix);
    }

    static NPrimitiveElement ofInt(Integer value) {
        return NElementRPI.of().createInt(value);
    }

    static NPrimitiveElement ofInt(int value) {
        return NElementRPI.of().createInt(value);
    }

    static NPrimitiveElement ofInt(Integer value, String suffix) {
        return NElementRPI.of().createInt(value, suffix);
    }

    static NPrimitiveElement ofInt(int value, String suffix) {
        return NElementRPI.of().createInt(value, suffix);
    }

    static NPrimitiveElement ofInt(Integer value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createInt(value, layout, suffix);
    }

    static NPrimitiveElement ofInt(int value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createInt(value, layout, suffix);
    }

    static NPrimitiveElement ofInt(Integer value, NNumberLayout layout) {
        return NElementRPI.of().createInt(value, layout);
    }

    static NPrimitiveElement ofInt(int value, NNumberLayout layout) {
        return NElementRPI.of().createInt(value, layout);
    }

    static NPrimitiveElement ofLong(Long value) {
        return NElementRPI.of().createLong(value);
    }

    static NPrimitiveElement ofLong(long value) {
        return NElementRPI.of().createLong(value);
    }

    static NPrimitiveElement ofLong(Long value, String suffix) {
        return NElementRPI.of().createLong(value, suffix);
    }

    static NPrimitiveElement ofLong(long value, String suffix) {
        return NElementRPI.of().createLong(value, suffix);
    }

    static NPrimitiveElement ofLong(Long value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createLong(value, layout, suffix);
    }

    static NPrimitiveElement ofLong(long value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createLong(value, layout, suffix);
    }

    static NPrimitiveElement ofLong(Long value, NNumberLayout layout) {
        return NElementRPI.of().createLong(value, layout);
    }

    static NPrimitiveElement ofLong(long value, NNumberLayout layout) {
        return NElementRPI.of().createLong(value, layout);
    }

    static NPrimitiveElement ofNull() {
        return NElementRPI.of().createNull();
    }

    static NPrimitiveElement ofNumber(String value) {
        return NElementRPI.of().createNumber(value);
    }

    static NPrimitiveElement ofInstant(Date value) {
        return NElementRPI.of().createInstant(value);
    }

    static NPrimitiveElement ofInstant(String value) {
        return NElementRPI.of().createInstant(value);
    }

    static NPrimitiveElement ofChar(Character value) {
        return NElementRPI.of().createChar(value);
    }

    static NPrimitiveElement ofDouble(Double value) {
        return NElementRPI.of().createDouble(value);
    }

    static NPrimitiveElement ofDouble(double value) {
        return NElementRPI.of().createDouble(value);
    }

    static NPrimitiveElement ofDouble(Double value, String suffix) {
        return NElementRPI.of().createDouble(value, suffix);
    }

    static NPrimitiveElement ofDouble(double value, String suffix) {
        return NElementRPI.of().createDouble(value, suffix);
    }

    static NPrimitiveElement ofDoubleComplex(double real) {
        return NElementRPI.of().createDoubleComplex(real);
    }

    static NPrimitiveElement ofDoubleComplex(double real, double imag) {
        return NElementRPI.of().createDoubleComplex(real, imag);
    }

    static NPrimitiveElement ofDoubleComplex(double real, double imag, String suffix) {
        return NElementRPI.of().createDoubleComplex(real, imag, suffix);
    }

    static NPrimitiveElement ofFloatComplex(float real) {
        return NElementRPI.of().createFloatComplex(real);
    }

    static NPrimitiveElement ofFloatComplex(float real, float imag) {
        return NElementRPI.of().createFloatComplex(real, imag);
    }

    static NPrimitiveElement ofFloatComplex(float real, float imag, String suffix) {
        return NElementRPI.of().createFloatComplex(real, imag, suffix);
    }

    static NPrimitiveElement ofBigComplex(BigDecimal real) {
        return NElementRPI.of().createBigComplex(real);
    }

    static NPrimitiveElement ofBigComplex(BigDecimal real, BigDecimal imag) {
        return NElementRPI.of().createBigComplex(real, imag);
    }

    static NPrimitiveElement ofBigComplex(BigDecimal real, BigDecimal imag, String suffix) {
        return NElementRPI.of().createBigComplex(real, imag, suffix);
    }

    static NPrimitiveElement ofNumber(Number value) {
        return NElementRPI.of().createNumber(value);
    }

    static NPrimitiveElement ofNumber(Number value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createNumber(value, layout, suffix);
    }

    static NPrimitiveElement ofBigDecimal(BigDecimal value) {
        return NElementRPI.of().createBigDecimal(value);
    }

    static NPrimitiveElement ofBigDecimal(BigDecimal value, String suffix) {
        return NElementRPI.of().createBigDecimal(value, suffix);
    }

    static NPrimitiveElement ofBigInt(BigInteger value) {
        return NElementRPI.of().createBigInt(value);
    }

    static NPrimitiveElement ofBigInt(BigInteger value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createBigInt(value, layout, suffix);
    }

    static NPrimitiveElement ofBigInt(BigInteger value, NNumberLayout layout) {
        return NElementRPI.of().createBigInt(value, layout);
    }

    static NPrimitiveElement ofBigInt(BigInteger value, String suffix) {
        return NElementRPI.of().createBigInt(value, suffix);
    }

    static NTupleElementBuilder ofTupleBuilder() {
        return NElementRPI.of().createTupleBuilder();
    }

    static NTupleElementBuilder ofTupleBuilder(String name) {
        return NElementRPI.of().createTupleBuilder(name);
    }

    static NTupleElement ofTuple() {
        return NElementRPI.of().createTuple();
    }

    static NTupleElement ofTuple(NElement... items) {
        return NElementRPI.of().createTuple(items);
    }

    static NTupleElement ofTuple(String name, NElement... items) {
        return NElementRPI.of().createTuple(name, items);
    }

    static NTupleElement ofNamedTuple(String name, NElement... items) {
        return NElementRPI.of().createNamedTuple(name, items);
    }

    static NArrayElement ofIntArray(int... items) {
        return NElementRPI.of().createIntArray(items);
    }

    static NArrayElement ofIntArray(Integer... items) {
        return NElementRPI.of().createIntArray(items);
    }

    static NArrayElement ofLongArray(long... items) {
        return NElementRPI.of().createLongArray(items);
    }

    static NArrayElement ofLongArray(Long... items) {
        return NElementRPI.of().createLongArray(items);
    }

    static NArrayElement ofNumberArray(Number... items) {
        return NElementRPI.of().createNumberArray(items);
    }

    static NArrayElement ofBooleanArray(boolean... items) {
        return NElementRPI.of().createBooleanArray(items);
    }

    static NArrayElement ofBooleanArray(Boolean... items) {
        return NElementRPI.of().createBooleanArray(items);
    }

    static NArrayElement ofArray(NElement... items) {
        return NElementRPI.of().createArray(items);
    }

    static NArrayElement ofArray(String name, NElement... items) {
        return NElementRPI.of().createArray(name, items);
    }

    static NArrayElement ofNamedArray(String name, NElement... items) {
        return NElementRPI.of().createNamedArray(name, items);
    }

    static NArrayElement ofFullArray(String name, NElement[] params, NElement... items) {
        return NElementRPI.of().createFullArray(name, items);
    }

    static NArrayElement ofArray(String name, NElement[] params, NElement... items) {
        return NElementRPI.of().createArray(name, params, items);
    }

    static NArrayElement ofParamArray(NElement[] params, NElement... items) {
        return NElementRPI.of().createParamArray(params, items);
    }

    static NArrayElement ofParamArray(NElement... params) {
        return NElementRPI.of().createParamArray(params);
    }

    static NArrayElement ofParamArray(String name, NElement[] params, NElement... items) {
        return NElementRPI.of().createParamArray(name, params, items);
    }

    static NArrayElement ofParamArray(String name, NElement... params) {
        return NElementRPI.of().createParamArray(name, params);
    }

    static NArrayElement ofStringArray(String... items) {
        return NElementRPI.of().createStringArray(items);
    }

    static NArrayElement ofEnumArray(Enum<?>... items) {
        return NElementRPI.of().createArray(
                Arrays.stream(items).map(NElement::ofEnum).toArray(NElement[]::new)
        );
    }

    static NArrayElement ofDoubleArray(double... items) {
        return NElementRPI.of().createDoubleArray(items);
    }

    static NArrayElement ofDoubleArray(Double... items) {
        return NElementRPI.of().createDoubleArray(items);
    }

    static NArrayElement ofFloatArray(float... items) {
        return NElementRPI.of().createFloatArray(items);
    }

    static NArrayElement ofFloatArray(Float... items) {
        return NElementRPI.of().createFloatArray(items);
    }

    static NArrayElement ofByteArray(byte... items) {
        return NElementRPI.of().createByteArray(items);
    }

    static NArrayElement ofCharArray(char... items) {
        return NElementRPI.of().createCharArray(items);
    }

    static NArrayElement ofCharArray(Character... items) {
        return NElementRPI.of().createCharArray(items);
    }

    static NArrayElement ofByteArray(Byte... items) {
        return NElementRPI.of().createByteArray(items);
    }

    static NArrayElement ofShortArray(short... items) {
        return NElementRPI.of().createShortArray(items);
    }

    static NArrayElement ofShortArray(Short... items) {
        return NElementRPI.of().createShortArray(items);
    }

    static NObjectElement ofObject(NElement... items) {
        return NElementRPI.of().createObject(items);
    }

    static NObjectElement ofObject(String name, NElement... items) {
        return NElementRPI.of().createObject(name, items);
    }

    static NObjectElement ofNamedObject(String name, NElement... items) {
        return NElementRPI.of().createNamedObject(name, items);
    }

    static NObjectElement ofFullObject(String name, NElement[] params, NElement... items) {
        return NElementRPI.of().createFullObject(name, params, items);
    }

    static NObjectElement ofParamObject(NElement[] params, NElement... items) {
        return NElementRPI.of().createParamObject(params, items);
    }

    static NObjectElement ofParamObject(NElement... params) {
        return NElementRPI.of().createParamObject(params);
    }

    static NObjectElement ofParamObject(String name, NElement[] params, NElement... items) {
        return NElementRPI.of().createParamObject(name, params, items);
    }

    static NObjectElement ofObject(String name, NElement[] params, NElement... items) {
        return NElementRPI.of().createObject(name, params, items);
    }

    static NObjectElement ofParamObject(String name, NElement... params) {
        return NElementRPI.of().createParamObject(name, params);
    }

    static NElementComment ofBlocComment(String comments) {
        return NElementRPI.of().createBlocComment(comments);
    }

    static NElementComment ofLineComment(String lines) {
        return NElementRPI.of().createLineComment(lines);
    }

    static NElementComment ofBlocComment(NElementLine... comments) {
        return NElementRPI.of().createBlocComment(comments);
    }

    static NElementComment ofLineComment(NElementLine... lines) {
        return NElementRPI.of().createLineComment(lines);
    }

    static NElement ofBinaryStream(NInputStreamProvider value) {
        return NElementRPI.of().createBinaryStream(value);
    }

    static NElement ofBinaryStream(NInputStreamProvider value, String blocIdentifier) {
        return NElementRPI.of().createBinaryStream(value, blocIdentifier);
    }

    static NElement ofCharStream(NReaderProvider value) {
        return NElementRPI.of().createCharStream(value, "");
    }

    static NElement ofCharStream(NReaderProvider value, String blocIdentifier) {
        return NElementRPI.of().createCharStream(value, blocIdentifier);
    }

    static NBinaryStreamElementBuilder ofBinaryStreamBuilder() {
        return NElementRPI.of().createBinaryStreamBuilder();
    }

    static NCharStreamElementBuilder ofCharStreamBuilder() {
        return NElementRPI.of().createCharStreamBuilder();
    }

    static NPrimitiveElementBuilder ofPrimitiveBuilder() {
        return NElementRPI.of().createPrimitiveBuilder();
    }

    static NFlatExprElementBuilder ofFlatExprBuilder() {
        return NElementRPI.of().createFlatExprBuilder();
    }

    static NEmptyElementBuilder ofEmptyBuilder() {
        return NElementRPI.of().createErrorBuilder();
    }

    static NElementDiagnosticBuilder ofDiagnosticBuilder() {
        return NElementRPI.of().createDiagnosticBuilder();
    }

    static NElement ofFragment(NElement... elements) {
        return NElementRPI.of().createFragment(elements);
    }

    boolean anyMatches(Predicate<NElement> predicate);

    List<NElementDiagnostic> diagnostics();


    /**
     * element type
     *
     * @return element type
     */
    NElementType type();

    String toString();

    /**
     * Traverse this element and its entire subtree (including annotations).
     *
     * @param visitor the visitor to apply
     * @return true if traversal completed fully, false if TERMINATE was returned
     */
    NTreeVisitResult traverse(NElementVisitor visitor);

    List<NElementDiagnostic> treeDiagnostics();

    boolean isCustomTree();

    boolean isErrorTree();

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


    /**
     * @since 0.8.9
     */
    boolean isNamedListContainer();

    /**
     * @since 0.8.9
     */
    boolean isNamedListContainer(String name);

    boolean isNamed(Predicate<String> nameCondition);

    boolean isParametrized();

    boolean isTuple();

    boolean isNamedTuple();

    boolean isNamedTuple(Predicate<String> nameCondition);

    boolean isNamedTuple(String name);

    boolean isNamedObject();

    boolean isNamedObject(String name);

    boolean isNamedObject(Predicate<String> nameCondition);

    boolean isAnyNamedObject();

    boolean isAnyNamedObject(String name);

    boolean isAnyNamedListContainer();

    boolean isAnyNamedListContainer(String name);

    boolean isParamObject();

    boolean isFullObject();

    boolean isFullObject(Predicate<String> nameCondition);

    boolean isFullObject(String name);

    boolean isNamedArray();

    boolean isAnyArray();

    boolean isListContainer();

    boolean isListOrParametrizedContainer();

    boolean isParametrizedContainer();

    boolean isAnyObject();

    boolean isAnyTuple();

    boolean isAnyNamedArray();

    boolean isAnyNamedArray(String name);

    boolean isParamArray();

    boolean isFullArray();

    boolean isFullArray(String name);

    boolean isName(String name);

    boolean isName(Predicate<String> nameCondition);

    boolean isNamed(String name);

    List<NBoundAffix> affixes();

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

    NOptional<NTupleElement> asTuple();

    NOptional<NPairElement> asPair();

    NOptional<NNumberElement> asInt();

    /**
     * cast this element to {@link NObjectElement} or throw
     * ClassCastException
     *
     * @return {@link NObjectElement}
     */
    NOptional<NListContainerElement> asListContainer();

    NOptional<NListOrParametrizedContainerElement> asListOrParametrizedContainer();

    NOptional<NParametrizedContainerElement> asParametrizedContainer();

    NOptional<NObjectElement> asParamObject();

    NOptional<NObjectElement> asNamedObject(String name);

    NOptional<NObjectElement> asFullObject(String name);

    NOptional<NNamedElement> asNamed();

    boolean isNamedArray(String name);

    NOptional<NObjectElement> asNamedArray(String name);

    NOptional<NObjectElement> asFullArray(String name);

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

    boolean isAnyStringOrName();


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

    boolean isFragment();


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

    List<NElement> toMany();

    List<NElementComment> comments();

    boolean isName();

    /**
     * converts the current element to a named Tuple is applicable without information loss
     *
     * @return
     */
    NOptional<NTupleElement> toNamedTuple();

    /**
     * converts the current element to a named Tuple
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

    NTupleElement wrapIntoTuple();

    NArrayElement wrapIntoNamedArray(String name);

    NObjectElement wrapIntoNamedObject(String name);

    NTupleElement wrapIntoNamedTuple(String name);

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

    boolean isFlatExpression();

    NOptional<NFlatExprElement> asFlatExpression();

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

    NOptional<NOperatorSymbolElement> asOperatorSymbol();

    NOptional<NOperatorSymbolElement> asOperatorSymbol(NOperatorSymbol symbol);

    NOptional<NBinaryOperatorElement> asBinaryOperator(NOperatorSymbol symbol);

    NOptional<NBinaryOperatorElement> asBinaryInfixOperator(NOperatorSymbol symbol);

    NOptional<NUnaryOperatorElement> asUnaryOperator(NOperatorSymbol symbol);

    NOptional<NUnaryOperatorElement> asUnaryPrefixOperator(NOperatorSymbol symbol);

    NOptional<NUnaryOperatorElement> asUnaryPostfixOperator(NOperatorSymbol symbol);

    NOptional<NBinaryOperatorElement> asBinaryOperator();

    NOptional<NUnaryOperatorElement> asUnaryOperator();

    boolean isOperatorSymbol();

    boolean isOperatorSymbol(NOperatorSymbol symbol);

    boolean isBinaryOperator();

    boolean isBinaryOperator(NOperatorSymbol type);

    boolean isUnaryOperator(NOperatorSymbol type);

    boolean isLeftNamedBinaryOperator(NOperatorSymbol type);

    boolean isLeftNamedBinaryOperator(NOperatorSymbol type, String name);

    boolean isAnyOperator();

    boolean isBinaryInfixOperator();

    boolean isBinaryInfixOperator(NOperatorSymbol symbol);

    boolean isUnaryOperator();

    boolean isUnaryPrefixOperator();

    boolean isUnaryPrefixOperator(NOperatorSymbol symbol);

    boolean isUnaryPostfixOperator(NOperatorSymbol symbol);

    NOptional<NElement> asNumberType(NElementType elemType);

    List<NElement> transform(NElementTransform transform);

    List<NElement> transform(NElementTransformContext context, NElementTransform transform);

    /**
     * Transforms this element using the provided transformer and returns
     * an optional result. Useful for 1:1 or 1:0 mappings.
     */
    default NOptional<NElement> transformOptional(NElementTransform transform) {
        List<NElement> list = transform(transform);
        if (list == null || list.isEmpty()) {
            return NOptional.ofEmpty(() -> NMsg.ofC("Transformation returned no elements"));
        }
        return NOptional.of(list.get(0));
    }

    /**
     * returns the reshaped element;
     * Semantic sugar for applying a formatter.
     * for direct string output use toFormattedString
     */
    NElement format(NContentType contentType, NElementFormatter formatter);

    NElementMetadata metadata();

    String snippet();

    String snippet(int size);

    NOptional<NPairElement> asNamedPair();

    NOptional<NPairElement> asNamedPair(String name);

    NOptional<NPairElement> asSimplePair();

    boolean isList();

    NOptional<NListElement> asList();

    NOptional<NFragmentElement> asFragment();

    boolean isOrderedList();

    NOptional<NListElement> asOrderedList();

    boolean isUnorderedList();

    NOptional<NListElement> asUnorderedList();

    NOptional<NStringElement> asName();

    NOptional<NStringElement> toName();

    NOptional<NTernaryOperatorElement> asTernaryOperator();

    NOptional<NAryOperatorElement> asNaryOperator();

    List<NElement> filter(NElementSelector selector);

    List<NElement> filter(String selector);

    String toPrettyString();

    String toStableString();

    String toVerbatimString();

    String toCompactString();

    String toFormattedString(NElementFormatter formatter);

    String toFormattedString(NContentType contentType, NElementFormatter formatter);

    NOptional<NBinaryStreamElement> asBinaryStream();

    boolean isBinaryStream();

    NOptional<NCharStreamElement> asCharStream();

    boolean isCharStream();

    NElement normalize(NContentType contentType);

    <T> T convertTo(Class<T> to);

}
