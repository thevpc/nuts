/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
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

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsNamedElement;
import net.thevpc.nuts.NutsObjectElement;
import net.thevpc.nuts.NutsObjectElementBuilder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultNutsObjectElementBuilder implements NutsObjectElementBuilder {

    private final Map<String, NutsElement> values = new LinkedHashMap<String, NutsElement>();

    public DefaultNutsObjectElementBuilder() {

    }

    @Override
    public Collection<NutsNamedElement> children() {
        return values.entrySet().stream().map(x -> new DefaultNutsNamedElement(x.getKey(), x.getValue())).collect(Collectors.toList());
    }

    @Override
    public NutsElement get(String s) {
        return values.get(s);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NutsObjectElementBuilder set(String s, NutsElement value) {
        if (value == null) {
            throw new NullPointerException();
        }
        values.put(s, value);
        return this;
    }


    @Override
    public NutsObjectElementBuilder remove(String name) {
        values.remove(name);
        return this;
    }

    @Override
    public NutsObjectElementBuilder clear() {
        values.clear();
        return this;
    }

    @Override
    public NutsObjectElementBuilder set(NutsObjectElement other) {
        clear();
        add(other);
        return this;
    }

    @Override
    public NutsObjectElementBuilder set(NutsObjectElementBuilder other) {
        clear();
        add(other);
        return this;
    }

    @Override
    public NutsObjectElementBuilder add(NutsObjectElement other) {
        if (other != null) {
            for (NutsNamedElement child : other.children()) {
                set(child.getName(), child.getValue());
            }
        }
        return this;
    }

    @Override
    public NutsObjectElementBuilder add(NutsObjectElementBuilder other) {
        if (other != null) {
            for (NutsNamedElement child : other.children()) {
                set(child.getName(), child.getValue());
            }
        }
        return this;
    }

    @Override
    public NutsObjectElement build() {
        return new DefaultNutsObjectElement(values);
    }

    @Override
    public String toString() {
        return "[" + children().stream().map(x -> "{"
                + x.getName()
                + ":"
                + x.getValue().toString()
                + "}").collect(Collectors.joining(", ")) + "]";
    }

}
