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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsNamedElement;

/**
 *
 * @author vpc
 */
class NutsObjectElementMap1 extends NutsObjectElementBase {

    private Map<String, Object> value;

    public NutsObjectElementMap1(Map value, NutsElementFactoryContext context) {
        super(context);
        this.value = value;
    }

    @Override
    public Collection<NutsNamedElement> children() {
        List<NutsNamedElement> all = new ArrayList<>();
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            NutsElement val = context.toElement(entry.getValue());
            all.add(new DefaultNutsNamedElement(entry.getKey(), val));
        }
        return all;
    }

    @Override
    public NutsElement get(String name) {
        if (value.containsKey(name)) {
            return context.toElement(value.get(name));
        }
        return null;
    }

    @Override
    public int size() {
        return value.size();
    }

}
