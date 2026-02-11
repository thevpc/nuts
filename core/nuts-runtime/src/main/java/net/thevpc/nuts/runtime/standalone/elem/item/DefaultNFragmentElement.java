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
package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.path.NElementPathImpl;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NTreeVisitResult;
import net.thevpc.nuts.util.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author thevpc
 */
public class DefaultNFragmentElement extends AbstractNElement
        implements NFragmentElement {
    public static final DefaultNFragmentElement EMPTY=new DefaultNFragmentElement(new ArrayList<>());
    private final List<NElement> values;

    public DefaultNFragmentElement(List<NElement> values) {
        this(values,null,null,null);
    }

    public DefaultNFragmentElement(List<NElement> values, List<NBoundAffix> affixes,
                                   List<NElementDiagnostic> diagnostics, NElementMetadata metadata) {
        super(NElementType.FRAGMENT,
                affixes,diagnostics,metadata);
        this.values = CoreNUtils.copyAndUnmodifiableList(values);
    }

    protected NTreeVisitResult traverseChildren(NElementVisitor visitor) {
        return traverseList(visitor, values); // body
    }

    @Override
    public List<NElement> children() {
        return values;
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public Stream<NElement> stream() {
        return values.stream();
    }

    @Override
    public NOptional<NElement> get(int index) {
        if (index >= 0 && index < values.size()) {
            return NOptional.of(values.get(index));
        }
        return NOptional.ofError(() -> NMsg.ofC("invalid array index %s not in [%s,%s[", index, 0, values.size()));
    }


    @Override
    public NOptional<NElement> getAt(int index) {
        if (index >= 0 && index < values.size()) {
            return NOptional.of(values.get(index));
        }
        return NOptional.ofError(() -> NMsg.ofC("invalid array index %s not in [%s,%s[", index, 0, values.size()));
    }


    @Override
    public NFragmentElementBuilder builder() {
        return NElement.ofFragmentBuilder()
                .copyFrom(this);
    }

    @Override
    public Iterator<NElement> iterator() {
        return values.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultNFragmentElement nElements = (DefaultNFragmentElement) o;
        return Objects.deepEquals(values, nElements.values)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), values.hashCode());
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean isBlank() {
        return values.isEmpty();
    }


    @Override
    public NOptional<NElement> get(String s) {
        for (NElement x : values) {
            if (x instanceof NPairElement) {
                NPairElement e = (NPairElement) x;
                if (s == null) {
                    if (e.key().isNull()) {
                        return NOptional.of(e.value());
                    }
                } else if (e.key().isAnyString()) {
                    if (Objects.equals(e.key().asStringValue().get(), s)) {
                        return NOptional.of(e.value());
                    }
                }
            }
        }
        if (NLiteral.of(s).asInt().isPresent()) {
            return get(NLiteral.of(s).asInt().get());
        }
        return NOptional.ofNamedEmpty("property " + s);
    }

    @Override
    public List<NElement> getAll(String s) {
        List<NElement> ret = new ArrayList<>();
        for (NElement x : values) {
            if (x instanceof NPairElement) {
                NPairElement e = (NPairElement) x;
                if (s == null) {
                    if (e.key().isNull()) {
                        ret.add(e.value());
                    }
                } else if (e.key().isAnyString()) {
                    if (Objects.equals(e.key().asStringValue().get(), s)) {
                        ret.add(e.value());
                    }
                }
            }
        }
        if (ret.isEmpty()) {
            if (NLiteral.of(s).asInt().isPresent()) {
                NOptional<NElement> u = get(NLiteral.of(s).asInt().get());
                if (u.isPresent()) {
                    ret.add(u.get());
                }
            }
        }
        return ret;
    }


    @Override
    public NOptional<NElement> get(NElement key) {
        return key.isString() ? key.asStringValue().flatMap(this::get) : key.asIntValue().flatMap(this::get);
    }

    @Override
    public List<NElement> getAll(NElement s) {
        int index = -1;
        if (s.isAnyString()) {
            NOptional<Integer> ii = NLiteral.of(s.asStringValue().get()).asInt();
            if (ii.isPresent()) {
                index = ii.get();
            } else {
                return Collections.emptyList();
            }
        } else if (s.asIntValue().isPresent()) {
            index = s.asIntValue().get();
        } else {
            return Collections.emptyList();
        }

        NOptional<NElement> a = get(index);
        if (a.isPresent()) {
            return Arrays.asList(a.get());
        }
        return Collections.emptyList();
    }

    @Override
    public List<NElement> resolveAll(String pattern) {
        pattern = NStringUtils.trimToNull(pattern);
        NElementPathImpl pp = new NElementPathImpl(pattern);
        NElement[] nElements = pp.resolveReversed(this);
        return new ArrayList<>(Arrays.asList(nElements));
    }

    @Override
    public NOptional<String> getStringValue(int index) {
        return get(index).flatMap(NElement::asStringValue);
    }

    @Override
    public NOptional<LocalTime> getLocalTimeValue(int index) {
        return get(index).flatMap(x -> x.asLiteral().asLocalTime());
    }

    @Override
    public NOptional<NArrayElement> getArray(int index) {
        return get(index).flatMap(NElement::asArray);
    }

    @Override
    public NOptional<NObjectElement> getObject(int index) {
        return get(index).flatMap(NElement::asObject);
    }

    @Override
    public NOptional<Boolean> getBooleanValue(int index) {
        return get(index).flatMap(x -> x.asLiteral().asBoolean());
    }

    @Override
    public NOptional<Byte> getByteValue(int index) {
        return get(index).flatMap(x -> x.asLiteral().asByte());
    }

    @Override
    public NOptional<Short> getShortValue(int index) {
        return get(index).flatMap(x -> x.asLiteral().asShort());
    }

    @Override
    public NOptional<Integer> getIntValue(int index) {
        return get(index).flatMap(x -> x.asLiteral().asInt());
    }

    @Override
    public NOptional<Long> getLongValue(int index) {
        return get(index).flatMap(x -> x.asLiteral().asLong());
    }

    @Override
    public NOptional<Float> getFloatValue(int index) {
        return get(index).flatMap(x -> x.asLiteral().asFloat());
    }

    @Override
    public NOptional<Double> getDoubleValue(int index) {
        return get(index).flatMap(x -> x.asLiteral().asDouble());
    }

    @Override
    public NOptional<Instant> getInstantValue(int index) {
        return get(index).flatMap(x -> x.asLiteral().asInstant());
    }

    @Override
    public NOptional<LocalDate> getLocalDateValue(int index) {
        return get(index).flatMap(x -> x.asLiteral().asLocalDate());
    }

    @Override
    public NOptional<LocalDateTime> getLocalDateTimeValue(int index) {
        return get(index).flatMap(x -> x.asLiteral().asLocalDateTime());
    }

//    @Override
//    public NOptional<String> getStringByPath(String... keys) {
//        return getByPath(keys).map(NElement::asLiteral).flatMap(NLiteral::asString);
//    }
//
//    @Override
//    public NOptional<Integer> getIntByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asInt);
//    }
//
//    @Override
//    public NOptional<Long> getLongByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asLong);
//    }
//
//    @Override
//    public NOptional<Float> getFloatByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asFloat);
//    }
//
//    @Override
//    public NOptional<Double> getDoubleByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asDouble);
//    }
//
//    @Override
//    public NOptional<Boolean> getBooleanByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asBoolean);
//    }
//
//    @Override
//    public NOptional<Byte> getByteByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asByte);
//    }
//
//    @Override
//    public NOptional<Short> getShortByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asShort);
//    }
//
//    @Override
//    public NOptional<Instant> getInstantByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asInstant);
//    }
//
//    @Override
//    public NOptional<LocalTime> getLocalDateByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asLocalTime);
//    }
//
//    @Override
//    public NOptional<LocalDate> getLocalTimeByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asLocalDate);
//    }
//
//    @Override
//    public NOptional<LocalDateTime> getLocalDateTimeByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asLocalDateTime);
//    }
//
//    @Override
//    public NOptional<BigInteger> getBigIntByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asBigInt);
//    }
//
//    @Override
//    public NOptional<BigDecimal> getBigDecimalByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asBigDecimal);
//    }
//
//    @Override
//    public NOptional<Number> getNumberByPath(String... keys) {
//        return getByPath(keys).flatMap(NLiteral::asNumber);
//    }

    @Override
    public NOptional<NArrayElement> getArray(String key) {
        return get(key).flatMap(NElement::asArray);
    }

    @Override
    public NOptional<NArrayElement> getArray(NElement key) {
        return get(key).flatMap(NElement::asArray);
    }

    @Override
    public NOptional<NObjectElement> getObject(String key) {
        return get(key).flatMap(NElement::asObject);
    }

    @Override
    public NOptional<NObjectElement> getObject(NElement key) {
        return get(key).flatMap(NElement::asObject);
    }

    @Override
    public NOptional<NListContainerElement> getListContainer(String key) {
        return get(key).flatMap(NElement::asListContainer);
    }

    @Override
    public NOptional<NListContainerElement> getListContainer(NElement key) {
        return get(key).flatMap(NElement::asListContainer);
    }

    @Override
    public NOptional<String> getStringValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asString);
    }

    @Override
    public NOptional<String> getStringValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asString);
    }

    @Override
    public NOptional<Boolean> getBooleanValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asBoolean);
    }

    @Override
    public NOptional<Boolean> getBooleanValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asBoolean);
    }

    @Override
    public NOptional<Number> getNumber(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asNumber);
    }

    @Override
    public NOptional<Number> getNumber(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asNumber);
    }

    @Override
    public NOptional<Byte> getByteValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asByte);
    }

    @Override
    public NOptional<Byte> getByteValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asByte);
    }

    @Override
    public NOptional<Integer> getIntValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asInt);
    }

    @Override
    public NOptional<Integer> getIntValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asInt);
    }

    @Override
    public NOptional<Long> getLongValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLong);
    }

    @Override
    public NOptional<Long> getLongValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLong);
    }

    @Override
    public NOptional<Short> getShortValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asShort);
    }

    @Override
    public NOptional<Short> getShortValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asShort);
    }

    @Override
    public NOptional<Instant> getInstantValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asInstant);
    }

    @Override
    public NOptional<LocalDate> getLocalDateValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLocalDate);
    }

    @Override
    public NOptional<LocalDateTime> getLocalDateTimeValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLocalDateTime);
    }

    @Override
    public NOptional<LocalTime> getLocalTimeValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLocalTime);
    }

    @Override
    public NOptional<Instant> getInstantValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asInstant);
    }

    @Override
    public NOptional<LocalDate> getLocalDateValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLocalDate);
    }

    @Override
    public NOptional<LocalDateTime> getLocalDateTimeValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLocalDateTime);
    }

    @Override
    public NOptional<LocalTime> getLocalTimeValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asLocalTime);
    }


    @Override
    public NOptional<Float> getFloatValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asFloat);
    }

    @Override
    public NOptional<Float> getFloatValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asFloat);
    }

    @Override
    public NOptional<Double> getDoubleValue(String key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asDouble);
    }

    @Override
    public NOptional<Double> getDoubleValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asDouble);
    }

    @Override
    public NOptional<BigInteger> getBigIntValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asBigInt);
    }

    @Override
    public NOptional<BigDecimal> getBigDecimalValue(NElement key) {
        return get(key).map(NElement::asLiteral).flatMap(NLiteral::asBigDecimal);
    }

    @Override
    public NOptional<NElement> getByPath(String... keys) {
        NOptional<NElement> r = NOptional.of(this);
        for (String key : keys) {
            r = r.flatMap(NElement::asListContainer).flatMap(x -> x.get(key));
        }
        return r;
    }

    @Override
    public NOptional<NArrayElement> getArrayByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asArray);
    }

    @Override
    public NOptional<NObjectElement> getObjectByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asObject);
    }

    @Override
    public NOptional<NListContainerElement> getListContainerByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asListContainer);
    }

    @Override
    public NOptional<LocalDateTime> getLocalDateTimeValueByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asLocalDateTimeValue);
    }

    @Override
    public NOptional<LocalDate> getLocalDateValueByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asLocalDateValue);
    }

    @Override
    public NOptional<Instant> getInstantValueByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asInstantValue);
    }

    @Override
    public NOptional<Double> getDoubleValueByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asDoubleValue);
    }

    @Override
    public NOptional<Float> getFloatValueByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asFloatValue);
    }

    @Override
    public NOptional<Long> getLongValueByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asLongValue);
    }

    @Override
    public NOptional<Integer> getIntValueByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asIntValue);
    }

    @Override
    public NOptional<Short> getShortValueByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asShortValue);
    }

    @Override
    public NOptional<Byte> getByteValueByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asByteValue);
    }

    @Override
    public NOptional<Boolean> getBooleanValueByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asBooleanValue);
    }

    @Override
    public NOptional<String> getStringValueByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asStringValue);
    }

    @Override
    public NOptional<LocalTime> getLocalTimeValueByPath(String... keys) {
        return getByPath(keys).flatMap(NElement::asLocalTimeValue);
    }

}
