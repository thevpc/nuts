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
package net.thevpc.nuts.elem;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.io.NReaderProvider;
import net.thevpc.nuts.spi.NComponent;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * Class responsible of creating {@link NElement} type. It help parsing
 * from, converting to and formatting such types.
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
public interface NElementFactory extends NComponent {

    static NElementFactory of() {
        return NExtensions.of(NElementFactory.class);
    }

    NPairElement ofPair(NElement key, NElement value);

    NPairElement ofPair(String key, NElement value);

    NPairElement ofPair(String key, Boolean value);

    NPairElement ofPair(String key, Byte value);

    NPairElement ofPair(String key, Short value);

    NPairElement ofPair(String key, Integer value);

    NPairElement ofPair(String key, Long value);

    NPairElement ofPair(String key, String value);

    NPairElement ofPair(String key, Double value);

    NPairElement ofPair(String key, Instant value);

    NPairElement ofPair(String key, LocalDate value);

    NPairElement ofPair(String key, LocalDateTime value);

    NPairElement ofPair(String key, LocalTime value);

    NPairElementBuilder ofPairBuilder(NElement key, NElement value);

    NPairElementBuilder ofPairBuilder();

    /**
     * create object element builder (mutable)
     *
     * @return object element
     */
    NObjectElementBuilder ofObjectBuilder();

    NObjectElementBuilder ofObjectBuilder(String name);

    /**
     * create array element builder (mutable)
     *
     * @return array element
     */
    NArrayElementBuilder ofArrayBuilder();

    NArrayElementBuilder ofArrayBuilder(String name);

    NArrayElement ofArray();

    NObjectElement ofObject();

    //    public NutsPrimitiveElement forNutsString(NutsString str) {
    //        return str == null ? DefaultNPrimitiveElementBuilder.NULL : new DefaultNPrimitiveElement(NutsElementType.NUTS_STRING, str);
    //    }
    NPrimitiveElement ofBoolean(String value);

    NPrimitiveElement ofBoolean(boolean value);

    NPrimitiveElement ofRegex(String value);

    NPrimitiveElement ofName(String value);

    NPrimitiveElement ofNameOrString(String value);

    <T extends Enum<T>> NPrimitiveElement ofEnum(Enum<T> value);

    NPrimitiveElement ofString(String str);

    NPrimitiveElement ofString(String str, NElementType stringLayout);

    NCustomElement ofCustom(Object object);

    NPrimitiveElement ofTrue();

    NPrimitiveElement ofFalse();

    NPrimitiveElement ofInstant(Instant instant);

    NPrimitiveElement ofLocalDate(LocalDate localDate);

    NPrimitiveElement ofLocalDateTime(LocalDateTime localDateTime);

    NPrimitiveElement ofLocalTime(LocalTime localTime);

    NPrimitiveElement ofFloat(Float value);

    NPrimitiveElement ofFloat(float value);

    NPrimitiveElement ofFloat(Float value, String suffix);

    NPrimitiveElement ofFloat(float value, String suffix);

    NPrimitiveElement ofByte(Byte value);

    NPrimitiveElement ofByte(byte value);

