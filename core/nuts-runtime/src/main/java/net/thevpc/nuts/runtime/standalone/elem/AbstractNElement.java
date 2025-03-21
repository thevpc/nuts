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
package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author thevpc
 */
public abstract class AbstractNElement implements NElement {

    private NElementType type;
    private NElementAnnotation[] annotations;
    private NElementComments comments;

    public AbstractNElement(NElementType type, NElementAnnotation[] annotations, NElementComments comments) {
        this.type = type;
        this.annotations = annotations == null ? new NElementAnnotation[0] : annotations;
        this.comments = comments == null ? new NElementCommentsImpl() : comments;
    }


    public NElementComments comments() {
        return comments;
    }

    @Override
    public boolean isNamedUplet() {
        return type() == NElementType.NAMED_UPLET;
    }

    @Override
    public boolean isNamedUplet(String name) {
        return false;
    }

    @Override
    public boolean isNamedObject() {
        return type() == NElementType.NAMED_UPLET;
    }

    @Override
    public boolean isAnyNamedObject() {
        return type().isAnyNamedObject();
    }

    @Override
    public boolean isAnyNamedObject(String name) {
        return isAnyNamedObject() && isNamed(name);
    }

    @Override
    public boolean isParametrizedObject() {
        return type() == NElementType.PARAMETRIZED_OBJECT;
    }

    @Override
    public boolean isNamedParametrizedObject() {
        return type().isAnyParametrizedObject();
    }

    @Override
    public boolean isNamedParametrizedObject(String name) {
        return type() == NElementType.NAMED_PARAMETRIZED_OBJECT && isNamed(name);
    }

    @Override
    public boolean isAnyArray() {
        return type().isAnyArray();
    }

    @Override
    public boolean isAnyObject() {
        return type().isAnyObject();
    }

    @Override
    public boolean isListContainer() {
        return type().isListContainer();
    }

    @Override
    public NOptional<NListContainerElement> asListContainer() {
        if (isListContainer()) {
            return NOptional.of((NListContainerElement) this);
        }
        return NOptional.ofEmpty(NMsg.ofC("%s is not a list container", type().id()));
    }

    @Override
    public boolean isAnyMatrix() {
        return type().isAnyMatrix();
    }

    @Override
    public boolean isAnyUplet() {
        return type().isAnyUplet();
    }

    @Override
    public boolean isNamedArray() {
        return type() == NElementType.NAMED_ARRAY;
    }

    @Override
    public boolean isAnyNamedArray() {
        return type().isAnyNamedArray();
    }

    @Override
    public boolean isAnyNamedArray(String name) {
        return isAnyNamedArray() && isNamed(name);
    }

    @Override
    public boolean isParametrizedArray() {
        return type() == NElementType.PARAMETRIZED_ARRAY;
    }

    @Override
    public boolean isNamedParametrizedArray() {
        return type().isAnyParametrizedArray();
    }

    @Override
    public boolean isNamedParametrizedArray(String name) {
        return type() == NElementType.NAMED_PARAMETRIZED_ARRAY && isNamed(name);
    }

    @Override
    public boolean isNamedMatrix() {
        return type() == NElementType.NAMED_MATRIX;
    }

    @Override
    public boolean isAnyNamedMatrix() {
        return false;
    }

    @Override
    public boolean isAnyNamedMatrix(String name) {
        return isNamedMatrix() && isNamed(name);
    }

    @Override
    public boolean isParametrizedMatrix() {
        return type() == NElementType.PARAMETRIZED_MATRIX;
    }

    @Override
    public boolean isAnyParametrizedMatrix() {
        return type().isAnyParametrizedMatrix();
    }

    @Override
    public boolean isAnyParametrizedMatrix(String name) {
        return isNamedParametrizedMatrix() && isNamed(name);
    }

    @Override
    public boolean isNamedParametrizedMatrix() {
        return type() == NElementType.NAMED_PARAMETRIZED_MATRIX;
    }

    @Override
    public boolean isNamed(String name) {
        return false;
    }

    @Override
    public List<NElement> resolveAll(String pattern) {
        return Collections.emptyList();
    }

    @Override
    public NElementBuilder builder() {
        return null;
    }

    @Override
    public NOptional<Object> asObjectAt(int index) {
        return null;
    }

    @Override
    public List<NElementAnnotation> annotations() {
        return annotations == null ? Collections.emptyList() : Arrays.asList(annotations);
    }

