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

import net.thevpc.nuts.NutsElementType;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsArrayElement;

/**
 *
 * @author thevpc
 */
public class NutsArrayElementMapper extends AbstractNutsElement implements NutsArrayElement {

    private final NutsElementFactoryContext context;
    private final List<Object> values = new ArrayList<>();

    public NutsArrayElementMapper(Object array, NutsElementFactoryContext context) {
        super(NutsElementType.ARRAY);
        this.context = context;
        if (array.getClass().isArray()) {
            int count = Array.getLength(array);
            for (int i = 0; i < count; i++) {
                values.add(Array.get(array, i));
            }
        } else if (array instanceof Collection) {
            values.addAll((Collection) array);
        } else if (array instanceof Iterator) {
            Iterator nl = (Iterator) array;
            while (nl.hasNext()) {
                values.add(nl.next());
            }
        } else {
            throw new IllegalArgumentException("Unsupported");
        }
    }

    @Override
    public Collection<NutsElement> children() {
        return values.stream().map(context::toElement).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "[" + children().stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NutsElement get(int index) {
        return context.toElement(values.get(index));
    }

}
