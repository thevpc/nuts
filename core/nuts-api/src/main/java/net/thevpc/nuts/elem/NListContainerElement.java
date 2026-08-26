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

import net.thevpc.nuts.util.NOptional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Object implementation of Nuts Element type. Nuts Element types are generic
 * JSON like parsable objects.
 *
 * @author thevpc
 * @app.category Elements
 * @since 0.5.6
 */
public interface NListContainerElement extends NElement, NListOrParametrizedContainerElement {

    /**
     * return value for name or null. If multiple values are available return
     * any of them.
     *
     * @param key key name
     * @return value for name or null
     */
    NOptional<NElement> get(String key);

    /**
     * Returns the at.
     *
     * @param index index
     * @return get at result
     */
    NOptional<NElement> getAt(int index);

    /**
     * Returns the get.
     *
     * @param index index
     * @return get result
     */
    NOptional<NElement> get(int index);

    /**
     * Returns the all.
     *
     * @param key key
     * @return get all result
     */
    List<NElement> getAll(String key);

    /**
     * Returns the by path.
     *
     * @param keys keys
     * @return get by path result
     */
    NOptional<NElement> getByPath(String... keys);

    /**
     * Returns the array by path.
     *
     * @param keys keys
     * @return get array by path result
     */
    NOptional<NArrayElement> getArrayByPath(String... keys);

    /**
     * Returns the object by path.
     *
     * @param keys keys
     * @return get object by path result
     */
    NOptional<NObjectElement> getObjectByPath(String... keys);

    /**
     * Returns the boolean value by path.
     *
     * @param keys keys
     * @return get boolean value by path result
     */
    NOptional<Boolean> getBooleanValueByPath(String... keys);

    /**
     * Returns the byte value by path.
     *
     * @param keys keys
     * @return get byte value by path result
     */
    NOptional<Byte> getByteValueByPath(String... keys);

    /**
     * Returns the short value by path.
     *
     * @param keys keys
     * @return get short value by path result
     */
    NOptional<Short> getShortValueByPath(String... keys);

    /**
     * Returns the int value by path.
     *
     * @param keys keys
     * @return get int value by path result
     */
    NOptional<Integer> getIntValueByPath(String... keys);

    /**
     * Returns the long value by path.
     *
     * @param keys keys
     * @return get long value by path result
     */
    NOptional<Long> getLongValueByPath(String... keys);

    /**
     * Returns the float value by path.
     *
     * @param keys keys
     * @return get float value by path result
     */
    NOptional<Float> getFloatValueByPath(String... keys);

    /**
     * Returns the double value by path.
     *
     * @param keys keys
     * @return get double value by path result
     */
    NOptional<Double> getDoubleValueByPath(String... keys);

    /**
     * Returns the instant value by path.
     *
     * @param keys keys
     * @return get instant value by path result
     */
    NOptional<Instant> getInstantValueByPath(String... keys);

    /**
     * Returns the local date value by path.
     *
     * @param keys keys
     * @return get local date value by path result
     */
    NOptional<LocalDate> getLocalDateValueByPath(String... keys);

    /**
     * Returns the local date time value by path.
     *
     * @param keys keys
     * @return get local date time value by path result
     */
    NOptional<LocalDateTime> getLocalDateTimeValueByPath(String... keys);

    /**
     * Returns the list container by path.
     *
     * @param keys keys
     * @return get list container by path result
     */
    NOptional<NListContainerElement> getListContainerByPath(String... keys);

    /**
     * Returns the get.
     *
     * @param key key
     * @return get result
     */
    NOptional<NElement> get(NElement key);

    /**
     * Returns the all.
     *
     * @param s s
     * @return get all result
     */
    List<NElement> getAll(NElement s);

    /**
     * Returns the string value.
     *
     * @param index index
     * @return get string value result
     */
    NOptional<String> getStringValue(int index);