    @Override
    public NElementType type() {
        return type;
    }

    @Override
    public NOptional<NElement> resolve(String pattern) {
        return NOptional.ofNamedSingleton(resolveAll(pattern), "resolvable " + pattern);
    }

    @Override
    public NOptional<NPrimitiveElement> asPrimitive() {
        if (this instanceof NPrimitiveElement) {
            return NOptional.of((NPrimitiveElement) this);
        }
        return NOptional.ofError(() -> NMsg.ofC("unable to cast % to primitive: %s", type().id(), this));
    }

    @Override
    public NOptional<NObjectElement> asObject() {
        if (this instanceof NObjectElement) {
            return NOptional.of((NObjectElement) this);
        }
        return NOptional.ofError(() -> NMsg.ofC("unable to cast %s to object: %s", type().id(), this));
    }

    @Override
    public NOptional<NPairElement> asPair() {
        if (this instanceof NPairElement) {
            return NOptional.of((NPairElement) this);
        }
        return NOptional.ofError(() -> NMsg.ofC("unable to cast %s to pair: %s", type().id(), this));
    }

    public NOptional<NCustomElement> asCustom() {
        if (this instanceof NCustomElement) {
            return NOptional.of((NCustomElement) this);
        }
        return NOptional.ofError(() -> NMsg.ofC("unable to cast %s to custom: %s", type().id(), this));
    }

    @Override
    public NOptional<NArrayElement> asArray() {
        if (this instanceof NArrayElement) {
            return NOptional.of((NArrayElement) this);
        }
        return NOptional.ofError(() -> NMsg.ofC("unable to cast %s to array: %s", type().id(), this));
    }

    @Override
    public boolean isCustom() {
        return this instanceof NCustomElement;
    }

    @Override
    public boolean isPrimitive() {
        return type().isPrimitive();
    }

    @Override
    public boolean isAnyString() {
        return type().isAnyString();
    }

    @Override
    public boolean isStream() {
        return type().isStream();
    }

    @Override
    public boolean isNumber() {
        return type().isNumber();
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
    public boolean isDecimalNumber() {
        return type().isDecimalNumber();
    }

    @Override
    public boolean isBigNumber() {
        return type().isBigNumber();
    }

    @Override
    public boolean isComplexNumber() {
        return type().isComplexNumber();
    }

    @Override
    public boolean isTemporal() {
        return type().isTemporal();
    }

    @Override
    public boolean isLocalTemporal() {
        return type().isLocalTemporal();
    }

    @Override
    public boolean isNamed() {
        return type().isNamed();
    }

    @Override
    public boolean isParametrized() {
        return type().isParametrized();
    }

    @Override
    public boolean isBigDecimal() {
        return type() == NElementType.BIG_DECIMAL;
    }

    @Override
    public boolean isBigInt() {
        return type() == NElementType.BIG_INTEGER;
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
    public boolean isPair() {
        NElementType t = type();
        return t == NElementType.PAIR;
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
    public NOptional<LocalDate> asLocalDate() {
        return asPrimitive().flatMap(NLiteral::asLocalDate);
    }

    @Override
    public NOptional<LocalDateTime> asLocalDateTime() {
        return asPrimitive().flatMap(NLiteral::asLocalDateTime);
    }

    @Override
    public NOptional<LocalTime> asLocalTime() {
        return asPrimitive().flatMap(NLiteral::asLocalTime);
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
    public NElement describe() {
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

    @Override
    public NOptional<String> asStringAt(int index) {
        return asLiteralAt(index).asString();
    }

    @Override
    public NOptional<Long> asLongAt(int index) {
        return asLiteralAt(index).asLong();
    }

    @Override
    public NOptional<Integer> asIntAt(int index) {
        return asLiteralAt(index).asInt();
    }

    @Override
    public NOptional<Double> asDoubleAt(int index) {
        return asLiteralAt(index).asDouble();
    }

    @Override
    public boolean isNullAt(int index) {
        return asLiteralAt(index).isNull();
    }

    @Override
    public NLiteral asLiteralAt(int index) {
        return NLiteral.of(asObjectAt(index).orNull());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AbstractNElement that = (AbstractNElement) o;
        return type == that.type && Objects.deepEquals(annotations, that.annotations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, Arrays.hashCode(annotations));
    }
}
