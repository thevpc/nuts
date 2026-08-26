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

import net.thevpc.nuts.expr.NFixity;
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

    /**
     * Creates a new instance of of.
     *
     * @param any any
     * @return of result
     */
    static NElement of(Object any) {
        return NElementRPI.of().getSharedElements().toElement(any);
    }

    /**
     * Simple of.
     *
     * @param any any
     * @return simple of result
     */
    static Object simpleOf(Object any) {
        return NElementRPI.of().getSharedElements().toSimple(any);
    }

    /**
     * Convert any.
     *
     * @param any any
     * @param to to
     * @return convert any result
     */
    static <T> T convertAny(Object any, Class<T> to) {
        return NElementRPI.of().getSharedElements().convert(any, to);
    }
    /**
     * Do with mapper store.
     *
     * @param doWith do with
     * @return do with mapper store result
     */
    static NElements doWithMapperStore(Consumer<NElementMapperStore> doWith){
        return NElementRPI.of().getSharedElements().doWithMapperStore(doWith);
    }


    /// ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new instance of of pair.
     *
     * @param key key
     * @param value value
     * @return of pair result
     */
    static NPairElement ofPair(NElement key, NElement value) {
        return NElementRPI.of().createPair(key, value);
    }

    /**
     * Creates a new instance of of operator symbol.
     *
     * @param kind kind
     * @return of operator symbol result
     */
    static NOperatorSymbolElement ofOperatorSymbol(NOperatorSymbol kind) {
        return NElementRPI.of().createOp(kind);
    }

    /**
     * Creates a new instance of of pair.
     *
     * @param key key
     * @param value value
     * @return of pair result
     */
    static NPairElement ofPair(String key, NElement value) {
        return NElementRPI.of().createPair(key, value);
    }

    /**
     * Creates a new instance of of pair.
     *
     * @param key key
     * @param value value
     * @return of pair result
     */
    static NPairElement ofPair(String key, Boolean value) {
        return NElementRPI.of().createPair(key, value);
    }

    /**
     * Creates a new instance of of pair.
     *
     * @param key key
     * @param value value
     * @return of pair result
     */
    static NPairElement ofPair(String key, Number value) {
        return NElementRPI.of().createPair(key, value);
    }

    /**
     * Creates a new instance of of pair.
     *
     * @param key key
     * @param value value
     * @return of pair result
     */
    static NPairElement ofPair(String key, Byte value) {
        return NElementRPI.of().createPair(key, value);
    }

    /**
     * Creates a new instance of of pair.
     *
     * @param key key
     * @param value value
     * @return of pair result
     */
    static NPairElement ofPair(String key, Short value) {
        return NElementRPI.of().createPair(key, value);
    }

    /**
     * Creates a new instance of of pair.
     *
     * @param key key
     * @param value value
     * @return of pair result
     */
    static NPairElement ofPair(String key, Integer value) {
        return NElementRPI.of().createPair(key, value);
    }

    /**
     * Creates a new instance of of pair.
     *
     * @param key key
     * @param value value
     * @return of pair result
     */
    static NPairElement ofPair(String key, Long value) {
        return NElementRPI.of().createPair(key, value);
    }

    /**
     * Creates a new instance of of pair.
     *
     * @param key key
     * @param value value
     * @return of pair result
     */
    static NPairElement ofPair(String key, String value) {
        return NElementRPI.of().createPair(key, value);
    }

    /**
     * Creates a new instance of of pair.
     *
     * @param key key
     * @param value value
     * @return of pair result
     */
    static NPairElement ofPair(String key, Double value) {
        return NElementRPI.of().createPair(key, value);
    }

    /**
     * Creates a new instance of of pair.
     *
     * @param key key
     * @param value value
     * @return of pair result
     */
    static NPairElement ofPair(String key, Instant value) {
        return NElementRPI.of().createPair(key, value);
    }

    /**
     * Creates a new instance of of pair.
     *
     * @param key key
     * @param value value
     * @return of pair result
     */
    static NPairElement ofPair(String key, LocalDate value) {
        return NElementRPI.of().createPair(key, value);
    }

    /**
     * Creates a new instance of of pair.
     *
     * @param key key
     * @param value value
     * @return of pair result
     */
    static NPairElement ofPair(String key, LocalDateTime value) {
        return NElementRPI.of().createPair(key, value);
    }

    /**
     * Creates a new instance of of pair.
     *
     * @param key key
     * @param value value
     * @return of pair result
     */
    static NPairElement ofPair(String key, LocalTime value) {
        return NElementRPI.of().createPair(key, value);
    }

    /**
     * Creates a new instance of of pair builder.
     *
     * @param key key
     * @param value value
     * @return of pair builder result
     */
    static NPairElementBuilder ofPairBuilder(NElement key, NElement value) {
        return NElementRPI.of().createPairBuilder(key, value);
    }

    /**
     * Creates a new instance of of pair builder.
     *
     * @return of pair builder result
     */
    static NPairElementBuilder ofPairBuilder() {
        return NElementRPI.of().createPairBuilder();
    }


    /**
     * Creates a new instance of of expr builder.
     *
     * @return of expr builder result
     */
    static NOperatorElementBuilder ofExprBuilder() {
        return NElementRPI.of().createOpBuilder();
    }

    /**
     * Creates a new instance of of expr builder.
     *
     * @param op op
     * @return of expr builder result
     */
    static NOperatorElementBuilder ofExprBuilder(NOperatorSymbol op) {
        return NElementRPI.of().createOpBuilder().operator(op);
    }

    /**
     * Creates a new instance of of binary infix operator.
     *
     * @param op op
     * @param first first
     * @param second second
     * @return of binary infix operator result
     */
    static NOperatorElement ofBinaryInfixOperator(NOperatorSymbol op, NElement first, NElement second) {
        return NElementRPI.of().createBinaryInfixOperator(op, first, second);
    }

    /**
     * Creates a new instance of of unary prefix operator.
     *
     * @param op op
     * @param first first
     * @return of unary prefix operator result
     */
    static NOperatorElement ofUnaryPrefixOperator(NOperatorSymbol op, NElement first) {
        return NElementRPI.of().createUnaryPrefixOperator(op, first);
    }

    /**
     * Creates a new instance of of expr builder.
     *
     * @param op op
     * @param fixity fixity
     * @param first first
     * @param second second
     * @return of expr builder result
     */
    static NOperatorElementBuilder ofExprBuilder(NOperatorSymbol op, NFixity fixity, NElement first, NElement second) {
        /**
         * Creates a new instance of of expr builder.
         *
         * @param ).operator(op).fixity(fixity).first(first).second(second ).operator(op).fixity(fixity).first(first).second(second
         * @return of expr builder result
         */
        return ofExprBuilder().operator(op).fixity(fixity).first(first).second(second);
    }

    /**
     * Creates a new instance of of expr builder.
     *
     * @param op op
     * @param first first
     * @param second second
     * @return of expr builder result
     */
    static NOperatorElementBuilder ofExprBuilder(NOperatorSymbol op, NElement first, NElement second) {
        /**
         * Creates a new instance of of expr builder.
         *
         * @param op op
         * @param null null
         * @param first first
         * @param second second
         * @return of expr builder result
         */
        return ofExprBuilder(op, null, first, second);
    }

    /**
     * Creates a new instance of of expr builder.
     *
     * @param op op
     * @param first first
     * @return of expr builder result
     */
    static NOperatorElementBuilder ofExprBuilder(NOperatorSymbol op, NElement first) {
        /**
         * Creates a new instance of of expr builder.
         *
         * @param op op
         * @param null null
         * @param first first
         * @param null null
         * @return of expr builder result
         */
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

    /**
     * Creates a new instance of of object builder.
     *
     * @param name name
     * @return of object builder result
     */
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

    /**
     * Creates a new instance of of fragment builder.
     *
     * @return of fragment builder result
     */
    static NFragmentElementBuilder ofFragmentBuilder() {
        return NElementRPI.of().createFragmentBuilder();
    }

    /**
     * Creates a new instance of of array builder.
     *
     * @param name name
     * @return of array builder result
     */
    static NArrayElementBuilder ofArrayBuilder(String name) {
        return NElementRPI.of().createArrayBuilder(name);
    }

    /**
     * Creates a new instance of of array.
     *
     * @return of array result
     */
    static NArrayElement ofArray() {
        return NElementRPI.of().createArray();
    }

    /**
     * Creates a new instance of of fragment.
     *
     * @return of fragment result
     */
    static NFragmentElement ofFragment() {
        return NElementRPI.of().createFragment();
    }

    /**
     * Creates a new instance of of object.
     *
     * @return of object result
     */
    static NObjectElement ofObject() {
        return NElementRPI.of().createObject();
    }

    /**
     * Creates a new instance of of boolean.
     *
     * @param value value
     * @return of boolean result
     */
    static NPrimitiveElement ofBoolean(String value) {
        return NElementRPI.of().createBoolean(value);
    }

    /**
     * Creates a new instance of of boolean.
     *
     * @param value value
     * @return of boolean result
     */
    static NPrimitiveElement ofBoolean(boolean value) {
        return NElementRPI.of().createBoolean(value);
    }

    /**
     * Creates a new instance of of name.
     *
     * @param value value
     * @return of name result
     */
    static NPrimitiveElement ofName(String value) {
        return NElementRPI.of().createName(value);
    }

    /**
     * Creates a new instance of of name or string.
     *
     * @param value value
     * @return of name or string result
     */
    static NPrimitiveElement ofNameOrString(String value) {
        return NElementRPI.of().createNameOrString(value);
    }

    /**
     * Creates a new instance of of string.
     *
     * @param value value
     * @return of string result
     */
    static NPrimitiveElement ofString(String value) {
        return NElementRPI.of().createString(value);
    }

    /**
     * Creates a new instance of of enum.
     *
     * @param value value
     * @return of enum result
     */
    static <T extends Enum<T>> NPrimitiveElement ofEnum(Enum<T> value) {
        return NElementRPI.of().createEnum(value);
    }

    /**
     * Creates a new instance of of string.
     *
     * @param value value
     * @param stringLayout string layout
     * @return of string result
     */
    static NPrimitiveElement ofString(String value, NElementType stringLayout) {
        return NElementRPI.of().createString(value, stringLayout);
    }

    /**
     * Creates a new instance of of custom.
     *
     * @param value value
     * @return of custom result
     */
    static NCustomElement ofCustom(Object value) {
        return NElementRPI.of().createCustom(value);
    }

    /**
     * Creates a new instance of of true.
     *
     * @return of true result
     */
    static NPrimitiveElement ofTrue() {
        return NElementRPI.of().createTrue();
    }

    /**
     * Creates a new instance of of false.
     *
     * @return of false result
     */
    static NPrimitiveElement ofFalse() {
        return NElementRPI.of().createFalse();
    }

    /**
     * Creates a new instance of of instant.
     *
     * @param value value
     * @return of instant result
     */
    static NPrimitiveElement ofInstant(Instant value) {
        return NElementRPI.of().createInstant(value);
    }

    /**
     * Creates a new instance of of local date.
     *
     * @param value value
     * @return of local date result
     */
    static NPrimitiveElement ofLocalDate(LocalDate value) {
        return NElementRPI.of().createLocalDate(value);
    }

    /**
     * Creates a new instance of of local date time.
     *
     * @param value value
     * @return of local date time result
     */
    static NPrimitiveElement ofLocalDateTime(LocalDateTime value) {
        return NElementRPI.of().createLocalDateTime(value);
    }

    /**
     * Creates a new instance of of local time.
     *
     * @param value value
     * @return of local time result
     */
    static NPrimitiveElement ofLocalTime(LocalTime value) {
        return NElementRPI.of().createLocalTime(value);
    }

    /**
     * Creates a new instance of of float.
     *
     * @param value value
     * @return of float result
     */
    static NPrimitiveElement ofFloat(Float value) {
        return NElementRPI.of().createFloat(value);
    }

    /**
     * Creates a new instance of of float.
     *
     * @param value value
     * @return of float result
     */
    static NPrimitiveElement ofFloat(float value) {
        return NElementRPI.of().createFloat(value);
    }

    /**
     * Creates a new instance of of float.
     *
     * @param value value
     * @param suffix suffix
     * @return of float result
     */
    static NPrimitiveElement ofFloat(Float value, String suffix) {
        return NElementRPI.of().createFloat(value, suffix);
    }

    /**
     * Creates a new instance of of float.
     *
     * @param value value
     * @param suffix suffix
     * @return of float result
     */
    static NPrimitiveElement ofFloat(float value, String suffix) {
        return NElementRPI.of().createFloat(value, suffix);
    }

    /**
     * Creates a new instance of of byte.
     *
     * @param value value
     * @return of byte result
     */
    static NPrimitiveElement ofByte(Byte value) {
        return NElementRPI.of().createByte(value);
    }

    /**
     * Creates a new instance of of byte.
     *
     * @param value value
     * @return of byte result
     */
    static NPrimitiveElement ofByte(byte value) {
        return NElementRPI.of().createByte(value);
    }

    /**
     * Creates a new instance of of byte.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return of byte result
     */
    static NPrimitiveElement ofByte(Byte value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createByte(value, layout, suffix);
    }

    /**
     * Creates a new instance of of byte.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return of byte result
     */
    static NPrimitiveElement ofByte(byte value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createByte(value, layout, suffix);
    }

    /**
     * Creates a new instance of of byte.
     *
     * @param value value
     * @param layout layout
     * @return of byte result
     */
    static NPrimitiveElement ofByte(Byte value, NNumberLayout layout) {
        return NElementRPI.of().createByte(value, layout);
    }

    /**
     * Creates a new instance of of byte.
     *
     * @param value value
     * @param layout layout
     * @return of byte result
     */
    static NPrimitiveElement ofByte(byte value, NNumberLayout layout) {
        return NElementRPI.of().createByte(value, layout);
    }

    /**
     * Creates a new instance of of byte.
     *
     * @param value value
     * @param suffix suffix
     * @return of byte result
     */
    static NPrimitiveElement ofByte(Byte value, String suffix) {
        return NElementRPI.of().createByte(value, suffix);
    }

    /**
     * Creates a new instance of of byte.
     *
     * @param value value
     * @param suffix suffix
     * @return of byte result
     */
    static NPrimitiveElement ofByte(byte value, String suffix) {
        return NElementRPI.of().createByte(value, suffix);
    }

    /**
     * Creates a new instance of of short.
     *
     * @param value value
     * @return of short result
     */
    static NPrimitiveElement ofShort(Short value) {
        return NElementRPI.of().createShort(value);
    }

    /**
     * Creates a new instance of of short.
     *
     * @param value value
     * @return of short result
     */
    static NPrimitiveElement ofShort(short value) {
        return NElementRPI.of().createShort(value);
    }

    /**
     * Creates a new instance of of short.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return of short result
     */
    static NPrimitiveElement ofShort(Short value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createShort(value, layout, suffix);
    }

    /**
     * Creates a new instance of of short.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return of short result
     */
    static NPrimitiveElement ofShort(short value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createShort(value, layout, suffix);
    }

    /**
     * Creates a new instance of of short.
     *
     * @param value value
     * @param layout layout
     * @return of short result
     */
    static NPrimitiveElement ofShort(Short value, NNumberLayout layout) {
        return NElementRPI.of().createShort(value, layout);
    }

    /**
     * Creates a new instance of of short.
     *
     * @param value value
     * @param layout layout
     * @return of short result
     */
    static NPrimitiveElement ofShort(short value, NNumberLayout layout) {
        return NElementRPI.of().createShort(value, layout);
    }

    /**
     * Creates a new instance of of short.
     *
     * @param value value
     * @param suffix suffix
     * @return of short result
     */
    static NPrimitiveElement ofShort(Short value, String suffix) {
        return NElementRPI.of().createShort(value, suffix);
    }

    /**
     * Creates a new instance of of short.
     *
     * @param value value
     * @param suffix suffix
     * @return of short result
     */
    static NPrimitiveElement ofShort(short value, String suffix) {
        return NElementRPI.of().createShort(value, suffix);
    }

    /**
     * Creates a new instance of of int.
     *
     * @param value value
     * @return of int result
     */
    static NPrimitiveElement ofInt(Integer value) {
        return NElementRPI.of().createInt(value);
    }

    /**
     * Creates a new instance of of int.
     *
     * @param value value
     * @return of int result
     */
    static NPrimitiveElement ofInt(int value) {
        return NElementRPI.of().createInt(value);
    }

    /**
     * Creates a new instance of of int.
     *
     * @param value value
     * @param suffix suffix
     * @return of int result
     */
    static NPrimitiveElement ofInt(Integer value, String suffix) {
        return NElementRPI.of().createInt(value, suffix);
    }

    /**
     * Creates a new instance of of int.
     *
     * @param value value
     * @param suffix suffix
     * @return of int result
     */
    static NPrimitiveElement ofInt(int value, String suffix) {
        return NElementRPI.of().createInt(value, suffix);
    }

    /**
     * Creates a new instance of of int.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return of int result
     */
    static NPrimitiveElement ofInt(Integer value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createInt(value, layout, suffix);
    }

    /**
     * Creates a new instance of of int.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return of int result
     */
    static NPrimitiveElement ofInt(int value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createInt(value, layout, suffix);
    }

    /**
     * Creates a new instance of of int.
     *
     * @param value value
     * @param layout layout
     * @return of int result
     */
    static NPrimitiveElement ofInt(Integer value, NNumberLayout layout) {
        return NElementRPI.of().createInt(value, layout);
    }

    /**
     * Creates a new instance of of int.
     *
     * @param value value
     * @param layout layout
     * @return of int result
     */
    static NPrimitiveElement ofInt(int value, NNumberLayout layout) {
        return NElementRPI.of().createInt(value, layout);
    }

    /**
     * Creates a new instance of of long.
     *
     * @param value value
     * @return of long result
     */
    static NPrimitiveElement ofLong(Long value) {
        return NElementRPI.of().createLong(value);
    }

    /**
     * Creates a new instance of of long.
     *
     * @param value value
     * @return of long result
     */
    static NPrimitiveElement ofLong(long value) {
        return NElementRPI.of().createLong(value);
    }

    /**
     * Creates a new instance of of long.
     *
     * @param value value
     * @param suffix suffix
     * @return of long result
     */
    static NPrimitiveElement ofLong(Long value, String suffix) {
        return NElementRPI.of().createLong(value, suffix);
    }

    /**
     * Creates a new instance of of long.
     *
     * @param value value
     * @param suffix suffix
     * @return of long result
     */
    static NPrimitiveElement ofLong(long value, String suffix) {
        return NElementRPI.of().createLong(value, suffix);
    }

    /**
     * Creates a new instance of of long.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return of long result
     */
    static NPrimitiveElement ofLong(Long value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createLong(value, layout, suffix);
    }

    /**
     * Creates a new instance of of long.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return of long result
     */
    static NPrimitiveElement ofLong(long value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createLong(value, layout, suffix);
    }

    /**
     * Creates a new instance of of long.
     *
     * @param value value
     * @param layout layout
     * @return of long result
     */
    static NPrimitiveElement ofLong(Long value, NNumberLayout layout) {
        return NElementRPI.of().createLong(value, layout);
    }

    /**
     * Creates a new instance of of long.
     *
     * @param value value
     * @param layout layout
     * @return of long result
     */
    static NPrimitiveElement ofLong(long value, NNumberLayout layout) {
        return NElementRPI.of().createLong(value, layout);
    }

    /**
     * Creates a new instance of of null.
     *
     * @return of null result
     */
    static NPrimitiveElement ofNull() {
        return NElementRPI.of().createNull();
    }

    /**
     * Creates a new instance of of number.
     *
     * @param value value
     * @return of number result
     */
    static NPrimitiveElement ofNumber(String value) {
        return NElementRPI.of().createNumber(value);
    }

    /**
     * Creates a new instance of of instant.
     *
     * @param value value
     * @return of instant result
     */
    static NPrimitiveElement ofInstant(Date value) {
        return NElementRPI.of().createInstant(value);
    }

    /**
     * Creates a new instance of of instant.
     *
     * @param value value
     * @return of instant result
     */
    static NPrimitiveElement ofInstant(String value) {
        return NElementRPI.of().createInstant(value);
    }

    /**
     * Creates a new instance of of char.
     *
     * @param value value
     * @return of char result
     */
    static NPrimitiveElement ofChar(Character value) {
        return NElementRPI.of().createChar(value);
    }

    /**
     * Creates a new instance of of double.
     *
     * @param value value
     * @return of double result
     */
    static NPrimitiveElement ofDouble(Double value) {
        return NElementRPI.of().createDouble(value);
    }

    /**
     * Creates a new instance of of double.
     *
     * @param value value
     * @return of double result
     */
    static NPrimitiveElement ofDouble(double value) {
        return NElementRPI.of().createDouble(value);
    }

    /**
     * Creates a new instance of of double.
     *
     * @param value value
     * @param suffix suffix
     * @return of double result
     */
    static NPrimitiveElement ofDouble(Double value, String suffix) {
        return NElementRPI.of().createDouble(value, suffix);
    }

    /**
     * Creates a new instance of of double.
     *
     * @param value value
     * @param suffix suffix
     * @return of double result
     */
    static NPrimitiveElement ofDouble(double value, String suffix) {
        return NElementRPI.of().createDouble(value, suffix);
    }

    /**
     * Creates a new instance of of double complex.
     *
     * @param real real
     * @return of double complex result
     */
    static NPrimitiveElement ofDoubleComplex(double real) {
        return NElementRPI.of().createDoubleComplex(real);
    }

    /**
     * Creates a new instance of of double complex.
     *
     * @param real real
     * @param imag imag
     * @return of double complex result
     */
    static NPrimitiveElement ofDoubleComplex(double real, double imag) {
        return NElementRPI.of().createDoubleComplex(real, imag);
    }

    /**
     * Creates a new instance of of double complex.
     *
     * @param real real
     * @param imag imag
     * @param suffix suffix
     * @return of double complex result
     */
    static NPrimitiveElement ofDoubleComplex(double real, double imag, String suffix) {
        return NElementRPI.of().createDoubleComplex(real, imag, suffix);
    }

    /**
     * Creates a new instance of of float complex.
     *
     * @param real real
     * @return of float complex result
     */
    static NPrimitiveElement ofFloatComplex(float real) {
        return NElementRPI.of().createFloatComplex(real);
    }

    /**
     * Creates a new instance of of float complex.
     *
     * @param real real
     * @param imag imag
     * @return of float complex result
     */
    static NPrimitiveElement ofFloatComplex(float real, float imag) {
        return NElementRPI.of().createFloatComplex(real, imag);
    }

    /**
     * Creates a new instance of of float complex.
     *
     * @param real real
     * @param imag imag
     * @param suffix suffix
     * @return of float complex result
     */
    static NPrimitiveElement ofFloatComplex(float real, float imag, String suffix) {
        return NElementRPI.of().createFloatComplex(real, imag, suffix);
    }

    /**
     * Creates a new instance of of big complex.
     *
     * @param real real
     * @return of big complex result
     */
    static NPrimitiveElement ofBigComplex(BigDecimal real) {
        return NElementRPI.of().createBigComplex(real);
    }

    /**
     * Creates a new instance of of big complex.
     *
     * @param real real
     * @param imag imag
     * @return of big complex result
     */
    static NPrimitiveElement ofBigComplex(BigDecimal real, BigDecimal imag) {
        return NElementRPI.of().createBigComplex(real, imag);
    }

    /**
     * Creates a new instance of of big complex.
     *
     * @param real real
     * @param imag imag
     * @param suffix suffix
     * @return of big complex result
     */
    static NPrimitiveElement ofBigComplex(BigDecimal real, BigDecimal imag, String suffix) {
        return NElementRPI.of().createBigComplex(real, imag, suffix);
    }

    /**
     * Creates a new instance of of number.
     *
     * @param value value
     * @return of number result
     */
    static NPrimitiveElement ofNumber(Number value) {
        return NElementRPI.of().createNumber(value);
    }

    /**
     * Creates a new instance of of number.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return of number result
     */
    static NPrimitiveElement ofNumber(Number value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createNumber(value, layout, suffix);
    }

    /**
     * Creates a new instance of of big decimal.
     *
     * @param value value
     * @return of big decimal result
     */
    static NPrimitiveElement ofBigDecimal(BigDecimal value) {
        return NElementRPI.of().createBigDecimal(value);
    }

    /**
     * Creates a new instance of of big decimal.
     *
     * @param value value
     * @param suffix suffix
     * @return of big decimal result
     */
    static NPrimitiveElement ofBigDecimal(BigDecimal value, String suffix) {
        return NElementRPI.of().createBigDecimal(value, suffix);
    }

    /**
     * Creates a new instance of of big int.
     *
     * @param value value
     * @return of big int result
     */
    static NPrimitiveElement ofBigInt(BigInteger value) {
        return NElementRPI.of().createBigInt(value);
    }

    /**
     * Creates a new instance of of big int.
     *
     * @param value value
     * @param layout layout
     * @param suffix suffix
     * @return of big int result
     */
    static NPrimitiveElement ofBigInt(BigInteger value, NNumberLayout layout, String suffix) {
        return NElementRPI.of().createBigInt(value, layout, suffix);
    }

    /**
     * Creates a new instance of of big int.
     *
     * @param value value
     * @param layout layout
     * @return of big int result
     */
    static NPrimitiveElement ofBigInt(BigInteger value, NNumberLayout layout) {
        return NElementRPI.of().createBigInt(value, layout);
    }

    /**
     * Creates a new instance of of big int.
     *
     * @param value value
     * @param suffix suffix
     * @return of big int result
     */
    static NPrimitiveElement ofBigInt(BigInteger value, String suffix) {
        return NElementRPI.of().createBigInt(value, suffix);
    }

    /**
     * Creates a new instance of of tuple builder.
     *
     * @return of tuple builder result
     */
    static NTupleElementBuilder ofTupleBuilder() {
        return NElementRPI.of().createTupleBuilder();
    }

    /**
     * Creates a new instance of of tuple builder.
     *
     * @param name name
     * @return of tuple builder result
     */
    static NTupleElementBuilder ofTupleBuilder(String name) {
        return NElementRPI.of().createTupleBuilder(name);
    }

    /**
     * Creates a new instance of of tuple.
     *
     * @return of tuple result
     */
    static NTupleElement ofTuple() {
        return NElementRPI.of().createTuple();
    }

    /**
     * Creates a new instance of of tuple.
     *
     * @param items items
     * @return of tuple result
     */
    static NTupleElement ofTuple(NElement... items) {
        return NElementRPI.of().createTuple(items);
    }

    /**
     * Creates a new instance of of tuple.
     *
     * @param name name
     * @param items items
     * @return of tuple result
     */
    static NTupleElement ofTuple(String name, NElement... items) {
        return NElementRPI.of().createTuple(name, items);
    }

    /**
     * Creates a new instance of of named tuple.
     *
     * @param name name
     * @param items items
     * @return of named tuple result
     */
    static NTupleElement ofNamedTuple(String name, NElement... items) {
        return NElementRPI.of().createNamedTuple(name, items);
    }

    /**
     * Creates a new instance of of int array.
     *
     * @param items items
     * @return of int array result
     */
    static NArrayElement ofIntArray(int... items) {
        return NElementRPI.of().createIntArray(items);
    }

    /**
     * Creates a new instance of of int array.
     *
     * @param items items
     * @return of int array result
     */
    static NArrayElement ofIntArray(Integer... items) {
        return NElementRPI.of().createIntArray(items);
    }

    /**
     * Creates a new instance of of long array.
     *
     * @param items items
     * @return of long array result
     */
    static NArrayElement ofLongArray(long... items) {
        return NElementRPI.of().createLongArray(items);
    }

    /**
     * Creates a new instance of of long array.
     *
     * @param items items
     * @return of long array result
     */
    static NArrayElement ofLongArray(Long... items) {
        return NElementRPI.of().createLongArray(items);
    }

    /**
     * Creates a new instance of of number array.
     *
     * @param items items
     * @return of number array result
     */
    static NArrayElement ofNumberArray(Number... items) {
        return NElementRPI.of().createNumberArray(items);
    }

    /**
     * Creates a new instance of of boolean array.
     *
     * @param items items
     * @return of boolean array result
     */
    static NArrayElement ofBooleanArray(boolean... items) {
        return NElementRPI.of().createBooleanArray(items);
    }

    /**
     * Creates a new instance of of boolean array.
     *
     * @param items items
     * @return of boolean array result
     */
    static NArrayElement ofBooleanArray(Boolean... items) {
        return NElementRPI.of().createBooleanArray(items);
    }

    /**
     * Creates a new instance of of array.
     *
     * @param items items
     * @return of array result
     */
    static NArrayElement ofArray(NElement... items) {
        return NElementRPI.of().createArray(items);
    }

    /**
     * Creates a new instance of of array.
     *
     * @param name name
     * @param items items
     * @return of array result
     */
    static NArrayElement ofArray(String name, NElement... items) {
        return NElementRPI.of().createArray(name, items);
    }

    /**
     * Creates a new instance of of named array.
     *
     * @param name name
     * @param items items
     * @return of named array result
     */
    static NArrayElement ofNamedArray(String name, NElement... items) {
        return NElementRPI.of().createNamedArray(name, items);
    }

    /**
     * Creates a new instance of of full array.
     *
     * @param name name
     * @param params params
     * @param items items
     * @return of full array result
     */
    static NArrayElement ofFullArray(String name, NElement[] params, NElement... items) {
        return NElementRPI.of().createFullArray(name, items);
    }

    /**
     * Creates a new instance of of array.
     *
     * @param name name
     * @param params params
     * @param items items
     * @return of array result
     */
    static NArrayElement ofArray(String name, NElement[] params, NElement... items) {
        return NElementRPI.of().createArray(name, params, items);
    }

    /**
     * Creates a new instance of of param array.
     *
     * @param params params
     * @param items items
     * @return of param array result
     */
    static NArrayElement ofParamArray(NElement[] params, NElement... items) {
        return NElementRPI.of().createParamArray(params, items);
    }

    /**
     * Creates a new instance of of param array.
     *
     * @param params params
     * @return of param array result
     */
    static NArrayElement ofParamArray(NElement... params) {
        return NElementRPI.of().createParamArray(params);
    }

    /**
     * Creates a new instance of of param array.
     *
     * @param name name
     * @param params params
     * @param items items
     * @return of param array result
     */
    static NArrayElement ofParamArray(String name, NElement[] params, NElement... items) {
        return NElementRPI.of().createParamArray(name, params, items);
    }

    /**
     * Creates a new instance of of param array.
     *
     * @param name name
     * @param params params
     * @return of param array result
     */
    static NArrayElement ofParamArray(String name, NElement... params) {
        return NElementRPI.of().createParamArray(name, params);
    }

    /**
     * Creates a new instance of of string array.
     *
     * @param items items
     * @return of string array result
     */
    static NArrayElement ofStringArray(String... items) {
        return NElementRPI.of().createStringArray(items);
    }

    /**
     * Creates a new instance of of enum array.
     *
     * @param items items
     * @return of enum array result
     */
    static NArrayElement ofEnumArray(Enum<?>... items) {
        return NElementRPI.of().createArray(
                Arrays.stream(items).map(NElement::ofEnum).toArray(NElement[]::new)
        );
    }

    /**
     * Creates a new instance of of double array.
     *
     * @param items items
     * @return of double array result
     */
    static NArrayElement ofDoubleArray(double... items) {
        return NElementRPI.of().createDoubleArray(items);
    }

    /**
     * Creates a new instance of of double array.
     *
     * @param items items
     * @return of double array result
     */
    static NArrayElement ofDoubleArray(Double... items) {
        return NElementRPI.of().createDoubleArray(items);
    }

    /**
     * Creates a new instance of of float array.
     *
     * @param items items
     * @return of float array result
     */
    static NArrayElement ofFloatArray(float... items) {
        return NElementRPI.of().createFloatArray(items);
    }

    /**
     * Creates a new instance of of float array.
     *
     * @param items items
     * @return of float array result
     */
    static NArrayElement ofFloatArray(Float... items) {
        return NElementRPI.of().createFloatArray(items);
    }

    /**
     * Creates a new instance of of byte array.
     *
     * @param items items
     * @return of byte array result
     */
    static NArrayElement ofByteArray(byte... items) {
        return NElementRPI.of().createByteArray(items);
    }

    /**
     * Creates a new instance of of char array.
     *
     * @param items items
     * @return of char array result
     */
    static NArrayElement ofCharArray(char... items) {
        return NElementRPI.of().createCharArray(items);
    }

    /**
     * Creates a new instance of of char array.
     *
     * @param items items
     * @return of char array result
     */
    static NArrayElement ofCharArray(Character... items) {
        return NElementRPI.of().createCharArray(items);
    }

    /**
     * Creates a new instance of of byte array.
     *
     * @param items items
     * @return of byte array result
     */
    static NArrayElement ofByteArray(Byte... items) {
        return NElementRPI.of().createByteArray(items);
    }

    /**
     * Creates a new instance of of short array.
     *
     * @param items items
     * @return of short array result
     */
    static NArrayElement ofShortArray(short... items) {
        return NElementRPI.of().createShortArray(items);
    }

    /**
     * Creates a new instance of of short array.
     *
     * @param items items
     * @return of short array result
     */
    static NArrayElement ofShortArray(Short... items) {
        return NElementRPI.of().createShortArray(items);
    }

    /**
     * Creates a new instance of of object.
     *
     * @param items items
     * @return of object result
     */
    static NObjectElement ofObject(NElement... items) {
        return NElementRPI.of().createObject(items);
    }

    /**
     * Creates a new instance of of object.
     *
     * @param name name
     * @param items items
     * @return of object result
     */
    static NObjectElement ofObject(String name, NElement... items) {
        return NElementRPI.of().createObject(name, items);
    }

    /**
     * Creates a new instance of of named object.
     *
     * @param name name
     * @param items items
     * @return of named object result
     */
    static NObjectElement ofNamedObject(String name, NElement... items) {
        return NElementRPI.of().createNamedObject(name, items);
    }

    /**
     * Creates a new instance of of full object.
     *
     * @param name name
     * @param params params
     * @param items items
     * @return of full object result
     */
    static NObjectElement ofFullObject(String name, NElement[] params, NElement... items) {
        return NElementRPI.of().createFullObject(name, params, items);
    }

    /**
     * Creates a new instance of of param object.
     *
     * @param params params
     * @param items items
     * @return of param object result
     */
    static NObjectElement ofParamObject(NElement[] params, NElement... items) {
        return NElementRPI.of().createParamObject(params, items);
    }

    /**
     * Creates a new instance of of param object.
     *
     * @param params params
     * @return of param object result
     */
    static NObjectElement ofParamObject(NElement... params) {
        return NElementRPI.of().createParamObject(params);
    }

    /**
     * Creates a new instance of of param object.
     *
     * @param name name
     * @param params params
     * @param items items
     * @return of param object result
     */
    static NObjectElement ofParamObject(String name, NElement[] params, NElement... items) {
        return NElementRPI.of().createParamObject(name, params, items);
    }

    /**
     * Creates a new instance of of object.
     *
     * @param name name
     * @param params params
     * @param items items
     * @return of object result
     */
    static NObjectElement ofObject(String name, NElement[] params, NElement... items) {
        return NElementRPI.of().createObject(name, params, items);
    }

    /**
     * Creates a new instance of of param object.
     *
     * @param name name
     * @param params params
     * @return of param object result
     */
    static NObjectElement ofParamObject(String name, NElement... params) {
        return NElementRPI.of().createParamObject(name, params);
    }

    /**
     * Creates a new instance of of bloc comment.
     *
     * @param comments comments
     * @return of bloc comment result
     */
    static NElementComment ofBlocComment(String comments) {
        return NElementRPI.of().createBlocComment(comments);
    }

    /**
     * Creates a new instance of of line comment.
     *
     * @param lines lines
     * @return of line comment result
     */
    static NElementComment ofLineComment(String lines) {
        return NElementRPI.of().createLineComment(lines);
    }

    /**
     * Creates a new instance of of bloc comment.
     *
     * @param comments comments
     * @return of bloc comment result
     */
    static NElementComment ofBlocComment(NElementLine... comments) {
        return NElementRPI.of().createBlocComment(comments);
    }

    /**
     * Creates a new instance of of line comment.
     *
     * @param lines lines
     * @return of line comment result
     */
    static NElementComment ofLineComment(NElementLine... lines) {
        return NElementRPI.of().createLineComment(lines);
    }

    /**
     * Creates a new instance of of binary stream.
     *
     * @param value value
     * @return of binary stream result
     */
    static NElement ofBinaryStream(NInputStreamProvider value) {
        return NElementRPI.of().createBinaryStream(value);
    }

    /**
     * Creates a new instance of of binary stream.
     *
     * @param value value
     * @param blocIdentifier bloc identifier
     * @return of binary stream result
     */
    static NElement ofBinaryStream(NInputStreamProvider value, String blocIdentifier) {
        return NElementRPI.of().createBinaryStream(value, blocIdentifier);
    }

    /**
     * Creates a new instance of of char stream.
     *
     * @param value value
     * @return of char stream result
     */
    static NElement ofCharStream(NReaderProvider value) {
        return NElementRPI.of().createCharStream(value, "");
    }

    /**
     * Creates a new instance of of char stream.
     *
     * @param value value
     * @param blocIdentifier bloc identifier
     * @return of char stream result
     */
    static NElement ofCharStream(NReaderProvider value, String blocIdentifier) {
        return NElementRPI.of().createCharStream(value, blocIdentifier);
    }

    /**
     * Creates a new instance of of binary stream builder.
     *
     * @return of binary stream builder result
     */
    static NBinaryStreamElementBuilder ofBinaryStreamBuilder() {
        return NElementRPI.of().createBinaryStreamBuilder();
    }

    /**
     * Creates a new instance of of char stream builder.
     *
     * @return of char stream builder result
     */
    static NCharStreamElementBuilder ofCharStreamBuilder() {
        return NElementRPI.of().createCharStreamBuilder();
    }

    /**
     * Creates a new instance of of primitive builder.
     *
     * @return of primitive builder result
     */
    static NPrimitiveElementBuilder ofPrimitiveBuilder() {
        return NElementRPI.of().createPrimitiveBuilder();
    }

    /**
     * Creates a new instance of of flat expr builder.
     *
     * @return of flat expr builder result
     */
    static NFlatExprElementBuilder ofFlatExprBuilder() {
        return NElementRPI.of().createFlatExprBuilder();
    }

    /**
     * Creates a new instance of of empty builder.
     *
     * @return of empty builder result
     */
    static NEmptyElementBuilder ofEmptyBuilder() {
        return NElementRPI.of().createErrorBuilder();
    }

    /**
     * Creates a new instance of of diagnostic builder.
     *
     * @return of diagnostic builder result
     */
    static NElementDiagnosticBuilder ofDiagnosticBuilder() {
        return NElementRPI.of().createDiagnosticBuilder();
    }

    /**
     * Creates a new instance of of fragment.
     *
     * @param elements elements
     * @return of fragment result
     */
    static NElement ofFragment(NElement... elements) {
        return NElementRPI.of().createFragment(elements);
    }

    /**
     * Any matches.
     *
     * @param predicate predicate
     * @return any matches result
     */
    boolean anyMatches(Predicate<NElement> predicate);

    /**
     * Diagnostics.
     *
     * @return diagnostics result
     */
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

    /**
     * Tree diagnostics.
     *
     * @return tree diagnostics result
     */
    List<NElementDiagnostic> treeDiagnostics();

    /**
     * Checks if is custom tree.
     *
     * @return is custom tree result
     */
    boolean isCustomTree();

    /**
     * Checks if is error tree.
     *
     * @return is error tree result
     */
    boolean isErrorTree();

    /**
     * Checks if is stream.
     *
     * @return is stream result
     */
    boolean isStream();

    /**
     * Checks if is number.
     *
     * @return is number result
     */
    boolean isNumber();

    /**
     * Checks if is floating number.
     *
     * @return is floating number result
     */
    boolean isFloatingNumber();

    /**
     * Checks if is ordinal number.
     *
     * @return is ordinal number result
     */
    boolean isOrdinalNumber();

    /**
     * Checks if is null.
     *
     * @return is null result
     */
    boolean isNull();

    /**
     * Checks if is string.
     *
     * @return is string result
     */
    boolean isString();

    /**
     * Checks if is byte.
     *
     * @return is byte result
     */
    boolean isByte();

    /**
     * Checks if is int.
     *
     * @return is int result
     */
    boolean isInt();

    /**
     * Checks if is long.
     *
     * @return is long result
     */
    boolean isLong();

    /**
     * Checks if is short.
     *
     * @return is short result
     */
    boolean isShort();

    /**
     * Checks if is float.
     *
     * @return is float result
     */
    boolean isFloat();

    /**
     * Checks if is double.
     *
     * @return is double result
     */
    boolean isDouble();

    /**
     * Checks if is boolean.
     *
     * @return is boolean result
     */
    boolean isBoolean();

    /**
     * Checks if is decimal number.
     *
     * @return is decimal number result
     */
    boolean isDecimalNumber();

    /**
     * Checks if is big number.
     *
     * @return is big number result
     */
    boolean isBigNumber();

    /**
     * Checks if is big decimal.
     *
     * @return is big decimal result
     */
    boolean isBigDecimal();

    /**
     * Checks if is big int.
     *
     * @return is big int result
     */
    boolean isBigInt();

    /**
     * Checks if is instant.
     *
     * @return is instant result
     */
    boolean isInstant();

    /**
     * Checks if is complex number.
     *
     * @return is complex number result
     */
    boolean isComplexNumber();

    /**
     * Checks if is temporal.
     *
     * @return is temporal result
     */
    boolean isTemporal();

    /**
     * Checks if is local temporal.
     *
     * @return is local temporal result
     */
    boolean isLocalTemporal();

    /**
     * Checks if is named.
     *
     * @return is named result
     */
    boolean isNamed();


    /**
     * @since 0.8.9
     */
    boolean isNamedListContainer();

    /**
     * @since 0.8.9
     */
    boolean isNamedListContainer(String name);

    /**
     * Checks if is named.
     *
     * @param nameCondition name condition
     * @return is named result
     */
    boolean isNamed(Predicate<String> nameCondition);

    /**
     * Checks if is parametrized.
     *
     * @return is parametrized result
     */
    boolean isParametrized();

    /**
     * Checks if is tuple.
     *
     * @return is tuple result
     */
    boolean isTuple();

    /**
     * Checks if is named tuple.
     *
     * @return is named tuple result
     */
    boolean isNamedTuple();

    /**
     * Checks if is named tuple.
     *
     * @param nameCondition name condition
     * @return is named tuple result
     */
    boolean isNamedTuple(Predicate<String> nameCondition);

    /**
     * Checks if is named tuple.
     *
     * @param name name
     * @return is named tuple result
     */
    boolean isNamedTuple(String name);

    /**
     * Checks if is named object.
     *
     * @return is named object result
     */
    boolean isNamedObject();

    /**
     * Checks if is named object.
     *
     * @param name name
     * @return is named object result
     */
    boolean isNamedObject(String name);

    /**
     * Checks if is named object.
     *
     * @param nameCondition name condition
     * @return is named object result
     */
    boolean isNamedObject(Predicate<String> nameCondition);

    /**
     * Checks if is any named object.
     *
     * @return is any named object result
     */
    boolean isAnyNamedObject();

    /**
     * Checks if is any named object.
     *
     * @param name name
     * @return is any named object result
     */
    boolean isAnyNamedObject(String name);

    /**
     * Checks if is any named list container.
     *
     * @return is any named list container result
     */
    boolean isAnyNamedListContainer();

    /**
     * Checks if is any named list container.
     *
     * @param name name
     * @return is any named list container result
     */
    boolean isAnyNamedListContainer(String name);

    /**
     * Checks if is param object.
     *
     * @return is param object result
     */
    boolean isParamObject();

    /**
     * Checks if is full object.
     *
     * @return is full object result
     */
    boolean isFullObject();

    /**
     * Checks if is full object.
     *
     * @param nameCondition name condition
     * @return is full object result
     */
    boolean isFullObject(Predicate<String> nameCondition);

    /**
     * Checks if is full object.
     *
     * @param name name
     * @return is full object result
     */
    boolean isFullObject(String name);

    /**
     * Checks if is named array.
     *
     * @return is named array result
     */
    boolean isNamedArray();

    /**
     * Checks if is any array.
     *
     * @return is any array result
     */
    boolean isAnyArray();

    /**
     * Checks if is list container.
     *
     * @return is list container result
     */
    boolean isListContainer();

    /**
     * Checks if is list or parametrized container.
     *
     * @return is list or parametrized container result
     */
    boolean isListOrParametrizedContainer();

    /**
     * Checks if is parametrized container.
     *
     * @return is parametrized container result
     */
    boolean isParametrizedContainer();

    /**
     * Checks if is any object.
     *
     * @return is any object result
     */
    boolean isAnyObject();

    /**
     * Checks if is any tuple.
     *
     * @return is any tuple result
     */
    boolean isAnyTuple();

    /**
     * Checks if is any named array.
     *
     * @return is any named array result
     */
    boolean isAnyNamedArray();

    /**
     * Checks if is any named array.
     *
     * @param name name
     * @return is any named array result
     */
    boolean isAnyNamedArray(String name);

    /**
     * Checks if is param array.
     *
     * @return is param array result
     */
    boolean isParamArray();

    /**
     * Checks if is full array.
     *
     * @return is full array result
     */
    boolean isFullArray();

    /**
     * Checks if is full array.
     *
     * @param name name
     * @return is full array result
     */
    boolean isFullArray(String name);

    /**
     * Checks if is name.
     *
     * @param name name
     * @return is name result
     */
    boolean isName(String name);

    /**
     * Checks if is name.
     *
     * @param nameCondition name condition
     * @return is name result
     */
    boolean isName(Predicate<String> nameCondition);

    /**
     * Checks if is named.
     *
     * @param name name
     * @return is named result
     */
    boolean isNamed(String name);

    /**
     * Affixes.
     *
     * @return affixes result
     */
    List<NBoundAffix> affixes();

    /**
     * Annotations.
     *
     * @return annotations result
     */
    List<NElementAnnotation> annotations();

    /**
     * Finds the find annotations.
     *
     * @param name name
     * @return find annotations result
     */
    List<NElementAnnotation> findAnnotations(String name);

    /**
     * Checks if is annotated.
     *
     * @param name name
     * @return is annotated result
     */
    boolean isAnnotated(String name);

    /**
     * convert this element to {@link NPrimitiveElement} or throw
     * ClassCastException
     *
     * @return {@link NPrimitiveElement}
     */
    NOptional<NPrimitiveElement> asPrimitive();

    /**
     * Resolve.
     *
     * @param pattern pattern
     * @return resolve result
     */
    NOptional<NElement> resolve(String pattern);

    /**
     * Resolve all.
     *
     * @param pattern pattern
     * @return resolve all result
     */
    List<NElement> resolveAll(String pattern);

    /**
     * cast this element to {@link NObjectElement} or throw
     * ClassCastException
     *
     * @return {@link NObjectElement}
     */
    NOptional<NObjectElement> asObject();

    /**
     * As element at.
     *
     * @param index index
     * @return as element at result
     */
    NOptional<NElement> asElementAt(int index);

    /**
     * As tuple.
     *
     * @return as tuple result
     */
    NOptional<NTupleElement> asTuple();

    /**
     * As pair.
     *
     * @return as pair result
     */
    NOptional<NPairElement> asPair();

    /**
     * As int.
     *
     * @return as int result
     */
    NOptional<NNumberElement> asInt();

    /**
     * cast this element to {@link NObjectElement} or throw
     * ClassCastException
     *
     * @return {@link NObjectElement}
     */
    NOptional<NListContainerElement> asListContainer();

    /**
     * As list or parametrized container.
     *
     * @return as list or parametrized container result
     */
    NOptional<NListOrParametrizedContainerElement> asListOrParametrizedContainer();

    /**
     * As parametrized container.
     *
     * @return as parametrized container result
     */
    NOptional<NParametrizedContainerElement> asParametrizedContainer();

    /**
     * As param object.
     *
     * @return as param object result
     */
    NOptional<NObjectElement> asParamObject();

    /**
     * As named object.
     *
     * @param name name
     * @return as named object result
     */
    NOptional<NObjectElement> asNamedObject(String name);

    /**
     * As full object.
     *
     * @param name name
     * @return as full object result
     */
    NOptional<NObjectElement> asFullObject(String name);

    /**
     * As named.
     *
     * @return as named result
     */
    NOptional<NNamedElement> asNamed();

    /**
     * Checks if is named array.
     *
     * @param name name
     * @return is named array result
     */
    boolean isNamedArray(String name);

    /**
     * As named array.
     *
     * @param name name
     * @return as named array result
     */
    NOptional<NObjectElement> asNamedArray(String name);

    /**
     * As full array.
     *
     * @param name name
     * @return as full array result
     */
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

    /**
     * Checks if is any string.
     *
     * @return is any string result
     */
    boolean isAnyString();

    /**
     * Checks if is any string or name.
     *
     * @return is any string or name result
     */
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

    /**
     * Checks if is fragment.
     *
     * @return is fragment result
     */
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

    /**
     * Builder.
     *
     * @return builder result
     */
    NElementBuilder builder();

    /**
     * Checks if is pair.
     *
     * @return is pair result
     */
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

    /**
     * Checks if is named pair.
     *
     * @param name name
     * @return is named pair result
     */
    boolean isNamedPair(String name);

    /**
     * Checks if is named pair.
     *
     * @param nameCondition name condition
     * @return is named pair result
     */
    boolean isNamedPair(Predicate<String> nameCondition);

    /**
     * best effort to convert to NListContainerElement
     *
     * @return NListContainerElement
     */
    NOptional<NListContainerElement> toListContainer();

    /**
     * Converts to many.
     *
     * @return to many result
     */
    List<NElement> toMany();

    /**
     * Comments.
     *
     * @return comments result
     */
    List<NElementComment> comments();

    /**
     * Checks if is name.
     *
     * @return is name result
     */
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

    /**
     * Converts to named object.
     *
     * @return to named object result
     */
    NOptional<NObjectElement> toNamedObject();

    /**
     * Converts to object.
     *
     * @return to object result
     */
    NOptional<NObjectElement> toObject();

    /**
     * Converts to named array.
     *
     * @return to named array result
     */
    NOptional<NArrayElement> toNamedArray();

    /**
     * Converts to named.
     *
     * @return to named result
     */
    NOptional<NNamedElement> toNamed();

    /**
     * Converts to array.
     *
     * @return to array result
     */
    NOptional<NArrayElement> toArray();

    /**
     * Wrap into array.
     *
     * @return wrap into array result
     */
    NArrayElement wrapIntoArray();

    /**
     * Wrap into object.
     *
     * @return wrap into object result
     */
    NObjectElement wrapIntoObject();

    /**
     * Wrap into tuple.
     *
     * @return wrap into tuple result
     */
    NTupleElement wrapIntoTuple();

    /**
     * Wrap into named array.
     *
     * @param name name
     * @return wrap into named array result
     */
    NArrayElement wrapIntoNamedArray(String name);

    /**
     * Wrap into named object.
     *
     * @param name name
     * @return wrap into named object result
     */
    NObjectElement wrapIntoNamedObject(String name);

    /**
     * Wrap into named tuple.
     *
     * @param name name
     * @return wrap into named tuple result
     */
    NTupleElement wrapIntoNamedTuple(String name);

    /**
     * Wrap into named pair.
     *
     * @param name name
     * @return wrap into named pair result
     */
    NPairElement wrapIntoNamedPair(String name);

    /**
     * As literal.
     *
     * @return as literal result
     */
    NLiteral asLiteral();

    /**
     * As string.
     *
     * @return as string result
     */
    NOptional<NStringElement> asString();

    /**
     * As string value.
     *
     * @return as string value result
     */
    NOptional<String> asStringValue();

    /**
     * As name value.
     *
     * @return as name value result
     */
    NOptional<String> asNameValue();

    /**
     * As local time value.
     *
     * @return as local time value result
     */
    NOptional<LocalTime> asLocalTimeValue();

    /**
     * As big int value.
     *
     * @return as big int value result
     */
    NOptional<BigInteger> asBigIntValue();

    /**
     * As big decimal value.
     *
     * @return as big decimal value result
     */
    NOptional<BigDecimal> asBigDecimalValue();

    /**
     * As boolean value.
     *
     * @return as boolean value result
     */
    NOptional<Boolean> asBooleanValue();

    /**
     * As number value.
     *
     * @return as number value result
     */
    NOptional<Number> asNumberValue();

    /**
     * As temporal value.
     *
     * @return as temporal value result
     */
    NOptional<Temporal> asTemporalValue();

    /**
     * As char value.
     *
     * @return as char value result
     */
    NOptional<Character> asCharValue();

    /**
     * As instant value.
     *
     * @return as instant value result
     */
    NOptional<Instant> asInstantValue();

    /**
     * As big complex value.
     *
     * @return as big complex value result
     */
    NOptional<NBigComplex> asBigComplexValue();

    /**
     * As double complex value.
     *
     * @return as double complex value result
     */
    NOptional<NDoubleComplex> asDoubleComplexValue();

    /**
     * As float complex value.
     *
     * @return as float complex value result
     */
    NOptional<NFloatComplex> asFloatComplexValue();

    /**
     * Checks if is flat expression.
     *
     * @return is flat expression result
     */
    boolean isFlatExpression();

    /**
     * As flat expression.
     *
     * @return as flat expression result
     */
    NOptional<NFlatExprElement> asFlatExpression();

    /**
     * As local date value.
     *
     * @return as local date value result
     */
    NOptional<LocalDate> asLocalDateValue();

    /**
     * As local date time value.
     *
     * @return as local date time value result
     */
    NOptional<LocalDateTime> asLocalDateTimeValue();

    /**
     * As byte value.
     *
     * @return as byte value result
     */
    NOptional<Byte> asByteValue();

    /**
     * As short value.
     *
     * @return as short value result
     */
    NOptional<Short> asShortValue();

    /**
     * As int value.
     *
     * @return as int value result
     */
    NOptional<Integer> asIntValue();

    /**
     * As long value.
     *
     * @return as long value result
     */
    NOptional<Long> asLongValue();

    /**
     * As float value.
     *
     * @return as float value result
     */
    NOptional<Float> asFloatValue();

    /**
     * As double value.
     *
     * @return as double value result
     */
    NOptional<Double> asDoubleValue();

    /**
     * As number.
     *
     * @return as number result
     */
    NOptional<NNumberElement> asNumber();

    /**
     * Checks if is any date.
     *
     * @return is any date result
     */
    boolean isAnyDate();

    /**
     * As operator.
     *
     * @return as operator result
     */
    NOptional<NOperatorElement> asOperator();

    /**
     * As operator symbol.
     *
     * @return as operator symbol result
     */
    NOptional<NOperatorSymbolElement> asOperatorSymbol();

    /**
     * As operator symbol.
     *
     * @param symbol symbol
     * @return as operator symbol result
     */
    NOptional<NOperatorSymbolElement> asOperatorSymbol(NOperatorSymbol symbol);

    /**
     * As binary operator.
     *
     * @param symbol symbol
     * @return as binary operator result
     */
    NOptional<NBinaryOperatorElement> asBinaryOperator(NOperatorSymbol symbol);

    /**
     * As binary infix operator.
     *
     * @param symbol symbol
     * @return as binary infix operator result
     */
    NOptional<NBinaryOperatorElement> asBinaryInfixOperator(NOperatorSymbol symbol);

    /**
     * As unary operator.
     *
     * @param symbol symbol
     * @return as unary operator result
     */
    NOptional<NUnaryOperatorElement> asUnaryOperator(NOperatorSymbol symbol);

    /**
     * As unary prefix operator.
     *
     * @param symbol symbol
     * @return as unary prefix operator result
     */
    NOptional<NUnaryOperatorElement> asUnaryPrefixOperator(NOperatorSymbol symbol);

    /**
     * As unary postfix operator.
     *
     * @param symbol symbol
     * @return as unary postfix operator result
     */
    NOptional<NUnaryOperatorElement> asUnaryPostfixOperator(NOperatorSymbol symbol);

    /**
     * As binary operator.
     *
     * @return as binary operator result
     */
    NOptional<NBinaryOperatorElement> asBinaryOperator();

    /**
     * As unary operator.
     *
     * @return as unary operator result
     */
    NOptional<NUnaryOperatorElement> asUnaryOperator();

    /**
     * Checks if is operator symbol.
     *
     * @return is operator symbol result
     */
    boolean isOperatorSymbol();

    /**
     * Checks if is operator symbol.
     *
     * @param symbol symbol
     * @return is operator symbol result
     */
    boolean isOperatorSymbol(NOperatorSymbol symbol);

    /**
     * Checks if is binary operator.
     *
     * @return is binary operator result
     */
    boolean isBinaryOperator();

    /**
     * Checks if is binary operator.
     *
     * @param type type
     * @return is binary operator result
     */
    boolean isBinaryOperator(NOperatorSymbol type);

    /**
     * Checks if is unary operator.
     *
     * @param type type
     * @return is unary operator result
     */
    boolean isUnaryOperator(NOperatorSymbol type);

    /**
     * Checks if is left named binary operator.
     *
     * @param type type
     * @return is left named binary operator result
     */
    boolean isLeftNamedBinaryOperator(NOperatorSymbol type);

    /**
     * Checks if is left named binary operator.
     *
     * @param type type
     * @param name name
     * @return is left named binary operator result
     */
    boolean isLeftNamedBinaryOperator(NOperatorSymbol type, String name);

    /**
     * Checks if is any operator.
     *
     * @return is any operator result
     */
    boolean isAnyOperator();

    /**
     * Checks if is binary infix operator.
     *
     * @return is binary infix operator result
     */
    boolean isBinaryInfixOperator();

    /**
     * Checks if is binary infix operator.
     *
     * @param symbol symbol
     * @return is binary infix operator result
     */
    boolean isBinaryInfixOperator(NOperatorSymbol symbol);

    /**
     * Checks if is unary operator.
     *
     * @return is unary operator result
     */
    boolean isUnaryOperator();

    /**
     * Checks if is unary prefix operator.
     *
     * @return is unary prefix operator result
     */
    boolean isUnaryPrefixOperator();

    /**
     * Checks if is unary prefix operator.
     *
     * @param symbol symbol
     * @return is unary prefix operator result
     */
    boolean isUnaryPrefixOperator(NOperatorSymbol symbol);

    /**
     * Checks if is unary postfix operator.
     *
     * @param symbol symbol
     * @return is unary postfix operator result
     */
    boolean isUnaryPostfixOperator(NOperatorSymbol symbol);

    /**
     * As number type.
     *
     * @param elemType elem type
     * @return as number type result
     */
    NOptional<NElement> asNumberType(NElementType elemType);

    /**
     * Transform.
     *
     * @param transform transform
     * @return transform result
     */
    List<NElement> transform(NElementTransform transform);

    /**
     * Transform.
     *
     * @param context context
     * @param transform transform
     * @return transform result
     */
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

    /**
     * Metadata.
     *
     * @return metadata result
     */
    NElementMetadata metadata();

    /**
     * Snippet.
     *
     * @return snippet result
     */
    String snippet();

    /**
     * Snippet.
     *
     * @param size size
     * @return snippet result
     */
    String snippet(int size);

    /**
     * As named pair.
     *
     * @return as named pair result
     */
    NOptional<NPairElement> asNamedPair();

    /**
     * As named pair.
     *
     * @param name name
     * @return as named pair result
     */
    NOptional<NPairElement> asNamedPair(String name);

    /**
     * As simple pair.
     *
     * @return as simple pair result
     */
    NOptional<NPairElement> asSimplePair();

    /**
     * Checks if is list.
     *
     * @return is list result
     */
    boolean isList();

    /**
     * As list.
     *
     * @return as list result
     */
    NOptional<NListElement> asList();

    /**
     * As fragment.
     *
     * @return as fragment result
     */
    NOptional<NFragmentElement> asFragment();

    /**
     * Checks if is ordered list.
     *
     * @return is ordered list result
     */
    boolean isOrderedList();

    /**
     * As ordered list.
     *
     * @return as ordered list result
     */
    NOptional<NListElement> asOrderedList();

    /**
     * Checks if is unordered list.
     *
     * @return is unordered list result
     */
    boolean isUnorderedList();

    /**
     * As unordered list.
     *
     * @return as unordered list result
     */
    NOptional<NListElement> asUnorderedList();

    /**
     * As name.
     *
     * @return as name result
     */
    NOptional<NStringElement> asName();

    /**
     * Converts to name.
     *
     * @return to name result
     */
    NOptional<NStringElement> toName();

    /**
     * As ternary operator.
     *
     * @return as ternary operator result
     */
    NOptional<NTernaryOperatorElement> asTernaryOperator();

    /**
     * As nary operator.
     *
     * @return as nary operator result
     */
    NOptional<NAryOperatorElement> asNaryOperator();

    /**
     * Filter.
     *
     * @param selector selector
     * @return filter result
     */
    List<NElement> filter(NElementSelector selector);

    /**
     * Filter.
     *
     * @param selector selector
     * @return filter result
     */
    List<NElement> filter(String selector);

    /**
     * Converts to pretty string.
     *
     * @return to pretty string result
     */
    String toPrettyString();

    /**
     * Converts to stable string.
     *
     * @return to stable string result
     */
    String toStableString();

    /**
     * Converts to verbatim string.
     *
     * @return to verbatim string result
     */
    String toVerbatimString();

    /**
     * Converts to compact string.
     *
     * @return to compact string result
     */
    String toCompactString();

    /**
     * Converts to formatted string.
     *
     * @param formatter formatter
     * @return to formatted string result
     */
    String toFormattedString(NElementFormatter formatter);

    /**
     * Converts to formatted string.
     *
     * @param contentType content type
     * @param formatter formatter
     * @return to formatted string result
     */
    String toFormattedString(NContentType contentType, NElementFormatter formatter);

    /**
     * As binary stream.
     *
     * @return as binary stream result
     */
    NOptional<NBinaryStreamElement> asBinaryStream();

    /**
     * Checks if is binary stream.
     *
     * @return is binary stream result
     */
    boolean isBinaryStream();

    /**
     * As char stream.
     *
     * @return as char stream result
     */
    NOptional<NCharStreamElement> asCharStream();

    /**
     * Checks if is char stream.
     *
     * @return is char stream result
     */
    boolean isCharStream();

    /**
     * Normalize.
     *
     * @param contentType content type
     * @return normalize result
     */
    NElement normalize(NContentType contentType);

    /**
     * Convert to.
     *
     * @param to to
     * @return convert to result
     */
    <T> T convertTo(Class<T> to);

}
