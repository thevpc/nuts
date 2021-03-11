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
package net.thevpc.nuts.runtime.core.format.elem;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.thevpc.nuts.NutsElement;

/**
 *
 * @author thevpc
 */
public class NutsArrayElementFromList extends AbstractNutsArrayElement {

    private final NutsElementFactoryContext context;
    private final List<Object> values;

    public NutsArrayElementFromList(List<Object> values, NutsElementFactoryContext context) {
        this.context = context;
        this.values = values;
        
    }

    @Override
    public Collection<NutsElement> children() {
        return values.stream().map(x->context.objectToElement(x, null)).collect(Collectors.toList());
    }


    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NutsElement get(int index) {
        return context.objectToElement(values.get(index), null);
    }

}
