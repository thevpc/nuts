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

import java.time.Instant;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author thevpc
 */
public class DefaultNArrayElement extends AbstractNArrayElement {

    private final NElement[] values;
    private String name;
    private List<NElement> args;

    public DefaultNArrayElement(String name, List<NElement> args, Collection<NElement> values, NElementAnnotation[] annotations) {
        super(annotations);
        this.values = values.toArray(new NElement[0]);
        this.name = name;
        this.args = args;
    }


    public DefaultNArrayElement(String name, List<NElement> args, NElement[] values, NElementAnnotation[] annotations) {
        super(annotations);
        this.values = Arrays.copyOf(values, values.length);
        this.name = name;
        this.args = args;
    }

    @Override
    public Collection<NElement> items() {
        return Arrays.asList(values);
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public Stream<NElement> stream() {
        return Arrays.asList(values).stream();
    }

    @Override
    public NOptional<NElement> get(int index) {
        if (index >= 0 && index < values.length) {
            return NOptional.of(values[index]);
        }
        return NOptional.ofError(() -> NMsg.ofC("invalid array index %s not in [%s,%s[", index, 0, values.length));
    }

    @Override
    public NOptional<String> getString(int index) {
        return get(index).flatMap(NElement::asString);
    }

    @Override
    public NOptional<Boolean> getBoolean(int index) {
        return get(index).flatMap(NElement::asBoolean);
    }

    @Override
    public NOptional<Byte> getByte(int index) {
        return get(index).flatMap(NElement::asByte);
    }

    @Override
    public NOptional<Short> getShort(int index) {
        return get(index).flatMap(NElement::asShort);
    }

    @Override
    public NOptional<Integer> getInt(int index) {
        return get(index).flatMap(NElement::asInt);
    }

    @Override
    public NOptional<Long> getLong(int index) {
        return get(index).flatMap(NElement::asLong);
    }

    @Override
    public NOptional<Float> getFloat(int index) {
        return get(index).flatMap(NElement::asFloat);
    }

    @Override
    public NOptional<Double> getDouble(int index) {
        return get(index).flatMap(NElement::asDouble);
    }

    @Override
    public NOptional<Instant> getInstant(int index) {
        return get(index).flatMap(NElement::asInstant);
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
    public NArrayElementBuilder builder() {
        return NElements.of()
                .ofArrayBuilder()
                .set(this);
    }

    @Override
    public Iterator<NElement> iterator() {
        return Arrays.asList(values).iterator();
    }

    @Override
    public String toString() {
        return "[" + items().stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultNArrayElement nElements = (DefaultNArrayElement) o;
        return Objects.deepEquals(values, nElements.values)
                && Objects.equals(name, nElements.name)
                && Objects.equals(args, nElements.args)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Arrays.hashCode(values), name, args);
    }

    @Override
    public boolean isEmpty() {
        return values.length == 0;
    }

    @Override
    public boolean isBlank() {
        return values.length == 0 && NBlankable.isBlank(name) && (args == null || args.isEmpty());
    }

    @Override
    public NOptional<NElement> get(String key) {
        return NLiteral.of(key).asInt().flatMap(this::get);
    }

    @Override
    public NOptional<NElement> get(NElement key) {
        return key.isString() ? key.asString().flatMap(this::get) : key.asInt().flatMap(this::get);
    }

    @Override
    public List<NElement> getAll(NElement s) {
        int index = -1;
        if (s.isString()) {
            NOptional<Integer> ii = NLiteral.of(s.asString().get()).asInt();
            if (ii.isPresent()) {
                index = ii.get();
            } else {
                return Collections.emptyList();
            }
        } else if (s.asInt().isPresent()) {
            index = s.asInt().get();
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
    public Collection<NElement> children() {
        return Arrays.asList(values);
    }

    public String name() {
        return name;
    }

    public boolean isNamed() {
        return name != null;
    }

    public boolean isWithArgs() {
        return args != null;
    }

    public List<NElement> args() {
        return args == null ? null : Collections.unmodifiableList(args);
    }

    public int argsCount() {
        return args == null ? null : args.size();
    }

    public NElement argAt(int index) {
        return args == null ? null : args.get(index);
    }
}
