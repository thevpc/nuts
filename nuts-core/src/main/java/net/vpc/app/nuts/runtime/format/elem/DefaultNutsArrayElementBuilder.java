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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.format.elem;

import net.vpc.app.nuts.NutsArrayElement;
import net.vpc.app.nuts.NutsArrayElementBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.vpc.app.nuts.NutsElement;

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
