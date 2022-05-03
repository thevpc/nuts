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

import java.time.Instant;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author thevpc
 */
public class DefaultNutsArrayElement extends AbstractNutsArrayElement {

    private final NutsElement[] values;

    public DefaultNutsArrayElement(Collection<NutsElement> values, NutsSession session) {
        super(session);
        this.values = values.toArray(new NutsElement[0]);
    }


    public DefaultNutsArrayElement(NutsElement[] values, NutsSession session) {
        super(session);
        this.values = Arrays.copyOf(values, values.length);
    }

    @Override
    public Collection<NutsElement> items() {
        return Arrays.asList(values);
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public Stream<NutsElement> stream() {
        return Arrays.asList(values).stream();
    }

    @Override
    public NutsOptional<NutsElement> get(int index) {
        if(index>=0 && index<values.length){
            return NutsOptional.of(values[index]);
        }
        return NutsOptional.ofError(s->NutsMessage.ofCstyle("invalid array index %s not in [%s,%s[",index,0,values.length));
    }

    @Override
    public NutsOptional<String> getString(int index) {
        return get(index).flatMap(NutsElement::asString);
    }

    @Override
    public NutsOptional<Boolean> getBoolean(int index) {
        return get(index).flatMap(NutsElement::asBoolean);
    }

    @Override
    public NutsOptional<Byte> getByte(int index) {
        return get(index).flatMap(NutsElement::asByte);
    }

    @Override
    public NutsOptional<Short> getShort(int index) {
        return get(index).flatMap(NutsElement::asShort);
    }

    @Override
    public NutsOptional<Integer> getInt(int index) {
        return get(index).flatMap(NutsElement::asInt);
    }

    @Override
    public NutsOptional<Long> getLong(int index) {
        return get(index).flatMap(NutsElement::asLong);
    }

    @Override
    public NutsOptional<Float> getFloat(int index) {
        return get(index).flatMap(NutsElement::asFloat);
    }

    @Override
    public NutsOptional<Double> getDouble(int index) {
        return get(index).flatMap(NutsElement::asDouble);
    }

    @Override
    public NutsOptional<Instant> getInstant(int index) {
        return get(index).flatMap(NutsElement::asInstant);
    }

    @Override
    public NutsOptional<NutsArrayElement> getArray(int index) {
        return get(index).flatMap(NutsElement::asArray);
    }

    @Override
    public NutsOptional<NutsObjectElement> getObject(int index) {
        return get(index).flatMap(NutsElement::asObject);
    }



    @Override
    public NutsArrayElementBuilder builder() {
        return NutsElements.of(session)
                .ofArray()
                .set(this);
    }

    @Override
    public Iterator<NutsElement> iterator() {
        return Arrays.asList(values).iterator();
    }

    @Override
    public String toString() {
        return "[" + items().stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Arrays.deepHashCode(this.values);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultNutsArrayElement other = (DefaultNutsArrayElement) obj;
        if (!Arrays.deepEquals(this.values, other.values)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return values.length == 0;
    }

    @Override
    public boolean isBlank() {
        return values.length == 0;
    }

    @Override
    public NutsOptional<NutsElement> get(String key) {
        return NutsValue.of(key).asInt().flatMap(this::get);
    }

    @Override
    public NutsOptional<NutsElement> get(NutsElement key) {
        return key.isString()?key.asString().flatMap(this::get):key.asInt().flatMap(this::get);
    }

    @Override
    public Collection<NutsElementEntry> entries() {
        return IntStream.range(0, size())
                .boxed()
                .map(x->new DefaultNutsElementEntry(
                        NutsElements.of(session).ofString(String.valueOf(x)),
                        get(x).orNull()
                )).collect(Collectors.toList());
    }
}
