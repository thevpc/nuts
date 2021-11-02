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

import net.thevpc.nuts.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultNutsObjectElementBuilder implements NutsObjectElementBuilder {

    private final Map<NutsElement, NutsElement> values = new LinkedHashMap<NutsElement, NutsElement>();

    private NutsSession session;

    public DefaultNutsObjectElementBuilder(NutsSession session) {
        if(session==null){
            throw new NullPointerException();
        }
        this.session = session;
    }

    @Override
    public Collection<NutsElementEntry> children() {
        return values.entrySet().stream().map(x -> new DefaultNutsElementEntry(x.getKey(), x.getValue())).collect(Collectors.toList());
    }

    @Override
    public NutsElement get(NutsElement s) {
        return values.get(s);
    }

    @Override
    public NutsElement get(String s) {
        return values.get(_elements().forBoolean(s));
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NutsObjectElementBuilder set(String name, NutsElement value) {
        values.put(_elements().forString(name), denull(value));
        return this;
    }

    @Override
    public NutsObjectElementBuilder set(String name, boolean value) {
        return set(_elements().forString(name), _elements().forBoolean(value));
    }

    @Override
    public NutsObjectElementBuilder set(String name, int value) {
        return set(_elements().forString(name), _elements().forInt(value));
    }

    @Override
    public NutsObjectElementBuilder set(String name, double value) {
        return set(_elements().forString(name), _elements().forDouble(value));
    }

    @Override
    public NutsObjectElementBuilder set(String name, String value) {
        return set(_elements().forString(name), _elements().forString(value));
    }

    @Override
    public NutsObjectElementBuilder remove(String name) {
        NutsElement v = name==null?_elements().forNull():_elements().forString(name);
        values.remove(v);
        return this;
    }

//    @Override
    public NutsObjectElementBuilder add(NutsElement name, NutsElement value) {
        values.put(name, denull(value));
        return this;
    }

    @Override
    public NutsObjectElementBuilder set(NutsElement name, NutsElement value) {
        values.put(name, denull(value));
        return this;
    }

    @Override
    public NutsObjectElementBuilder set(NutsElement name, boolean value) {
        return set(name, _elements().forBoolean(value));
    }

    @Override
    public NutsObjectElementBuilder set(NutsElement name, int value) {
        return set(name, _elements().forInt(value));
    }

    @Override
    public NutsObjectElementBuilder set(NutsElement name, double value) {
        return set(name, _elements().forDouble(value));
    }

    @Override
    public NutsObjectElementBuilder set(NutsElement name, String value) {
        return set(name, _elements().forString(value));
    }

    @Override
    public NutsObjectElementBuilder remove(NutsElement name) {
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
            for (NutsElementEntry child : other.children()) {
                add(child.getKey(), child.getValue());
            }
        }
        return this;
    }

    @Override
    public NutsObjectElementBuilder add(NutsObjectElementBuilder other) {
        if (other != null) {
            for (NutsElementEntry child : other.children()) {
                add(child.getKey(), child.getValue());
            }
        }
        return this;
    }

    @Override
    public NutsObjectElementBuilder add(NutsElementEntry entry) {
        if (entry != null) {
            add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public NutsObjectElement build() {
        return new DefaultNutsObjectElement(values, session);
    }

    @Override
    public String toString() {
        return "{" + children().stream().map(x
                -> x.getKey()
                + ":"
                + x.getValue().toString()
        ).collect(Collectors.joining(", ")) + "}";
    }

    private NutsElement denull(NutsElement e) {
        if (e == null) {
            return _elements().forNull();
        }
        return e;
    }

    private NutsElements _elements() {
        return NutsElements.of(session).setSession(session);
    }
}
