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
import net.thevpc.nuts.util.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author thevpc
 */
public class DefaultNUpletElement extends AbstractNListContainerElement
        implements NUpletElement {

    private final NElement[] params;
    private String name;

    public DefaultNUpletElement(String name, Collection<NElement> params, NElementAnnotation[] annotations, NElementComments comments) {
        super(name == null ? NElementType.UPLET
                        : NElementType.NAMED_UPLET,
                annotations, comments);
        this.params = params.toArray(new NElement[0]);
        this.name = name;
    }

    public DefaultNUpletElement(String name, NElement[] params, NElementAnnotation[] annotations, NElementComments comments) {
        super(name == null ? NElementType.UPLET
                : NElementType.NAMED_UPLET, annotations, comments);
        this.params = Arrays.copyOf(params, params.length);
        this.name = name;
    }

    @Override
    public NOptional<Object> asObjectValueAt(int index) {
        return get(index).map(x -> x);
    }


    @Override
    public List<NElement> resolveAll(String pattern) {
        pattern = NStringUtils.trimToNull(pattern);
        NElementPathImpl pp = new NElementPathImpl(pattern);
        NElement[] nElements = pp.resolveReversed(this);
        return new ArrayList<>(Arrays.asList(nElements));
    }

    @Override
    public boolean isNamed(String name) {
        return isNamed() && Objects.equals(name, this.name);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isNamed() {
        return name != null;
    }

    @Override
    public List<NElement> items() {
        return Arrays.asList(params);
    }


    @Override
    public List<NElement> children() {
        return Arrays.asList(params);
    }

    @Override
    public int size() {
        return params.length;
    }

    @Override
    public Stream<NElement> stream() {
        return Arrays.asList(params).stream();
    }

    @Override
    public NOptional<NElement> get(int index) {
        if (index >= 0 && index < params.length) {
            return NOptional.of(params[index]);
        }
        return NOptional.ofError(() -> NMsg.ofC("invalid array index %s not in [%s,%s[", index, 0, params.length));
    }

    @Override
    public NOptional<String> getString(int index) {
        return get(index).flatMap(NElement::asStringValue);
    }

    @Override
    public NOptional<Boolean> getBoolean(int index) {
        return get(index).flatMap(NElement::asBooleanValue);
    }

    @Override
    public NOptional<Byte> getByte(int index) {
        return get(index).flatMap(NElement::asByteValue);
    }

    @Override
    public NOptional<Short> getShort(int index) {
        return get(index).flatMap(NElement::asShortValue);
    }

    @Override
    public NOptional<Integer> getInt(int index) {
        return get(index).flatMap(NElement::asIntValue);
    }

    @Override
    public NOptional<Long> getLong(int index) {
        return get(index).flatMap(NElement::asLongValue);
    }

    @Override
    public NOptional<Float> getFloat(int index) {
        return get(index).flatMap(NElement::asFloatValue);
    }

    @Override
    public NOptional<Double> getDouble(int index) {
        return get(index).flatMap(NElement::asDoubleValue);
    }

    @Override
    public NOptional<Instant> getInstant(int index) {
        return get(index).flatMap(NElement::asInstantValue);
    }

    @Override
    public NOptional<LocalDate> getLocalDate(int index) {
        return get(index).flatMap(NElement::asLocalDateValue);
    }

    @Override
    public NOptional<LocalDateTime> getLocalDateTime(int index) {
        return get(index).flatMap(NElement::asLocalDateTimeValue);
    }

    @Override
    public NOptional<LocalTime> getLocalTime(int index) {
        return get(index).flatMap(NElement::asLocalTimeValue);
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
    public NUpletElementBuilder builder() {
        return NElements.of()
                .ofUpletBuilder()
                .copyFrom(this);
    }

    @Override
    public Iterator<NElement> iterator() {
        return Arrays.asList(params).iterator();
    }

    public String toString() {
        return toString(false);
    }

    @Override
    public String toString(boolean compact) {
        NStringBuilder sb = new NStringBuilder();
        sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
        if (name != null) {
            sb.append(name);
        }
        if (params != null) {
            sb.append("(");
            NElementToStringHelper.appendChildren(Arrays.asList(params), compact,
                    new NElementToStringHelper.SemiCompactInfo()
                            .setMaxLineSize(3)
                            .setMaxLineSize(80)
                    , sb);
            sb.append(")");
        }
        sb.append(NElementToStringHelper.trailingComments(this, compact));
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultNUpletElement nElements = (DefaultNUpletElement) o;
        return Objects.deepEquals(params, nElements.params)
                && Objects.equals(name, nElements.name())
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Arrays.hashCode(params), name);
    }

    @Override
    public boolean isEmpty() {
        return params.length == 0;
    }

    @Override
    public boolean isBlank() {
        return params.length == 0 && NBlankable.isBlank(name);
    }

    @Override
    public NOptional<NElement> get(String key) {
        return NLiteral.of(key).asIntValue().flatMap(this::get);
    }

    @Override
    public NOptional<NElement> get(NElement key) {
        return key.isString() ? key.asStringValue().flatMap(this::get) : key.asIntValue().flatMap(this::get);
    }

    @Override
    public List<NElement> getAll(NElement s) {
        int index = -1;
        if (s.isString()) {
            NOptional<Integer> ii = NLiteral.of(s.asStringValue().get()).asIntValue();
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

}
