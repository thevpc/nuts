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

public interface NRef<T> extends Supplier<T> {

    static <T> NRef<T> of() {
        return new NObjectRef<>(null);
    }

    static <T> NRef<T> of(T t) {
        return new NObjectRef<>(t);
    }

    static <T> NRef<T> of(T t, Class<T> type) {
        return new NObjectRef<>(t);
    }

    static <T> NRef<T> ofNull(Class<T> t) {
        return new NObjectRef<>(null);
    }

    static <T> NRef<T> ofNull() {
        return of(null);
    }

    static NLongRef ofLong(Long value) {
        return new NLongRef(value);
    }

    static NLongRef ofLong(long value) {
        return new NLongRef(value);
    }

    static NLongRef ofLong() {
        return new NLongRef(null);
    }

    static NIntRef ofInt(Integer value) {
        return new NIntRef(value);
    }

    static NIntRef ofInt(int value) {
        return new NIntRef(value);
    }

    static NIntRef ofInt() {
        return new NIntRef(null);
    }

    static NBooleanRef ofFalse() {
        return new NBooleanRef(false);
    }

    static NBooleanRef ofTrue() {
        return new NBooleanRef(true);
    }

    static NBooleanRef ofBoolean(Boolean value) {
        return new NBooleanRef(value);
    }

    static NBooleanRef ofBoolean(boolean value) {
        return new NBooleanRef(value);
    }

    static NBooleanRef ofBoolean() {
        return new NBooleanRef(null);
    }

    static NByteRef ofByte(Byte value) {
        return new NByteRef(value);
    }

    static NByteRef ofByte(byte value) {
        return new NByteRef(value);
    }

    static NByteRef ofByte() {
        return new NByteRef(null);
    }

    static NShortRef ofShort(Short value) {
        return new NShortRef(value);
    }

    static NShortRef ofShort(short value) {
        return new NShortRef(value);
    }

    static NShortRef ofShort() {
        return new NShortRef(null);
    }

    static NFloatRef ofFloat(Float value) {
        return new NFloatRef(value);
    }

    static NFloatRef ofFloat(float value) {
        return new NFloatRef(value);
    }

    static NFloatRef ofFloat() {
        return new NFloatRef(null);
    }

    static NDoubleRef ofDouble(Double value) {
        return new NDoubleRef(value);
    }

    static NDoubleRef ofDouble(double value) {
        return new NDoubleRef(value);
    }

    static NDoubleRef ofDouble() {
        return new NDoubleRef(null);
    }

    T get();

    T orElse(T other);

    void setNonNull(T value);

    void set(T value);

    void unset();

    boolean isNotNull();

    boolean isBlank();

    boolean isEmpty();

    boolean isNull();

    boolean isSet();

    @Override
    String toString();

    boolean isValue(Object o);

}
