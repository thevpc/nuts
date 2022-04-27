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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Objects;

/**
 * @author thevpc
 */
class DefaultNutsPrimitiveElement extends AbstractNutsElement implements NutsPrimitiveElement {
    private final NutsValue value;

    DefaultNutsPrimitiveElement(NutsElementType type, Object value, NutsSession session) {
        super(type, session);
        this.value = NutsValue.of(value);
    }


    @Override
    public Object getRaw() {
        return value.getRaw();
    }

    @Override
    public NutsOptional<Instant> asInstant() {
        return value.asInstant();
    }

    @Override
    public NutsOptional<Number> asNumber() {
        return value.asNumber();
    }

    @Override
    public NutsOptional<Boolean> asBoolean() {
        return value.asBoolean();
    }

    @Override
    public NutsOptional<Long> asLong() {
        return value.asLong();
    }

    @Override
    public NutsOptional<Double> asDouble() {
        return value.asDouble();
    }

    @Override
    public NutsOptional<Float> asFloat() {
        return value.asFloat();
    }

    @Override
    public NutsOptional<Byte> asByte() {
        return value.asByte();
    }

    @Override
    public NutsOptional<Short> asShort() {
        return value.asShort();
    }

    @Override
    public NutsOptional<Integer> asInt() {
        return value.asInt();
    }

    @Override
    public NutsOptional<String> asString() {
        return value.asString();
    }

    @Override
    public boolean isBoolean() {
        return value.isBoolean();
    }

    @Override
    public boolean isNull() {
        return value.isNull();
    }

    @Override
    public boolean isByte() {
        return value.isByte();
    }

    @Override
    public boolean isInt() {
        return value.isInt();
    }

    @Override
    public boolean isLong() {
        return value.isLong();
    }

    @Override
    public boolean isShort() {
        return value.isShort();
    }

    @Override
    public boolean isFloat() {
        return value.isFloat();
    }

    @Override
    public boolean isDouble() {
        return value.isDouble();
    }

    @Override
    public boolean isInstant() {
        return value.isInstant();
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public boolean isBlank() {
        return value.isBlank();
    }

    @Override
    public NutsOptional<BigInteger> asBigInt() {
        return value.asBigInt();
    }

    @Override
    public NutsOptional<BigDecimal> asBigDecimal() {
        return value.asBigDecimal();
    }

    @Override
    public boolean isNumber() {
        return value.isNumber();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsPrimitiveElement that = (DefaultNutsPrimitiveElement) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