    /**
     * Returns the local time value.
     *
     * @param index index
     * @return get local time value result
     */
    NOptional<LocalTime> getLocalTimeValue(int index);

    /**
     * Returns the array.
     *
     * @param index index
     * @return get array result
     */
    NOptional<NArrayElement> getArray(int index);

    /**
     * Returns the object.
     *
     * @param index index
     * @return get object result
     */
    NOptional<NObjectElement> getObject(int index);

    /**
     * Returns the boolean value.
     *
     * @param index index
     * @return get boolean value result
     */
    NOptional<Boolean> getBooleanValue(int index);

    /**
     * Returns the byte value.
     *
     * @param index index
     * @return get byte value result
     */
    NOptional<Byte> getByteValue(int index);

    /**
     * Returns the short value.
     *
     * @param index index
     * @return get short value result
     */
    NOptional<Short> getShortValue(int index);

    /**
     * Returns the int value.
     *
     * @param index index
     * @return get int value result
     */
    NOptional<Integer> getIntValue(int index);

    /**
     * Returns the long value.
     *
     * @param index index
     * @return get long value result
     */
    NOptional<Long> getLongValue(int index);

    /**
     * Returns the float value.
     *
     * @param index index
     * @return get float value result
     */
    NOptional<Float> getFloatValue(int index);

    /**
     * Returns the double value.
     *
     * @param index index
     * @return get double value result
     */
    NOptional<Double> getDoubleValue(int index);

    /**
     * Returns the instant value.
     *
     * @param index index
     * @return get instant value result
     */
    NOptional<Instant> getInstantValue(int index);

    /**
     * Returns the local date value.
     *
     * @param index index
     * @return get local date value result
     */
    NOptional<LocalDate> getLocalDateValue(int index);

    /**
     * Returns the local date time value.
     *
     * @param index index
     * @return get local date time value result
     */
    NOptional<LocalDateTime> getLocalDateTimeValue(int index);

    /**
     * Returns the string value by path.
     *
     * @param keys keys
     * @return get string value by path result
     */
    NOptional<String> getStringValueByPath(String... keys);

    /**
     * Returns the local time value by path.
     *
     * @param keys keys
     * @return get local time value by path result
     */
    NOptional<LocalTime> getLocalTimeValueByPath(String... keys);

    /**
     * Returns the array.
     *
     * @param key key
     * @return get array result
     */
    NOptional<NArrayElement> getArray(String key);

    /**
     * Returns the array.
     *
     * @param key key
     * @return get array result
     */
    NOptional<NArrayElement> getArray(NElement key);

    /**
     * Returns the object.
     *
     * @param key key
     * @return get object result
     */
    NOptional<NObjectElement> getObject(String key);

    /**
     * Returns the object.
     *
     * @param key key
     * @return get object result
     */
    NOptional<NObjectElement> getObject(NElement key);

    /**
     * Returns the list container.
     *
     * @param key key
     * @return get list container result
     */
    NOptional<NListContainerElement> getListContainer(String key);

    /**
     * Returns the list container.
     *
     * @param key key
     * @return get list container result
     */
    NOptional<NListContainerElement> getListContainer(NElement key);

    /**
     * Returns the string value.
     *
     * @param key key
     * @return get string value result
     */
    NOptional<String> getStringValue(String key);

    /**
     * Returns the string value.
     *
     * @param key key
     * @return get string value result
     */
    NOptional<String> getStringValue(NElement key);

    /**
     * Returns the boolean value.
     *
     * @param key key
     * @return get boolean value result
     */
    NOptional<Boolean> getBooleanValue(String key);

    /**
     * Returns the boolean value.
     *
     * @param key key
     * @return get boolean value result
     */
    NOptional<Boolean> getBooleanValue(NElement key);

    /**
     * Returns the number.
     *
     * @param key key
     * @return get number result
     */
    NOptional<Number> getNumber(String key);

