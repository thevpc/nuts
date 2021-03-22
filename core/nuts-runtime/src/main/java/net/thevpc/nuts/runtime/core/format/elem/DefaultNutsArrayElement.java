/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.core.format.elem;

import java.time.Instant;
import net.thevpc.nuts.NutsElement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.thevpc.nuts.NutsArrayElement;
import net.thevpc.nuts.NutsObjectElement;

/**
 *
 * @author thevpc
 */
public class DefaultNutsArrayElement extends AbstractNutsArrayElement {

    private final NutsElement[] values;

    public DefaultNutsArrayElement(Collection<NutsElement> values) {
        this.values = values.toArray(new NutsElement[0]);
    }

    public DefaultNutsArrayElement(NutsElement[] values) {
        this.values = Arrays.copyOf(values, values.length);
    }

    @Override
    public Collection<NutsElement> children() {
        return Arrays.asList(values);
    }

    @Override
    public Iterator<NutsElement> iterator() {
        return Arrays.asList(values).iterator();
    }

    @Override
    public boolean isEmpty() {
        return values.length == 0;
    }

    @Override
    public Stream<NutsElement> stream() {
        return Arrays.asList(values).stream();
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public NutsElement get(int index) {
        return values[index];
    }

    @Override
    public String getString(int index) {
        return get(index).asString();
    }

    @Override
    public boolean getBoolean(int index) {
        return get(index).asBoolean();
    }

    @Override
    public byte getByte(int index) {
        return get(index).asByte();
    }

    @Override
    public short getShort(int index) {
        return get(index).asShort();
    }

    @Override
    public int getInt(int index) {
        return get(index).asInt();
    }

    @Override
    public long getLong(int index) {
        return get(index).asLong();
    }

    @Override
    public float getFloat(int index) {
        return get(index).asFloat();
    }

    @Override
    public double getDouble(int index) {
        return get(index).asDouble();
    }

    @Override
    public Instant getInstant(int index) {
        return get(index).asInstant();
    }

    @Override
    public NutsArrayElement getArray(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public NutsObjectElement getObject(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return "[" + children().stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
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

}
