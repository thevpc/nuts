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
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NOptional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultNObjectElementBuilder implements NObjectElementBuilder {

    private final List<NElement> values = new ArrayList<>();
    private final List<NElementAnnotation> annotations = new ArrayList<>();

    private String name;
    private boolean hasArgs;
    private final List<NElement> args = new ArrayList<>();

    public DefaultNObjectElementBuilder() {
    }

    public String getName() {
        return name;
    }

    public NObjectElementBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isWithArgs() {
        return hasArgs;
    }


    public NObjectElementBuilder setHasArgs(boolean hasArgs) {
        this.hasArgs = hasArgs;
        return this;
    }

    @Override
    public NObjectElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        if (annotations != null) {
            for (NElementAnnotation a : annotations) {
                if (a != null) {
                    this.annotations.add(a);
                }
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addAnnotation(NElementAnnotation annotation) {
        if (annotation != null) {
            annotations.add(annotation);
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        if (annotation != null) {
            annotations.add(index, annotation);
        }
        return this;
    }

    @Override
    public NObjectElementBuilder removeAnnotationAt(int index) {
        annotations.remove(index);
        return this;
    }

    @Override
    public NObjectElementBuilder clearAnnotations() {
        annotations.clear();
        return this;
    }

    @Override
    public List<NElementAnnotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    @Override
    public NObjectElementBuilder addArgs(List<NElement> args) {
        if (args != null) {
            for (NElement a : args) {
                if (a != null) {
                    this.args.add(a);
                }
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addArg(NElement arg) {
        if (arg != null) {
            this.args.add(arg);
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addArgAt(int index, NElement arg) {
        if (arg != null) {
            args.add(index, arg);
        }
        return this;
    }

    @Override
    public NObjectElementBuilder removeArgAt(int index) {
        args.remove(index);
        return this;
    }

    @Override
    public NObjectElementBuilder clearArgs() {
        args.clear();
        return this;
    }

    @Override
    public List<NElement> getArgs() {
        return Collections.unmodifiableList(args);
    }


    @Override
    public Collection<NElement> children() {
        return values;
    }

    @Override
    public List<NElement> getAll(NElement s) {
        List<NElement> ret = new ArrayList<>();
        for (NElement x : values) {
            if (x instanceof NElementEntry) {
                NElementEntry e = (NElementEntry) x;
                if (Objects.equals(e.getKey(), s)) {
                    ret.add(e.getValue());
                }
            }
        }
        return ret;
    }

    @Override
    public NOptional<NElement> get(NElement s) {
        return NOptional.ofNamedSingleton(getAll(s), "property " + s);
    }

    @Override
    public NOptional<NElement> get(String s) {
        return get(_elements().ofString(s));
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NObjectElementBuilder add(String name, NElement value) {
        return add(_elements().ofString(name), denull(value));
    }

    @Override
    public NObjectElementBuilder add(NElement name, NElement value) {
        values.add(new DefaultNElementEntry(denull(name), denull(value), new NElementAnnotation[0]));
        return this;
    }

    @Override
    public NObjectElementBuilder set(NElement name, NElement value) {
        name = denull(name);
        value = denull(value);
        for (int i = 0; i < values.size(); i++) {
            NElement nElement = values.get(i);
            if (nElement instanceof NElementEntry) {
                NElement k = ((NElementEntry) nElement).getKey();
                if (Objects.equals(k, name)) {
                    values.set(i, new DefaultNElementEntry(name, value, new NElementAnnotation[0]));
                    return this;
                }
            } else if (Objects.equals(nElement, name)) {
                values.set(i, new DefaultNElementEntry(name, value, new NElementAnnotation[0]));
                return this;
            }
        }
        values.add(new DefaultNElementEntry(name, value, new NElementAnnotation[0]));
        return this;
    }

    @Override
    public NObjectElementBuilder set(String name, NElement value) {
        return set(_elements().ofString(name), denull(value));
    }

    @Override
    public NObjectElementBuilder set(String name, boolean value) {
        return set(_elements().ofString(name), _elements().ofBoolean(value));
    }

    @Override
    public NObjectElementBuilder set(String name, int value) {
        return set(_elements().ofString(name), _elements().ofInt(value));
    }

    @Override
    public NObjectElementBuilder set(String name, double value) {
        return set(_elements().ofString(name), _elements().ofDouble(value));
    }

    @Override
    public NObjectElementBuilder set(String name, String value) {
        return set(_elements().ofString(name), _elements().ofString(value));
    }

    @Override
    public NObjectElementBuilder remove(NElement name) {
        name = denull(name);
        for (int i = 0; i < values.size(); i++) {
            NElement nElement = values.get(i);
            if (nElement instanceof NElementEntry) {
                NElement k = ((NElementEntry) nElement).getKey();
                if (Objects.equals(k, name)) {
                    values.remove(i);
                    return this;
                }
            } else if (Objects.equals(nElement, name)) {
                values.remove(i);
                return this;
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder removeAll(NElement name) {
        name = denull(name);
        for (int i = values.size() - 1; i >= 0; i--) {
            NElement nElement = values.get(i);
            if (nElement instanceof NElementEntry) {
                NElement k = ((NElementEntry) nElement).getKey();
                if (Objects.equals(k, name)) {
                    values.remove(i);
                }
            } else if (Objects.equals(nElement, name)) {
                values.remove(i);
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder remove(String name) {
        return remove(_elements().ofString(name));
    }

    @Override
    public NObjectElementBuilder removeAll(String name) {
        return removeAll(_elements().ofString(name));
    }


    @Override
    public NObjectElementBuilder addAll(Map<NElement, NElement> other) {
        if (other != null) {
            for (Map.Entry<NElement, NElement> e : other.entrySet()) {
                add(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addAll(List<NElementEntry> other) {
        if (other != null) {
            for (NElementEntry e : other) {
                add(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder setAll(Map<NElement, NElement> other) {
        if (other != null) {
            for (Map.Entry<NElement, NElement> e : other.entrySet()) {
                set(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder set(NElement name, boolean value) {
        return set(name, _elements().ofBoolean(value));
    }

    @Override
    public NObjectElementBuilder set(NElement name, int value) {
        return set(name, _elements().ofInt(value));
    }

    @Override
    public NObjectElementBuilder set(NElement name, double value) {
        return set(name, _elements().ofDouble(value));
    }

    @Override
    public NObjectElementBuilder set(NElement name, String value) {
        return set(name, _elements().ofString(value));
    }

    @Override
    public NObjectElementBuilder clear() {
        values.clear();
        return this;
    }

    @Override
    public NObjectElementBuilder set(NObjectElement other) {
        clear();
        this.addAll(other);
        return this;
    }

    @Override
    public NObjectElementBuilder setAll(NObjectElementBuilder other) {
        clear();
        if (other != null) {
            for (NElement entry : other.children()) {
                add(entry);
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addAll(NObjectElement other) {
        if (other != null) {
            for (NElement entry : other) {
                add(entry);
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder add(NObjectElementBuilder other) {
        if (other != null) {
            for (NElement child : other.children()) {
                add(child);
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder add(NElement entry) {
        if (entry != null) {
            values.add(entry);
        }
        return this;
    }

    @Override
    public NObjectElementBuilder set(NElementEntry entry) {
        if (entry != null) {
            set(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addAll(NElement... entries) {
        if (entries != null) {
            for (NElement entry : entries) {
                add(entry);
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder add(String name, boolean value) {
        return add(_elements().ofString(name), _elements().ofBoolean(value));
    }

    @Override
    public NObjectElementBuilder add(String name, int value) {
        return add(_elements().ofString(name), _elements().ofInt(value));
    }

    @Override
    public NObjectElementBuilder add(String name, double value) {
        return add(_elements().ofString(name), _elements().ofDouble(value));
    }

    @Override
    public NObjectElementBuilder add(String name, String value) {
        return add(_elements().ofString(name), _elements().ofString(value));
    }

    @Override
    public NObjectElementBuilder addAll(NObjectElementBuilder other) {
        if (other != null) {
            for (NElement entry : other.build()) {
                add(entry);
            }
        }
        return this;
    }

    @Override
    public NObjectElement build() {
        return new DefaultNObjectElement(name, hasArgs? args:null,values,
                annotations.toArray(new NElementAnnotation[0]));
    }

    @Override
    public String toString() {
        return "{" + children().stream().map(x
                -> x.toString()
        ).collect(Collectors.joining(", ")) + "}";
    }

    private NElement denull(NElement e) {
        if (e == null) {
            return _elements().ofNull();
        }
        return e;
    }

    private NElements _elements() {
        return NElements.of();
    }

    @Override
    public NElementType type() {
        return NElementType.OBJECT;
    }
}