    NPrimitiveElement ofByte(Byte value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofByte(byte value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofByte(Byte value, NNumberLayout layout);

    NPrimitiveElement ofByte(byte value, NNumberLayout layout);

    NPrimitiveElement ofByte(Byte value, String suffix);

    NPrimitiveElement ofByte(byte value, String suffix);

    NPrimitiveElement ofShort(Short value);

    NPrimitiveElement ofShort(short value);

    NPrimitiveElement ofShort(Short value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofShort(short value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofShort(Short value, NNumberLayout layout);

    NPrimitiveElement ofShort(short value, NNumberLayout layout);

    NPrimitiveElement ofShort(Short value, String suffix);

    NPrimitiveElement ofShort(short value, String suffix);


    NPrimitiveElement ofInt(Integer value);

    NPrimitiveElement ofInt(int value);

    NPrimitiveElement ofInt(Integer value, String suffix);

    NPrimitiveElement ofInt(int value, String suffix);

    NPrimitiveElement ofInt(Integer value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofInt(int value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofInt(Integer value, NNumberLayout layout);

    NPrimitiveElement ofInt(int value, NNumberLayout layout);

    NPrimitiveElement ofLong(Long value);

    NPrimitiveElement ofLong(long value);

    NPrimitiveElement ofLong(Long value, String suffix);

    NPrimitiveElement ofLong(long value, String suffix);

    NPrimitiveElement ofLong(Long value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofLong(long value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofLong(Long value, NNumberLayout layout);

    NPrimitiveElement ofLong(long value, NNumberLayout layout);


    NPrimitiveElement ofNull();

    NPrimitiveElement ofNumber(String value);

    NPrimitiveElement ofInstant(Date value);

    NPrimitiveElement ofInstant(String value);

    NPrimitiveElement ofChar(Character value);

    NPrimitiveElement ofDouble(Double value);

    NPrimitiveElement ofDouble(double value);

    NPrimitiveElement ofDouble(Double value, String suffix);

    NPrimitiveElement ofDouble(double value, String suffix);

    NPrimitiveElement ofDoubleComplex(double real);

    NPrimitiveElement ofDoubleComplex(double real, double imag);

    NPrimitiveElement ofFloatComplex(float real);

    NPrimitiveElement ofFloatComplex(float real, float imag);

    NPrimitiveElement ofBigComplex(BigDecimal real);

    NPrimitiveElement ofBigComplex(BigDecimal real, BigDecimal imag);


    NPrimitiveElement ofNumber(Number value);

    NPrimitiveElement ofBigDecimal(BigDecimal value);

    NPrimitiveElement ofBigDecimal(BigDecimal value, String suffix);

    NPrimitiveElement ofBigInt(BigInteger value);

    NPrimitiveElement ofBigInt(BigInteger value, NNumberLayout layout, String suffix);

    NPrimitiveElement ofBigInt(BigInteger value, NNumberLayout layout);

    NPrimitiveElement ofBigInt(BigInteger value, String suffix);

    NUpletElementBuilder ofUpletBuilder();

    NUpletElementBuilder ofUpletBuilder(String name);

    NUpletElement ofUplet();

    NUpletElement ofUplet(NElement... items);

    NUpletElement ofUplet(String name, NElement... items);

    NUpletElement ofNamedUplet(String name, NElement... items);

    NMatrixElementBuilder ofMatrixBuilder();

    NArrayElement ofIntArray(int... items);

    NArrayElement ofIntArray(Integer... items);

    NArrayElement ofLongArray(long... items);

    NArrayElement ofLongArray(Long... items);

    NArrayElement ofNumberArray(Number... items);

    NArrayElement ofBooleanArray(boolean... items);

    NArrayElement ofBooleanArray(Boolean... items);

    NArrayElement ofArray(NElement... items);

    NArrayElement ofArray(String name, NElement... items);

    NArrayElement ofNamedArray(String name, NElement... items);

    NArrayElement ofNamedParametrizedArray(String name, NElement[] params, NElement... items);

    NArrayElement ofArray(String name, NElement[] params, NElement... items);

    NArrayElement ofParametrizedArray(NElement[] params, NElement... items);

    NArrayElement ofParametrizedArray(NElement... params);

    NArrayElement ofParametrizedArray(String name, NElement[] params, NElement... items);

    NArrayElement ofParametrizedArray(String name, NElement... params);

    NArrayElement ofStringArray(String... items);

    NArrayElement ofDoubleArray(double... items);

    NArrayElement ofDoubleArray(Double... items);

    NObjectElement ofObject(NElement... items);

    NObjectElement ofObject(String name, NElement... items);

    NObjectElement ofNamedObject(String name, NElement... items);

    NObjectElement ofNamedParametrizedObject(String name, NElement[] params, NElement... items);

    NObjectElement ofParametrizedObject(NElement[] params, NElement... items);

    NObjectElement ofParametrizedObject(NElement... params);

    NObjectElement ofParametrizedObject(String name, NElement[] params, NElement... items);

    NObjectElement ofObject(String name, NElement[] params, NElement... items);

    NObjectElement ofParametrizedObject(String name, NElement... params);

    NElementComments ofMultiLineComments(String... a);

    NElementComments ofSingleLineComments(String... a);

    NElementComments ofComments(NElementComment[] leading, NElementComment[] trailing);

    NElementComment ofMultiLineComment(String... a);

    NElementComment ofSingleLineComment(String... lines);

    NElement ofBinaryStream(NInputStreamProvider value);

    NElement ofCharStream(NReaderProvider value);

    NBinaryStreamElementBuilder ofBinaryStreamBuilder();

    NCharStreamElementBuilder ofCharStreamBuilder();

    NElementAnnotation ofAnnotation(String name, NElement... values);

    NPrimitiveElementBuilder ofPrimitiveBuilder();
}
