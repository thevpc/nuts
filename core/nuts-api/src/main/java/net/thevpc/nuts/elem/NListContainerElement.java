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
public interface NListContainerElement extends NElement {

    /**
     * return value for name or null. If multiple values are available return
     * any of them.
     *
     * @param key key name
     * @return value for name or null
     */
    NOptional<NElement> get(String key);

    NOptional<NElement> getAt(int index);
    NOptional<NElement> get(int index);

    List<NElement> getAll(String key);

    NOptional<NElement> getByPath(String... keys);

    NOptional<NArrayElement> getArrayByPath(String... keys);

    NOptional<NObjectElement> getObjectByPath(String... keys);

    NOptional<Boolean> getBooleanValueByPath(String... keys);

    NOptional<Byte> getByteValueByPath(String... keys);

    NOptional<Short> getShortValueByPath(String... keys);

    NOptional<Integer> getIntValueByPath(String... keys);

    NOptional<Long> getLongValueByPath(String... keys);

    NOptional<Float> getFloatValueByPath(String... keys);

    NOptional<Double> getDoubleValueByPath(String... keys);

    NOptional<Instant> getInstantValueByPath(String... keys);

    NOptional<LocalDate> getLocalDateValueByPath(String... keys);

    NOptional<LocalDateTime> getLocalDateTimeValueByPath(String... keys);

    NOptional<NListContainerElement> getListContainerByPath(String... keys);

    NOptional<NElement> get(NElement key);

    List<NElement> getAll(NElement s);

    NOptional<String> getStringValue(int index);

    NOptional<LocalTime> getLocalTimeValue(int index);

    NOptional<NArrayElement> getArray(int index);

    NOptional<NObjectElement> getObject(int index);

    NOptional<Boolean> getBooleanValue(int index);

    NOptional<Byte> getByteValue(int index);

    NOptional<Short> getShortValue(int index);

    NOptional<Integer> getIntValue(int index);

    NOptional<Long> getLongValue(int index);

    NOptional<Float> getFloatValue(int index);

    NOptional<Double> getDoubleValue(int index);

    NOptional<Instant> getInstantValue(int index);

    NOptional<LocalDate> getLocalDateValue(int index);

    NOptional<LocalDateTime> getLocalDateTimeValue(int index);

    NOptional<String> getStringValueByPath(String... keys);

    NOptional<LocalTime> getLocalTimeValueByPath(String... keys);

    NOptional<NArrayElement> getArray(String key);

    NOptional<NArrayElement> getArray(NElement key);

    NOptional<NObjectElement> getObject(String key);

    NOptional<NObjectElement> getObject(NElement key);

    NOptional<NListContainerElement> getListContainer(String key);

    NOptional<NListContainerElement> getListContainer(NElement key);

    NOptional<String> getStringValue(String key);

    NOptional<String> getStringValue(NElement key);

    NOptional<Boolean> getBooleanValue(String key);

    NOptional<Boolean> getBooleanValue(NElement key);

    NOptional<Number> getNumber(String key);

    NOptional<Number> getNumber(NElement key);

    NOptional<Byte> getByteValue(String key);

    NOptional<Byte> getByteValue(NElement key);

    NOptional<Integer> getIntValue(String key);

    NOptional<Integer> getIntValue(NElement key);

    NOptional<Long> getLongValue(String key);

    NOptional<Long> getLongValue(NElement key);

    NOptional<Short> getShortValue(String key);

    NOptional<Short> getShortValue(NElement key);

    NOptional<Instant> getInstantValue(String key);

    NOptional<LocalDate> getLocalDateValue(String key);

    NOptional<LocalDateTime> getLocalDateTimeValue(String key);

    NOptional<LocalTime> getLocalTimeValue(String key);

    NOptional<Instant> getInstantValue(NElement key);

    NOptional<LocalDate> getLocalDateValue(NElement key);

    NOptional<LocalDateTime> getLocalDateTimeValue(NElement key);

    NOptional<LocalTime> getLocalTimeValue(NElement key);

    NOptional<Float> getFloatValue(String key);

    NOptional<Float> getFloatValue(NElement key);

    NOptional<Double> getDoubleValue(String key);

    NOptional<Double> getDoubleValue(NElement key);

    NOptional<BigInteger> getBigIntValue(NElement key);

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


}
