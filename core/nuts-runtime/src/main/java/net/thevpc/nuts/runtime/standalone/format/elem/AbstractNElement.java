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
package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

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
    public boolean isName() {
        return type() == NElementType.NAME;
    }

    @Override
    public boolean isNamedUplet() {
        return type() == NElementType.NAMED_UPLET;
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
    public boolean isInstant() {
        return type() == NElementType.INSTANT;
    }

    @Override
    public boolean isNamedUplet(String name) {
        return false;
    }

    @Override
    public boolean isNamedObject() {
        return type() == NElementType.NAMED_OBJECT;
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
    public boolean isNamedObject(String name) {
        return type() == NElementType.NAMED_OBJECT && isNamed(name);
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
    public NOptional<NStringElement> asString() {
        return NOptional.of((NStringElement) NElements.of().ofString(toString()));
    }

    @Override
    public NOptional<NNumberElement> asNumber() {
        return NOptional.ofEmpty(NMsg.ofC("%s is not a number", type().id()));
    }

    @Override
    public NOptional<NListContainerElement> asListContainer() {
        if (isListContainer()) {
            return NOptional.of((NListContainerElement) this);
        }
        return NOptional.ofEmpty(NMsg.ofC("%s is not a list container", type().id()));
    }

    @Override
    public NOptional<NParametrizedContainerElement> asParametrizedContainer() {
        if (isListContainer()) {
            return NOptional.of((NParametrizedContainerElement) this);
        }
        return NOptional.ofEmpty(NMsg.ofC("%s is not a list container", type().id()));
    }

    @Override
    public NOptional<NNamedElement> asNamed() {
        if (isNamed()) {
            return NOptional.of((NNamedElement) this);
        }
        return NOptional.ofEmpty(NMsg.ofC("%s is not a named", type().id()));
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
    public NOptional<NElement> asElementAt(int index) {
        if (type().isListContainer()) {
            return asListContainer().map(x ->
                    index >= 0 && index < x.size() ?
                            x.children().get(index)
                            : null
            );
        }
        return NOptional.ofNamedEmpty("object at " + index);
    }

    @Override
    public NOptional<NUpletElement> asUplet() {
        if (this instanceof NUpletElement) {
            return NOptional.of((NUpletElement) this);
        }
        return NOptional.ofError(() -> NMsg.ofC("unable to cast %s to uplet: %s", type().id(), this));
    }

    @Override
    public NOptional<NNumberElement> asInt() {
        if (isInt()) {
            return NOptional.of((NNumberElement) this);
        }
        return NOptional.ofError(() -> NMsg.ofC("unable to cast %s to int: %s", type().id(), this));
    }

    @Override
    public NOptional<NPairElement> asPair() {
        if (this instanceof NPairElement) {
            return NOptional.of((NPairElement) this);
        }
        return NOptional.ofError(() -> NMsg.ofC("unable to cast %s to pair: %s", type().id(), this));
    }

    @Override
    public NOptional<NStringElement> asStr() {
        if (this instanceof NStringElement) {
            return NOptional.of((NStringElement) this);
        }
        return NOptional.ofError(() -> NMsg.ofC("unable to cast %s to string: %s", type().id(), this));
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

    @Override
    public boolean isFloatingNumber() {
        return type().isFloatingNumber();
    }

    @Override
    public boolean isOrdinalNumber() {
        return type().isOrdinalNumber();
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
        return t.isString();
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
    public boolean isAnyDate() {
        return type().isAnyDate();
    }

    @Override
    public NOptional<NOperatorElement> asOperator() {
        if (this instanceof NOperatorElement) {
            return NOptional.of((NOperatorElement) this);
        }
        return NOptional.ofNamedEmpty("operator");
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
    public NOptional<NNamedElement> toNamed() {
        if (this instanceof NNamedElement) {
            return NOptional.of((NNamedElement) this);
        }
        ;
        return NOptional.ofNamedEmpty("named element");
    }

    @Override
    public boolean isParametrized() {
        return type().isParametrized();
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
    public boolean isSimplePair() {
        if (!isPair()) {
            return false;
        }
        NElement key = asPair().get().key();
        return key.isPrimitive();
    }

    @Override
    public boolean isNamedPair() {
        if (!isPair()) {
            return false;
        }
        NElement key = asPair().get().key();
        return key.isAnyString();
    }

    @Override
    public boolean isParametrizedContainer() {
        return this instanceof NParametrizedContainerElement && ((NParametrizedContainerElement) this).isParametrized();
    }

    @Override
    public NOptional<NListContainerElement> toListContainer() {
        if (isListContainer()) {
            return asListContainer();
        }
        if (isNamedPair()) {
            NArrayElementBuilder ab = NElements.of().ofArrayBuilder();
            ab.name(asNamed().get().name());
            NPairElement pair = asPair().get();
            NElement value = pair.value();
            if (value.isListContainer()) {
                NListContainerElement cc = value.asListContainer().get();
                if (cc.isNamed() || cc.isParametrized()) {
                    ab.add(cc);
                } else {
                    ab.addAll(cc.children());
                }
            } else {
                ab.add(value);
            }
            return NOptional.of(ab.build());
        } else {
            NArrayElementBuilder ab = NElements.of().ofArrayBuilder();
            ab.add(this);
            return NOptional.of(ab.build());
        }
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
    public NElement withDesc(NEDesc description) {
        return this;
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

    @Override
    public NOptional<NPairElement> toNamedPair() {
        switch (type) {
            case PAIR: {
                if (isNamed()) {
                    return NOptional.of((NPairElement) this);
                }
                NPairElement u = asPair().orNull();
                if (u.isNamed()) {
                    return NOptional.of(NElements.of().ofPair(u.name(), u.value()));
                }
                break;
            }
            case NAMED_UPLET: {
                NUpletElement u = asUplet().orNull();
                return NOptional.of(NElements.of().ofPair(u.name(), u.builder().name(null).build()));
            }
            case NAMED_OBJECT: {
                NObjectElement u = asObject().orNull();
                if (u.isNamed() && !u.isParametrized()) {
                    return NOptional.of(NElements.of().ofPair(u.name(), u.builder().name(null).build()));
                }
                break;
            }
            case NAMED_ARRAY: {
                NArrayElement u = asArray().orNull();
                if (u.isNamed() && !u.isParametrized()) {
                    return NOptional.of(NElements.of().ofPair(u.name(), u.builder().name(null).build()));
                }
                break;
            }
        }
        return NOptional.ofNamedEmpty("named-pair");
    }

    @Override
    public NOptional<NUpletElement> toNamedUplet() {
        switch (type) {
            case PAIR: {
                NPairElement u = asPair().orNull();
                if (u.isNamed()) {
                    NElement v = u.value();
                    return NOptional.of(NElements.of().ofUplet(u.name(), v));
                }
                break;
            }
            case NAMED_UPLET: {
                return NOptional.of((NUpletElement) this);
            }
            case NAMED_OBJECT: {
                NObjectElement u = asObject().orNull();
                if (!u.isParametrized()) {
                    return NOptional.of(NElements.of().ofUplet(u.name(), u.children().toArray(new NElement[0])));
                }
                break;
            }
            case NAMED_ARRAY: {
                NArrayElement u = asArray().orNull();
                if (!u.isParametrized()) {
                    return NOptional.of(NElements.of().ofUplet(u.name(), u.children().toArray(new NElement[0])));
                }
                break;
            }
        }
        return NOptional.ofNamedEmpty("named-uplet");
    }

    @Override
    public NOptional<NObjectElement> toNamedObject() {
        switch (type) {
            case PAIR: {
                NPairElement u = asPair().orNull();
                if (u.isNamed()) {
                    NElement v = u.value();
                    return NOptional.of(NElements.of().ofObjectBuilder(u.name()).add(v).build());
                }
                break;
            }
            case NAMED_UPLET: {
                NUpletElement u = asUplet().orNull();
                return NOptional.of(NElements.of().ofObjectBuilder(u.name()).addAll(u.children().toArray(new NElement[0])).build());
            }
            case NAMED_OBJECT: {
                return NOptional.of((NObjectElement) this);
            }
            case NAMED_ARRAY: {
                NArrayElement u = asArray().orNull();
                return NOptional.of(NElements.of().ofObjectBuilder(u.name())
                        .addParams(u.params().orNull())
                        .addAll(u.children().toArray(new NElement[0])).build());
            }
        }
        return NOptional.ofNamedEmpty("named-object");
    }

    @Override
    public NOptional<NArrayElement> toNamedArray() {
        switch (type) {
            case PAIR: {
                NPairElement u = asPair().orNull();
                if (u.isNamed()) {
                    NElement v = u.value();
                    return NOptional.of(NElements.of().ofArrayBuilder(u.name()).add(v).build());
                }
                break;
            }
            case NAMED_UPLET: {
                NUpletElement u = asUplet().orNull();
                return NOptional.of(NElements.of().ofArrayBuilder(u.name()).addAll(u.children().toArray(new NElement[0])).build());
            }
            case NAMED_OBJECT: {
                NObjectElement u = asObject().orNull();
                return NOptional.of(NElements.of().ofArrayBuilder(u.name())
                        .addParams(u.params().orNull())
                        .addAll(u.children().toArray(new NElement[0])).build());
            }
            case NAMED_ARRAY: {
                return NOptional.of((NArrayElement) this);
            }
        }
        return NOptional.ofNamedEmpty("named-array");
    }

    @Override
    public NOptional<NObjectElement> toObject() {
        switch (type) {
            case PAIR: {
                NPairElement u = asPair().orNull();
                if (u.isNamed()) {
                    NElement v = u.value();
                    return NOptional.of(NElements.of().ofObjectBuilder(u.name()).add(v).build());
                }
                return NOptional.of(NElements.of().ofObjectBuilder().add(this).build());
            }
            case NAMED_UPLET: {
                NUpletElement u = asUplet().orNull();
                return NOptional.of(NElements.of().ofObjectBuilder().addAll(u.children().toArray(new NElement[0])).build());
            }
            case UPLET: {
                NUpletElement u = asUplet().orNull();
                return NOptional.of(NElements.of().ofObjectBuilder().addAll(u.children().toArray(new NElement[0])).build());
            }
            case OBJECT: {
                return NOptional.of((NObjectElement) this);
            }
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT: {
                NObjectElement u = asObject().orNull();
                return NOptional.of(NElements.of().ofObjectBuilder().name(u.name())
                        .addParams(u.params().orNull())
                        .addAll(u.children().toArray(new NElement[0])).build());
            }
            case ARRAY:
            case NAMED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY: {
                NArrayElement u = asArray().orNull();
                return NOptional.of(NElements.of().ofObjectBuilder().name(u.name())
                        .addParams(u.params().orNull())
                        .addAll(u.children().toArray(new NElement[0])).build());
            }
            default:{
                return NOptional.of(NElements.of().ofObjectBuilder().add(this).build());
            }
        }
    }

    @Override
    public NOptional<NArrayElement> toArray() {
        switch (type) {
            case PAIR: {
                NPairElement u = asPair().orNull();
                if (u.isNamed()) {
                    NElement v = u.value();
                    return NOptional.of(NElements.of().ofArrayBuilder(u.name()).add(v).build());
                }
                return NOptional.of(NElements.of().ofArrayBuilder().add(this).build());
            }
            case NAMED_UPLET: {
                NUpletElement u = asUplet().orNull();
                return NOptional.of(NElements.of().ofArrayBuilder().addAll(u.children().toArray(new NElement[0])).build());
            }
            case UPLET: {
                NUpletElement u = asUplet().orNull();
                return NOptional.of(NElements.of().ofArrayBuilder().addAll(u.children().toArray(new NElement[0])).build());
            }
            case OBJECT:
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT: {
                NObjectElement u = asObject().orNull();
                return NOptional.of(NElements.of().ofArrayBuilder()
                        .addParams(u.params().orNull())
                        .addAll(u.children().toArray(new NElement[0])).build());
            }
            case ARRAY: {
                return NOptional.of((NArrayElement) this);
            }
            case NAMED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY: {
                NArrayElement u = asArray().orNull();
                return NOptional.of(NElements.of().ofArrayBuilder()
                        .addAll(u.children().toArray(new NElement[0])).build());
            }
            default:{
                return NOptional.of(NElements.of().ofArrayBuilder().add(this).build());
            }
        }
    }

    @Override
    public NArrayElement wrapIntoArray() {
        return NElements.of().ofArray(this);
    }

    @Override
    public NObjectElement wrapIntoObject() {
        return NElements.of().ofObjectBuilder().add(this).build();
    }

    @Override
    public NUpletElement wrapIntoUplet() {
        return NElements.of().ofUplet(this);
    }

    @Override
    public NArrayElement wrapIntoNamedArray(String name) {
        return NElements.of().ofArrayBuilder(name).add(this).build();
    }

    @Override
    public NObjectElement wrapIntoNamedObject(String name) {
        return NElements.of().ofObjectBuilder(name).add(this).build();
    }

    @Override
    public NUpletElement wrapIntoNamedUplet(String name) {
        return NElements.of().ofUpletBuilder(name).add(this).build();
    }

    @Override
    public NPairElement wrapIntoNamedPair(String name) {
        NElements e = NElements.of();
        return e.ofPairBuilder().key(name).value(this).build();
    }

    @Override
    public NLiteral asLiteral() {
        return new NElementAsLiteral(this);
    }


    @Override
    public NOptional<String> asStringValue() {
        return asLiteral().asString();
    }

    @Override
    public NOptional<LocalTime> asLocalTimeValue() {
        return asLiteral().asLocalTime();
    }

    @Override
    public NOptional<LocalDate> asLocalDateValue() {
        return asLiteral().asLocalDate();
    }

    @Override
    public NOptional<LocalDateTime> asLocalDateTimeValue() {
        return asLiteral().asLocalDateTime();
    }

    @Override
    public NOptional<Double> asDoubleValue() {
        return asLiteral().asDouble();
    }

    @Override
    public NOptional<Float> asFloatValue() {
        return asLiteral().asFloat();
    }

    @Override
    public NOptional<Long> asLongValue() {
        return asLiteral().asLong();
    }

    @Override
    public NOptional<Integer> asIntValue() {
        return asLiteral().asInt();
    }

    @Override
    public NOptional<Short> asShortValue() {
        return asLiteral().asShort();
    }

    @Override
    public NOptional<Byte> asByteValue() {
        return asLiteral().asByte();
    }

    @Override
    public NOptional<NFloatComplex> asFloatComplexValue() {
        return asLiteral().asFloatComplex();
    }

    @Override
    public NOptional<NDoubleComplex> asDoubleComplexValue() {
        return asLiteral().asDoubleComplex();
    }

    @Override
    public NOptional<NBigComplex> asBigComplexValue() {
        return asLiteral().asBigComplex();
    }

    @Override
    public NOptional<Instant> asInstantValue() {
        return asLiteral().asInstant();
    }

    @Override
    public NOptional<Character> asCharValue() {
        return asLiteral().asChar();
    }

    @Override
    public NOptional<Boolean> asBooleanValue() {
        return asLiteral().asBoolean();
    }

    @Override
    public NOptional<BigDecimal> asBigDecimalValue() {
        return asLiteral().asBigDecimal();
    }

    @Override
    public NOptional<BigInteger> asBigIntValue() {
        return asLiteral().asBigInt();
    }

    @Override
    public NOptional<Number> asNumberValue() {
        return asLiteral().asNumber();
    }
}
