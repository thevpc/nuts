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
import net.thevpc.nuts.NutsArrayElementBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.thevpc.nuts.NutsElement;

/**
 *
 * @author vpc
 */
public class DefaultNutsArrayElementBuilder implements NutsArrayElementBuilder {

    private final List<NutsElement> values = new ArrayList<>();

    public DefaultNutsArrayElementBuilder() {

    }

    @Override
    public List<NutsElement> children() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public String toString() {
        return "[" + children().stream().map(x -> x.toString()).collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NutsElement get(int index) {
        return values.get(index);
    }

    @Override
    public NutsArrayElementBuilder set(int index, NutsElement e) {
        if(e==null){
            throw new NullPointerException();
        }
        values.set(index, e);
        return this;
    }

    @Override
    public NutsArrayElementBuilder addAll(NutsArrayElement value) {
        if(value ==null){
            throw new NullPointerException();
        }
        for (NutsElement child : value.children()) {
            add(child);
        }
        return this;
    }

    @Override
    public NutsArrayElementBuilder addAll(NutsArrayElementBuilder value) {
        if(value ==null){
            throw new NullPointerException();
        }
        for (NutsElement child : value.children()) {
            add(child);
        }
        return this;
    }

    @Override
    public NutsArrayElementBuilder add(NutsElement e) {
        if(e==null){
            throw new NullPointerException();
        }
        values.add(e);
        return this;
    }

    @Override
    public NutsArrayElementBuilder insert(int index, NutsElement e) {
        if(e==null){
            throw new NullPointerException();
        }
        values.add(index, e);
        return this;
    }

    @Override
    public NutsArrayElementBuilder remove(int index) {
        values.remove(index);
        return this;
    }

    @Override
    public NutsArrayElementBuilder clear() {
        values.clear();
        return this;
    }

    @Override
    public NutsArrayElementBuilder set(NutsArrayElementBuilder other) {
        clear();
        addAll(other);
        return this;
    }

    @Override
    public NutsArrayElementBuilder set(NutsArrayElement other) {
        clear();
        addAll(other);
        return this;
    }

    @Override
    public NutsArrayElement build() {
        return new DefaultNutsArrayElement(values);
    }
}
