/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.format.elem;

import net.thevpc.nuts.NutsArrayElement;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementType;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 *
 * @author vpc
 */
public class DefaultNutsArrayElement extends AbstractNutsElement implements NutsArrayElement {

    private final NutsElement[] values;

    public DefaultNutsArrayElement(Collection<NutsElement> values) {
        super(NutsElementType.ARRAY);
        this.values= values.toArray(new NutsElement[0]);
    }

    public DefaultNutsArrayElement(NutsElement[] values) {
        super(NutsElementType.ARRAY);
        this.values= Arrays.copyOf(values,values.length);
    }

    @Override
    public Collection<NutsElement> children() {
        return Arrays.asList(values);
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
    public String toString() {
        return "[" + children().stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }
}
