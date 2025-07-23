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
package net.thevpc.nuts.runtime.standalone.format.elem.item;

import net.thevpc.nuts.NBigComplex;
import net.thevpc.nuts.NDoubleComplex;
import net.thevpc.nuts.NFloatComplex;
import net.thevpc.nuts.NUnsupportedOperationException;
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
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    @Override
    public boolean isCustomTree() {
        if (annotations != null) {
            for (NElementAnnotation annotation : annotations) {
                if (annotation.isCustomTree()) {
                    return true;
                }
            }
        }
        return false;
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

    public boolean isUplet() {
        return type() == NElementType.UPLET;
    }

    @Override
    public boolean isBigDecimal() {
        return type() == NElementType.BIG_DECIMAL;
    }

    @Override
    public boolean isBigInt() {
        return type() == NElementType.BIG_INT;
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
        return type().isAnyListContainer();
    }


    @Override
    public NOptional<NNumberElement> asNumber() {
        return NOptional.ofEmpty(_expected("number"));
    }

    @Override
    public NOptional<NListContainerElement> asListContainer() {
        if (isListContainer()) {
            return NOptional.of((NListContainerElement) this);
        }
        return NOptional.ofEmpty(_expected("list container"));
    }

    @Override
    public NOptional<NParametrizedContainerElement> asParametrizedContainer() {
        if (isListContainer()) {
            return NOptional.of((NParametrizedContainerElement) this);
        }
        return NOptional.ofEmpty(_expected("parametrized container"));
    }

    @Override
    public NOptional<NObjectElement> asParametrizedObject() {
        if (isParametrizedObject()) {
            return NOptional.of((NObjectElement) this);
        }
        return NOptional.ofEmpty(_expected("parametrized object"));
    }

    protected NMsg _expected(String any) {
        return NMsg.ofC("expected a %s, got %s : %s", any, type().id(), snippet());
    }

    private String snippet() {
        String s = toString(true);
        int u = s.indexOf("\n");
        boolean truncated = false;
        if (u >= 0) {
            s = s.substring(0, u);
            truncated = true;
        }
        if (s.length() > 100) {
            s = s.substring(0, 100);
            truncated = true;
        }
        if (truncated) {
            return s + "...";
        }
        return s;
    }

    @Override
    public NOptional<NObjectElement> asNamedParametrizedObject(String name) {
        if (isNamedParametrizedObject(name)) {
            return NOptional.of((NObjectElement) this);
        }
        return NOptional.ofEmpty(_expected("parametrized object " + name));
    }

    @Override
    public NOptional<NNamedElement> asNamed() {
        if (isNamed()) {
            return NOptional.of((NNamedElement) this);
        }
        return NOptional.ofEmpty(_expected("named element"));
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
        return isNamed() && Objects.equals(asNamed().get(), name);
    }

    public boolean isNamed(Predicate<String> name) {
        return isNamed() && (name == null || name.test(asNamed().get().name().get()));
    }

    @Override
    public boolean isName(String name) {
        return isName() && Objects.equals(asStringValue().get(), name);
    }

    @Override
    public boolean isName(Predicate<String> nameCondition) {
        return isName() && (nameCondition == null || nameCondition.test(asStringValue().get()));
    }

    @Override
    public boolean isNamedUplet(Predicate<String> nameCondition) {
        return isNamedUplet() && isNamed(nameCondition);
    }

    @Override
    public boolean isNamedObject(Predicate<String> nameCondition) {
        return isNamedObject() && isNamed(nameCondition);
    }

    @Override
    public boolean isNamedParametrizedObject(Predicate<String> nameCondition) {
        return isNamedParametrizedObject() && isNamed(nameCondition);
    }

    @Override
    public boolean isNamedParametrizedMatrix(Predicate<String> nameCondition) {
        return isNamedParametrizedMatrix() && isNamed(nameCondition);
    }

    @Override
    public boolean isNamedParametrizedMatrix(String name) {
        return isNamedParametrizedMatrix() && isNamed(name);
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
    public List<NElementAnnotation> findAnnotations(String name) {
        return annotations().stream().filter(x -> Objects.equals(x.name(), name)).collect(Collectors.toList());
    }

    @Override
    public boolean isAnnotated(String name) {
        return !findAnnotations(name).isEmpty();
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
        return NOptional.ofError(() -> NMsg.ofC("unable to cast %s to primitive: %s", type().id(), this));
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
        if (type().isAnyListContainer()) {
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
        return NOptional.ofError(() -> _expected("uplet"));
    }

    @Override
    public NOptional<NNumberElement> asInt() {
        if (isInt()) {
            return NOptional.of((NNumberElement) this);
        }
        return NOptional.ofError(() -> _expected("int"));
    }

    @Override
    public NOptional<NPairElement> asPair() {
        if (this instanceof NPairElement) {
            return NOptional.of((NPairElement) this);
        }
        return NOptional.ofError(() -> _expected("pair"));
    }

    @Override
    public NOptional<NStringElement> asString() {
        if (this instanceof NStringElement) {
            return NOptional.of((NStringElement) this);
        }
        return NOptional.of((NStringElement) NElement.ofString(toString()));
    }

    public NOptional<NCustomElement> asCustom() {
        if (this instanceof NCustomElement) {
            return NOptional.of((NCustomElement) this);
        }
        return NOptional.ofError(() -> _expected("custom"));
    }

    @Override
    public NOptional<NArrayElement> asArray() {
        if (this instanceof NArrayElement) {
            return NOptional.of((NArrayElement) this);
        }
        return NOptional.ofError(() -> _expected("array"));
    }

    @Override
    public boolean isCustom() {
        return this instanceof NCustomElement;
    }

    @Override
    public boolean isPrimitive() {
        return type().isAnyPrimitive();
    }

    @Override
    public boolean isAnyString() {
        return type().isAnyStringOrName();
    }

    @Override
    public boolean isStream() {
        return type().isAnyStream();
    }

    @Override
    public boolean isNumber() {
        return type().isAnyNumber();
    }

    @Override
    public boolean isFloatingNumber() {
        return type().isAnyFloatingNumber();
    }

    @Override
    public boolean isOrdinalNumber() {
        return type().isAnyOrdinalNumber();
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
        return t.isAnyString();
    }

    @Override
    public boolean isByte() {
        return type() == NElementType.BYTE;
    }

    @Override
    public boolean isInt() {
        NElementType t = type();
        return t == NElementType.INT;
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
        return NOptional.ofEmpty(_expected("operator"));
    }

    @Override
    public boolean isBinaryInfixOperator() {
        NOptional<NOperatorElement> o = asOperator();
        if (o.isPresent()) {
            NOperatorElement oo = o.get();
            return oo.operatorType() == NOperatorType.BINARY_INFIX;
        }
        return false;
    }

    @Override
    public boolean isUnaryPrefixOperator() {
        NOptional<NOperatorElement> o = asOperator();
        if (o.isPresent()) {
            NOperatorElement oo = o.get();
            return oo.operatorType() == NOperatorType.UNARY_PREFIX;
        }
        return false;
    }

    @Override
    public boolean isBinaryOperator() {
        NOptional<NOperatorElement> o = asOperator();
        if (o.isPresent()) {
            NOperatorElement oo = o.get();
            return oo.operatorType() == NOperatorType.BINARY_INFIX;
        }
        return false;
    }

    @Override
    public boolean isAnyOperator() {
        return type().typeGroup() == NElementTypeGroup.OPERATOR;
    }

    @Override
    public boolean isUnaryOperator() {
        NOptional<NOperatorElement> o = asOperator();
        if (o.isPresent()) {
            NOperatorElement oo = o.get();
            return oo.operatorType() == NOperatorType.UNARY_PREFIX;
        }
        return false;
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
        return type().isAnyDecimalNumber();
    }

    @Override
    public boolean isBigNumber() {
        return type().isAnyBigNumber();
    }

    @Override
    public boolean isComplexNumber() {
        return type().isAnyComplexNumber();
    }

    @Override
    public boolean isTemporal() {
        return type().isAnyTemporal();
    }

    @Override
    public boolean isLocalTemporal() {
        return type().isAnyLocalTemporal();
    }

    @Override
    public boolean isNamed() {
        return type().isAnyNamed();
    }

    @Override
    public NOptional<NNamedElement> toNamed() {
        if (this instanceof NNamedElement) {
            return NOptional.of((NNamedElement) this);
        }
        return NOptional.ofEmpty(_expected("named element"));
    }

    @Override
    public boolean isParametrized() {
        return type().isAnyParametrized();
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
    public boolean isNamedPair(String name) {
        if (!isPair()) {
            return false;
        }
        NElement key = asPair().get().key();
        boolean anyString = key.isAnyString();
        if (anyString) {
            return Objects.equals(name, key.asStringValue().get());
        }
        return false;
    }

    @Override
    public boolean isNamedPair(Predicate<String> nameCondition) {
        if (!isPair()) {
            return false;
        }
        NElement key = asPair().get().key();
        boolean anyString = key.isAnyString();
        if (anyString) {
            return nameCondition == null || nameCondition.test(key.asStringValue().get());
        }
        return false;
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
            NArrayElementBuilder ab = NElement.ofArrayBuilder();
            ab.name(asNamed().get().name().orNull());
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
            NArrayElementBuilder ab = NElement.ofArrayBuilder();
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
                    return NOptional.of(NElement.ofPair(u.name().orNull(), u.value()));
                }
                break;
            }
            case NAMED_UPLET: {
                NUpletElement u = asUplet().orNull();
                return NOptional.of(NElement.ofPair(u.name().orNull(), u.builder().name(null).build()));
            }
            case NAMED_OBJECT: {
                NObjectElement u = asObject().orNull();
                if (u.isNamed() && !u.isParametrized()) {
                    return NOptional.of(NElement.ofPair(u.name().orNull(), u.builder().name(null).build()));
                }
                break;
            }
            case NAMED_ARRAY: {
                NArrayElement u = asArray().orNull();
                if (u.isNamed() && !u.isParametrized()) {
                    return NOptional.of(NElement.ofPair(u.name().orNull(), u.builder().name(null).build()));
                }
                break;
            }
        }
        return NOptional.ofEmpty(_expected("named pair"));
    }

    @Override
    public NOptional<NUpletElement> toNamedUplet() {
        switch (type) {
            case PAIR: {
                NPairElement u = asPair().orNull();
                if (u.isNamed()) {
                    NElement v = u.value();
                    return NOptional.of(NElement.ofUplet(u.name().orNull(), v));
                }
                break;
            }
            case NAMED_UPLET: {
                return NOptional.of((NUpletElement) this);
            }
            case NAMED_OBJECT: {
                NObjectElement u = asObject().orNull();
                if (!u.isParametrized()) {
                    return NOptional.of(NElement.ofUplet(u.name().orNull(), u.children().toArray(new NElement[0])));
                }
                break;
            }
            case NAMED_ARRAY: {
                NArrayElement u = asArray().orNull();
                if (!u.isParametrized()) {
                    return NOptional.of(NElement.ofUplet(u.name().orNull(), u.children().toArray(new NElement[0])));
                }
                break;
            }
        }
        return NOptional.ofEmpty(_expected("named uplet"));
    }

    @Override
    public NOptional<NObjectElement> toNamedObject() {
        switch (type) {
            case PAIR: {
                NPairElement u = asPair().orNull();
                if (u.isNamed()) {
                    NElement v = u.value();
                    return NOptional.of(NElement.ofObjectBuilder(u.name().orNull()).add(v).build());
                }
                break;
            }
            case NAMED_UPLET: {
                NUpletElement u = asUplet().orNull();
                return NOptional.of(NElement.ofObjectBuilder(u.name().orNull()).addAll(u.children().toArray(new NElement[0])).build());
            }
            case NAMED_OBJECT: {
                return NOptional.of((NObjectElement) this);
            }
            case NAMED_ARRAY: {
                NArrayElement u = asArray().orNull();
                return NOptional.of(NElement.ofObjectBuilder(u.name().orNull())
                        .addParams(u.params().orNull())
                        .addAll(u.children().toArray(new NElement[0])).build());
            }
        }
        return NOptional.ofEmpty(_expected("named object"));
    }

    @Override
    public NOptional<NArrayElement> toNamedArray() {
        switch (type) {
            case PAIR: {
                NPairElement u = asPair().orNull();
                if (u.isNamed()) {
                    NElement v = u.value();
                    return NOptional.of(NElement.ofArrayBuilder(u.name().orNull()).add(v).build());
                }
                break;
            }
            case NAMED_UPLET: {
                NUpletElement u = asUplet().orNull();
                return NOptional.of(NElement.ofArrayBuilder(u.name().orNull()).addAll(u.children().toArray(new NElement[0])).build());
            }
            case NAMED_OBJECT: {
                NObjectElement u = asObject().orNull();
                return NOptional.of(NElement.ofArrayBuilder(u.name().orNull())
                        .addParams(u.params().orNull())
                        .addAll(u.children().toArray(new NElement[0])).build());
            }
            case NAMED_ARRAY: {
                return NOptional.of((NArrayElement) this);
            }
        }
        return NOptional.ofEmpty(_expected("named array"));
    }

    @Override
    public NOptional<NObjectElement> toObject() {
        switch (type) {
            case PAIR: {
                NPairElement u = asPair().orNull();
                if (u.isNamed()) {
                    NElement v = u.value();
                    return NOptional.of(NElement.ofObjectBuilder(u.name().orNull()).add(v).build());
                }
                return NOptional.of(NElement.ofObjectBuilder().add(this).build());
            }
            case NAMED_UPLET: {
                NUpletElement u = asUplet().orNull();
                return NOptional.of(NElement.ofObjectBuilder().addAll(u.children().toArray(new NElement[0])).build());
            }
            case UPLET: {
                NUpletElement u = asUplet().orNull();
                return NOptional.of(NElement.ofObjectBuilder().addAll(u.children().toArray(new NElement[0])).build());
            }
            case OBJECT: {
                return NOptional.of((NObjectElement) this);
            }
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT: {
                NObjectElement u = asObject().orNull();
                return NOptional.of(NElement.ofObjectBuilder().name(u.name().orNull())
                        .addParams(u.params().orNull())
                        .addAll(u.children().toArray(new NElement[0])).build());
            }
            case ARRAY:
            case NAMED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY: {
                NArrayElement u = asArray().orNull();
                return NOptional.of(NElement.ofObjectBuilder().name(u.name().orNull())
                        .addParams(u.params().orNull())
                        .addAll(u.children().toArray(new NElement[0])).build());
            }
            default: {
                return NOptional.of(NElement.ofObjectBuilder().add(this).build());
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
                    return NOptional.of(NElement.ofArrayBuilder(u.name().orNull()).add(v).build());
                }
                return NOptional.of(NElement.ofArrayBuilder().add(this).build());
            }
            case NAMED_UPLET: {
                NUpletElement u = asUplet().orNull();
                return NOptional.of(NElement.ofArrayBuilder().addAll(u.children().toArray(new NElement[0])).build());
            }
            case UPLET: {
                NUpletElement u = asUplet().orNull();
                return NOptional.of(NElement.ofArrayBuilder().addAll(u.children().toArray(new NElement[0])).build());
            }
            case OBJECT:
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT: {
                NObjectElement u = asObject().orNull();
                return NOptional.of(NElement.ofArrayBuilder()
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
                return NOptional.of(NElement.ofArrayBuilder()
                        .addAll(u.children().toArray(new NElement[0])).build());
            }
            default: {
                return NOptional.of(NElement.ofArrayBuilder().add(this).build());
            }
        }
    }

    @Override
    public NArrayElement wrapIntoArray() {
        return NElement.ofArray(this);
    }

    @Override
    public NObjectElement wrapIntoObject() {
        return NElement.ofObjectBuilder().add(this).build();
    }

    @Override
    public NUpletElement wrapIntoUplet() {
        return NElement.ofUplet(this);
    }

    @Override
    public NArrayElement wrapIntoNamedArray(String name) {
        return NElement.ofArrayBuilder(name).add(this).build();
    }

    @Override
    public NObjectElement wrapIntoNamedObject(String name) {
        return NElement.ofObjectBuilder(name).add(this).build();
    }

    @Override
    public NUpletElement wrapIntoNamedUplet(String name) {
        return NElement.ofUpletBuilder(name).add(this).build();
    }

    @Override
    public NPairElement wrapIntoNamedPair(String name) {
        return NElement.ofPair(name, this);
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

    @Override
    public NOptional<Temporal> asTemporalValue() {
        return NOptional.ofError(() -> _expected("temporal"));
    }

    @Override
    public NOptional<NElement> asNumberType(NElementType elemType) {
        return NOptional.ofEmpty(NMsg.ofC("not a number %s", this));
    }

    @Override
    public NElement[] transform(NElementTransform transform) {
        return NElementTransformHelper.transform(this, transform);
    }
}
