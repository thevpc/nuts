/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
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
import java.util.Collection;
import java.util.List;

/**
 * Object implementation of Nuts Element type. Nuts Element types are generic
 * JSON like parsable objects.
 *
 * @author thevpc
 * @app.category Elements
 * @since 0.5.6
 */
public interface NNavigatableElement extends NElement {

    /**
     * return value for name or null. If multiple values are available return
     * any of them.
     *
     * @param key key name
     * @return value for name or null
     */
    NOptional<NElement> get(String key);

    NOptional<String> getStringByPath(String... keys);

    NOptional<Integer> getIntByPath(String... keys);

    NOptional<Long> getLongByPath(String... keys);

    NOptional<Float> getFloatByPath(String... keys);

    NOptional<Double> getDoubleByPath(String... keys);

    NOptional<Boolean> getBooleanByPath(String... keys);

    NOptional<Byte> getByteByPath(String... keys);

    NOptional<Short> getShortByPath(String... keys);

    NOptional<Instant> getInstantByPath(String... keys);

    NOptional<BigInteger> getBigIntByPath(String... keys);

    NOptional<BigDecimal> getBigDecimalByPath(String... keys);

    NOptional<Number> getNumberByPath(String... keys);

    NOptional<NElement> getByPath(String... keys);
    NOptional<NArrayElement> getArrayByPath(String... keys);
    NOptional<NObjectElement> getObjectByPath(String... keys);
    NOptional<NNavigatableElement> getNavigatableByPath(String... keys);

    NOptional<NElement> get(NElement key);
    List<NElement> getAll(NElement s);

    NOptional<NArrayElement> getArray(String key);

    NOptional<NArrayElement> getArray(NElement key);

    NOptional<NObjectElement> getObject(String key);

    NOptional<NObjectElement> getObject(NElement key);

    NOptional<NNavigatableElement> getNavigatable(String key);

    NOptional<NNavigatableElement> getNavigatable(NElement key);

    NOptional<String> getString(String key);

    NOptional<String> getString(NElement key);

    NOptional<Boolean> getBoolean(String key);

    NOptional<Boolean> getBoolean(NElement key);

    NOptional<Number> getNumber(String key);

    NOptional<Number> getNumber(NElement key);

    NOptional<Byte> getByte(String key);

    NOptional<Byte> getByte(NElement key);

    NOptional<Integer> getInt(String key);

    NOptional<Integer> getInt(NElement key);

    NOptional<Long> getLong(String key);

    NOptional<Long> getLong(NElement key);

    NOptional<Short> getShort(String key);

    NOptional<Short> getShort(NElement key);

    NOptional<Instant> getInstant(String key);

    NOptional<Instant> getInstant(NElement key);

    NOptional<Float> getFloat(String key);

    NOptional<Float> getFloat(NElement key);

    NOptional<Double> getDouble(String key);

    NOptional<Double> getDouble(NElement key);

    NOptional<BigInteger> getBigInt(NElement key);

    NOptional<BigDecimal> getBigDecimal(NElement key);

    /**
     * object (key,value) attributes
     *
     * @return object attributes
     */
    Collection<NElementEntry> entries();

    /**
     * element count
     *
     * @return element count
     */
    int size();
}
