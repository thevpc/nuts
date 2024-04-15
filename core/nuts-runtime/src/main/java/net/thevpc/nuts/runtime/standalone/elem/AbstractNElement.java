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
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

/**
 * @author thevpc
 */
public abstract class AbstractNElement implements NElement {

    protected transient NSession session;
    private NElementType type;

    public AbstractNElement(NElementType type, NSession session) {
        this.type = type;
        this.session = session;
    }

    @Override
    public NElementType type() {
        return type;
    }

    @Override
    public NOptional<NPrimitiveElement> asPrimitive() {
        if (this instanceof NPrimitiveElement) {
            return NOptional.of((NPrimitiveElement) this);
        }
        return NOptional.ofError(s -> NMsg.ofC("unable to cast % to primitive: %s", type().id(), this));
    }

    @Override
    public NOptional<NObjectElement> asObject() {
        if (this instanceof NObjectElement) {
            return NOptional.of((NObjectElement) this);
        }
        return NOptional.ofError(s -> NMsg.ofC("unable to cast %s to object: %s", type().id(), this));
    }

    @Override
    public NOptional<NNavigatableElement> asNavigatable() {
        if (this instanceof NNavigatableElement) {
            return NOptional.of((NNavigatableElement) this);
        }
        return NOptional.ofError(s -> NMsg.ofC("unable to cast % sto object/array: %s", type().id(), this));
    }

    public NOptional<NCustomElement> asCustom() {
        if (this instanceof NCustomElement) {
            return NOptional.of((NCustomElement) this);
        }
        return NOptional.ofError(s -> NMsg.ofC("unable to cast %s to custom: %s", type().id(), this));
    }

    @Override
    public NOptional<NArrayElement> asArray() {
        if (this instanceof NArrayElement) {
            return NOptional.of((NArrayElement) this);
        }
        return NOptional.ofError(s -> NMsg.ofC("unable to cast %s to array: %s", type().id(), this));
    }

    @Override
    public boolean isCustom() {
        return this instanceof NCustomElement;
    }

    @Override
    public boolean isPrimitive() {
        NElementType t = type();
        return t != NElementType.ARRAY
                && t != NElementType.OBJECT
                && t != NElementType.CUSTOM
                ;
    }

    @Override
    public boolean isNumber() {
        NElementType t = type();
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
        NElementType t = type();
        return t == NElementType.NULL;
    }

    @Override
    public boolean isString() {
        NElementType t = type();
        return t == NElementType.STRING;
    }

    @Override
    public boolean isByte() {
        return type() == NElementType.BYTE;
    }

    @Override
    public boolean isInt() {
        NElementType t = type();
        return t == NElementType.INTEGER;
    }

    @Override
    public boolean isLong() {
        return type() == NElementType.LONG;
    }

    @Override
    public boolean isShort() {
        return type() == NElementType.SHORT;
    }

    @Override
    public boolean isFloat() {
        return type() == NElementType.FLOAT;
    }

    @Override
    public boolean isDouble() {
        return type() == NElementType.DOUBLE;
    }

    @Override
    public boolean isBoolean() {
        return type() == NElementType.BOOLEAN;
    }

    @Override
    public boolean isObject() {
        NElementType t = type();
        return t == NElementType.OBJECT;
    }

    @Override
    public boolean isArray() {
        NElementType t = type();
        return t == NElementType.ARRAY;
    }

    @Override
    public boolean isInstant() {
        return type() == NElementType.INSTANT;
    }

    @Override
    public NOptional<String> asString() {
        return asPrimitive().flatMap(NLiteral::asString);
    }

    @Override
    public NOptional<Number> asNumber() {
        return asPrimitive().flatMap(NLiteral::asNumber);
    }

    @Override
    public NOptional<BigInteger> asBigInt() {
        return asPrimitive().flatMap(NLiteral::asBigInt);
    }

    @Override
    public NOptional<BigDecimal> asBigDecimal() {
        return asPrimitive().flatMap(NLiteral::asBigDecimal);
    }

    @Override
    public Object getRaw() {
        if (isPrimitive()) {
            return asPrimitive().get();
        }
        return null;
    }

    @Override
    public NOptional<Boolean> asBoolean() {
        return asPrimitive().flatMap(NLiteral::asBoolean);
    }

    @Override
    public NOptional<Byte> asByte() {
        return asPrimitive().flatMap(NLiteral::asByte);
    }

    @Override
    public NOptional<Double> asDouble() {
        return asPrimitive().flatMap(NLiteral::asDouble);
    }

    @Override
    public NOptional<Float> asFloat() {
        return asPrimitive().flatMap(NLiteral::asFloat);
    }

    @Override
    public NOptional<Instant> asInstant() {
        return asPrimitive().flatMap(NLiteral::asInstant);
    }

    @Override
    public NOptional<Integer> asInt() {
        return asPrimitive().flatMap(NLiteral::asInt);
    }

    @Override
    public NOptional<Long> asLong() {
        return asPrimitive().flatMap(NLiteral::asLong);
    }

    @Override
    public NOptional<Short> asShort() {
        return asPrimitive().flatMap(NLiteral::asShort);
    }

    @Override
    public NOptional<Character> asChar() {
        return asPrimitive().flatMap(NLiteral::asChar);
    }

    @Override
    public boolean isSupportedType(Class<?> type) {
        NOptional<NPrimitiveElement> p = asPrimitive();
        if (p.isPresent()) {
            return p.get().isSupportedType(type);
        }
        return false;
    }

    @Override
    public <ET> NOptional<ET> asType(Class<ET> expectedType) {
        return asPrimitive().flatMap(x -> x.asType(expectedType));
    }

    @Override
    public <ET> NOptional<ET> asType(Type expectedType) {
        return asPrimitive().flatMap(x -> x.asType(expectedType));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isBlank() {
        return false;
    }

    @Override
    public NElement describe(NSession session) {
        return this;
    }

    @Override
    public String toStringLiteral() {
        return toString();
    }

    @Override
    public NElement withDesc(NEDesc description) {
        return this;
    }
}