    /**
     * Returns the number.
     *
     * @param key key
     * @return get number result
     */
    NOptional<Number> getNumber(NElement key);

    /**
     * Returns the byte value.
     *
     * @param key key
     * @return get byte value result
     */
    NOptional<Byte> getByteValue(String key);

    /**
     * Returns the byte value.
     *
     * @param key key
     * @return get byte value result
     */
    NOptional<Byte> getByteValue(NElement key);

    /**
     * Returns the int value.
     *
     * @param key key
     * @return get int value result
     */
    NOptional<Integer> getIntValue(String key);

    /**
     * Returns the int value.
     *
     * @param key key
     * @return get int value result
     */
    NOptional<Integer> getIntValue(NElement key);

    /**
     * Returns the long value.
     *
     * @param key key
     * @return get long value result
     */
    NOptional<Long> getLongValue(String key);

    /**
     * Returns the long value.
     *
     * @param key key
     * @return get long value result
     */
    NOptional<Long> getLongValue(NElement key);

    /**
     * Returns the short value.
     *
     * @param key key
     * @return get short value result
     */
    NOptional<Short> getShortValue(String key);

    /**
     * Returns the short value.
     *
     * @param key key
     * @return get short value result
     */
    NOptional<Short> getShortValue(NElement key);

    /**
     * Returns the instant value.
     *
     * @param key key
     * @return get instant value result
     */
    NOptional<Instant> getInstantValue(String key);

    /**
     * Returns the local date value.
     *
     * @param key key
     * @return get local date value result
     */
    NOptional<LocalDate> getLocalDateValue(String key);

    /**
     * Returns the local date time value.
     *
     * @param key key
     * @return get local date time value result
     */
    NOptional<LocalDateTime> getLocalDateTimeValue(String key);

    /**
     * Returns the local time value.
     *
     * @param key key
     * @return get local time value result
     */
    NOptional<LocalTime> getLocalTimeValue(String key);

    /**
     * Returns the instant value.
     *
     * @param key key
     * @return get instant value result
     */
    NOptional<Instant> getInstantValue(NElement key);

    /**
     * Returns the local date value.
     *
     * @param key key
     * @return get local date value result
     */
    NOptional<LocalDate> getLocalDateValue(NElement key);

    /**
     * Returns the local date time value.
     *
     * @param key key
     * @return get local date time value result
     */
    NOptional<LocalDateTime> getLocalDateTimeValue(NElement key);

    /**
     * Returns the local time value.
     *
     * @param key key
     * @return get local time value result
     */
    NOptional<LocalTime> getLocalTimeValue(NElement key);

    /**
     * Returns the float value.
     *
     * @param key key
     * @return get float value result
     */
    NOptional<Float> getFloatValue(String key);

    /**
     * Returns the float value.
     *
     * @param key key
     * @return get float value result
     */
    NOptional<Float> getFloatValue(NElement key);

    /**
     * Returns the double value.
     *
     * @param key key
     * @return get double value result
     */
    NOptional<Double> getDoubleValue(String key);

    /**
     * Returns the double value.
     *
     * @param key key
     * @return get double value result
     */
    NOptional<Double> getDoubleValue(NElement key);

    /**
     * Returns the big int value.
     *
     * @param key key
     * @return get big int value result
     */
    NOptional<BigInteger> getBigIntValue(NElement key);

    /**
     * Returns the big decimal value.
     *
     * @param key key
     * @return get big decimal value result
     */
    NOptional<BigDecimal> getBigDecimalValue(NElement key);

    /**
     * object (key,value) attributes
     *
     * @return object attributes
     */
    List<NElement> children();

    /**
     * element count
     *
     * @return element count
     */
    int size();

    /**
     * Pairs.
     *
     * @return pairs result
     */
    List<NPairElement> pairs();
    /**
     * Named pairs.
     *
     * @return named pairs result
     */
    List<NPairElement> namedPairs();

}
