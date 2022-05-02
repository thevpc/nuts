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
import net.thevpc.nuts.elem.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

/**
 * @author thevpc
 */
public abstract class AbstractNutsElement implements NutsElement {

    protected transient NutsSession session;
    private NutsElementType type;

    public AbstractNutsElement(NutsElementType type, NutsSession session) {
        this.type = type;
        this.session = session;
    }

    @Override
    public NutsElementType type() {
        return type;
    }

    @Override
    public NutsOptional<NutsPrimitiveElement> asPrimitive() {
        if (this instanceof NutsPrimitiveElement) {
            return NutsOptional.of((NutsPrimitiveElement) this);
        }
        return NutsOptional.ofError(s -> NutsMessage.cstyle("unable to cast % to primitive: %s", type().id(), this));
    }

    @Override
    public NutsOptional<NutsObjectElement> asObject() {
        if (this instanceof NutsObjectElement) {
            return NutsOptional.of((NutsObjectElement) this);
        }
        return NutsOptional.ofError(s -> NutsMessage.cstyle("unable to cast % to object: %s", type().id(), this));
    }
    @Override
    public NutsOptional<NutsNavigatableElement> asNavigatable() {
        if (this instanceof NutsNavigatableElement) {
            return NutsOptional.of((NutsNavigatableElement) this);
        }
        return NutsOptional.ofError(s -> NutsMessage.cstyle("unable to cast % to object/array: %s", type().id(), this));
    }

    public NutsOptional<NutsCustomElement> asCustom() {
        if (this instanceof NutsCustomElement) {
            return NutsOptional.of((NutsCustomElement) this);
        }
        return NutsOptional.ofError(s -> NutsMessage.cstyle("unable to cast % to custom: %s", type().id(), this));
    }

    @Override
    public NutsOptional<NutsArrayElement> asArray() {
        if (this instanceof NutsArrayElement) {
            return NutsOptional.of((NutsArrayElement) this);
        }
        return NutsOptional.ofError(s -> NutsMessage.cstyle("unable to cast % to array: %s", type().id(), this));
    }

    @Override
    public boolean isCustom() {
        return this instanceof NutsCustomElement;
    }

    @Override
    public boolean isPrimitive() {
        NutsElementType t = type();
        return t != NutsElementType.ARRAY
                && t != NutsElementType.OBJECT
                && t != NutsElementType.CUSTOM
                ;
    }

    @Override
    public boolean isNumber() {
        NutsElementType t = type();
        switch (t) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case FLOAT:
            case DOUBLE:
                return true;
        }
        return false;
    }

//    @Override
//    public NutsString asNutsString() {
//        return asPrimitive().getNutsString();
//    }


    @Override
    public boolean isNull() {
        NutsElementType t = type();
        return t == NutsElementType.NULL;
    }

    @Override
    public boolean isString() {
        NutsElementType t = type();
        return t == NutsElementType.STRING;
    }

    @Override
    public boolean isByte() {
        return type() == NutsElementType.BYTE;
    }

    @Override
    public boolean isInt() {
        NutsElementType t = type();
        return t == NutsElementType.INTEGER;
    }

    @Override
    public boolean isLong() {
        return type() == NutsElementType.LONG;
    }

    @Override
    public boolean isShort() {
        return type() == NutsElementType.SHORT;
    }

    @Override
    public boolean isFloat() {
        return type() == NutsElementType.FLOAT;
    }

    @Override
    public boolean isDouble() {
        return type() == NutsElementType.DOUBLE;
    }

    @Override
    public boolean isBoolean() {
        return type() == NutsElementType.BOOLEAN;
    }

    @Override
    public boolean isObject() {
        NutsElementType t = type();
        return t == NutsElementType.OBJECT;
    }

    @Override
    public boolean isArray() {
        NutsElementType t = type();
        return t == NutsElementType.ARRAY;
    }

    @Override
    public boolean isInstant() {
        return type() == NutsElementType.INSTANT;
    }

    @Override
    public NutsOptional<String> asString() {
        return asPrimitive().flatMap(NutsValue::asString);
    }

    @Override
    public NutsOptional<Number> asNumber() {
        return asPrimitive().flatMap(NutsValue::asNumber);
    }

    @Override
    public NutsOptional<BigInteger> asBigInt() {
        return asPrimitive().flatMap(NutsValue::asBigInt);
    }

    @Override
    public NutsOptional<BigDecimal> asBigDecimal() {
        return asPrimitive().flatMap(NutsValue::asBigDecimal);
    }

    @Override
    public Object getRaw() {
        if (isPrimitive()) {
            return asPrimitive().get();
        }
        return null;
    }

    @Override
    public NutsOptional<Boolean> asBoolean() {
        return asPrimitive().flatMap(NutsValue::asBoolean);
    }

    @Override
    public NutsOptional<Byte> asByte() {
        return asPrimitive().flatMap(NutsValue::asByte);
    }

    @Override
    public NutsOptional<Double> asDouble() {
        return asPrimitive().flatMap(NutsValue::asDouble);
    }

    @Override
    public NutsOptional<Float> asFloat() {
        return asPrimitive().flatMap(NutsValue::asFloat);
    }

    @Override
    public NutsOptional<Instant> asInstant() {
        return asPrimitive().flatMap(NutsValue::asInstant);
    }

    @Override
    public NutsOptional<Integer> asInt() {
        return asPrimitive().flatMap(NutsValue::asInt);
    }

    @Override
    public NutsOptional<Long> asLong() {
        return asPrimitive().flatMap(NutsValue::asLong);
    }

    @Override
    public NutsOptional<Short> asShort() {
        return asPrimitive().flatMap(NutsValue::asShort);
    }

    @Override
    public NutsElement describe(NutsSession session) {
        return this;
    }

    @Override
    public String toStringLiteral() {
        return toString();
    }

}
