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
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.util;

import java.util.function.Supplier;

/**
 * NRef interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NRef<T> extends Supplier<T> {

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static <T> NRef<T> of() {
        return new NObjectRef<>(null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param t t
     * @return of result
     */
    static <T> NRef<T> of(T t) {
        return new NObjectRef<>(t);
    }

    /**
     * Creates a new instance of of.
     *
     * @param t t
     * @param type type
     * @return of result
     */
    static <T> NRef<T> of(T t, Class<T> type) {
        return new NObjectRef<>(t);
    }

    /**
     * Creates a new instance of of null.
     *
     * @param t t
     * @return of null result
     */
    static <T> NRef<T> ofNull(Class<T> t) {
        return new NObjectRef<>(null);
    }

    /**
     * Creates a new instance of of null.
     *
     * @return of null result
     */
    static <T> NRef<T> ofNull() {
        /**
         * Creates a new instance of of.
         *
         * @param null null
         * @return of result
         */
        return of(null);
    }

    /**
     * Creates a new instance of of long.
     *
     * @param value value
     * @return of long result
     */
    static NLongRef ofLong(Long value) {
        return new NLongRef(value);
    }

    /**
     * Creates a new instance of of long.
     *
     * @param value value
     * @return of long result
     */
    static NLongRef ofLong(long value) {
        return new NLongRef(value);
    }

    /**
     * Creates a new instance of of long.
     *
     * @return of long result
     */
    static NLongRef ofLong() {
        return new NLongRef(null);
    }

    /**
     * Creates a new instance of of int.
     *
     * @param value value
     * @return of int result
     */
    static NIntRef ofInt(Integer value) {
        return new NIntRef(value);
    }

    /**
     * Creates a new instance of of int.
     *
     * @param value value
     * @return of int result
     */
    static NIntRef ofInt(int value) {
        return new NIntRef(value);
    }

    /**
     * Creates a new instance of of int.
     *
     * @return of int result
     */
    static NIntRef ofInt() {
        return new NIntRef(null);
    }

    /**
     * Creates a new instance of of false.
     *
     * @return of false result
     */
    static NBooleanRef ofFalse() {
        return new NBooleanRef(false);
    }

    /**
     * Creates a new instance of of true.
     *
     * @return of true result
     */
    static NBooleanRef ofTrue() {
        return new NBooleanRef(true);
    }

    /**
     * Creates a new instance of of boolean.
     *
     * @param value value
     * @return of boolean result
     */
    static NBooleanRef ofBoolean(Boolean value) {
        return new NBooleanRef(value);
    }

    /**
     * Creates a new instance of of boolean.
     *
     * @param value value
     * @return of boolean result
     */
    static NBooleanRef ofBoolean(boolean value) {
        return new NBooleanRef(value);
    }

    /**
     * Creates a new instance of of boolean.
     *
     * @return of boolean result
     */
    static NBooleanRef ofBoolean() {
        return new NBooleanRef(null);
    }

    /**
     * Creates a new instance of of byte.
     *
     * @param value value
     * @return of byte result
     */
    static NByteRef ofByte(Byte value) {
        return new NByteRef(value);
    }

    /**
     * Creates a new instance of of byte.
     *
     * @param value value
     * @return of byte result
     */
    static NByteRef ofByte(byte value) {
        return new NByteRef(value);
    }

    /**
     * Creates a new instance of of byte.
     *
     * @return of byte result
     */
    static NByteRef ofByte() {
        return new NByteRef(null);
    }

    /**
     * Creates a new instance of of short.
     *
     * @param value value
     * @return of short result
     */
    static NShortRef ofShort(Short value) {
        return new NShortRef(value);
    }

    /**
     * Creates a new instance of of short.
     *
     * @param value value
     * @return of short result
     */
    static NShortRef ofShort(short value) {
        return new NShortRef(value);
    }

    /**
     * Creates a new instance of of short.
     *
     * @return of short result
     */
    static NShortRef ofShort() {
        return new NShortRef(null);
    }

    /**
     * Creates a new instance of of float.
     *
     * @param value value
     * @return of float result
     */
    static NFloatRef ofFloat(Float value) {
        return new NFloatRef(value);
    }

    /**
     * Creates a new instance of of float.
     *
     * @param value value
     * @return of float result
     */
    static NFloatRef ofFloat(float value) {
        return new NFloatRef(value);
    }

    /**
     * Creates a new instance of of float.
     *
     * @return of float result
     */
    static NFloatRef ofFloat() {
        return new NFloatRef(null);
    }

    /**
     * Creates a new instance of of double.
     *
     * @param value value
     * @return of double result
     */
    static NDoubleRef ofDouble(Double value) {
        return new NDoubleRef(value);
    }

    /**
     * Creates a new instance of of double.
     *
     * @param value value
     * @return of double result
     */
    static NDoubleRef ofDouble(double value) {
        return new NDoubleRef(value);
    }

    /**
     * Creates a new instance of of double.
     *
     * @return of double result
     */
    static NDoubleRef ofDouble() {
        return new NDoubleRef(null);
    }

    /**
     * Returns the get.
     *
     * @return get result
     */
    T get();

    /**
     * Or else.
     *
     * @param other other
     * @return or else result
     */
    T orElse(T other);

    /**
     * Sets the non null.
     *
     * @param value value
     */
    void setNonNull(T value);

    /**
     * Sets the if null.
     *
     * @param value value
     */
    void setIfNull(T value);

    /**
     * Sets the set.
     *
     * @param value value
     */
    void set(T value);

    /**
     * Unset.
     */
    void unset();

    /**
     * Checks if is not null.
     *
     * @return is not null result
     */
    boolean isNotNull();

    /**
     * Checks if is blank.
     *
     * @return is blank result
     */
    boolean isBlank();

    /**
     * Checks if is empty.
     *
     * @return is empty result
     */
    boolean isEmpty();

    /**
     * Checks if is null.
     *
     * @return is null result
     */
    boolean isNull();

    /**
     * Checks if is set.
     *
     * @return is set result
     */
    boolean isSet();

    @Override
    String toString();

    /**
     * Checks if is value.
     *
     * @param o o
     * @return is value result
     */
    boolean isValue(Object o);

}
