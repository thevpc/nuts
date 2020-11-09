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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsNamedElement;

/**
 *
 * @author vpc
 */
class NutsObjectElementMap2 extends NutsObjectElementBase {

    private Map<String, Object> value;

    public NutsObjectElementMap2(Map value, NutsElementFactoryContext context) {
        super(context);
        this.value = value;
    }

    @Override
    public NutsElementType type() {
        return NutsElementType.OBJECT;
    }

    @Override
    public Collection<NutsNamedElement> children() {
        List<NutsNamedElement> all = new ArrayList<>();
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            Map<String, Object> m2 = new HashMap<>();
            m2.put("key", entry.getKey());
            m2.put("value", entry.getValue());
            all.add(new DefaultNutsNamedElement("entry", context.toElement(m2)));
        }
        return all;
    }

    @Override
    public NutsElement get(String name) {
        if ("entry".equals(name)) {
            for (Map.Entry<String, Object> entry : value.entrySet()) {
                Map<String, Object> m2 = new HashMap<>();
                m2.put("key", entry.getKey());
                m2.put("value", entry.getValue());
                return context.toElement(m2);
            }
        }
        return null;
    }

    @Override
    public int size() {
        return value.size();
    }

}
